package com.SzpontCompany.check.data.social

data class Battle(
    val id: String = "",
    val title: String = "",
    val totalDays: Int = 7, // domyslnie 7, potem bedziemy przekazywac z invite
    val betAmount: Int = 0,
    val endDate: String? = null,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, SURRENDERED

    val participants: List<String> = emptyList(),

    // Player 1 (sender)
    val player1Id: String = "",
    val player1Name: String = "",
    val player1Hp: Int = 100,
    val player1Days: Int = 0,
    val player1LastLogDate: String? = null,

    //Player 2 ( receiver)
    val player2Id: String = "",
    val player2Name: String = "",
    val player2Hp: Int = 100,
    val player2Days: Int = 0,
    val player2LastLogDate: String? = null
)