package com.kingree.scanimal.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun IdentifyAnimalScreen(
    onRegisterNew: () -> Unit
) {
    var isMatchFound by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Identify Animal",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Camera Preview Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.LightGray, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Align animal face within the frame",
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // TODO: Replace with ML / image matching logic
                isMatchFound = false // simulate "No Match Found"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Capture & Identify")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Result Section
        when (isMatchFound) {
            true -> {
                Text(
                    text = "Animal Found ✅",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
            }

            false -> {
                Text(
                    text = "No Match Found\nThis animal is not registered.",
                    color = Color.Red,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onRegisterNew,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Register as New")
                }
            }

            null -> {}
        }
    }
}
