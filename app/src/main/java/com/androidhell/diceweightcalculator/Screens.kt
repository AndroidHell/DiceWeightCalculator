package com.androidhell.diceweightcalculator

sealed class Screens(val route : String) {
    object Home : Screens("home_screen")
    object Sessions : Screens("sessions_screen")
    object Settings : Screens("settings_screen")
    object NewSession : Screens("new_session_screen")
    object Roll : Screens("roll_screen")
    object RollsResult : Screens("rolls_result_screen")
}