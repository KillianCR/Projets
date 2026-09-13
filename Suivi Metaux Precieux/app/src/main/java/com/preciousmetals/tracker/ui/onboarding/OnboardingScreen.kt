package com.preciousmetals.tracker.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.CombinedMetalsLogo
import com.preciousmetals.tracker.ui.theme.IconTileSurfaceDark
import com.preciousmetals.tracker.ui.theme.TextMuted33Dark
import com.preciousmetals.tracker.ui.theme.TextMuted56Dark
import com.preciousmetals.tracker.ui.theme.TextMuted67Dark
import kotlinx.coroutines.launch

/**
 * Shown once, the first time the app is opened after install (gated by
 * [com.preciousmetals.tracker.data.preferences.UserPreferences.onboardingCompleted]) — a quick
 * walkthrough of the app's main features before the user lands on their (empty) portfolio.
 */
@Composable
fun OnboardingGate(content: @Composable () -> Unit) {
    val container = LocalAppContainer.current
    val completed by container.userPreferences.onboardingCompleted.collectAsStateWithLifecycle(initialValue = false)
    // Set as soon as the user finishes/skips, instead of waiting for the DataStore write to round
    // back through the flow — otherwise "Commencer" would feel like it did nothing for a beat.
    var justCompleted by rememberSaveable { mutableStateOf(false) }

    if (completed || justCompleted) {
        content()
        return
    }

    val scope = rememberCoroutineScope()
    OnboardingScreen(
        onFinish = {
            justCompleted = true
            scope.launch { container.userPreferences.setOnboardingCompleted(true) }
        },
    )
}

private data class OnboardingPage(
    val icon: ImageVector? = null,
    val useCombinedLogo: Boolean = false,
    val title: String,
    val description: String,
)

private val OnboardingPages = listOf(
    OnboardingPage(
        useCombinedLogo = true,
        title = "Bienvenue sur Suivi Métaux",
        description = "Suivez la valeur de votre or, argent, platine, palladium et cuivre, au même endroit.",
    ),
    OnboardingPage(
        icon = Icons.Outlined.AccountBalanceWallet,
        title = "Votre portefeuille",
        description = "Ajoutez vos lingots, pièces et bijoux : valeur totale, coût d'achat et plus-value calculés automatiquement, en euros ou en dollars.",
    ),
    OnboardingPage(
        icon = Icons.AutoMirrored.Outlined.ShowChart,
        title = "Cours en temps réel",
        description = "Consultez le cours de chaque métal avec historique et graphiques, actualisés automatiquement même quand l'app est fermée.",
    ),
    OnboardingPage(
        icon = Icons.Outlined.NotificationsActive,
        title = "Alertes de cours",
        description = "Soyez notifié dès qu'un métal monte ou descend d'un pourcentage du cours actuel — 5, 10, 20 %... ou la valeur de votre choix.",
    ),
    OnboardingPage(
        icon = Icons.Outlined.Inventory2,
        title = "Lieux de stockage",
        description = "Organisez vos avoirs par lieu de stockage (coffre, banque, domicile...) et consultez la valeur de chaque lieu séparément.",
    ),
    OnboardingPage(
        icon = Icons.Outlined.Widgets,
        title = "Widgets & sécurité",
        description = "Ajoutez les cours et votre solde à l'écran d'accueil, et protégez l'app par empreinte digitale si vous le souhaitez.",
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == OnboardingPages.lastIndex

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.End) {
                if (!isLastPage) {
                    TextButton(onClick = onFinish) {
                        Text("Passer", color = TextMuted56Dark)
                    }
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
                OnboardingPageContent(OnboardingPages[index])
            }
            PageIndicator(
                count = OnboardingPages.size,
                current = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            )
            Button(
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 0.dp).padding(bottom = 28.dp),
            ) {
                Text(if (isLastPage) "Commencer" else "Suivant")
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(96.dp).background(IconTileSurfaceDark, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (page.useCombinedLogo) {
                CombinedMetalsLogo(size = 56.dp)
            } else if (page.icon != null) {
                Icon(page.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
        Text(
            page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 28.dp),
        )
        Text(
            page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted67Dark,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun PageIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        repeat(count) { index ->
            val selected = index == current
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (selected) 8.dp else 6.dp)
                    .background(if (selected) Color.White else TextMuted33Dark, CircleShape),
            )
        }
    }
}
