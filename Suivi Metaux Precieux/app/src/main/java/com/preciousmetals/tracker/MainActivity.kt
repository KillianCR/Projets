package com.preciousmetals.tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.preciousmetals.tracker.ui.LocalAppContainer
import com.preciousmetals.tracker.ui.components.EmberGradientBackground
import com.preciousmetals.tracker.ui.lock.AppLockGate
import com.preciousmetals.tracker.ui.navigation.AppNavHost
import com.preciousmetals.tracker.ui.theme.SuiviMetauxTheme

/**
 * A [FragmentActivity] (not the usual bare `ComponentActivity`) because [AppLockGate] drives a
 * [androidx.biometric.BiometricPrompt], which requires one.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SuiviMetauxApp).container

        setContent {
            SuiviMetauxTheme {
                RequestNotificationPermissionIfNeeded()
                CompositionLocalProvider(LocalAppContainer provides container) {
                    // The ember radial gradient is a dark-theme-only look (see redesign report);
                    // light theme keeps its flat background, never specified by the brief.
                    if (androidx.compose.foundation.isSystemInDarkTheme()) {
                        EmberGradientBackground(modifier = Modifier.fillMaxSize()) {
                            AppLockGate {
                                AppNavHost()
                            }
                        }
                    } else {
                        androidx.compose.material3.Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.background,
                        ) {
                            AppLockGate {
                                AppNavHost()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
