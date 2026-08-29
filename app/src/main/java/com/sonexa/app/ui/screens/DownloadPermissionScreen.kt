package com.sonexa.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.SonexaInputBg
import com.sonexa.app.ui.theme.SonexaInputBorder
import com.sonexa.app.ui.theme.SonexaTextMuted
import com.sonexa.app.ui.theme.SonexaTextWhite
import com.sonexa.app.ui.viewmodel.PermissionsOnboardingViewModel

@Composable
fun DownloadPermissionScreen(
    onPermissionGranted: () -> Unit,
    onSkip: () -> Unit,
    viewModel: PermissionsOnboardingViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val saving by viewModel.saving.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.saveDownloads(enabled = granted) {
            if (granted) onPermissionGranted() else onSkip()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0726),
                        Color(0xFF080512),
                        Color(0xFF05030A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Offline Listening Storage",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SonexaTextWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Allow storage access to download high quality audio tracks for seamless offline playback anytime, anywhere.",
                    fontSize = 14.sp,
                    color = SonexaTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SonexaGradientButton(
                    text = if (saving) "Saving..." else "Allow Download Access →",
                    onClick = {
                        if (saving) return@SonexaGradientButton
                        when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                                viewModel.saveDownloads(enabled = true, onDone = onPermissionGranted)
                            else ->
                                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SonexaInputBg)
                        .border(1.dp, SonexaInputBorder, RoundedCornerShape(16.dp))
                        .clickable(enabled = !saving) {
                            viewModel.saveDownloads(enabled = false, onDone = onSkip)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Skip for Now",
                        color = SonexaTextMuted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
