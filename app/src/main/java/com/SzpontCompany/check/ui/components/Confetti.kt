package com.SzpontCompany.check.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val id: Int,
    val x: Float, val y: Float,
    val vx: Float, val vy: Float,
    val color: Color,
    val size: Float,
    val life: Float
)

@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    val particles = remember { mutableStateListOf<Particle>() }
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val neonColors = listOf(
        Color(0xFF00FF00),
        Color(0xFFFFD700),
        Color(0xFF00FFFF),
        Color(0xFFFF1493),
        Color(0xFFFFFF00)
    )

    LaunchedEffect(time) {
        if (time < 0.05f && particles.isEmpty()) {
            repeat(150) { id ->
                val angle = Random.nextFloat() * 2 * Math.PI.toFloat()

                val speed = Random.nextFloat() * 15f + 8f

                particles.add(
                    Particle(
                        id = id,
                        x = 0.5f, y = 0.45f,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * (speed * 0.8f) - 6f,
                        color = neonColors.random(),
                        size = Random.nextFloat() * 12f + 8f,
                        life = Random.nextFloat() * 0.6f + 0.6f
                    )
                )
            }
        } else if (time > 0.95f) {
            particles.clear()
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            val normalizedTime = time / p.life
            if (normalizedTime <= 1f) {

                val newX = (p.x * width) + (p.vx * normalizedTime * 70f)
                val newY = (p.y * height) + (p.vy * normalizedTime * 70f) + (0.5f * 9.8f * normalizedTime * normalizedTime * 100f)

                drawCircle(
                    color = p.color.copy(alpha = 1f - (normalizedTime * 0.8f)),
                    radius = (p.size * (1f - normalizedTime * 0.4f)).dp.toPx(),
                    center = Offset(newX, newY)
                )
            }
        }
    }
}