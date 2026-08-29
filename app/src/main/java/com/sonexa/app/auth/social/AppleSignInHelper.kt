package com.sonexa.app.auth.social

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.sonexa.app.BuildConfig
import java.util.UUID
import androidx.core.content.edit
import androidx.core.net.toUri

object AppleSignInHelper {

    const val PREFS = "sonexa_apple_auth"
    const val KEY_STATE = "oauth_state"

    fun launchAuthorize(context: Context) {
        val serviceId = BuildConfig.APPLE_SERVICE_ID.trim()
        val redirectUri = BuildConfig.APPLE_REDIRECT_URI.trim()
        if (serviceId.isBlank() || redirectUri.isBlank()) {
            throw SocialAuthException.NotConfigured("Apple")
        }

        val state = UUID.randomUUID().toString()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_STATE, state)
            }

        val uri = "https://appleid.apple.com/auth/authorize".toUri().buildUpon()
            .appendQueryParameter("client_id", serviceId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", "code id_token")
            .appendQueryParameter("response_mode", "form_post")
            .appendQueryParameter("scope", "name email")
            .appendQueryParameter("state", state)
            .build()

        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, uri)
    }

    fun parseCallbackIntent(intent: Intent?): SocialProfile? {
        val data = intent?.data ?: return null
        val idToken = data.getQueryParameter("id_token")
            ?: data.getQueryParameter("identity_token")
            ?: return null
        val email = data.getQueryParameter("email")
        val name = listOfNotNull(
            data.getQueryParameter("first_name"),
            data.getQueryParameter("last_name")
        ).joinToString(" ").ifBlank { data.getQueryParameter("name") }

        return SocialProfile(
            idToken = idToken,
            email = email,
            name = name
        )
    }
}
