package com.androidhell.diceweightcalculator.data

data class RollStatistics(
    val sessionName: String,
    val diceType: String,
    var mean: Double,
    var median: Double,
    var mode: Double,
    var oddRolls: Int,
    val evenRolls: Int
)