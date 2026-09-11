package com.preciousmetals.tracker.ui.lock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.preciousmetals.tracker.ui.LocalAppContainer

private val allowedAuthenticators =
    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

/**
 * Gates [content] behind biometric/device-credential auth when the user enabled "Verrouillage
 * biométrique" in Réglages — this app tracks a net-worth-bearing portfolio, so protecting it from
 * a casual glance at an unlocked phone is worth the one extra tap. Re-locks whenever the app is
 * backgrounded ([Lifecycle.Event.ON_STOP]), not just on cold start.
 */
@Composable
fun AppLockGate(content: @Composable () -> Unit) {
    val container = LocalAppContainer.current
    val lockEnabled by container.userPreferences.appLockEnabled.collectAsStateWithLifecycle(initialValue = false)
    var isUnlocked by rememberSaveable { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, lockEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && lockEnabled) {
                isUnlocked = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val activity = LocalContext.current as? FragmentActivity

    if (!lockEnabled || isUnlocked || activity == null) {
        content()
        return
    }

    LockScreen(onUnlockClick = { promptBiometricAuth(activity, onSuccess = { isUnlocked = true }) })

    // Prompt immediately on first show, so the user isn't forced to tap "Déverrouiller" every time.
    LaunchedEffect(activity) {
        promptBiometricAuth(activity, onSuccess = { isUnlocked = true })
    }
}

@Composable
private fun LockScreen(onUnlockClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(56.dp).width(56.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Application verrouillée", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Authentifiez-vous pour accéder à votre portefeuille.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onUnlockClick) {
                Icon(imageVector = Icons.Outlined.Fingerprint, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Déverrouiller")
            }
        }
    }
}

private fun promptBiometricAuth(activity: FragmentActivity, onSuccess: () -> Unit) {
    val canAuthenticate = BiometricManager.from(activity).canAuthenticate(allowedAuthenticators)
    if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) return

    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        },
    )
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Suivi Métaux verrouillé")
        .setSubtitle("Authentifiez-vous pour continuer")
        .setAllowedAuthenticators(allowedAuthenticators)
        .build()
    prompt.authenticate(promptInfo)
}
