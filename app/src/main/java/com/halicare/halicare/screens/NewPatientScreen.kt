package com.halicare.halicare.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halicare.halicare.R
import com.halicare.halicare.ui.theme.HalicareTheme

@Composable
fun HaliCareLogoWithText() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "HaliCare Logo",
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(15.dp))
        Text(
            text = "HaliCare",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPatientScreen(
    onLogout: () -> Unit = {},
    onNavigateToClinicRequirements: () -> Unit
) {
    val context = LocalContext.current
    val firstName = remember {
        val prefs = context.getSharedPreferences("HALICARE_PREFS", Context.MODE_PRIVATE)
        val name = prefs.getString("FIRST_NAME", null)
        name?.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() } ?: "User"
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { HaliCareLogoWithText() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.home_banner_bg),
                        contentDescription = "Clinic Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-50).dp)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Welcome, $firstName",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                        color = Color(0xFF001F54),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onNavigateToClinicRequirements,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(75.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F54)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "VIEW CLINIC REQUIREMENTS",
                                color = Color.White,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Arrow up right",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(45f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = "Health Tips",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                        color = Color(0xFF001F54),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HealthTipCard(
                        iconRes = R.drawable.hydration_image,
                        title = "Stay Hydrated",
                        description = "Drink 8-10 glasses of water daily for optimal health",
                        blueBackground = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HealthTipCard(
                        iconRes = R.drawable.exercise_image,
                        title = "Regular Exercise",
                        description = "30 minutes of activity can boost your mood and energy",
                        blueBackground = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HealthTipCard(
                        iconRes = R.drawable.fruitsvegetables,
                        title = "Nutrition",
                        description = "Eat a balanced diet rich in fruits and vegetables",
                        blueBackground = true
                    )
                }
            }
        }
    }
}

@Composable
fun HealthTipCard(iconRes: Int, title: String, description: String, blueBackground: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (blueBackground) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun StatisticCard(value: String, label: String, inBorder: Boolean = false) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (inBorder) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface),
        border = if (inBorder) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewPatientScreenPreview() {
    HalicareTheme {
        NewPatientScreen(
            onNavigateToClinicRequirements = {}
        )
    }
}
