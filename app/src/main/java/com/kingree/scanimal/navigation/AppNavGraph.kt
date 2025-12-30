package com.kingree.scanimal.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kingree.scanimal.view.LoginScreen
import com.kingree.scanimal.view.ProfileScreen
import com.kingree.scanimal.view.SignUpScreen
import com.kingree.scanimal.view.MainScreen

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val isLoggedIn = prefs.getBoolean("is_logged_in", false)

    val startDestination =
        if (isLoggedIn) Screen.Main.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ------------------ LOGIN ------------------
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    prefs.edit().putBoolean("is_logged_in", true).apply()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        // ------------------ SIGN UP ------------------
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // ------------------ MAIN (Bottom Navigation) ------------------
        composable(Screen.Main.route) {
            MainScreen(
                rootNavController = navController // ✅ FIXED
            )
        }
    }
}
