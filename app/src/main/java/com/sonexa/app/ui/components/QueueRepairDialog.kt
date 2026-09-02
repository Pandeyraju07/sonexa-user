package com.sonexa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.data.model.FixQueueResponseDto
import com.sonexa.app.ui.theme.SpotifyGreen

@Composable
fun QueueRepairDialog(
    response: FixQueueResponseDto,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C152B),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AI Queue Optimization",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = response.balanceSummary.ifBlank { "Balanced track queue and distributed artist fatigue for smooth playback transitions." },
                    fontSize = 14.sp,
                    color = Color(0xFFDDD6FE),
                    lineHeight = 20.sp
                )
                if (response.removedDuplicatesCount > 0) {
                    Text(
                        text = "• Removed ${response.removedDuplicatesCount} duplicate track(s)",
                        fontSize = 13.sp,
                        color = SpotifyGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "• Total optimized tracks: ${response.balancedQueue.size}",
                    fontSize = 13.sp,
                    color = Color(0xFFAFA9BB)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
            ) {
                Text("Apply Balanced Queue", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}