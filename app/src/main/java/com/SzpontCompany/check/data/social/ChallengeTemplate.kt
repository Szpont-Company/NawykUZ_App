package com.SzpontCompany.check.data.social

import androidx.compose.ui.graphics.Color
import com.SzpontCompany.check.ui.theme.Amber
import com.SzpontCompany.check.ui.theme.Coral
import com.SzpontCompany.check.ui.theme.Mint
import com.SzpontCompany.check.ui.theme.Sky

data class ChallengeTemplate(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val color: Color
)

val PredefinedChallenges = listOf(
    ChallengeTemplate("1", "Królowie Kroków", "Kto zrobi więcej kroków w 3 dni?", "👟", Sky),
    ChallengeTemplate("2", "Wodny Pojedynek", "Pij minimum 2L wody przez 5 dni.", "💧", Mint),
    ChallengeTemplate("3", "Cukrowy Odwyk", "Zero słodyczy przez cały tydzień!", "🚫", Coral),
    ChallengeTemplate("4", "Ranny Ptaszek", "Wstawanie przed 7:00 przez 3 dni.", "🌅", Amber)
)