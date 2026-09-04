package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.*

@Composable
fun GenericErrorScreen(
    title: String,
    message: String,
    icon: ImageVector,
    buttonText: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SonexaBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0x309825DD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = SonexaPurpleLight, modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SonexaTextWhite, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, fontSize = 14.sp, color = SonexaTextMuted, textAlign = TextAlign.Center, lineHeight = 20.sp)

            Spacer(modifier = Modifier.height(28.dp))

            SonexaGradientButton(
                text = buttonText,
                onClick = onRetry
            )
        }
    }
}

// 8 Dedicated Fallback Screen Variations
@Composable
fun NoInternetScreen(onRetry: () -> Unit) {
    GenericErrorScreen(
        title = "No Internet Connection",
        message = "Please check your Wi-Fi or mobile data connection and try again.",
        icon = Icons.Default.WifiOff,
        buttonText = "Retry Connection",
        onRetry = onRetry
    )
}

@Composable
fun ServerErrorScreen(onRetry: () -> Unit) {
    GenericErrorScreen(
        title = "Server Maintenance Error (500)",
        message = "Zynera servers are experiencing high traffic. Our engineers are on it!",
        icon = Icons.Default.CloudOff,
        buttonText = "Refresh Server",
        onRetry = onRetry
    )
}

@Composable
fun EmptyStateScreen(onAction: () -> Unit) {
    GenericErrorScreen(
        title = "Your Library is Empty",
        message = "Start exploring trending music, playlists, and AI DJ mixes to populate your library.",
        icon = Icons.Default.MusicOff,
        buttonText = "Explore Music Now",
        onRetry = onAction
    )
}

@Composable
fun MaintenanceScreen(onAction: () -> Unit) {
    GenericErrorScreen(
        title = "Scheduled Maintenance",
        message = "Zynera is upgrading its Lossless Audio engine. We will be back online shortly.",
        icon = Icons.Default.Build,
        buttonText = "Check Status",
        onRetry = onAction
    )
}

@Composable
fun SessionExpiredScreen(onReLogin: () -> Unit) {
    GenericErrorScreen(
        title = "Session Expired",
        message = "Your login session has expired for security reasons. Please log in again.",
        icon = Icons.Default.LockReset,
        buttonText = "Log In Again",
        onRetry = onReLogin
    )
}

@Composable
fun UpdateRequiredScreen(onUpdate: () -> Unit) {
    GenericErrorScreen(
        title = "App Update Required",
        message = "A mandatory Zynera update (v2.4.0) with AI Voice features is ready for download.",
        icon = Icons.Default.SystemUpdate,
        buttonText = "Update Now",
        onRetry = onUpdate
    )
}

@Composable
fun NoSearchResultsScreen(onClear: () -> Unit) {
    GenericErrorScreen(
        title = "No Results Found",
        message = "We couldn't find any match for your search. Try searching by artist, album, or genre.",
        icon = Icons.Default.SearchOff,
        buttonText = "Clear Search",
        onRetry = onClear
    )
}

@Composable
fun DownloadFailedScreen(onRetry: () -> Unit) {
    GenericErrorScreen(
        title = "Download Failed",
        message = "Failed to save Hi-Fi Lossless audio file. Please check storage space and network.",
        icon = Icons.Default.FileDownloadOff,
        buttonText = "Retry Download",
        onRetry = onRetry
    )
}
