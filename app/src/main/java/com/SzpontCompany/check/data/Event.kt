package com.SzpontCompany.check.data

import androidx.compose.ui.graphics.Color

data class Event(
    val title: String,
    val subtitle: String,
    val isSubtitleColored: Boolean = false,
    val badgeText: String,
    val themeColor: Color,
    val progress: Float? = null,
    val progressText: String? = null,
    val timeText: String? = null,
    val rewardHighlight: String? = null,
    val description: String? = null,
    val participantsCount: String? = null,
    val buttonText: String? = null
)
