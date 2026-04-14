package com.kingree.scanimal.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.kingree.scanimal.Model.AnimalRecord
import com.kingree.scanimal.R
import com.kingree.scanimal.repository.AnimalRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onIdentifyClick: () -> Unit = {},
    onVerifyClick: () -> Unit = {},
    onAnimalClick: (AnimalRecord) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", 0)
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    val name =
        firebaseUser?.displayName?.takeIf { it.isNotBlank() }
            ?: firebaseUser?.email
                ?.let { prefs.getString(it, null) }
                ?.takeIf { it.isNotBlank() }
            ?: prefs.getString("USER_NAME", null)
                ?.takeIf { it.isNotBlank() }
            ?: "User"

    var animals by remember { mutableStateOf<List<AnimalRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var editingAnimal by remember { mutableStateOf<AnimalRecord?>(null) }
    var deleteCandidate by remember { mutableStateOf<AnimalRecord?>(null) }
    var showDeleteFinalConfirm by remember { mutableStateOf(false) }
    var isActionLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load animals for this user from Firestore
    LaunchedEffect(firebaseUser?.uid) {
        val uid = firebaseUser?.uid
        if (uid == null) {
            isLoading = false
            return@LaunchedEffect
        }
        scope.launch {
            val result = AnimalRepository.getAnimalsForUser(uid)
            result.fold(
                onSuccess = { animals = it },
                onFailure = { errorMsg = it.message }
            )
            isLoading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth().height(96.dp).background(Color(0xFF1B5E20)),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(start = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Scanimal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(name, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp)
        ) {
            Text("Quick Actions", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    title = "Identify Animal",
                    iconRes = R.drawable.baseline_content_paste_search_24,
                    bgColor = Color(0xFF1B5E20),
                    onClick = onIdentifyClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Verify Ownership",
                    iconRes = R.drawable.outline_check_box_24,
                    bgColor = Color(0xFF2E7D32),
                    onClick = onVerifyClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Your Animals", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20))
            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF22C55E))
                    }
                }
                errorMsg != null -> {
                    Text("Error: $errorMsg", color = Color.Red, fontSize = 13.sp)
                }
                animals.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🐾", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No animals registered yet", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
                            Text("Register your first animal using the + tab", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(animals) { animal ->
                            AnimalCard(
                                animal = animal,
                                actionEnabled = !isActionLoading,
                                onClick = { onAnimalClick(animal) },
                                onEditClick = { editingAnimal = animal },
                                onDeleteClick = {
                                    deleteCandidate = animal
                                    showDeleteFinalConfirm = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    editingAnimal?.let { animal ->
        EditAnimalDialog(
            animal = animal,
            isLoading = isActionLoading,
            onDismiss = { if (!isActionLoading) editingAnimal = null },
            onSave = { nameInput, speciesInput, ageInput, colorInput ->
                scope.launch {
                    val oldAnimal = animal
                    val updatedAnimal = oldAnimal.copy(
                        name = nameInput,
                        species = speciesInput,
                        age = ageInput,
                        color = colorInput
                    )

                    // Optimistic UI update so users see the change instantly.
                    animals = animals.map { if (it.animalId == oldAnimal.animalId) updatedAnimal else it }
                    editingAnimal = null

                    isActionLoading = true
                    val result = withTimeoutOrNull(8000) {
                        AnimalRepository.updateAnimalDetails(
                            animalId = oldAnimal.animalId,
                            name = nameInput,
                            species = speciesInput,
                            age = ageInput,
                            color = colorInput
                        )
                    } ?: Result.failure(Exception("Update request timed out. Please try again."))

                    result.fold(
                        onSuccess = {
                            snackbarHostState.showSnackbar("Saved successfully")
                        },
                        onFailure = {
                            animals = animals.map { if (it.animalId == oldAnimal.animalId) oldAnimal else it }
                            errorMsg = "Update failed: ${it.message}"
                            snackbarHostState.showSnackbar("Update failed. Changes were reverted.")
                        }
                    )
                    isActionLoading = false
                }
            }
        )
    }

    if (deleteCandidate != null && !showDeleteFinalConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isActionLoading) deleteCandidate = null },
            title = { Text("Delete ${deleteCandidate!!.name}?") },
            text = { Text("Step 1 of 2: This animal will be removed from your list.") },
            confirmButton = {
                TextButton(
                    enabled = !isActionLoading,
                    onClick = { showDeleteFinalConfirm = true }
                ) { Text("Continue") }
            },
            dismissButton = {
                TextButton(
                    enabled = !isActionLoading,
                    onClick = {
                        deleteCandidate = null
                        showDeleteFinalConfirm = false
                    }
                ) { Text("Cancel") }
            }
        )
    }

    if (deleteCandidate != null && showDeleteFinalConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isActionLoading) showDeleteFinalConfirm = false },
            title = { Text("Final confirmation") },
            text = { Text("Step 2 of 2: This action cannot be undone. Delete permanently?") },
            confirmButton = {
                TextButton(
                    enabled = !isActionLoading,
                    onClick = {
                        val target = deleteCandidate ?: return@TextButton
                        val previousAnimals = animals

                        // Optimistic removal and immediate dialog close keeps the UI snappy.
                        animals = animals.filterNot { it.animalId == target.animalId }
                        deleteCandidate = null
                        showDeleteFinalConfirm = false

                        scope.launch {
                            isActionLoading = true
                            val result = withTimeoutOrNull(8000) {
                                AnimalRepository.deleteAnimal(target.animalId)
                            } ?: Result.failure(Exception("Delete request timed out. Please try again."))

                            result.fold(
                                onSuccess = {
                                    snackbarHostState.showSnackbar("Animal deleted")
                                },
                                onFailure = {
                                    animals = previousAnimals
                                    errorMsg = "Delete failed: ${it.message}"
                                    snackbarHostState.showSnackbar("Delete failed. Animal restored.")
                                }
                            )
                            isActionLoading = false
                        }
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    enabled = !isActionLoading,
                    onClick = { showDeleteFinalConfirm = false }
                ) { Text("Back") }
            }
        )
    }
}

