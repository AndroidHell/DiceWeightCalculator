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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.androidhell.diceweightcalculator.AppScaffold
import com.androidhell.diceweightcalculator.Screens
import com.androidhell.diceweightcalculator.data.DBHelper
import com.androidhell.diceweightcalculator.ui.theme.DiceWeightCalculatorTheme


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NewSessionScreen(
    navController: NavController,
    newSessionViewModel: NewSessionViewModel = viewModel()
) {
    var sessionName by remember { mutableStateOf("New Session") }
    var numberOfRolls by remember { mutableStateOf(20) }
    var showDialog by remember { mutableStateOf(false) }

    var defaultRolls by remember { mutableStateOf("20") }
    val rollsTextFieldFocusRequester = remember { FocusRequester() }

    val dbHelper = DBHelper(LocalContext.current)

    DiceWeightCalculatorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
//            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = "New Session") },
                        navigationIcon = {
                            IconButton(onClick = {
                                navController.popBackStack()
                            }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
                        Spacer(modifier = Modifier.height(50.dp))
                        OutlinedTextField(
                            value = sessionName,
                            onValueChange = { sessionName = it },
                            label = { Text("Session Name") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    rollsTextFieldFocusRequester.requestFocus()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )

                        OutlinedTextField(
                            value = defaultRolls,
                            onValueChange = { text ->
                                defaultRolls = text
                                val intValue = text.toIntOrNull()
                                if (intValue != null) {
                                    numberOfRolls = intValue
                                } else {
                                    numberOfRolls = 20
                                }
                            },
                            label = { Text("Number of Rolls") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                             }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .focusRequester(rollsTextFieldFocusRequester)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val diceTypes = listOf("D20", "D4", "D6", "D8", "D10", "Percent", "D12")
                        CheckboxGroup(
                            options = diceTypes,
                            selectedDice = newSessionViewModel.selectedDice,
                            onSelectedDiceChange = { newSessionViewModel.selectedDice = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SessionNameExistsDialog(
                            showDialog = showDialog,
                        onDismiss = {
                                showDialog = false
                            }
                        )

                        Button(
                            onClick = {
                                newSessionViewModel.sessionName = sessionName
                                newSessionViewModel.numberOfRolls = numberOfRolls

                                val sessionNameExists = dbHelper.sessionNameExists(sessionName)

                                if (sessionNameExists) {
                                    showDialog = true
                                } else {
                                    navController.navigate(Screens.Roll.route)
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Continue")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CheckboxGroup(
    options: List<String>,
    selectedDice: List<String>,
    onSelectedDiceChange: (List<String>) -> Unit
) {
    Text("Select Dice:")
    Column {
        // Create a row for D20 checkbox and label
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val d20 = "D20"
            Checkbox(
                checked = selectedDice.contains(d20),
                onCheckedChange = {
                    val updatedDice = selectedDice.toMutableList()
                    if (it) {
                        updatedDice.add(d20)
                    } else {
                        updatedDice.remove(d20)
                    }
                    onSelectedDiceChange(updatedDice)
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(d20)
        }

        Divider(modifier = Modifier.fillMaxWidth(), color = Color.LightGray, thickness = 1.dp,)

        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val chunkedOptions = options.filter { it != "D20" }.chunked(options.size / 2)

            chunkedOptions.forEachIndexed { index, columnOptions ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    columnOptions.forEach { dice ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = selectedDice.contains(dice),
                                onCheckedChange = {
                                    val updatedDice = selectedDice.toMutableList()
                                    if (it) {
                                        updatedDice.add(dice)
                                    } else {
                                        updatedDice.remove(dice)
                                    }
                                    onSelectedDiceChange(updatedDice)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dice)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionNameExistsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
       },
            title = {
                    Text("Session Name Already Exists")
            },
            text = {
                Text("A session with this name already exists in the database. Please use another name.")
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun NewSessionScreenPreview() {
    DiceWeightCalculatorTheme {
        NewSessionScreen(rememberNavController())
    }
}