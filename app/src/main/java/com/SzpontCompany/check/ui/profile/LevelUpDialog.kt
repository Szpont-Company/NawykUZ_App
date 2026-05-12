package com.SzpontCompany.check.ui.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.components.ConfettiEffect

val LevelUpGold = Color(0xFFFFC107)

@Composable
fun LevelUpDialog(
    newLevel: Int,
    onDismiss: () -> Unit
) {
    var isEntered by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale_anim"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    LaunchedEffect(Unit) {
        isEntered = true
    }

    Dialog(
        onDismissRequest = { /* Wyłączamy anulowanie poza oknem */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            ConfettiEffect()

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, LevelUpGold, RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .padding(32.dp)
                    .alpha(alpha)
                    .scale(scale),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.level_up_dialog_title).uppercase(),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(LevelUpGold.copy(alpha = glowAlpha))
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "$newLevel", fontSize = 54.sp, fontWeight = FontWeight.ExtraBold, color = LevelUpGold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.level_up_dialog_message, newLevel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LevelUpGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = stringResource(R.string.level_up_dialog_button), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}