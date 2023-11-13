package com.androidhell.diceweightcalculator.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.androidhell.diceweightcalculator.data.DBHelper
import com.androidhell.diceweightcalculator.data.RollData
import com.androidhell.diceweightcalculator.ui.theme.DiceWeightCalculatorTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.androidhell.diceweightcalculator.Screens


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RollScreen(
    navController: NavController,
    viewModel: NewSessionViewModel = viewModel()
) {
    val sessionName = viewModel.sessionName ?: "New Session"
    val numberOfRolls = viewModel.numberOfRolls
    val selectedDice = viewModel.selectedDice.toList()
    val dbHelper = DBHelper(LocalContext.current)

    val expectedOrder = listOf("D20", "D4", "D6", "D8", "D10", "Percent", "D12" )
    val reorderedSelectedDice = reorderAndExcludeMissing(selectedDice, expectedOrder)

    var currentDiceIndex by remember { mutableStateOf(0) }
    var rollsDone by remember { mutableStateOf(0) }
    var rollsRemaining by remember { mutableStateOf(0) }
    var rollCountText by remember { mutableStateOf("") }
    var totalRolls by remember { mutableStateOf(0) }

    var showDiscardConfirmationDialog by remember { mutableStateOf(false) }

    val currentDice = reorderedSelectedDice.getOrNull(currentDiceIndex)
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DiceWeightCalculatorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = sessionName) },
                        navigationIcon = {
                            IconButton(onClick = {
                                showDiscardConfirmationDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            // Add any additional actions/icons here if needed
                        }
                    )

                    DiscardConfirmationDialog(
                        showDialog = showDiscardConfirmationDialog,
                        onConfirm = {
                            dbHelper.deleteSessionRollsData(sessionName)
                            navController.popBackStack()
                        },
                        onCancel = {
                            showDiscardConfirmationDialog = false
                        }
                    )

                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    Spacer(modifier = Modifier.height(50.dp))

                    Log.d("MyApp", "Selected Dice: ${reorderedSelectedDice.joinToString()}")
                    if (currentDiceIndex < reorderedSelectedDice.size) {
                        Text(
                            "Roll for: ${reorderedSelectedDice[currentDiceIndex]}",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                        Text(
                            "Roll ${rollsDone + 1}/$numberOfRolls",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TextField(
                            value = rollCountText,
                            onValueChange = {
                                rollCountText =it
                            },
                            label = { Text("Enter Roll Count") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {

                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    if (rollsDone > 0) {
                                        rollsDone--
                                    } else if (currentDiceIndex > 0) {
                                        currentDiceIndex--
                                        rollsDone = numberOfRolls - 1
                                    } else {
                                        // Handle the case when there are no previous rolls to go back to
                                    }
                                    val previousRoll = dbHelper.getPreviousRoll(sessionName, currentDiceIndex, currentDice, rollsDone + 2)

                                    if (previousRoll != null) {
                                        rollCountText = previousRoll.rollValue.toString()
                                    } else {
                                        rollCountText = ""
                                    }
                                }
                            ) {
                                Text("Prev")
                            }

                            val isPercentDice = currentDice == "Percent"

                            // Validate the roll count based on the type of dice
                            val isValidRollCount = when {
                                isPercentDice -> {
                                    // For percent dice, validate that the roll count is a multiple of 10
                                    val rollCount = rollCountText.toIntOrNull()
                                    val isValid = rollCount != null && rollCount in 10..100 && rollCount % 10 == 0

                                    if(isValid) {
                                        errorMessage = null
                                    }
                                    isValid
                                }
                                currentDice != null -> {
                                    // For other dice, validate that the roll count is between 1 and the number of faces of the dice
                                    val diceFaces = currentDice.substring(1).toIntOrNull()
                                    val rollCount = rollCountText.toIntOrNull()
                                    val isValid = diceFaces != null && rollCount != null && rollCount in 1..diceFaces

                                    if(isValid) {
                                        errorMessage = null
                                    }
                                    isValid
                                }
                                else -> false
                            }

                            Button(
                                onClick = {

                                    if (isValidRollCount) {
                                        val rollCount = rollCountText.toInt()
                                        val rollValue = rollCountText.toInt()
                                        val rollData = RollData(
                                            sessionName = sessionName,
                                            diceIndex = currentDiceIndex,
                                            diceType = currentDice,
                                            rollNumber = rollsDone + 1,
                                            rollValue = rollValue
                                        )
                                        dbHelper.insertRoll(rollData)

                                        Log.d("MyApp", "New Roll: $rollData")
                                        dbHelper.logAllRolls()

                                        rollsRemaining = rollCount - 1
                                        rollsDone++

                                        if (rollsDone >= numberOfRolls) {
                                            currentDiceIndex++
                                            rollsDone = 0
                                        }

                                        if (currentDiceIndex < reorderedSelectedDice.size) {
                                            rollsRemaining = totalRolls
                                        }
                                        val nextRoll = dbHelper.getNextRoll(sessionName, currentDiceIndex, currentDice, rollsDone)

                                        if (nextRoll != null) {
                                            rollCountText = nextRoll.rollValue.toString()
                                        } else {
                                            rollCountText = ""
                                        }
                                    } else {
                                        if (isPercentDice) {
                                            errorMessage = "Invalid roll count (must be a multiple of 10, between 10 and 100)"
                                            rollCountText = ""
                                        } else {
                                            val diceFaces = currentDice?.substring(1)
                                            errorMessage = "Invalid roll count (1 to $diceFaces)"
                                            rollCountText = ""
                                        }
                                    }
                                },
                            ) {
                                Text("Next")
                            }
                        }
                    } else {
                        dbHelper.calculateAndInsertStatistics(sessionName)
                        navController.navigate(Screens.RollsResult.route)
                    }
                    errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = Color.Red,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DiscardConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    if(showDialog) {
        AlertDialog(onDismissRequest = {
           onCancel()
        },
        title = {
            Text("Discard Session Data")
        },
        text = {
            Text("Do you want to discard the data entered for this session?")
        },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    onConfirm()
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
               Text("Yes, Discard")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onCancel()
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Cancel")
            }
        })
    }
}

fun reorderAndExcludeMissing(selectedDice: List<String>, expectedOrder: List<String>): List<String> {
    val reorderedSelectedDice = mutableListOf<String>()

    for (item in expectedOrder) {
        if (item in selectedDice) {
            reorderedSelectedDice.add(item)
        }
    }

    return reorderedSelectedDice
}

@Preview(showBackground = true)
@Composable
fun RollScreenPreview() {
    DiceWeightCalculatorTheme {
        RollScreen(rememberNavController())
    }
}