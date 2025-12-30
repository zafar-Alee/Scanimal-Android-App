package com.kingree.scanimal.navigation

    sealed class Screen(val route: String) {

    object Login : Screen("login")
    object SignUp : Screen("signup")

    object Main : Screen("main")

    object Dashboard : Screen("dashboard")
    object Register : Screen("register")
    object Identify : Screen("identify")
    object Profile : Screen("profile")
}
