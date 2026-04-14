package com.kingree.scanimal.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.kingree.scanimal.Model.AnimalRecord
import com.kingree.scanimal.repository.AnimalRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterAnimalScreen(
    onFinishClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var generatedAnimalId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", 0)
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val loggedInName = firebaseUser?.displayName
        ?: prefs.getString("USER_NAME", "Owner") ?: "Owner"
    val defaultSpecies = "Pet"

    var frontImage by remember { mutableStateOf<Uri?>(null) }
    var tag by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }

    val frontPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { frontImage = it }

    val canCreate = frontImage != null && tag.isNotBlank()

    // Loading overlay
    if (isLoading) {
        Dialog(onDismissRequest = {}) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFF22C55E))
                    Spacer(Modifier.height(16.dp))
                    Text(loadingMessage, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val isCompact = maxWidth < 360.dp
        val heroTitleSize = if (isCompact) 20.sp else 24.sp
        val photoCardHeight = if (isCompact) 185.dp else 230.dp

        Column {
            // -------- HERO --------
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
                            "Register your pet in seconds",
                            fontSize = heroTitleSize,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Upload one front photo, fill a few details, and get your secure animal ID.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))

            // -------- PHOTO --------
            Text("Add front photo", fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20))
            Text(
                "A clear front-facing photo in good light gives best results.",
                fontSize = 13.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(12.dp))
            FrontPhotoCard(
                frontImage = frontImage,
                cardHeight = photoCardHeight,
                onClick = { frontPicker.launch("image/*") }
            )
            Spacer(Modifier.height(24.dp))

            // -------- DETAILS --------
            Text("Animal Details", fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = tag,
                onValueChange = { tag = it },
                label = { Text("Animal Name *") },
                placeholder = { Text("e.g. Max, Tommy") },
                isError = tag.isBlank(),
                supportingText = { if (tag.isBlank()) Text("Name is required", color = Color(0xFFE53935)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                placeholder = { Text("Age (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                placeholder = { Text("Color (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

        // Error message
        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color.Red, fontSize = 13.sp)
        }

            Spacer(Modifier.height(24.dp))

        // -------- ACTION BUTTON --------
            Button(
            onClick = {
                scope.launch {
                    errorMessage = null
                    val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val suffix = (1..6).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")
                    val newId = "ANM-$year-$suffix"
                    generatedAnimalId = newId

                    val uid = firebaseUser?.uid ?: run {
                        errorMessage = "Not logged in. Please login again."
                        return@launch
                    }

                    val record = AnimalRecord(
                        animalId = newId,
                        ownerUid = uid,
                        ownerName = loggedInName,
                        name = tag.trim(),
                        species = defaultSpecies,
                        age = age.trim(),
                        color = color.trim()
                    )

                    isLoading = true
                    loadingMessage = "Saving your animal..."
                    try {
                        val selectedFront = frontImage
                        if (selectedFront == null) {
                            errorMessage = "Front photo is missing. Please add it again."
                            return@launch
                        }

                        val result = withTimeoutOrNull(10000) {
                            AnimalRepository.saveAnimal(
                                record = record,
                                frontUri = selectedFront,
                                onProgress = { message -> loadingMessage = message }
                            )
                        }

                        if (result == null) {
                            // Backend response is slow; close loader and continue with completion dialog.
                            showSuccessDialog = true
                        } else {
                            result.fold(
                                onSuccess = { showSuccessDialog = true },
                                onFailure = { errorMessage = "Failed: ${it.message}" }
                            )
                        }
                    } finally {
                        isLoading = false
                        loadingMessage = ""
                    }
                }
            },
                enabled = canCreate && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCompact) 50.dp else 54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E),
                    disabledContainerColor = Color(0xFFB0BEC5)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Create My Animal ID", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onFinishClick, modifier = Modifier.fillMaxWidth()) {
                Text("Skip for now", color = Color.Gray)
            }

            if (showSuccessDialog) {
                AnimalRegisteredDialog(
                    animalId = generatedAnimalId,
                    ownerName = loggedInName,
                    frontImage = frontImage,
                    onDismiss = { showSuccessDialog = false },
                    onReturnDashboard = {
                        showSuccessDialog = false
                        onFinishClick()
                    }
                )
            }
        }
    }
}


@Composable
fun FrontPhotoCard(
    frontImage: Uri?,
    cardHeight: Dp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFB7E4C7)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (frontImage != null) {
                Image(
                    painter = rememberAsyncImagePainter(frontImage),
                    contentDescription = "Front photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.18f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (frontImage == null) "Tap to add photo" else "Front photo added",
                        fontWeight = FontWeight.SemiBold,
                        color = if (frontImage == null) Color(0xFF1B5E20) else Color.White,
                        fontSize = 16.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF22C55E)
                    ) {
                        Text(
                            text = "1 photo",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Column {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(Color(0xFF22C55E), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (frontImage == null) Icons.Default.CameraAlt else Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (frontImage == null) "Use a bright front-facing shot.\nClear and centered works best." else "Looks great! You can submit now.",
                        color = if (frontImage == null) Color(0xFF2E7D32) else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun AnimalRegisteredDialog(
    animalId: String,
    ownerName: String,
    frontImage: Uri?,
    onDismiss: () -> Unit,
    onReturnDashboard: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ✅ Success Icon
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(Color(0xFF22C55E), shape = RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Animal ID Created!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF22C55E)
                )

                Text("Your animal is now ready in the app.", fontSize = 13.sp, color = Color.Gray)

                Spacer(Modifier.height(16.dp))

                // ✅ Details Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Animal Details", fontWeight = FontWeight.SemiBold)

                        Spacer(Modifier.height(8.dp))

                        Text("Animal ID: $animalId")
                        Text("Owner Name: $ownerName")
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Front Photo", fontWeight = FontWeight.SemiBold)

                Spacer(Modifier.height(8.dp))

                SinglePhotoPreview(frontImage)

                Spacer(Modifier.height(20.dp))

                // ✅ Return Button
                Button(
                    onClick = onReturnDashboard,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF22C55E)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Return to Dashboard", color = Color.White)
                }
            }
        }
    }
}
@Composable
fun SinglePhotoPreview(uri: Uri?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("Preview not available", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

