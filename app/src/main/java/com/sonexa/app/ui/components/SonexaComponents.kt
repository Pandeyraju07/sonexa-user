package com.sonexa.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.ui.theme.*

@Composable
fun SonexaHeaderLogo(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo Graphic Mark (Stylized 'S' + Soundwave Equalizer)
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                // Glow circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x709825DD), Color.Transparent),
                        center = Offset(width / 2, height / 2),
                        radius = width * 0.8f
                    )
                )

                // Sound Wave bars inside 'S' curve shape
                val purpleGrad = Brush.verticalGradient(
                    colors = listOf(Color(0xFFB062FF), Color(0xFFE534B2))
                )
                
                // Sound Bars
                val barWidth = 3.dp.toPx()
                val barGap = 4.dp.toPx()
                val heights = listOf(0.3f, 0.55f, 0.85f, 0.6f, 0.4f)
                val startX = width * 0.25f
                
                heights.forEachIndexed { index, hRatio ->
                    val x = startX + index * (barWidth + barGap)
                    val barH = height * 0.4f * hRatio
                    val y = height * 0.5f - barH / 2
                    drawLine(
                        brush = purpleGrad,
                        start = Offset(x, y),
                        end = Offset(x, y + barH),
                        strokeWidth = barWidth,
                        cap = StrokeCap.Round
                    )
                }

                // S-Curve Outline
                val path = Path().apply {
                    moveTo(width * 0.72f, height * 0.22f)
                    cubicTo(
                        width * 0.4f, height * 0.12f,
                        width * 0.25f, height * 0.35f,
                        width * 0.45f, height * 0.5f
                    )
                    cubicTo(
                        width * 0.75f, height * 0.65f,
                        width * 0.6f, height * 0.88f,
                        width * 0.22f, height * 0.78f
                    )
                }
                
                drawPath(
                    path = path,
                    brush = purpleGrad,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Brand Name Text
        Text(
            text = "SONEXA",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SonexaTextWhite,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Tagline Text
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Feel the ",
                fontSize = 12.sp,
                color = SonexaTextMuted
            )
            Text(
                text = "Music",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SonexaPurpleLight
            )
            Text(
                text = ". Live the ",
                fontSize = 12.sp,
                color = SonexaTextMuted
            )
            Text(
                text = "Vibe",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SonexaMagenta
            )
            Text(
                text = ".",
                fontSize = 12.sp,
                color = SonexaTextMuted
            )
        }
    }
}

@Composable
fun LoginHeroArtwork(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Deep background glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x609A25E2), Color(0x205935E5), Color.Transparent),
                    center = Offset(w / 2, h / 2),
                    radius = w * 0.45f
                )
            )

            // Equalizer sound waves on left and right
            val waveColorLeft = Color(0x90B062FF)
            val waveColorRight = Color(0x90E534B2)
            
            val barCount = 14
            val barW = 3.dp.toPx()
            val spacing = 5.dp.toPx()

            // Left Spectrum
            for (i in 0 until barCount) {
                val x = (w * 0.18f) - (barCount - i) * spacing
                val bh = (Math.sin(i * 0.6) * 0.4 + 0.5).toFloat() * (h * 0.45f)
                drawLine(
                    color = waveColorLeft,
                    start = Offset(x, h * 0.5f - bh / 2),
                    end = Offset(x, h * 0.5f + bh / 2),
                    strokeWidth = barW,
                    cap = StrokeCap.Round
                )
            }

            // Right Spectrum
            for (i in 0 until barCount) {
                val x = (w * 0.82f) + i * spacing
                val bh = (Math.cos(i * 0.6) * 0.4 + 0.5).toFloat() * (h * 0.45f)
                drawLine(
                    color = waveColorRight,
                    start = Offset(x, h * 0.5f - bh / 2),
                    end = Offset(x, h * 0.5f + bh / 2),
                    strokeWidth = barW,
                    cap = StrokeCap.Round
                )
            }

            // Glowing Headphones Silhouette Visual
            val center = Offset(w / 2, h * 0.48f)
            val radius = 52.dp.toPx()

            // Headband Arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(Color(0xFF5935E5), Color(0xFFE534B2), Color(0xFF5935E5)),
                    center = center
                ),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            // Left Ear Cup
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFB062FF), Color(0xFF5935E5))
                ),
                topLeft = Offset(center.x - radius - 10.dp.toPx(), center.y - 12.dp.toPx()),
                size = Size(18.dp.toPx(), 36.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(9.dp.toPx())
            )

            // Right Ear Cup
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE534B2), Color(0xFF9825DD))
                ),
                topLeft = Offset(center.x + radius - 8.dp.toPx(), center.y - 12.dp.toPx()),
                size = Size(18.dp.toPx(), 36.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(9.dp.toPx())
            )

            // Ambient particles / stars
            drawCircle(Color.White.copy(alpha = 0.8f), 2.dp.toPx(), Offset(w * 0.25f, h * 0.25f))
            drawCircle(Color.White.copy(alpha = 0.6f), 1.5.dp.toPx(), Offset(w * 0.75f, h * 0.3f))
            drawCircle(Color(0xFFFF52C4).copy(alpha = 0.7f), 2.5.dp.toPx(), Offset(w * 0.3f, h * 0.75f))
            drawCircle(Color(0xFFB062FF).copy(alpha = 0.7f), 2.dp.toPx(), Offset(w * 0.7f, h * 0.7f))
        }
    }
}

