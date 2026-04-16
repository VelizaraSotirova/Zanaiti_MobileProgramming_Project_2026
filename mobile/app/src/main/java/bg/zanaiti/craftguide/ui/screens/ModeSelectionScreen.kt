package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bg.zanaiti.craftguide.ui.LanguageViewModel

@Composable
fun ModeSelectionScreen(
    langViewModel: LanguageViewModel,
    onSelectList: () -> Unit,
    onSelectMap: () -> Unit
) {
    val currentLanguage by langViewModel.currentLanguage.collectAsState()

    // Състояния за преведените текстове
    var headline by remember { mutableStateOf("Как искате да разгледате занаятите?") }
    var listBtnText by remember { mutableStateOf("📋 Списък със занаяти") }
    var mapBtnText by remember { mutableStateOf("🗺️ Виж занаяти в близост до мен") }

    LaunchedEffect(currentLanguage) {
        headline = langViewModel.translate("Как искате да разгледате занаятите?")
        // Превеждаме само текста след емотиконата
        listBtnText = "📋 " + langViewModel.translate("Списък със занаяти")
        mapBtnText = "🗺️ " + langViewModel.translate("Виж занаяти в близост до мен")
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onSelectList, modifier = Modifier.fillMaxWidth()) {
            Text(listBtnText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onSelectMap, modifier = Modifier.fillMaxWidth()) {
            Text(mapBtnText)
        }
    }
}