package com.sonexa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.AppUpdateViewModel
import com.sonexa.app.ui.viewmodel.CatalogUiState

@Composable
fun AppUpdateCheckScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppUpdateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isChecking = uiState is CatalogUiState.Loading
    val message = when (val s = uiState) {
        is CatalogUiState.Ready -> s.data.message.ifBlank { "You're running the latest Sonexa v${s.data.latestVersion}" }
        is CatalogUiState.Error -> s.message
        else -> "Verifying Sonexa version & security features"
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
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Center Icon Card
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(SonexaInputBg)
                        .border(1.5.dp, SonexaPurpleLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            color = SonexaPurpleLight,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(42.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isChecking) "Checking for Updates..." else "App is Up to Date!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SonexaTextWhite
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isChecking)
                        "Verifying Sonexa version & security features"
                    else
                        message,
                    fontSize = 13.sp,
                    color = SonexaTextMuted
                )
            }

            // Bottom Action
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isChecking) {
                    SonexaGradientButton(
                        text = "Continue",
                        onClick = onContinue
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
