package com.androidhell.diceweightcalculator.screens


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class NewSessionViewModel : ViewModel() {
    var sessionName: String? = ""
    var numberOfRolls by mutableIntStateOf(20)
    var selectedDice by mutableStateOf(
        listOf("D20")
    )
//    this will load all dice checked, the above only loads D20
//    var selectedDice by mutableStateOf(
//        listOf("D20", "D4", "D6", "D8", "D10", "Percent", "D12")
//    )
}
