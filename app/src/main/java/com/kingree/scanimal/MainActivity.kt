package com.kingree.scanimal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.kingree.scanimal.navigation.AppNavGraph
import com.kingree.scanimal.navigation.Screen
import com.kingree.scanimal.ui.theme.ScanimalTheme

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth   // ✅ correct place

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()     // ✅ correct init

        setContent {
            ScanimalTheme {

                val navController = rememberNavController()

                // Decide start screen based on auth state
                val startDestination = if (auth.currentUser != null)
                    Screen.Main.route
                else
                    Screen.Login.route

                AppNavGraph(
                    navController = navController
                )
            }
        }
    }
}
