package com.kingree.scanimal.view

 import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.kingree.scanimal.Model.AnimalRecord
import com.kingree.scanimal.repository.AnimalRepository
import kotlinx.coroutines.launch



@Composable
fun IdentifyAnimalScreen(
    onRegisterNew: () -> Unit
) {
    var scanState by remember { mutableStateOf(ScanState.Idle) }
    var matchedAnimal by remember { mutableStateOf<AnimalRecord?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var infoMessage by remember { mutableStateOf("Ready to scan") }
    var isScanning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap == null) {
            scanState = ScanState.Error
            infoMessage = "Camera capture was cancelled. Please try again."
            return@rememberLauncherForActivityResult
        }

        capturedBitmap = bitmap
        scope.launch {
            val uid = firebaseUser?.uid
            if (uid == null) {
                scanState = ScanState.Error
                infoMessage = "Please log in to scan and verify registrations."
                return@launch
            }

            isScanning = true
            scanState = ScanState.Scanning
            infoMessage = "Scanning face and checking registered animals..."

            // Placeholder matching logic until face-embedding comparison is integrated.
            val result = AnimalRepository.getAnimalsForUser(uid)

            result.fold(
                onSuccess = { animals ->
                    val found = animals.firstOrNull()
                    if (found != null) {
                        matchedAnimal = found
                        scanState = ScanState.Found
                        infoMessage = "Animal registered"
                    } else {
                        matchedAnimal = null
                        scanState = ScanState.NotFound
                        infoMessage = "No registered animal matched this scan."
                    }
                },
                onFailure = {
                    matchedAnimal = null
                    scanState = ScanState.Error
                    infoMessage = "Scan failed: ${it.message ?: "Unknown error"}"
                }
            )

            isScanning = false
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4FBF6))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        val isCompact = maxWidth < 360.dp
        val previewHeight = if (isCompact) 250.dp else 320.dp
        val titleSize = if (isCompact) 20.sp else 24.sp
        val contentSpacing = if (isCompact) 14.dp else 18.dp

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Identify Animal",
                            color = Color.White,
                            fontSize = titleSize,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Center the animal face in frame and scan for a quick match.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(contentSpacing))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(previewHeight)
                        .background(Color(0xFFDCEFE0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (capturedBitmap != null) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Captured animal face",
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.12f))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.78f)
                                .aspectRatio(1f)
                                .border(2.dp, Color(0xFF22C55E), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Align animal face here",
                                color = Color(0xFF1B5E20),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("Tip: keep face centered") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF1B5E20))
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(contentSpacing))

            Button(
                onClick = {
                    cameraLauncher.launch(null)
                },
                enabled = !isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCompact) 50.dp else 54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Scanning...")
                } else {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Open Camera & Scan")
                }
            }

            Spacer(modifier = Modifier.height(contentSpacing))

            when (scanState) {
                ScanState.Found -> {
                    val animal = matchedAnimal
                    ResultCard(
                        title = "Animal Registered",
                        subtitle = if (animal == null) {
                            "A matching registered profile was found."
                        } else {
                            "${animal.name} (${animal.animalId})"
                        },
                        containerColor = Color(0xFFE8F5E9),
                        iconColor = Color(0xFF2E7D32),
                        isSuccess = true
                    )
                }

                ScanState.NotFound -> {
                    ResultCard(
                        title = "No Match Found",
                        subtitle = "No registered animal matched this face scan.",
                        containerColor = Color(0xFFFFEBEE),
                        iconColor = Color(0xFFC62828),
                        isSuccess = false
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onRegisterNew,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Register as New")
                    }
                }

                ScanState.Scanning -> {
                    ResultCard(
                        title = "Scanning in progress",
                        subtitle = infoMessage,
                        containerColor = Color(0xFFE3F2FD),
                        iconColor = Color(0xFF1565C0),
                        isSuccess = null
                    )
                }

                ScanState.Error -> {
                    ResultCard(
                        title = "Scan unavailable",
                        subtitle = infoMessage,
                        containerColor = Color(0xFFFFF8E1),
                        iconColor = Color(0xFFEF6C00),
                        isSuccess = null
                    )
                }

                ScanState.Idle -> {
                    Text(
                        text = infoMessage,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    title: String,
    subtitle: String,
    containerColor: Color,
    iconColor: Color,
    isSuccess: Boolean?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (isSuccess) {
                    true -> Icons.Default.CheckCircle
                    false -> Icons.Default.Warning
                    null -> Icons.Default.Info
                },
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(text = title, color = iconColor, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color.DarkGray, fontSize = 13.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IdentifyAnimalScreenPreview() {
    IdentifyAnimalScreen(onRegisterNew = {})
}

private enum class ScanState {
    Idle,
    Scanning,
    Found,
    NotFound,
    Error
}

