package com.sonexa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.auth.social.SocialProvider
import com.sonexa.app.ui.theme.*

/**
 * Spotify-style consent sheet shown before launching Google / Apple account picker.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialConsentBottomSheet(
    provider: SocialProvider,
    onContinue: () -> Unit,
    onDismiss: () -> Unit
) {
    val isGoogle = provider == SocialProvider.GOOGLE
    val providerName = if (isGoogle) "Google" else "Apple"
    val accent = if (isGoogle) Color(0xFF4285F4) else Color.White

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF12101C),
        contentColor = SonexaTextWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3A3450))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SonexaTextMuted
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BrandChip(label = "Z", color = SonexaPurpleLight)
                Text(text = "↔", color = SonexaTextMuted, fontSize = 18.sp)
                BrandChip(
                    label = if (isGoogle) "G" else "",
                    color = accent
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Continue with $providerName",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = SonexaTextWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Zynera uses $providerName to let you sign in securely. " +
                    "You’ll choose your $providerName account next and review Google/Apple’s consent screen.",
                fontSize = 13.sp,
                color = SonexaTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1628))
                    .border(1.dp, Color(0xFF2E2742), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Zynera will receive",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SonexaPurpleLight,
                        letterSpacing = 0.6.sp
                    )
                    ConsentRow(
                        icon = Icons.Outlined.Person,
                        title = "Name & profile photo",
                        subtitle = "Used to personalize your Zynera profile"
                    )
                    ConsentRow(
                        icon = Icons.Outlined.Email,
                        title = "Email address",
                        subtitle = "Used for account recovery and notifications"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "By continuing, you agree to Zynera’s Terms and Privacy Policy. " +
                    "We never post on your behalf.",
                fontSize = 11.sp,
                color = Color(0xFF7A7194),
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGoogle) Color(0xFF4285F4) else Color.White,
                    contentColor = if (isGoogle) Color.White else Color.Black
                )
            ) {
                Text(
                    text = "Continue with $providerName",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = onDismiss) {
                Text(text = "Not now", color = SonexaTextMuted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun BrandChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1A2E))
            .border(1.dp, Color(0xFF3A3450), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ConsentRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SonexaPurpleLight,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SonexaTextWhite
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SonexaTextMuted
            )
        }
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF34D399),
            modifier = Modifier.size(18.dp)
        )
    }
}
