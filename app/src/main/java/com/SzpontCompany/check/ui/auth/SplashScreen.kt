package com.SzpontCompany.check.ui.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedSplashScreen(onSplashFinished: () -> Unit) {
    // Stany animacji
    val logoScale = remember { Animatable(0.4f) } // Zaczynamy od pomniejszonego logo
    val logoAlpha = remember { Animatable(0f) }   // Zaczynamy od niewidocznego
    val textAlpha = remember { Animatable(0f) }

    // Uruchomienie sekwencji animacji
    LaunchedEffect(key1 = true) {
        // 1. Krótka pauza na start (płynne przejęcie od systemu)
        delay(200)

        // 2. Wskok logo (animujemy skalę i przezroczystość jednocześnie)
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(durationMillis = 400))
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy, // Efekt delikatnej sprężyny
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // 3. Płynne pojawienie się napisów
        delay(300)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )

        // 4. Chwila przerwy i przejście do ekranu logowania
        delay(800)
        onSplashFinished()
    }

    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Kafel z Waszym IDEALNYM logo (używa tej samej funkcji co ekran logowania)
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(logoScale.value) // Podpięta animacja powiększania
                .alpha(logoAlpha.value) // Podpięta animacja przezroczystości
                .clip(RoundedCornerShape(28.dp))
                .background(accent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = getLogoForAccent(accent)),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pojawiający się tekst (dokładnie taki sam kształt i kolor jak poprzednio)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(textAlpha.value)
        ) {
            Text(
                text = "Check",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = ".",
                style = MaterialTheme.typography.headlineLarge,
                color = accent,
            )
        }
    }
}