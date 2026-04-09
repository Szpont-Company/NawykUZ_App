package com.SzpontCompany.check.ui.addhabit

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.R

@Composable
fun AddHabitSuccess(
    name: String, icon: String, color: Color,
    dailyReminder: Boolean, reminderTime: String,
    onFinishClick: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var animationTriggered by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "halo")
    val haloScale by infiniteTransition.animateFloat(1f, 1.6f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), "haloScale")
    val haloAlpha by infiniteTransition.animateFloat(0.5f, 0f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), "haloAlpha")

    val checkmarkOffsetY = remember { Animatable(-300f) }
    val checkmarkScale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        // Haptyka
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION") vibrator.vibrate(50)
            }
        } catch (e: Exception) { e.printStackTrace() }

        checkmarkOffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))

        try {
            MediaPlayer.create(context, R.raw.success_sound)?.apply {
                start()
                setOnCompletionListener { release() }
            }
        } catch (e: Exception) { e.printStackTrace() }

        checkmarkScale.animateTo(1.5f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        checkmarkScale.animateTo(1f, tween(400))

        animationTriggered = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                if (animationTriggered) {
                    Box(
                        modifier = Modifier.size(120.dp).scale(haloScale).alpha(haloAlpha)
                            .background(brush = Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0f))), shape = CircleShape)
                    )
                }

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = checkmarkOffsetY.value * density.density
                            scaleX = checkmarkScale.value
                            scaleY = checkmarkScale.value
                        }
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, "Sukces", tint = Color.White, modifier = Modifier.size(60.dp))
                }
            }

            AnimatedVisibility(
                visible = animationTriggered,
                enter = fadeIn(tween(400)) + slideInVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy)) { 50 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = stringResource(R.string.habit_success_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = name.ifEmpty { stringResource(R.string.habit_unnamed) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val reminderText = if (dailyReminder) {
                        stringResource(R.string.habit_success_reminder_on, reminderTime)
                    } else {
                        stringResource(R.string.habit_success_reminder_off)
                    }
                    Text(
                        text = reminderText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                                Text(icon, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name.ifEmpty { stringResource(R.string.habit_unnamed) },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = stringResource(R.string.habit_success_subtitle),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("0%", style = MaterialTheme.typography.titleMedium, color = color)
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.animation.AnimatedVisibility(visible = animationTriggered, enter = fadeIn(tween(300, delayMillis = 500))) {
                OutlinedButton(
                    onClick = onFinishClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(stringResource(R.string.habit_success_return), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}