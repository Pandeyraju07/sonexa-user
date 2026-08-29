package com.sonexa.app.auth.social

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SocialAuthEvents {
    private val _appleResult = MutableSharedFlow<SocialProfile>(extraBufferCapacity = 1)
    val appleResult = _appleResult.asSharedFlow()

    fun emitAppleResult(profile: SocialProfile) {
        _appleResult.tryEmit(profile)
    }
}
