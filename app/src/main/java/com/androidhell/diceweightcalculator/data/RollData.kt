package com.androidhell.diceweightcalculator.data

data class RollData(
    val sessionName: String,
    val diceIndex: Int,
    val diceType: String?,
    val rollNumber: Int,
    var rollValue: Int
)


