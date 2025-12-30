package com.kingree.scanimal.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingree.scanimal.R

// ------------------ DATA MODEL ------------------
data class Animal(
    val id: String,
    val species: String,
    val imageRes: Int,
    val status: String
)

// ------------------ DASHBOARD SCREEN ------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    animals: List<Animal> = sampleAnimals,
    onIdentifyClick: () -> Unit = {},
    onVerifyClick: () -> Unit = {},
    onAnimalClick: (Animal) -> Unit = {}
) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", 0)
    val name = prefs.getString("USER_NAME", "User")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Scanimal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = name ?: "$name",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20)
                )
            )


},
        containerColor = Color(0xFFE8F5E9)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {

            Text(
                text = "Quick Actions",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1B5E20)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

            Text(
                text = "Your Animals",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1B5E20)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(animals) { animal ->
                    AnimalCard(
                        animal = animal,
                        onClick = { onAnimalClick(animal) }
                    )
                }
            }
        }
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
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }
}

// ------------------ ANIMAL CARD ------------------
@Composable
fun AnimalCard(
    animal: Animal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = animal.imageRes),
                contentDescription = animal.species,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.LightGray, RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = animal.species,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${animal.id}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            StatusBadge(status = animal.status)
        }
    }
}

// ------------------ STATUS BADGE ------------------
@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Verified" -> Color(0xFF1B5E20)
        "Pending" -> Color(0xFFFFA000)
        else -> Color.Gray
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

// ------------------ SAMPLE DATA ------------------
val sampleAnimals = listOf(
    Animal("A001", "Cow", R.drawable.cow_image, "Verified"),
    Animal("A002", "Buffalo", R.drawable.buffalo_image, "Pending"),
    Animal("A003", "Goat", R.drawable.goat_image, "Verified"),
    Animal("A004", "Cow", R.drawable.cow_image, "Verified"),
    Animal("A005", "Goat", R.drawable.goat_image, "Pending"),
    Animal("A001", "Cow", R.drawable.cow_image, "Verified"),
    Animal("A002", "Buffalo", R.drawable.buffalo_image, "Pending"),
    Animal("A003", "Goat", R.drawable.goat_image, "Verified"),
    Animal("A004", "Cow", R.drawable.cow_image, "Verified"),
    Animal("A005", "Goat", R.drawable.goat_image, "Pending")
)
