package com.halicare.halicare.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.halicare.halicare.ui.theme.ColorOnPrimary
import com.halicare.halicare.ui.theme.ColorPrimary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicRequirementsScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Clinic Requirements",
                            color = ColorOnPrimary,
                            fontSize = 23.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ColorOnPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPrimary
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .align(Alignment.TopCenter)
                    .background(ColorPrimary)
            )
            Card(
                modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .padding(start = 2.dp)
                .height(500.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                ClinicRequirementsContent()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ClinicRequirementsContent() {
    val requirements = listOf(
        "Pick a transfer letter from your clinic. This document has your treatment history and is essential before you shift to a new clinic.",
        "If your clinic closes, contact your nearest county health office to assist you with obtaining the letter.",
        "Upload your transfer letter while making an appointment to your new clinic to ensure seamless care."
    )
    var page by remember { mutableStateOf(0) }

    LaunchedEffect(page) {
        delay(6000L)
        page = (page + 1) % requirements.size
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp, horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(ColorPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "!",
                color = ColorOnPrimary,
                fontWeight = MaterialTheme.typography.displayLarge.fontWeight,
                fontSize = 110.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Read Instructions Carefully",
            color = ColorPrimary,
            fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
            fontSize = 22.sp,
        )

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    page = (page - 1 + requirements.size) % requirements.size
                },
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = ColorPrimary
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        val correctedDirection = if (initialState == requirements.size - 1 && targetState == 0) 1
                        else if (initialState == 0 && targetState == requirements.size - 1) -1
                        else direction

                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth * correctedDirection },
                            animationSpec = tween(300)
                        ) with slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth * correctedDirection },
                            animationSpec = tween(300)
                        )
                    },
                    label = "requirement_animation"
                ) { targetPage ->
                    Text(
                        text = requirements[targetPage],
                        color = ColorPrimary,
                        fontSize = 20.sp,
                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                        textAlign = TextAlign.Center
                    )
                }
            }
            IconButton(
                onClick = {
                    page = (page + 1) % requirements.size
                },
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = ColorPrimary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(requirements.size) { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == page) 18.dp else 9.dp)
                        .clip(CircleShape)
                        .background(if (i == page) ColorPrimary else Color(0xFFD9D9D9))
                )
                if (i != requirements.size - 1) Spacer(modifier = Modifier.width(12.dp))
            }
        }

    }
}