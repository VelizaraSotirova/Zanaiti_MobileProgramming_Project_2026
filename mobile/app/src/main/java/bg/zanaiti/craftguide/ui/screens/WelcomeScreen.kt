package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bg.zanaiti.craftguide.R
import androidx.compose.foundation.Image

@Composable
fun WelcomeScreen(onStartClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Фонова снимка (сложи картинка в res/drawable)
        Image(
            painter = painterResource(id = R.drawable.bg_nature),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Полупрозрачен overlay за по-добра четимост на текста
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                    startY = 0.5f,
                    endY = 1f
                ))
        )

        // Съдържание
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Опознайте старинните занаяти на България с нас!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Елате на разходка, в която ще ви покажем най-интересните занаяти на територията на България!",
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5A2B)
                )
            ) {
                Text(
                    text = "Нека започнем обиколката!",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}