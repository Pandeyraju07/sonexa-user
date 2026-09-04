package com.sonexa.app.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.sonexa.app.audio.playback.YouTubePlaybackProvider
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerView(
    youtubeProvider: YouTubePlaybackProvider,
    modifier: Modifier = Modifier,
    videoAspectRatio: Float = 16f / 9f
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isApiReady by remember { mutableStateOf(false) }
    val currentVideoId by youtubeProvider.currentVideoId.collectAsState()

    // Listen for commands emitted by the YouTube playback provider
    LaunchedEffect(webViewRef, isApiReady) {
        val wv = webViewRef ?: return@LaunchedEffect
        youtubeProvider.commands.collectLatest { cmd ->
            when (cmd.type) {
                YouTubePlaybackProvider.CommandType.LOAD_VIDEO -> {
                    val script = "if (window.loadVideo) { window.loadVideo('${cmd.videoId}', ${cmd.startSeconds}); }"
                    wv.evaluateJavascript(script, null)
                }
                YouTubePlaybackProvider.CommandType.PLAY -> {
                    wv.evaluateJavascript("if (window.playVideo) { window.playVideo(); }", null)
                }
                YouTubePlaybackProvider.CommandType.PAUSE -> {
                    wv.evaluateJavascript("if (window.pauseVideo) { window.pauseVideo(); }", null)
                }
                YouTubePlaybackProvider.CommandType.SEEK -> {
                    wv.evaluateJavascript("if (window.seekTo) { window.seekTo(${cmd.startSeconds}); }", null)
                }
                YouTubePlaybackProvider.CommandType.STOP -> {
                    wv.evaluateJavascript("if (window.stopVideo) { window.stopVideo(); }", null)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .border(1.dp, Color(0xFF2E2452), RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(videoAspectRatio)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    createYouTubeWebView(ctx, youtubeProvider) {
                        isApiReady = true
                    }.also { webViewRef = it }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Official YouTube Attribution & Open in YouTube bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14121C))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartDisplay,
                    contentDescription = null,
                    tint = Color(0xFFFF0000),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Powered by YouTube",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        val vid = currentVideoId
                        if (!vid.isNullOrBlank()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$vid"))
                            context.startActivity(intent)
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Open in YouTube",
                    fontSize = 11.sp,
                    color = Color(0xFFC084FC),
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open in YouTube",
                    tint = Color(0xFFC084FC),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createYouTubeWebView(
    context: Context,
    provider: YouTubePlaybackProvider,
    onReady: () -> Unit
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(android.graphics.Color.BLACK)

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        webChromeClient = WebChromeClient()
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onReady()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // Keep player within iframe
                return false
            }
        }

        // Add JavaScript bridge for bi-directional communication
        addJavascriptInterface(object {
            @JavascriptInterface
            fun onPlayerReady(duration: Float) {
                post { provider.onPlayerReady(duration) }
            }

            @JavascriptInterface
            fun onPlayerStateChange(state: Int) {
                post { provider.onPlayerStateChange(state) }
            }

            @JavascriptInterface
            fun onTimeUpdate(current: Float, duration: Float) {
                post { provider.onTimeUpdate(current, duration) }
            }

            @JavascriptInterface
            fun onPlayerError(code: Int, msg: String) {
                post { provider.onPlayerError(code, msg) }
            }
        }, "SonexaBridge")

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
              <style>
                * { box-sizing: border-box; }
                body, html { margin:0; padding:0; width:100%; height:100%; background-color:#000000; overflow:hidden; }
                #player { width:100vw; height:100vh; position:absolute; top:0; left:0; }
              </style>
              <script src="https://www.youtube.com/iframe_api"></script>
            </head>
            <body>
              <div id="player"></div>
              <script>
                var player;
                var trackerInterval;
                function onYouTubeIframeAPIReady() {
                  player = new YT.Player('player', {
                    height: '100%',
                    width: '100%',
                    playerVars: {
                      'autoplay': 1,
                      'controls': 1,
                      'rel': 0,
                      'playsinline': 1,
                      'enablejsapi': 1,
                      'modestbranding': 1,
                      'fs': 1,
                      'origin': 'https://zynera.app'
                    },
                    events: {
                      'onReady': onPlayerReady,
                      'onStateChange': onPlayerStateChange,
                      'onError': onPlayerError
                    }
                  });
                }
                function onPlayerReady(event) {
                  if (window.SonexaBridge) {
                    SonexaBridge.onPlayerReady(player.getDuration() || 0);
                  }
                  startTracker();
                }
                function onPlayerStateChange(event) {
                  if (window.SonexaBridge) {
                    SonexaBridge.onPlayerStateChange(event.data);
                  }
                }
                function onPlayerError(event) {
                  if (window.SonexaBridge) {
                    SonexaBridge.onPlayerError(event.data, "YouTube Player Error " + event.data);
                  }
                }
                function startTracker() {
                  if (trackerInterval) clearInterval(trackerInterval);
                  trackerInterval = setInterval(function() {
                    if (player && player.getCurrentTime && window.SonexaBridge) {
                      SonexaBridge.onTimeUpdate(player.getCurrentTime(), player.getDuration() || 0);
                    }
                  }, 300);
                }
                function loadVideo(videoId, startSeconds) {
                  if (player && player.loadVideoById) {
                    player.loadVideoById({'videoId': videoId, 'startSeconds': startSeconds || 0});
                  }
                }
                function playVideo() { if (player && player.playVideo) player.playVideo(); }
                function pauseVideo() { if (player && player.pauseVideo) player.pauseVideo(); }
                function seekTo(seconds) { if (player && player.seekTo) player.seekTo(seconds, true); }
                function stopVideo() { if (player && player.stopVideo) player.stopVideo(); }
              </script>
            </body>
            </html>
        """.trimIndent()

        loadDataWithBaseURL("https://zynera.app", htmlContent, "text/html", "UTF-8", null)
    }
}
