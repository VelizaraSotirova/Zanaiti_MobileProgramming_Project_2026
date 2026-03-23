package bg.zanaiti.craftguide.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bg.zanaiti.craftguide.models.Craft
import bg.zanaiti.craftguide.ui.CraftViewModel
import androidx.compose.runtime.collectAsState

import androidx.compose.foundation.layout.padding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CraftViewModel,
    startDestination: String = "list"
) {
    val navController = rememberNavController()
    var selectedCraft by remember { mutableStateOf<Craft?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Списък") },
                    label = { Text("Занаяти") },
                    selected = navController.currentDestination?.route == "list",
                    onClick = { navController.navigate("list") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Map, contentDescription = "Карта") },
                    label = { Text("Карта") },
                    selected = navController.currentDestination?.route == "map",
                    onClick = { navController.navigate("map") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("list") {
                CraftListScreen(
                    onCraftClick = { craft ->
                        selectedCraft = craft
                        navController.navigate("detail/${craft.id}")
                    }
                )
            }
            composable("map") {
                MapScreen(
                    showOnlyCraftId = null,
                    onCraftClick = { craft ->
                        selectedCraft = craft
                        navController.navigate("detail/${craft.id}")
                    }
                )
            }
            composable("detail/{craftId}", arguments = listOf(navArgument("craftId") { type = NavType.LongType })) { backStackEntry ->
                val craftId = backStackEntry.arguments?.getLong("craftId")
                val craft = viewModel.crafts.collectAsState().value.find { it.id == craftId }
                craft?.let {
                    CraftDetailScreen(
                        craft = it,
                        onBack = { navController.popBackStack() },
                        onShowOnMap = {
                            navController.navigate("map") {
                                // Засега просто отваряме картата
                            }
                        },
                        onStartQuiz = {
                            navController.navigate("quiz/${craft.id}")
                        }
                    )
                }
            }
            composable("quiz/{craftId}", arguments = listOf(navArgument("craftId") { type = NavType.LongType })) { backStackEntry ->
                val craftId = backStackEntry.arguments?.getLong("craftId")
                val craft = viewModel.crafts.collectAsState().value.find { it.id == craftId }
                craft?.let {
                    QuizScreen(
                        craft = it,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}