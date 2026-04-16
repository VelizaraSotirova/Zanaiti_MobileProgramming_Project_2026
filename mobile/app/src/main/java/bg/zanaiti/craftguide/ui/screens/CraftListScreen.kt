package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import bg.zanaiti.craftguide.models.Craft
import bg.zanaiti.craftguide.ui.CraftViewModel
import bg.zanaiti.craftguide.ui.LanguageViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CraftListScreen(
    langViewModel: LanguageViewModel,
    onCraftClick: (Craft) -> Unit,
    viewModel: CraftViewModel = viewModel()
) {
    val crafts by viewModel.crafts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentLanguage by langViewModel.currentLanguage.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn {
            items(crafts) { craft ->
                CraftItemRow(craft, langViewModel) { onCraftClick(craft) }
            }
        }
    }
}

@Composable
fun CraftItemRow(
    craft: Craft,
    langViewModel: LanguageViewModel,
    onClick: () -> Unit
) {
    val currentLanguage by langViewModel.currentLanguage.collectAsState()

    // Взимаме BG версията като базов текст за превод
    val baseName = craft.translations["bg"]?.name ?: "Няма име"
    val baseDesc = craft.translations["bg"]?.description?.take(80) ?: ""

    var translatedName by remember { mutableStateOf(baseName) }
    var translatedDesc by remember { mutableStateOf(baseDesc) }

    LaunchedEffect(currentLanguage, baseName) {
        translatedName = langViewModel.translate(baseName)
        translatedDesc = langViewModel.translate(baseDesc)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            AsyncImage(
                model = craft.imageUrl,
                contentDescription = translatedName,
                modifier = Modifier.size(80.dp).padding(end = 16.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(text = translatedName, style = MaterialTheme.typography.titleLarge)
                Text(text = translatedDesc, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            }
        }
    }
}