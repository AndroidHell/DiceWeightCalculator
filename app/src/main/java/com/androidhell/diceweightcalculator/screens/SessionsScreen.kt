package com.androidhell.diceweightcalculator.screens

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.androidhell.diceweightcalculator.Screens
import com.androidhell.diceweightcalculator.data.DBHelper
import com.androidhell.diceweightcalculator.data.RollStatistics
import com.androidhell.diceweightcalculator.ui.theme.DiceWeightCalculatorTheme


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SessionsScreen(navController: NavController) {

    val dbHelper = DBHelper(LocalContext.current)
    val sessionListWithStats = dbHelper.getAllSessionsWithStats()

    DiceWeightCalculatorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { // This is a composable lambda
                            Text(text = "Sessions")
                        },
                        navigationIcon = {
                            IconButton(onClick = { /* Handle navigation back or other action */ }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Sessions")
                            }
                        },
                        actions = {
                            // Add any additional actions/icons here if needed
                        }
                    )
                },
                content = {
                    Column(){
                        //this is here because I can't get the spacer to work with this list
                        //I am a trash programmer
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(White)
                        )
                        if (sessionListWithStats.isNotEmpty()){
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {

                                items(sessionListWithStats) { (sessionName, stats) ->
                                    SessionCard(sessionName, stats, navController)
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "No Data to Display",
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

//fade in new text when expanding
//long press to delete session and reload list
//add empty data text if no data is in sessions screen
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionCard(sessionName: String, stats: List<RollStatistics>, navController: NavController) {
    val haptics = LocalHapticFeedback.current

    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    val dbHelper = DBHelper(LocalContext.current)

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
 //            .clickable { expanded = !expanded }
            .combinedClickable(
                onClick = { expanded = !expanded },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDeleteConfirmationDialog = true
                }
            )
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = if (expanded) 8.dp else 0.dp
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "Session: $sessionName",
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(rotationState)
                        .padding(8.dp)
                )
            }

            if (expanded) {
                Row {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Overall Mean: ${String.format("%.2f", stats.map { it.mean }.average())}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "Overall Median: ${String.format("%.2f", stats.map { it.median }.average())}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "Overall Mode: ${String.format("%.2f", stats.map { it.mode }.average())}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Overall Odd Rolls: ${stats.map { it.oddRolls }.sum()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Overall Even Rolls: ${stats.map { it.evenRolls }.sum()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Divider(modifier = Modifier.fillMaxWidth(), color = Color.LightGray, thickness = 1.dp,)

                stats.groupBy { it.diceType }
                    .toList()
                    .sortedBy { (diceType, _) ->
                        when (diceType) {
                            "D20" -> 1
                            "D4" -> 2
                            "D6" -> 3
                            "D8" -> 4
                            "D10" -> 5
                            "Percent" -> 6
                            "D12" -> 7
                            else -> Int.MAX_VALUE
                        }
                    }
                    .forEach { (diceType, statsList) ->
                        Text(
                            text = "$diceType",
                            style = MaterialTheme.typography.bodySmall
                        )
                        statsList.forEach { stat ->
                            Text(
                                text = "Mean: ${stat.mean}, Median: ${stat.median}, Mode: ${stat.mode}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Even Rolls: ${stat.evenRolls}, Odd Rolls: ${stat.oddRolls}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
            } else {
                Row {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Overall Mean: ${String.format("%.2f", stats.map { it.mean }.average())}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "Overall Median: ${String.format("%.2f", stats.map { it.median }.average())}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "Overall Mode: ${String.format("%.2f", stats.map { it.mode }.average())}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Overall Odd Rolls: ${stats.map { it.oddRolls }.sum()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Overall Even Rolls: ${stats.map { it.evenRolls }.sum()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    DeleteConfirmationDialog(
        showDialog = showDeleteConfirmationDialog,
        sessionName = sessionName,
        onConfirm = {
            dbHelper.deleteSessionRollsData(sessionName)
            dbHelper.deleteSessionStatsData(sessionName)
            navController.navigate(Screens.Sessions.route)
            showDeleteConfirmationDialog = false
        },
        onCancel = {
            showDeleteConfirmationDialog = false
        }
    )

}

@Composable
fun DeleteConfirmationDialog(
    sessionName: String,
    showDialog: Boolean,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
) {
    if(showDialog) {
        AlertDialog(onDismissRequest = {
            onCancel()
        },
            title = {
                Text("Delete Session Data")
            },
            text = {
                Text("Do you want to delete session: $sessionName")
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        onConfirm(sessionName)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Yes, Delete")
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

@Preview(showBackground = true)
@Composable
fun SessionsScreenPreview() {
    DiceWeightCalculatorTheme {
        SessionsScreen(rememberNavController())
    }
}