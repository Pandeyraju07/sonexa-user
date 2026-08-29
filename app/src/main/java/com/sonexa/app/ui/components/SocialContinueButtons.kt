package com.sonexa.app.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sonexa.app.auth.social.AppleSignInHelper
import com.sonexa.app.auth.social.GoogleSignInHelper
import com.sonexa.app.auth.social.SocialAuthException
import com.sonexa.app.auth.social.SocialProfile
import com.sonexa.app.auth.social.SocialProvider
import kotlinx.coroutines.launch

/**
 * Google / Apple continue buttons with Spotify-style consent sheet,
 * then native account picker / Custom Tabs.
 */
@Composable
fun SocialContinueButtons(
    enabled: Boolean = true,
    onGoogleSuccess: (SocialProfile) -> Unit,
    onAppleSuccess: (SocialProfile) -> Unit = {},
    onBusyChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingProvider by remember { mutableStateOf<SocialProvider?>(null) }
    val activity = context as? Activity

    if (pendingProvider != null) {
        SocialConsentBottomSheet(
            provider = pendingProvider!!,
            onDismiss = { pendingProvider = null },
            onContinue = {
                val provider = pendingProvider
                pendingProvider = null
                when (provider) {
                    SocialProvider.GOOGLE -> {
                        if (activity == null) {
                            Toast.makeText(context, "Unable to open Google Sign-In", Toast.LENGTH_SHORT).show()
                            return@SocialConsentBottomSheet
                        }
                        onBusyChanged(true)
                        scope.launch {
                            try {
                                val profile = GoogleSignInHelper(activity).signIn()
                                onGoogleSuccess(profile)
                            } catch (e: SocialAuthException.Cancelled) {
                                Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
                            } catch (e: SocialAuthException) {
                                Toast.makeText(
                                    context,
                                    e.message ?: "Google Sign-In failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    e.localizedMessage ?: "Google Sign-In failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                onBusyChanged(false)
                            }
                        }
                    }
                    SocialProvider.APPLE -> {
                        try {
                            AppleSignInHelper.launchAuthorize(context)
                            Toast.makeText(
                                context,
                                "Complete Sign in with Apple in the browser window",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: SocialAuthException) {
                            Toast.makeText(
                                context,
                                e.message ?: "Apple Sign-In is not configured yet",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    null -> Unit
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SonexaSocialButton(
            text = "Continue with Google",
            isGoogle = true,
            onClick = {
                if (enabled) pendingProvider = SocialProvider.GOOGLE
            }
        )
        SonexaSocialButton(
            text = "Continue with Apple",
            isGoogle = false,
            onClick = {
                if (enabled) pendingProvider = SocialProvider.APPLE
            }
        )
    }
}
