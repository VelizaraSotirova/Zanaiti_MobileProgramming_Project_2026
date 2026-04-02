package bg.zanaiti.craftguide.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bg.zanaiti.craftguide.ui.CraftViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CraftViewModel,
    startDestination: String = "mode_selection",
    isLoggedIn: Boolean,
    username: String? = null,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onArScannerClick: () -> Unit // Подава се от MainActivity
) {
    val navController = rememberNavController()
    val crafts by viewModel.crafts.collectAsState()
    val context = LocalContext.current

    // Диалогови прозорци
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showExitAppDialog by remember { mutableStateOf(false) }

    // Прихващане на системния бутон "Назад"
    BackHandler(enabled = true) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute == "mode_selection") {
            showExitAppDialog = true
        } else {
            navController.popBackStack()
        }
    }

    // Следим текущия маршрут за заглавието и бутоните
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Динамично заглавие
    val topBarTitle = when {
        currentRoute == "mode_selection" -> {
            if (isLoggedIn && !username.isNullOrBlank()) "Здравей, $username!"
            else "Старинни занаяти"
        }
        currentRoute == "list" -> "Списък със занаяти"
        currentRoute == "map" -> "Карта на занаятите"
        currentRoute == "profile" -> "Моят профил"
        currentRoute?.startsWith("detail/") == true -> {
            val craftId = navBackStackEntry?.arguments?.getLong("craftId")
            crafts.find { it.id == craftId }?.translations?.get("bg")?.name ?: "Детайли"
        }
        currentRoute?.startsWith("quiz/") == true -> "Тест за занаят"
        else -> "Старинни занаяти"
    }

    // Екранна логика (Диалози)
    if (showExitAppDialog) {
        AlertDialog(
            onDismissRequest = { showExitAppDialog = false },
            title = { Text("Изход") },
            text = { Text("Сигурни ли сте, че искате да затворите приложението?") },
            confirmButton = {
                TextButton(onClick = { (context as? Activity)?.finish() }) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { showExitAppDialog = false }) { Text("Не") }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Изход от профила") },
            text = { Text("Сигурни ли сте, че искате да напуснете профила си?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogoutClick()
                }) { Text("Да", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Не") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    if (currentRoute != "mode_selection") {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                },
                actions = {
                    // Бутон за AR Скенер в TopBar
                    IconButton(onClick = onArScannerClick) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "AR Скенер")
                    }

                    if (isLoggedIn) {
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Профил")
                        }
                        IconButton(onClick = { navController.navigate("leaderboard") }) {
                            Icon(Icons.Default.Leaderboard, contentDescription = "Класация")
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Изход")
                        }
                    } else {
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Default.Login, contentDescription = "Вход")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Пулсиращ бутон за скенера на началните екрани
            if (currentRoute == "mode_selection" || currentRoute == "list") {
                FloatingActionButton(
                    onClick = onArScannerClick,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Сканирай")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("mode_selection") {
                ModeSelectionScreen(
                    onSelectList = { navController.navigate("list") },
                    onSelectMap = { navController.navigate("map") }
                )
            }

            composable("list") {
                CraftListScreen(
                    onCraftClick = { craft -> navController.navigate("detail/${craft.id}") }
                )
            }

            composable("map") {
                MapScreen(
                    onCraftClick = { craft -> navController.navigate("detail/${craft.id}") }
                )
            }

            composable(
                route = "detail/{craftId}",
                arguments = listOf(navArgument("craftId") { type = NavType.LongType })
            ) { backStackEntry ->
                val craftId = backStackEntry.arguments?.getLong("craftId")
                val craft = crafts.find { it.id == craftId }
                craft?.let {
                    CraftDetailScreen(
                        craft = it,
                        onBack = { navController.popBackStack() },
                        onShowOnMap = { navController.navigate("map_single/${it.id}") },
                        onStartQuiz = { navController.navigate("quiz/${it.id}") }
                    )
                }
            }

            composable(
                route = "map_single/{craftId}",
                arguments = listOf(navArgument("craftId") { type = NavType.LongType })
            ) { backStackEntry ->
                val craftId = backStackEntry.arguments?.getLong("craftId")
                MapScreen(
                    showOnlyCraftId = craftId,
                    onCraftClick = { craft -> navController.navigate("detail/${craft.id}") }
                )
            }

            composable(
                route = "quiz/{craftId}",
                arguments = listOf(navArgument("craftId") { type = NavType.LongType })
            ) { backStackEntry ->
                val craftId = backStackEntry.arguments?.getLong("craftId")
                val craft = crafts.find { it.id == craftId }
                craft?.let {
                    QuizScreen(
                        craft = it,
                        onBack = { navController.popBackStack() },
                        isLoggedIn = isLoggedIn,
                        userId = null
                    )
                }
            }

            composable("leaderboard") {
                LeaderboardScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}