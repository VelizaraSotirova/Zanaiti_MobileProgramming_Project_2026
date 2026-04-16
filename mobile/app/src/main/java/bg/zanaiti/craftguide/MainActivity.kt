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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bg.zanaiti.craftguide.network.RetrofitClient
import bg.zanaiti.craftguide.ui.AuthViewModel
import bg.zanaiti.craftguide.ui.AuthViewModelFactory
import bg.zanaiti.craftguide.ui.CraftViewModel
import bg.zanaiti.craftguide.ui.LanguageViewModel
import bg.zanaiti.craftguide.ui.screens.*
import bg.zanaiti.craftguide.ui.theme.CraftGuideTheme
import bg.zanaiti.craftguide.utils.TokenManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(this)
        lifecycleScope.launch {
            tokenManager.loadTokenFromStorage()
            android.util.Log.d("MainActivity", "Token after load: ${if (tokenManager != null) "present" else "NULL"}")
        }
        RetrofitClient.initialize(tokenManager)

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
    val context = LocalContext.current

    // ВАЖНО: Тук създаваме споделения езиков модел
    val langViewModel: LanguageViewModel = viewModel()

    val tokenManager = remember { TokenManager(context) }
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(tokenManager)
    )
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val username by authViewModel.username.collectAsState()
    val crafts by viewModel.crafts.collectAsState()

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        // ==================== WELCOME SCREEN ====================
        composable("welcome") {
            WelcomeScreen(
                onStartClick = {
                    navController.navigate("main")
                },
                langViewModel = langViewModel
            )
        }

        // ==================== ОСНОВЕН ЕКРАН ====================
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                langViewModel = langViewModel,
                startDestination = "mode_selection",
                isLoggedIn = isAuthenticated,
                username = username,
                onProfileClick = {
                    if (isAuthenticated) {
                        navController.navigate("profile")
                    } else {
                        navController.navigate("auth")
                    }
                },
                onLogoutClick = {
                    authViewModel.logout()
                },
                onArScannerClick = {
                    navController.navigate("ar_scanner")
                }
            )
        }

        // ==================== АВТЕНТИКАЦИЯ ====================
        composable("auth") {
            AuthScreen(
                tokenManager = tokenManager,
                langViewModel = langViewModel,
                onAuthSuccess = {
                    navController.popBackStack()
                    navController.navigate("main")
                }
            )
        }

        // ==================== ПРОФИЛ ====================
        composable("profile") {
            val currentUserId by authViewModel.userId.collectAsState()
            ProfileScreen(
                userId = currentUserId ?: 0L,
                onBack = { navController.popBackStack() },
                langViewModel = langViewModel
            )
        }

        // ==================== ДЕТАЙЛИ ЗА ЗАНАЯТ ====================
        composable(
            route = "detail/{craftId}",
            arguments = listOf(navArgument("craftId") { type = NavType.LongType })
        ) { backStackEntry ->
            val craftId = backStackEntry.arguments?.getLong("craftId")
            val craft = crafts.find { it.id == craftId }
            craft?.let {
                CraftDetailScreen(
                    craft = it,
                    langViewModel = langViewModel,
                    onBack = { navController.popBackStack() },
                    onShowOnMap = {
                        navController.navigate("map_single/${it.id}")
                    },
                    onStartQuiz = {
                        navController.navigate("quiz/${it.id}")
                    }
                )
            }
        }

        // ==================== AR СКЕНЕР ====================
        composable("ar_scanner") {
            ARScannerScreen(
                onObjectDetected = { detectedName ->
                    val craft = crafts.find { craftItem ->
                        val bgTranslation = craftItem.translations["bg"]
                        bgTranslation?.name?.contains(detectedName, ignoreCase = true) == true
                    }

                    if (craft != null) {
                        navController.navigate("detail/${craft.id}") {
                            popUpTo("ar_scanner") { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
                langViewModel = langViewModel

            )
        }

        // ==================== КАРТА (ЕДИНИЧНА) ====================
        composable("map_single/{craftId}", arguments = listOf(navArgument("craftId") { type = NavType.LongType })) { backStackEntry ->
            val craftId = backStackEntry.arguments?.getLong("craftId")
            MapScreen(
                showOnlyCraftId = craftId,
                onCraftClick = { craft ->
                    navController.navigate("detail/${craft.id}")
                },
                langViewModel = langViewModel
            )
        }

        // ==================== QUIZ ====================
        composable(
            route = "quiz/{craftId}",
            arguments = listOf(navArgument("craftId") { type = NavType.LongType })
        ) { backStackEntry ->
            val craftId = backStackEntry.arguments?.getLong("craftId")
            val craft = crafts.find { it.id == craftId }
            craft?.let {
                QuizScreen(
                    craft = it,
                    langViewModel = langViewModel,
                    onBack = { navController.popBackStack() },
                    isLoggedIn = isAuthenticated,
                    userId = authViewModel.userId.value
                )
            }
        }

        // ==================== LEADERBOARD ====================
        composable("leaderboard") {
            LeaderboardScreen(
                onBack = { navController.popBackStack() },
                langViewModel = langViewModel
            )
        }
    }
}