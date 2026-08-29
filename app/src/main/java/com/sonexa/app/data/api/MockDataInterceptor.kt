package com.sonexa.app.data.api

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * Offline fallback for non-auth APIs only.
 * Auth endpoints always use the real backend response (or surface network errors).
 */
class MockDataInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val isAuth = url.contains("/auth/")

        return try {
            val response = chain.proceed(request)
            if (response.isSuccessful || isAuth) {
                response
            } else {
                response.close()
                buildMockResponse(request)
            }
        } catch (e: IOException) {
            if (isAuth) throw e
            buildMockResponse(request)
        }
    }

    private fun buildMockResponse(request: okhttp3.Request): Response {
        val url = request.url.toString()
        val method = request.method
        val track = """{"id":"tr_1","title":"Starboy","artist":"The Weeknd","album":"Starboy","durationMs":230000,"coverUrl":"https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500","playsCount":"48M","isLiked":true}"""
        val album = """{"id":"alb_1","title":"Starboy","artist":"The Weeknd","year":"2016","coverUrl":"https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500","trackCount":18}"""
        val playlist = """{"id":"pl_1","title":"Daily Mix 1","subtitle":"Arijit Singh, Atif Aslam","artworkType":"waves","coverUrl":"https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500"}"""
        val artist = """{"id":"art_1","name":"The Weeknd","genre":"R&B / Pop","bio":"Canadian artist","imageUrl":"https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500","color1":"#E534B2","color2":"#FF52C4","followersCount":95000000,"verified":true}"""

        val json = when {
            url.contains("config/splash") -> """
                {"success":true,"appName":"Sonexa AI","version":"1.0.0","minSupportedVersion":"1.0.0","forceUpdate":false,"maintenanceMode":false,"message":"Welcome to Sonexa AI Platform"}
            """.trimIndent()

            url.contains("config/onboarding") -> """
                {"success":true,"slides":[
                  {"title":"AI Personal DJ","subtitle":"Music adapted to your mood in real-time"},
                  {"title":"Lossless Audio","subtitle":"Studio-quality sound with spatial audio"},
                  {"title":"Smart Discovery","subtitle":"Discover emerging tracks with Sonexa AI"}
                ]}
            """.trimIndent()

            url.contains("config/app-update") -> """
                {"success":true,"updateAvailable":false,"forceUpdate":false,"latestVersion":"1.0.0","message":"You're on the latest version","storeUrl":""}
            """.trimIndent()

            url.contains("config/permissions") -> """
                {"success":true,"notifications":{"title":"Stay in the loop","subtitle":"Allow notifications","required":false},"downloads":{"title":"Offline listening","subtitle":"Allow storage access","required":false}}
            """.trimIndent()

            url.contains("config/languages") -> """
                {"success":true,"title":"Choose Music Languages","subtitle":"Select languages you love","minSelection":1,"defaultSelected":["English","Hindi"],"languages":[
                  {"code":"en","name":"English","nativeName":"International"},{"code":"hi","name":"Hindi","nativeName":"हिंदी"},
                  {"code":"pa","name":"Punjabi","nativeName":"ਪੰਜਾਬੀ"},{"code":"ta","name":"Tamil","nativeName":"தமிழ்"}
                ]}
            """.trimIndent()

            url.contains("music/home") -> """
                {"success":true,"continueListening":[$track],
                 "trendingNow":[$track,{"id":"tr_2","title":"Kesariya","artist":"Arijit Singh","playsCount":"45M","isLiked":false}],
                 "popularAlbums":[$album],
                 "madeForYou":[$playlist,{"id":"pl_2","title":"Energy Boost","subtitle":"Workout hits","artworkType":"runner"}]}
            """.trimIndent()

            url.contains("music/search") -> """
                {"success":true,"tracks":[$track],"albums":[$album],"artists":["The Weeknd","Arijit Singh","Dua Lipa"]}
            """.trimIndent()

            url.contains("music/trending") || url.contains("music/queue") -> """
                {"success":true,"tracks":[$track],"nowPlaying":$track,"queue":[$track]}
            """.trimIndent()

            url.contains("music/albums/") -> """{"success":true,"album":$album,"tracks":[$track]}"""
            url.contains("music/playlists/") -> """{"success":true,"playlist":$playlist,"tracks":[$track]}"""
            url.contains("music/artists/") && !url.endsWith("/artists") -> """{"success":true,"artist":$artist,"tracks":[$track],"albums":[$album]}"""
            url.contains("music/tracks/") -> """{"success":true,"track":$track}"""

            url.contains("music/genres") || url.contains("profile-setup/genres") && method == "GET" -> """
                {"success":true,"genres":[
                  {"id":"g_1","name":"Pop","color1":"#E534B2","color2":"#FF52C4"},
                  {"id":"g_2","name":"Hip-Hop","color1":"#6B3CE9","color2":"#9825DD"},
                  {"id":"g_3","name":"Bollywood","color1":"#F59E0B","color2":"#EF4444"},
                  {"id":"g_4","name":"EDM / Dance","color1":"#06B6D4","color2":"#3B82F6"}
                ]}
            """.trimIndent()

            url.contains("music/artists") || (url.contains("profile-setup/artists") && method == "GET") -> """
                {"success":true,"artists":[$artist,
                  {"id":"art_2","name":"Arijit Singh","genre":"Bollywood","color1":"#5935E5","color2":"#9825DD","followersCount":48000000,"verified":true},
                  {"id":"art_3","name":"Dua Lipa","genre":"Dance Pop","color1":"#8B5CF6","color2":"#EC4899","followersCount":72000000,"verified":true}
                ]}
            """.trimIndent()

            url.contains("music/moods") || (url.contains("profile-setup/moods") && method == "GET") -> """
                {"success":true,"moods":[
                  {"id":"m_1","name":"Energetic","iconKey":"bolt","colorHex":"#F59E0B"},
                  {"id":"m_2","name":"Relaxed","iconKey":"spa","colorHex":"#06B6D4"},
                  {"id":"m_3","name":"Focused","iconKey":"center_focus","colorHex":"#8B5CF6"},
                  {"id":"m_4","name":"Party","iconKey":"celebration","colorHex":"#E534B2"}
                ]}
            """.trimIndent()

            url.contains("podcasts/") -> """
                {"success":true,"podcast":{"id":"pod_1","title":"Sonexa Tech Talks","host":"Maya Chen","description":"Music tech deep dives","coverUrl":"https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500","category":"Technology"},
                 "episodes":[{"id":"ep_1","title":"AI DJs of Tomorrow","description":"Generative remixes","durationLabel":"42 min","episodeNumber":1}]}
            """.trimIndent()

            url.contains("podcasts") -> """
                {"success":true,"podcasts":[
                  {"id":"pod_1","title":"Sonexa Tech Talks","host":"Maya Chen","description":"Music tech","coverUrl":"https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500","category":"Technology"},
                  {"id":"pod_2","title":"Beat Culture","host":"Arjun Mehta","description":"Song stories","coverUrl":"https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500","category":"Culture"}
                ]}
            """.trimIndent()

            url.contains("ai/signature") -> """
                {"success":true,"signatureId":"ai_sig_mock","vibeTitle":"AI Signature: NEON CYBERVIBE","aiGeneratedAudioUrl":"https://example.com/ai.mp3","bpm":128,"key":"F# Minor","recommendedTracks":[$track]}
            """.trimIndent()

            url.contains("ai/chat") -> """{"success":true,"reply":"Sonexa AI: Try Energy Boost or a lo-fi chill mix."}"""

            url.contains("user/notifications") -> """
                {"success":true,"notifications":[
                  {"id":"notif_1","title":"New Single Alert","message":"Arijit Singh released a new track","iconKey":"music_note","colorHex":"#E534B2","timeAgo":"10m ago","read":false},
                  {"id":"notif_2","title":"AI Mix Ready","message":"Your weekly mix is ready","iconKey":"auto_awesome","colorHex":"#8B5CF6","timeAgo":"1h ago","read":false}
                ]}
            """.trimIndent()

            url.contains("user/library") -> """{"success":true,"likedSongs":[$track],"savedAlbums":[$album]}"""

            url.contains("user/premium") -> """
                {"success":true,"isPremium":false,"plans":[
                  {"id":"individual","name":"Individual","price":"₹119/mo","description":"1 account"},
                  {"id":"duo","name":"Duo","price":"₹149/mo","description":"2 accounts"}
                ],"benefits":["Ad-free","Offline downloads","Hi-Fi audio"]}
            """.trimIndent()

            url.contains("user/settings") -> """
                {"success":true,"settings":{
                  "audioQuality":"High",
                  "downloadQuality":"High",
                  "downloadOverWifiOnly":true,
                  "crossfade":false,
                  "normalizeVolume":true,
                  "explicitContent":false,
                  "gaplessPlayback":true,
                  "language":"English (India) • Hindi • Punjabi",
                  "languages":["English (India)","Hindi","Punjabi"],
                  "theme":"Dark",
                  "accentStyle":"Glassmorphism",
                  "pushNotifications":true,
                  "friendActivity":true,
                  "newReleaseAlerts":true,
                  "aiSensitivity":"High",
                  "aiVoiceModel":"Sonexa Voice v2.4",
                  "smartLyrics":true,
                  "dataSharing":false,
                  "twoFactorEnabled":false,
                  "showActiveSessions":true,
                  "connectedDevices":["This Android phone","Bluetooth earbuds"],
                  "appVersion":"2.4.0"
                }}
            """.trimIndent()

            url.contains("user/profile") -> """
                {"success":true,"user":{"id":"usr_1","name":"Sonexa Listener","email":"user@sonexa.ai","profilePicUrl":"https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300","isPremium":false,"followersCount":1420,"followingCount":380}}
            """.trimIndent()

            url.contains("user/like") || url.contains("premium/subscribe") ||
                url.contains("profile-setup/") || method == "POST" || method == "PUT" -> """
                {"success":true,"message":"Success","trackId":"tr_1","isLiked":true,"items":[],"count":0,"languages":["English","Hindi"]}
            """.trimIndent()

            else -> """{"success":true,"message":"Success"}"""
        }

        return Response.Builder()
            .code(200)
            .message("OK")
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }
}
