package com.androidhell.diceweightcalculator.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.androidhell.diceweightcalculator.data.DBHelper
import com.androidhell.diceweightcalculator.data.RollStatistics
import com.androidhell.diceweightcalculator.ui.theme.DiceWeightCalculatorTheme

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController) {

    val dbHelper = DBHelper(LocalContext.current)
    val diceTypeStatistics = dbHelper.getGlobalRollStatistics()

    DiceWeightCalculatorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "Home")
                        },
                        navigationIcon = {
                            IconButton(onClick = { /* Handle navigation back or other action */ }) {
                                Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
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
                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(50.dp))
                        Text(
                            "Global Statistics",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if (diceTypeStatistics.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                for (diceTypeStat in diceTypeStatistics) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Text("Dice Type: ${diceTypeStat.diceType}")
                                    }
                                    Row {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("Mean: ${String.format("%.2f", diceTypeStat.mean)}")
                                            Text("Median: ${String.format("%.2f", diceTypeStat.median)}")
                                            Text("Mode: ${String.format("%.2f", diceTypeStat.mode)}")
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ){
                                            Text("Total Rolls: ${diceTypeStat.evenRolls + diceTypeStat.oddRolls}")
                                            Text("Even Rolls: ${diceTypeStat.evenRolls}")
                                            Text("Odd Rolls: ${diceTypeStat.oddRolls}")
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Divider(modifier = Modifier.fillMaxWidth(), color = Color.LightGray, thickness = 1.dp)
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "No Global Data to Display",
                                    style = MaterialTheme.typography.titleLarge.copy(color = Color.LightGray),
                                    modifier = Modifier.padding(vertical = 20.dp)
                                )
                                Text(
                                    "Click + to start a new session",
                                    style = MaterialTheme.typography.titleLarge.copy(color = Color.LightGray),
                                    modifier = Modifier.padding(vertical = 20.dp)
                                )
                            }
                        }

                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    DiceWeightCalculatorTheme {
        HomeScreen(rememberNavController())
    }
}