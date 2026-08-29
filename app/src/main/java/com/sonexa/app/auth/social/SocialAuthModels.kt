package com.sonexa.app.auth.social

data class SocialProfile(
    val idToken: String,
    val email: String? = null,
    val name: String? = null,
    val photoUrl: String? = null
)

sealed class SocialAuthException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NotConfigured(provider: String) : SocialAuthException(
        "$provider Sign-In is not configured. Add the client ID in local.properties and rebuild."
    )
    class Cancelled : SocialAuthException("Sign-in was cancelled")
    class Failed(message: String, cause: Throwable? = null) : SocialAuthException(message, cause)
}

enum class SocialProvider {
    GOOGLE,
    APPLE
}
