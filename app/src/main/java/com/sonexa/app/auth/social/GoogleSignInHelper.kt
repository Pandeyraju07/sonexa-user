package com.sonexa.app.auth.social

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sonexa.app.BuildConfig
import java.util.UUID

class GoogleSignInHelper(
    private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(): SocialProfile {
        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        if (webClientId.isBlank()) {
            throw SocialAuthException.NotConfigured("Google")
        }

        return try {
            tryGoogleIdBottomSheet(webClientId)
        } catch (e: NoCredentialException) {
            // Fall back to the explicit "Sign in with Google" button flow
            trySignInWithGoogleButton(webClientId)
        } catch (e: GetCredentialCancellationException) {
            throw SocialAuthException.Cancelled()
        } catch (e: SocialAuthException) {
            throw e
        } catch (e: Exception) {
            throw SocialAuthException.Failed(
                e.localizedMessage ?: "Google Sign-In failed. Please try again.",
                e
            )
        }
    }

    private suspend fun tryGoogleIdBottomSheet(webClientId: String): SocialProfile {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .setNonce(UUID.randomUUID().toString())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )
        return parseCredential(result.credential)
    }

    private suspend fun trySignInWithGoogleButton(webClientId: String): SocialProfile {
        val signInOption = GetSignInWithGoogleOption.Builder(webClientId)
            .setNonce(UUID.randomUUID().toString())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )
        return parseCredential(result.credential)
    }

    private fun parseCredential(credential: androidx.credentials.Credential): SocialProfile {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val google = GoogleIdTokenCredential.createFrom(credential.data)
            return SocialProfile(
                idToken = google.idToken,
                email = google.id,
                name = google.displayName,
                photoUrl = google.profilePictureUri?.toString()
            )
        }
        throw SocialAuthException.Failed("Unexpected Google credential type")
    }
}