@Composable
fun CreateAccountHeroArtwork(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Center glow towards right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x609825DD), Color(0x155935E5), Color.Transparent),
                    center = Offset(w * 0.75f, h * 0.5f),
                    radius = h * 0.7f
                )
            )

            // Glowing Outer Ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(Color(0xFFE534B2), Color(0xFF5935E5), Color(0xFFE534B2)),
                    center = Offset(w * 0.75f, h * 0.45f)
                ),
                radius = 48.dp.toPx(),
                center = Offset(w * 0.75f, h * 0.45f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3D Headphones Graphic
            val center = Offset(w * 0.75f, h * 0.45f)
            val hpRadius = 36.dp.toPx()

            // Headband
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFB062FF), Color(0xFFE534B2))
                ),
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - hpRadius, center.y - hpRadius),
                size = Size(hpRadius * 2, hpRadius * 2)
            )

            // Left Cup
            drawRoundRect(
                color = Color(0xFF6B3CE9),
                topLeft = Offset(center.x - hpRadius - 8.dp.toPx(), center.y - 8.dp.toPx()),
                size = Size(14.dp.toPx(), 28.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.dp.toPx())
            )

            // Right Cup
            drawRoundRect(
                color = Color(0xFFE534B2),
                topLeft = Offset(center.x + hpRadius - 6.dp.toPx(), center.y - 8.dp.toPx()),
                size = Size(14.dp.toPx(), 28.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.dp.toPx())
            )

            // Floating Music Notes
            drawCircle(Color(0xFFFF52C4), 3.dp.toPx(), Offset(center.x - 55.dp.toPx(), center.y - 30.dp.toPx()))
            drawCircle(Color(0xFFB062FF), 2.dp.toPx(), Offset(center.x + 50.dp.toPx(), center.y - 40.dp.toPx()))
            drawCircle(Color.White, 2.5.dp.toPx(), Offset(center.x + 40.dp.toPx(), center.y + 35.dp.toPx()))
        }
    }
}

@Composable
fun SonexaInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var passwordVisible = false
    if (isPassword) {
        val (pVis, setPVis) = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        passwordVisible = pVis
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholderText, color = SonexaTextSubtle, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = SonexaTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = { setPVis(!pVis) }) {
                    Icon(
                        imageVector = if (pVis) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility",
                        tint = SonexaTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (pVis) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SonexaInputBg,
                unfocusedContainerColor = SonexaInputBg,
                focusedBorderColor = SonexaPurpleLight,
                unfocusedBorderColor = SonexaInputBorder,
                focusedTextColor = SonexaTextWhite,
                unfocusedTextColor = SonexaTextWhite,
                cursorColor = SonexaPurpleLight
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
        )
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholderText, color = SonexaTextSubtle, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = SonexaTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SonexaInputBg,
                unfocusedContainerColor = SonexaInputBg,
                focusedBorderColor = SonexaPurpleLight,
                unfocusedBorderColor = SonexaInputBorder,
                focusedTextColor = SonexaTextWhite,
                unfocusedTextColor = SonexaTextWhite,
                cursorColor = SonexaPurpleLight
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
        )
    }
}

@Composable
fun SonexaGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SonexaGradientBrush)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SonexaOrDivider(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = SonexaCardBorder,
            thickness = 1.dp
        )
        Text(
            text = "or continue with",
            color = SonexaTextSubtle,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = SonexaCardBorder,
            thickness = 1.dp
        )
    }
}

@Composable
fun SonexaSocialButton(
    text: String,
    isGoogle: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SonexaInputBg)
            .border(1.dp, SonexaInputBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isGoogle) {
                // Google logo representation
                Text(
                    text = "G",
                    color = Color(0xFF4285F4),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            } else {
                // Apple logo representation
                Text(
                    text = "",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = SonexaTextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SonexaCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) SonexaPurplePrimary else SonexaInputBg)
                .border(
                    1.dp,
                    if (checked) SonexaPurplePrimary else SonexaInputBorder,
                    RoundedCornerShape(6.dp)
                )
                .clickable { onCheckedChange(!checked) },
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        
        val annotatedText = buildAnnotatedString {
            append("I agree to the ")
            withStyle(style = SpanStyle(color = SonexaPurpleLight, fontWeight = FontWeight.SemiBold)) {
                append("Terms of Service")
            }
            append(" and ")
            withStyle(style = SpanStyle(color = SonexaPurpleLight, fontWeight = FontWeight.SemiBold)) {
                append("Privacy Policy")
            }
        }
        
        Text(
            text = annotatedText,
            color = SonexaTextMuted,
            fontSize = 12.sp
        )
    }
}

/** Industry-standard OTP resend: faded + disabled until cooldown hits 0. */
@Composable
fun OtpResendRow(
    secondsRemaining: Int,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
    prompt: String = "Didn't receive code?"
) {
    val canResend = secondsRemaining <= 0
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = prompt,
            fontSize = 12.sp,
            color = SonexaTextMuted
        )
        Text(
            text = if (canResend) {
                "Resend OTP"
            } else {
                val mins = secondsRemaining / 60
                val secs = secondsRemaining % 60
                "Resend in %d:%02d".format(mins, secs)
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (canResend) SonexaPurpleLight else SonexaTextSubtle,
            modifier = Modifier
                .alpha(if (canResend) 1f else 0.45f)
                .then(
                    if (canResend) {
                        Modifier.clickable(onClick = onResend)
                    } else {
                        Modifier
                    }
                )
        )
    }
}
