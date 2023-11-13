package com.androidhell.diceweightcalculator.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.androidhell.diceweightcalculator.data.DBHelper
import com.androidhell.diceweightcalculator.ui.theme.DiceWeightCalculatorTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(navController: NavController) {

    DiceWeightCalculatorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "Settings")
                        },
                        navigationIcon = {
                            IconButton(onClick = { /* Handle navigation back or other action */ }) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
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
                            .padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(60.dp))
//                        ImportExport()
                        Donate()
                        //add danger zone text either here or in DeleteData function
                        DeleteData()
                    }
                }
            )
        }
    }
}

@Composable
fun ImportExport() {
    //add this after first release
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Donate() {

    var showDonateSheet by remember { mutableStateOf(false) }
    if (showDonateSheet) {
        DonateSheet() {
            showDonateSheet = false
        }
    }

    Column {
        Text(
            "Buy Me A Beer",
            style = MaterialTheme.typography.titleLarge
        )

        Button(
            onClick = {
                showDonateSheet = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Donate!")
        }
    }
}

@SuppressLint("QueryPermissionsNeeded", "RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateSheet(onDismiss: () -> Unit) {
    val modalBottomSheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = modalBottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "\uD83C\uDF7B Thank you for being awesome! ❤\uFE0F",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            val openUrlLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    val paypalUrl = "https://www.paypal.com/donate/?business=ZR758PD8QQ7TC&no_recurring=1&currency_code=USD"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paypalUrl))
                    openUrlLauncher.launch(intent)
                }) {
                    Text("PayPal")
                }
                Button(onClick = {
                    val venmoUrl = "https://venmo.com/rouxination"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(venmoUrl))
                    openUrlLauncher.launch(intent)
                }) {
                    Text("Venmo")
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun DeleteData() {
    val dbHelper = DBHelper(LocalContext.current)
    var showDiscardConfirmationDialog by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .padding(top = 32.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "DANGER ZONE",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.Red)
            )
        }
        Text(
            "Delete All Data",
            style = MaterialTheme.typography.titleLarge
        )

        Button(
            onClick = {
                showDiscardConfirmationDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Delete all Session data")
        }

        DeleteAllDataConfirmationDialog(
            showDialog = showDiscardConfirmationDialog,
            onConfirm = {
                dbHelper.deleteAllData()
                showDiscardConfirmationDialog = false
            },
            onCancel = {
                showDiscardConfirmationDialog = false
            }
        )
    }
}

@Composable
fun DeleteAllDataConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    if(showDialog) {
        AlertDialog(onDismissRequest = {
            onCancel()
        },
            title = {
                Text("Delete All Session Data")
            },
            text = {
                Text("Do you want to delete all session data(this cannot be undone)?")
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
                    Text("Delete")
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
fun SettingsScreenPreview() {
    DiceWeightCalculatorTheme {
        SettingsScreen(rememberNavController())
    }
}