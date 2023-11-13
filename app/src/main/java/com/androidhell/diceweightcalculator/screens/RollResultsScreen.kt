package com.androidhell.diceweightcalculator.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.androidhell.diceweightcalculator.Screens
import com.androidhell.diceweightcalculator.data.DBHelper
import com.androidhell.diceweightcalculator.ui.theme.DiceWeightCalculatorTheme

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RollResultsScreen(
    navController: NavController,
    viewModel: NewSessionViewModel = viewModel()
) {

    val sessionName = viewModel.sessionName ?: "New Session"
    val dbHelper = DBHelper(LocalContext.current)
    val allRollStatistics = remember { dbHelper.getRollStats(sessionName) }

    val diceTypesOrder = listOf("D4", "D6", "D8", "D10", "D12", "Percent", "D20")

    val rollStatisticsList = remember {
        diceTypesOrder
            .map { diceType ->
                allRollStatistics.find { it.diceType == diceType }
            }
            .filterNotNull()
    }

    DiceWeightCalculatorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "Roll Results")
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                navController.navigate(Screens.Home.route)
                            }) {
                                Icon(imageVector = Icons.Default.Done, contentDescription = "Roll Results")
                            }
                        },
                        actions = {
                            // Add any additional actions/icons here if needed
                        }
                    )
                },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(50.dp))
                        Text(
                            text = "Session: $sessionName",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

//                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            for (rollStats in rollStatisticsList) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Dice: ${rollStats.diceType}",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Mean: ${rollStats.mean}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Median: ${rollStats.median}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Mode: ${rollStats.mode}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Even Rolls: ${rollStats.evenRolls}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Odd Rolls: ${rollStats.oddRolls}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }

                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                navController.navigate(Screens.Home.route)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done")
                        }

                    }

                }
            )
        }
    }
}

//preview doesn't work for some reason
@Preview(showBackground = true)
@Composable
fun RollResultsScreenPreview() {
    DiceWeightCalculatorTheme {
        RollResultsScreen(rememberNavController())
    }
}