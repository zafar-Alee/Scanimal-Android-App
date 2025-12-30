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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterAnimalScreen(
    onAddAnimalClick: () -> Unit = {},
    onFinishClick: () -> Unit = {}
) {

    var showSuccessDialog by remember { mutableStateOf(false) }

    var selectedAnimal by remember { mutableStateOf("Dog") }

    var frontImage: Uri? by remember { mutableStateOf<Uri?>(null) }
    var sideImage by remember { mutableStateOf<Uri?>(null) }
    var noseImage by remember { mutableStateOf<Uri?>(null) }

    var tag by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }

    val frontPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { frontImage = it }

    val sidePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { sideImage = it }

    val nosePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { noseImage = it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // -------- HEADER --------
        Text(
            text = "Register Animal",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )

        Spacer(Modifier.height(16.dp))

        // -------- ANIMAL TYPE --------
        Text("Select Animal Type", fontWeight = FontWeight.SemiBold)

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnimalTypeCard("Dog", selectedAnimal) { selectedAnimal = "Dog" }
            AnimalTypeCard("Redo", selectedAnimal) { selectedAnimal = "Redo" }
        }

        Spacer(Modifier.height(12.dp))
        AnimalTypeCard("Flame", selectedAnimal) { selectedAnimal = "Flame" }

        Spacer(Modifier.height(24.dp))

        // -------- PHOTOS --------
        Text("Capture Animal Photos", fontWeight = FontWeight.SemiBold)

        Text(
            "Keep animal still for clear photos.\nEnsure good lighting, avoid shadows.",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ImagePicker("Front View", frontImage) {
                frontPicker.launch("image/*")
            }
            ImagePicker("Side View", sideImage) {
                sidePicker.launch("image/*")
            }
            ImagePicker("Nose Close-up", noseImage) {
                nosePicker.launch("image/*")
            }
        }

        Spacer(Modifier.height(24.dp))

        // -------- DETAILS --------
        Text("Animal Details", fontWeight = FontWeight.SemiBold)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = tag,
            onValueChange = { tag = it },
            placeholder = { Text("Nickname / Tag Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            placeholder = { Text("Age (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            placeholder = { Text("Color (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // -------- ACTION BUTTON --------
        Button(
            onClick = {
                // TODO: validation + save logic later
                showSuccessDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22C55E)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Create Animal ID",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }


        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onFinishClick) {
            Text("Skip for now", color = Color.Gray)
        }

        if (showSuccessDialog) {
            AnimalRegisteredDialog(
                animalId = "MWH-2023-000001",
                ownerName = "Ali Khan",
                frontImage = frontImage,
                sideImage = sideImage,
                noseImage = noseImage,
                onDismiss = { showSuccessDialog = false },
                onReturnDashboard = {
                    showSuccessDialog = false
                    onFinishClick()
                }
            )
        }

    }
}


@Composable
fun AnimalTypeCard(
    title: String,
    selected: String,
    onClick: () -> Unit
) {
    val isSelected = title == selected

    Card(
        modifier = Modifier
            .size(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF22C55E) else Color.LightGray
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(title)
        }
    }
}
@Composable
fun ImagePicker(
    label: String,
    imageUri: Uri?,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri == null) {
                Icon(Icons.Default.CameraAlt, null)
            } else {
                Image(
                    rememberAsyncImagePainter(imageUri),
                    null,
                    Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun AnimalRegisteredDialog(
    animalId: String,
    ownerName: String,
    frontImage: Uri?,
    sideImage: Uri?,
    noseImage: Uri?,
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

                Text(
                    text = "Your animal has been successfully registered.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

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

                // ✅ Photos
                Text("Animal Photos", fontWeight = FontWeight.SemiBold)

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PhotoPreview("Front View", frontImage)
                    PhotoPreview("Side View", sideImage)
                    PhotoPreview("Nose Print", noseImage)
                }

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
fun PhotoPreview(label: String, uri: Uri?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (uri != null) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp)
    }
}




