package com.kingree.scanimal.view

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.kingree.scanimal.navigation.Screen
import com.kingree.scanimal.view.DashboardScreen
import com.kingree.scanimal.view.ProfileScreen
import com.kingree.scanimal.view.RegisterAnimalScreen
import com.kingree.scanimal.view.IdentifyAnimalScreen

@Composable
fun MainScreen(
    rootNavController: NavHostController

) {
    val bottomNavController = rememberNavController()
    val context = LocalContext.current

    val currentBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    BackHandler {
        (context as? Activity)?.finish()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Screen.Dashboard.route,
                    onClick = {
                        bottomNavController.navigate(Screen.Dashboard.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.Register.route,
                    onClick = {
                        bottomNavController.navigate(Screen.Register.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("Register") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.Identify.route,
                    onClick = {
                        bottomNavController.navigate(Screen.Identify.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                    label = { Text("Identify") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.Profile.route,
                    onClick = {
                        bottomNavController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->

        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen()
            }

            composable(Screen.Register.route) {
                RegisterAnimalScreen()
            }

            composable(Screen.Identify.route) {
                IdentifyAnimalScreen(
                    onRegisterNew = {
                        bottomNavController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(navController = rootNavController)
            }
        }
    }
}
