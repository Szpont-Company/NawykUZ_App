package com.SzpontCompany.check.data.social

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.theme.Amber
import com.SzpontCompany.check.ui.theme.Coral
import com.SzpontCompany.check.ui.theme.Mint
import com.SzpontCompany.check.ui.theme.Sky

data class ChallengeTemplate(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    val emoji: String,
    val color: Color
)

val PredefinedChallenges = listOf(
    ChallengeTemplate("1", R.string.challenge_template_steps_title, R.string.challenge_template_steps_desc, "👟", Sky),
    ChallengeTemplate("2", R.string.challenge_template_water_title, R.string.challenge_template_water_desc, "💧", Mint),
    ChallengeTemplate("3", R.string.challenge_template_sugar_title, R.string.challenge_template_sugar_desc, "🚫", Coral),
    ChallengeTemplate("4", R.string.challenge_template_early_title, R.string.challenge_template_early_desc, "🌅", Amber)
)