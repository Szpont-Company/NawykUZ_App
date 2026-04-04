package com.SzpontCompany.check.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EmojiExplosionEffect(
    modifier: Modifier = Modifier,
    emoji: String,
    triggerId: Long = 0L
) {
    if (triggerId == 0L) return

    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val yOffset = remember { Animatable(150f) }
    val rotation = remember { Animatable(-15f) }

    LaunchedEffect(triggerId) {
        launch {
            alpha.animateTo(1f, tween(150))
            delay(500)
            alpha.animateTo(0f, tween(300))
        }

        launch {
            scale.animateTo(
                1.3f,
                spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow)
            )
            delay(300)
            scale.animateTo(0.5f, tween(300))
        }

        launch {
            yOffset.animateTo(
                -100f,
                tween(1000, easing = FastOutSlowInEasing)
            )
        }

        launch {
            rotation.animateTo(
                15f,
                tween(1000, easing = LinearOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (alpha.value > 0f) {
            Text(
                text = emoji,
                fontSize = 120.sp,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        this.alpha = alpha.value
                        translationY = yOffset.value
                        rotationZ = rotation.value
                    }
            )
        }
    }
}
