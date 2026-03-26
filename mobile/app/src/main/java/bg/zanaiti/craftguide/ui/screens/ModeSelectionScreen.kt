package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ModeSelectionScreen(
    onSelectList: () -> Unit,
    onSelectMap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, // Центрира бутоните и текста хоризонтално
        verticalArrangement = Arrangement.Center // Центрира цялата група вертикално
    ) {
        Text(
            text = "Как искате да разгледате занаятите?",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center, // Центрира самия тескт
            modifier = Modifier.fillMaxWidth() // Разпъва текста, за да може TextAlign да работи
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSelectList,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📋 Списък със занаяти")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSelectMap,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🗺️ Виж занаяти в близост до мен")
        }
    }
}