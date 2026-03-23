package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import bg.zanaiti.craftguide.models.Craft
import bg.zanaiti.craftguide.ui.CraftViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CraftListScreen(
    onCraftClick: (Craft) -> Unit,
    viewModel: CraftViewModel = viewModel()
) {
    val crafts by viewModel.crafts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Занаяти") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize())
                }
                crafts.isEmpty() -> {
                    Text(
                        text = "Няма занаяти",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn {
                        items(crafts) { craft ->
                            println("📸 Занаят: ${craft.translations["bg"]?.name}, URL: ${craft.imageUrl}")
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { onCraftClick(craft) },
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Снимка
                                    AsyncImage(
                                        model = craft.imageUrl,
                                        contentDescription = craft.translations["bg"]?.name,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .padding(end = 16.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Текст
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = craft.translations["bg"]?.name ?: "Няма име",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = craft.translations["bg"]?.description?.take(80) ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}