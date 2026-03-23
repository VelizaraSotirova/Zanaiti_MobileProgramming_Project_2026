package bg.zanaiti.craftguide

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bg.zanaiti.craftguide.models.Craft
import bg.zanaiti.craftguide.ui.CraftViewModel
import bg.zanaiti.craftguide.ui.screens.*
import bg.zanaiti.craftguide.ui.theme.CraftGuideTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CraftGuideTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CraftApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CraftApp(viewModel: CraftViewModel = viewModel()) {
    val navController = rememberNavController()
    val crafts by viewModel.crafts.collectAsState()

    // Състояние за локацията
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Заявка за разрешение при нужда
    LaunchedEffect(Unit) {
        // Нищо не правим веднага – ще поискаме когато потребителят избере картата
    }

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        // Welcome Screen
        composable("welcome") {
            WelcomeScreen(
                onStartClick = {
                    navController.navigate("mode_selection")
                }
            )
        }

        // Избор на режим
        composable("mode_selection") {
            ModeSelectionScreen(
                onSelectList = {
                    navController.navigate("list")
                },
                onSelectMap = {
                    // Изискваме локация преди да покажем картата
                    locationPermissionState.launchPermissionRequest()
                    navController.navigate("map")
                }
            )
        }

        // Списък със занаяти
        composable("list") {
            CraftListScreen(
                onCraftClick = { craft ->
                    navController.navigate("detail/${craft.id}")
                }
            )
        }

        // Карта (с проверка за локация)
        composable("map") {
            if (locationPermissionState.status.isGranted) {
                MapScreen(
                    showOnlyCraftId = null,
                    onCraftClick = { craft ->
                        navController.navigate("detail/${craft.id}")
                    }
                )
            } else {
                // Показваме екран за искане на разрешение
                PermissionRequestScreen(
                    onRequestPermission = {
                        locationPermissionState.launchPermissionRequest()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Детайли за занаят
        composable("detail/{craftId}", arguments = listOf(navArgument("craftId") { type = NavType.LongType })) { backStackEntry ->
            val craftId = backStackEntry.arguments?.getLong("craftId")
            val craft = crafts.find { it.id == craftId }
            craft?.let {
                CraftDetailScreen(
                    craft = it,
                    onBack = { navController.popBackStack() },
                    onShowOnMap = {
                        navController.navigate("map_single/${it.id}")
                    },
                    onStartQuiz = { navController.navigate("quiz/${it.id}") }
                )
            }
        }

        // Карта само с един занаят (от детайлите)
        composable("map_single/{craftId}", arguments = listOf(navArgument("craftId") { type = NavType.LongType })) { backStackEntry ->
            val craftId = backStackEntry.arguments?.getLong("craftId")
            MapScreen(
                showOnlyCraftId = craftId,
                onCraftClick = { craft ->
                    navController.navigate("detail/${craft.id}")
                }
            )
        }

        // Quiz
        composable("quiz/{craftId}", arguments = listOf(navArgument("craftId") { type = NavType.LongType })) { backStackEntry ->
            val craftId = backStackEntry.arguments?.getLong("craftId")
            val craft = crafts.find { it.id == craftId }
            craft?.let {
                QuizScreen(
                    craft = it,
                    onBack = { navController.popBackStack() },
                    isLoggedIn = false,  // ← тук ще сложиш true, когато имаш логнат потребител
                    userId = null        // ← тук ще сложиш ID-то на логнатия
                )
            }
        }
    }
}