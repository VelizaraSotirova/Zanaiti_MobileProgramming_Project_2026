package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.Image
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
import bg.zanaiti.craftguide.ui.LanguageViewModel
import com.google.mlkit.nl.translate.TranslateLanguage

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    langViewModel: LanguageViewModel // ПРИЕМАМЕ СПОДЕЛЕНИЯ МОДЕЛ
) {
    // 1. Свързваме се със състоянието на езика и прогреса на теглене
    val currentLanguage by langViewModel.currentLanguage.collectAsState()
    val isDownloading by langViewModel.isDownloading.collectAsState()

    // 2. Състояния за текстовете, които ще се превеждат динамично
    var titleText by remember { mutableStateOf("Опознайте старинните занаяти на България с нас!") }
    var subtitleText by remember { mutableStateOf("Елате на разходка, в която ще ви покажем най-интересните занаяти на територията на България!") }
    var buttonText by remember { mutableStateOf("Нека започнем обиколката!") }

    // 3. Спусък за превод при всяка промяна на избрания език
    LaunchedEffect(currentLanguage) {
        titleText = langViewModel.translate("Опознайте старинните занаяти на България с нас!")
        subtitleText = langViewModel.translate("Елате на разходка, в която ще ви покажем най-интересните занаяти на територията на България!")
        buttonText = langViewModel.translate("Нека започнем обиколката!")
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Фонова снимка
        Image(
            painter = painterResource(id = R.drawable.bg_nature),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Полупрозрачен overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                    startY = 0.4f,
                    endY = 1f
                ))
        )

        // Основно съдържание
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 4. Селектор за език в горната част
            LanguageSelectorRow(
                selectedLang = currentLanguage,
                onLangSelected = { langViewModel.setLanguage(it) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 5. Визуален индикатор, ако се тегли езиков пакет (напр. Английски)
            if (isDownloading) {
                CircularProgressIndicator(color = Color(0xFF8B5A2B))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading language pack...", color = Color.White, fontSize = 12.sp)
            }

            Text(
                text = titleText, // Използваме пременливата за превод
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = subtitleText, // Използваме пременливата за превод
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5A2B)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = buttonText, // Използваме пременливата за превод
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}

@Composable
fun LanguageSelectorRow(
    selectedLang: String,
    onLangSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val languages = listOf(
            "BG" to TranslateLanguage.BULGARIAN,
            "EN" to TranslateLanguage.ENGLISH,
            "DE" to TranslateLanguage.GERMAN
        )

        languages.forEachIndexed { index, (label, code) ->
            TextButton(
                onClick = { onLangSelected(code) }
            ) {
                Text(
                    text = label,
                    color = if (selectedLang == code) Color(0xFF8B5A2B) else Color.White,
                    fontWeight = if (selectedLang == code) FontWeight.Bold else FontWeight.Normal
                )
            }
            if (index < languages.size - 1) {
                Text("|", color = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}