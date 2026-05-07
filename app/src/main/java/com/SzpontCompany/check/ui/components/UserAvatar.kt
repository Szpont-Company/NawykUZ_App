package com.SzpontCompany.check.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.ui.theme.getColorByName

@Composable
fun UserAvatar(
    avatarEmoji: String,
    initials: String,
    bgColor: String = "Mint",
    size: Dp = 40.dp,
    emojiSize: Float = 24f,
    initialsSize: Float = 15f
) {
    val backgroundColor = getColorByName(bgColor)
    Box(
        modifier = Modifier
            .size(size)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (avatarEmoji.isNotEmpty() && avatarEmoji != "👤") {
            Text(text = avatarEmoji, fontSize = emojiSize.sp)
        } else {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = initialsSize.sp
            )
        }
    }
}

