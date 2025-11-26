package com.halicare.halicare.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halicare.halicare.R
import com.halicare.halicare.ui.theme.HalicareTheme

@Composable
fun TeaserScreenFour(
    onBack: () -> Unit,
    onGetStarted: () -> Unit,
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
                .height(320.dp),
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
            painter = painterResource(R.drawable.teaser_d),
            contentDescription = "HaliCare Illustration",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .size(220.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 122.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Always Connected to Care",
                fontSize = 25.sp,
                color = Color(0xFF001F54),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(25.dp))
            Text(
                text = "HaliCare is more than an app, it’s\n" +
                        "your trusted partner in wellness.\n" +
                        "Appointments, counselling, and care,\n" +
                        "all in one place.",
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF001F54),
                modifier = Modifier.padding(bottom = 42.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001E6B)),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .height(50.dp)
                    .width(230.dp)
            ) {
                Text(text = "GET STARTED", color = Color.White, fontSize = 20.sp)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TeaserScreenFourPreview() {
    TeaserScreenFour(
        onBack = {},
        onGetStarted = {},
        onSkip = {}
    )
}
