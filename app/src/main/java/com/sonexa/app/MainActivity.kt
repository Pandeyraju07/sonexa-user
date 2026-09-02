package com.sonexa.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import coil.Coil
import coil.ImageLoader
import coil.intercept.Interceptor
import com.sonexa.app.auth.social.AppleSignInHelper
import com.sonexa.app.auth.social.SocialAuthEvents
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.local.SessionManager
import com.sonexa.app.ui.SonexaAppFlow
import com.sonexa.app.ui.theme.SonexaBgDark
import com.sonexa.app.ui.theme.SonexaTheme
import com.sonexa.app.ui.viewmodel.PlaybackViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(SessionManager.getInstance(this))
        com.sonexa.app.data.local.LikedSongsStore.init(this)
        com.sonexa.app.data.local.PodcastDownloadManager.init(this)
        com.sonexa.app.data.local.UserPlaylistStore.init(this)
        setupCoil()
        handleSocialCallback(intent)
        com.sonexa.app.util.DeepLinkManager.handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            SonexaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SonexaBgDark
                ) {
                    SonexaAppFlow()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSocialCallback(intent)
        com.sonexa.app.util.DeepLinkManager.handleIntent(intent)
    }

    private fun handleSocialCallback(intent: Intent?) {
        val profile = AppleSignInHelper.parseCallbackIntent(intent) ?: return
        SocialAuthEvents.emitAppleResult(profile)
    }

    private fun setupCoil() {
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(Interceptor { chain ->
                    val request = chain.request
                    val data = request.data
                    if (data is String) {
                        val resolved = PlaybackViewModel.resolveMediaUrl(data)
                        if (resolved != data) {
                            val newRequest = request.newBuilder().data(resolved).build()
                            return@Interceptor chain.proceed(newRequest)
                        }
                    }
                    chain.proceed(request)
                })
            }
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)
    }
}
