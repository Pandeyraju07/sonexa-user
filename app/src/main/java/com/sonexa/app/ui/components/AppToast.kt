package com.sonexa.app.ui.components

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ToastType(
    val accentColor: Color,
    val defaultTitle: String,
    val icon: ImageVector
) {
    SUCCESS(Color(0xFF10B981), "Update Success", Icons.Default.Check),
    ERROR(Color(0xFFEF4444), "Action Failed", Icons.Default.Close),
    WARNING(Color(0xFFF59E0B), "Notice", Icons.Default.Warning),
    INFO(Color(0xFF0284C7), "Information", Icons.Default.Info)
}

data class ToastData(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val title: String? = null,
    val type: ToastType = ToastType.SUCCESS,
    val durationMs: Long = 3000L
)

object ToastManager {
    private val scope = MainScope()
    private val _toastState = MutableStateFlow<ToastData?>(null)
    val toastState: StateFlow<ToastData?> = _toastState.asStateFlow()
    private var dismissJob: Job? = null

    fun show(
        message: String,
        type: ToastType = ToastType.SUCCESS,
        title: String? = null,
        durationMs: Long = 3000L
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post {
                show(message, type, title, durationMs)
            }
            return
        }

        dismissJob?.cancel()
        val data = ToastData(
            id = System.currentTimeMillis(),
            message = message,
            title = title,
            type = type,
            durationMs = durationMs
        )
        _toastState.value = data

        dismissJob = scope.launch {
            delay(durationMs)
            if (_toastState.value?.id == data.id) {
                _toastState.value = null
            }
        }
    }

    fun showSuccess(message: String, title: String? = "Update Success") {
        show(message = message, type = ToastType.SUCCESS, title = title)
    }

    fun showError(message: String, title: String? = "Action Failed") {
        show(message = message, type = ToastType.ERROR, title = title)
    }

    fun showWarning(message: String, title: String? = "Warning") {
        show(message = message, type = ToastType.WARNING, title = title)
    }

    fun showInfo(message: String, title: String? = "Information") {
        show(message = message, type = ToastType.INFO, title = title)
    }

    fun dismiss() {
        dismissJob?.cancel()
        _toastState.value = null
    }
}

fun Context.showAppToast(
    message: String,
    type: ToastType = ToastType.SUCCESS,
    title: String? = null
) {
    ToastManager.show(message = message, type = type, title = title)
}

@Composable
fun AppToastHost(
    modifier: Modifier = Modifier
) {
    val toast by ToastManager.toastState.collectAsState()
    val dark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 10.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = toast != null,
            enter = slideInVertically(
                initialOffsetY = { -it - 40 },
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it - 40 }
            ) + fadeOut()
        ) {
            toast?.let { currentToast ->
                AppToastItem(
                    toast = currentToast,
                    dark = dark,
                    onDismiss = { ToastManager.dismiss() }
                )
            }
        }
    }
}

@Composable
fun AppToastItem(
    toast: ToastData,
    dark: Boolean = isSystemInDarkTheme(),
    onDismiss: () -> Unit
) {
    val cardBg = if (dark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (dark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (dark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        shadowElevation = if (dark) 2.dp else 6.dp,
        modifier = Modifier
            .widthIn(min = 260.dp, max = 400.dp)
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onDismiss)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(toast.type.accentColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(toast.type.accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = toast.type.icon,
                        contentDescription = null,
                        tint = toast.type.accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val displayTitle = toast.title ?: toast.type.defaultTitle
                    val isSingleLine = toast.title == null && !toast.message.contains("\n") && toast.message.length <= 35

                    if (isSingleLine) {
                        Text(
                            text = toast.message,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = displayTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = toast.message,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(15.dp)
                        .clickable(onClick = onDismiss)
                )
            }
        }
    }
}
