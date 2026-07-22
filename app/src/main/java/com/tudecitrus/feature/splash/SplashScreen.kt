package com.tudecitrus.feature.splash

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tudecitrus.R
import com.tudecitrus.ui.theme.Poppins
import kotlinx.coroutines.delay

private val BgTop = Color(0xFF9BD60A)
private val BgBottom = Color(0xFF5FA600)

@Composable
fun SplashScreen(
    onSplashCompleted: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var started by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.6f,
        animationSpec = tween(700, easing = EaseOutBack),
        label = "logoScale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600),
        label = "contentAlpha"
    )

    LaunchedEffect(Unit) {
        started = true
        delay(120)
        while (progress < 1f) {
            delay(18)
            progress = (progress + 0.013f).coerceAtMost(1f)
        }
        delay(280)
        onSplashCompleted()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
    ) {
        // Logo + nama (tengah)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.splash_logo),
                contentDescription = "Logo aplikasi",
                modifier = Modifier
                    .size(136.dp)
                    .graphicsLayer { scaleX = logoScale; scaleY = logoScale; alpha = contentAlpha }
            )

            Text(
                modifier = Modifier
                    .padding(top = 26.dp, start = 24.dp, end = 24.dp)
                    .graphicsLayer { alpha = contentAlpha },
                text = "Sistem Deteksi\nPenyakit Jeruk",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 27.sp,
                lineHeight = 33.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .graphicsLayer { alpha = contentAlpha },
                text = "Berbasis Kecerdasan Buatan",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .graphicsLayer { alpha = contentAlpha },
                text = "Arsitektur MobileNetV3",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.78f),
                textAlign = TextAlign.Center
            )
        }

        // Progress (bawah)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp)
                .graphicsLayer { alpha = contentAlpha },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(200.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.30f)
            )
            Text(
                modifier = Modifier.padding(top = 14.dp),
                text = "v1.0.0",
                fontFamily = Poppins,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

