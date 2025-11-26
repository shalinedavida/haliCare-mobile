package com.halicare.halicare.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halicare.halicare.R

@Composable
fun TeaserScreenOne(
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Image(
            painter = painterResource(id = R.drawable.semicircle),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            contentScale = ContentScale.FillBounds
        )

        Text(
            text = "Skip",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 46.dp, end = 16.dp)
                .clickable { onSkip() }
        )

        Image(
            painter = painterResource(id = R.drawable.teaser_a),
            contentDescription = "HaliCare Illustration",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .size(200.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "HaliCare- Health Made Simple",
                fontSize = 25.sp,
                color = Color(0xFF001F54),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(25.dp))
            Text(
                text = "With HaliCare, healthcare is no longer\n" +
                        "out of reach. Find nearby clinics, connect\n" +
                        "instantly, and take control of your health\n" +
                        "with just a few taps.",
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF001F54),
                modifier = Modifier.padding(bottom = 42.dp)
            )
            Spacer(modifier = Modifier.height(55.dp))

                IconButton(onClick = onNext) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF001E6B), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Next", tint = Color.White)
                    }
                }
            }
        }
    }

