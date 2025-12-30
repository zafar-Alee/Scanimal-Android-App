package com.kingree.scanimal.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingree.scanimal.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
                onLoginSuccess: () -> Unit,
                onSignUpClick: () -> Unit
) {

    // States for text fields

    val auth = FirebaseAuth.getInstance()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", 0)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC)),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {

            // Logo (Replace with actual logo later)
            Image(
                painter = painterResource(id = R.drawable.logo ),
                contentDescription = "Scanimal Logo",
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Scanimal",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Text(
                text = "Smart Animal Detection & Scanning",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = {

                    // 1️⃣ Input validation
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter email and password"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    // 2️⃣ Firebase Sign In
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            isLoading = false

                            if (task.isSuccessful) {


                                val savedName = prefs.getString(email, null)

                                if (savedName != null) {
                                    prefs.edit()
                                        .putString("USER_NAME", savedName)
                                        .apply()
                                }

                                onLoginSuccess()
                            } else {
                                // 4️⃣ Failure → show error
                                errorMessage =
                                    task.exception?.message ?: "Login failed"
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A8F39)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isLoading) "Logging in..." else "Login",
                    color = Color.White
                )
            }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    if (email.isBlank()) {
                        errorMessage = "Enter email to reset password"
                        return@TextButton
                    }

                    auth.sendPasswordResetEmail(email.trim())
                        .addOnCompleteListener { task ->
                            errorMessage =
                                if (task.isSuccessful)
                                    "Password reset email sent"
                                else
                                    task.exception?.message
                        }
                }
            ) {
                Text("Forgot Password?", color = Color(0xFF1A8F39))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account?")
                Spacer(Modifier.width(4.dp))
                Clickable(onClick = onSignUpClick) {
                    Text(
                        text = "Sign Up",
                        color = Color(0xFF1A8F39),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }


        }}}

            @Composable
            fun Clickable(
                onClick: () -> Unit,
                contentPadding: PaddingValues = PaddingValues(0.dp),
                modifier: Modifier = Modifier,
                content: @Composable () -> Unit
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = contentPadding,
                    modifier = modifier
                ) {
                    content()
                }
            }




