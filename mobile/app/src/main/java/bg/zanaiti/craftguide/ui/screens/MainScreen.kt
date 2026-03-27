package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bg.zanaiti.craftguide.ui.CraftViewModel
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CraftViewModel,
    startDestination: String = "mode_selection",
    isLoggedIn: Boolean,
    username: String? = null,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val navController = rememberNavController()
    val crafts by viewModel.crafts.collectAsState()

    // Диалогов прозорец при напускане
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Диалогов прозорец при напускане на приложението
    var showExitAppDialog by remember { mutableStateOf(false) }
    // Вземаме контекста, за да можем да затворим Activity-то
    val context = LocalContext.current

    // Прихващане на системния бутон "Назад"
    BackHandler(enabled = true) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        // Ако сме на началния екран, показваме диалога за изход
        if (currentRoute == "mode_selection") {
            showExitAppDialog = true
        } else {
            // Ако сме навътре в приложението, просто се връщаме назад
            navController.popBackStack()
        }
    }

    // Диалог за изход от приложението
    if (showExitAppDialog) {
        AlertDialog(
            onDismissRequest = { showExitAppDialog = false },
            title = { Text("Изход") },
            text = { Text("Сигурни ли сте, че искате да затворите приложението?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitAppDialog = false
                    // Затваряме цялото Activity и приложението
                    (context as? android.app.Activity)?.finish()
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitAppDialog = false }) {
                    Text("Не")
                }
            }
        )
    }

    // Следим текущия "гръбнак" на навигацията
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Динамично определяне на заглавието
    val topBarTitle = when {
        currentRoute == "mode_selection" -> {
            if (isLoggedIn && !username.isNullOrBlank()) "Здравей, $username!"
            else "Старинни занаяти"
        }
        currentRoute == "list" -> "Списък със занаяти"
        currentRoute == "map" -> "Карта на занаятите"
        currentRoute == "profile" -> "Моят профил"

        // Логика за заглавие с името на занаята в "Detail" екрана
        currentRoute?.startsWith("detail/") == true -> {
            val craftId = navBackStackEntry?.arguments?.getLong("craftId")
            val craftName = crafts.find { it.id == craftId }?.translations?.get("bg")?.name
            craftName ?: "Детайли"
        }

        // Логика за заглавие в "Quiz" екрана
        currentRoute?.startsWith("quiz/") == true -> {
            val craftId = navBackStackEntry?.arguments?.getLong("craftId")
            val craftName = crafts.find { it.id == craftId }?.translations?.get("bg")?.name
            if (craftName != null) "Тест: $craftName" else "Тест"
        }

        else -> "Старинни занаяти"
    }

    // Дефинирам какво ще прави самия диалогов прозорец
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Изход") },
            text = { Text("Сигурни ли сте, че искате да напуснете профила си?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick() // Извикваме реалната функция за изход
                    }
                ) {
                    Text("Да", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Не")
                }
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
                    // Показваме стрелка само ако не сме на началния екран
                    if (currentRoute != "mode_selection") {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                },
                actions = {
                    if (isLoggedIn) {
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Профил")
                        }
                        IconButton(onClick = {
                            navController.navigate("leaderboard")
                        }) {
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
                    onCraftClick = { craft ->
                        navController.navigate("detail/${craft.id}")
                    }
                )
            }

            composable("map") {
                MapScreen(
                    onCraftClick = { craft ->
                        navController.navigate("detail/${craft.id}")
                    }
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
                    onCraftClick = { craft ->
                        navController.navigate("detail/${craft.id}")
                    }
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
                        userId = null // Тук може да се подаде реалното ID, ако се пази в AuthViewModel
                    )
                }
            }

            composable("leaderboard") {
                LeaderboardScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}