// ------------------ QUICK ACTION CARD ------------------
@Composable
fun QuickActionCard(
    title: String,
    iconRes: Int,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(id = iconRes), contentDescription = title, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, fontSize = 14.sp)
        }
    }
}

// ------------------ ANIMAL CARD ------------------
@Composable
fun AnimalCard(
    animal: AnimalRecord,
    actionEnabled: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Front image from Firebase Storage URL
            if (animal.frontImageUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(animal.frontImageUrl),
                    contentDescription = animal.name,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(64.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐾", fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = animal.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "Species: ${animal.species}", fontSize = 13.sp, color = Color.Gray)
                Text(text = "ID: ${animal.animalId}", fontSize = 12.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(status = animal.status)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(enabled = actionEnabled, onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit animal", tint = Color(0xFF1B5E20))
                    }
                    IconButton(enabled = actionEnabled, onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete animal", tint = Color(0xFFC62828))
                    }
                }
            }
        }
    }
}

@Composable
fun EditAnimalDialog(
    animal: AnimalRecord,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, species: String, age: String, color: String) -> Unit
) {
    var name by remember(animal.animalId) { mutableStateOf(animal.name) }
    var species by remember(animal.animalId) { mutableStateOf(animal.species) }
    var age by remember(animal.animalId) { mutableStateOf(animal.age) }
    var color by remember(animal.animalId) { mutableStateOf(animal.color) }

    val canSave = name.trim().isNotEmpty() && !isLoading

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Animal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Animal Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Species") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = { onSave(name.trim(), species.trim(), age.trim(), color.trim()) }
            ) {
                Text(if (isLoading) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(enabled = !isLoading, onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ------------------ STATUS BADGE ------------------
@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Verified" -> Color(0xFF1B5E20)
        "Pending"  -> Color(0xFFFFA000)
        else       -> Color.Gray
    }
    Text(
        text = status,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(color, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}
