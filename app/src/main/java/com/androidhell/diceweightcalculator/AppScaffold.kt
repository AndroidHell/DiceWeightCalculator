package com.androidhell.diceweightcalculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.androidhell.diceweightcalculator.screens.HomeScreen
import com.androidhell.diceweightcalculator.screens.SessionsScreen
import com.androidhell.diceweightcalculator.screens.SettingsScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.androidhell.diceweightcalculator.screens.NewSessionScreen
import com.androidhell.diceweightcalculator.screens.NewSessionViewModel
import com.androidhell.diceweightcalculator.screens.RollScreen
import com.androidhell.diceweightcalculator.screens.RollResultsScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    var navigationSelectedItem by remember { mutableStateOf(0) }
    val navController = rememberNavController()

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    var isFabPressed by remember { mutableStateOf(false) }
    val bottomBarState = rememberSaveable { (mutableStateOf(true)) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val newSessionViewModel: NewSessionViewModel = viewModel()

    when(navBackStackEntry?.destination?.route) {
        Screens.Home.route -> {
            bottomBarState.value = true
            isFabPressed = false
        }
        Screens.Sessions.route -> {
            bottomBarState.value = true
         }
        Screens.Settings.route -> {
             bottomBarState.value = true
        }
        else -> bottomBarState.value = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = bottomBarState.value,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                content = {
                    Box(
                        modifier = Modifier
                    ) {
                        NavigationBar {
                            BottomNavigationItem().bottomNavigationItems().forEachIndexed { index, navigationItem ->
                                NavigationBarItem(
                                    selected = index == navigationSelectedItem,
                                    label = {
                                        Text(navigationItem.label)
                                    },
                                    icon = {
                                        Icon(
                                            navigationItem.icon,
                                            contentDescription = navigationItem.label
                                        )
                                    },
                                    onClick = {
                                        navigationSelectedItem = index
                                        navController.navigate(navigationItem.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )

        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = currentDestination?.route == Screens.Home.route,
                enter = scaleIn(animationSpec = tween(300)),
                exit = scaleOut(animationSpec = tween(300))
            ) {
                FloatingActionButton(
                    onClick = {
                        isFabPressed = !isFabPressed
                        if (isFabPressed) {
                            navController.navigate(Screens.NewSession.route)
                        }

                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) {paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screens.Home.route,

            modifier = Modifier.padding(paddingValues = paddingValues)) {
            composable(Screens.Home.route) {
                HomeScreen(
                    navController
                )
            }
            composable(Screens.Sessions.route) {
                SessionsScreen(
                    navController
                )
            }
            composable(Screens.Settings.route) {
                SettingsScreen(
                    navController
                )
            }
            composable(Screens.NewSession.route) {
                NewSessionScreen(
                    navController, newSessionViewModel
                )
            }
            composable(Screens.Roll.route) {
                RollScreen(
                    navController, newSessionViewModel
                )
            }
            composable(Screens.RollsResult.route) {
                RollResultsScreen(
                    navController, newSessionViewModel
                )
            }

        }
    }
}

