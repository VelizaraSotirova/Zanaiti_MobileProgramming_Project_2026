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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bg.zanaiti.craftguide.ui.CraftViewModel
import bg.zanaiti.craftguide.ui.LanguageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CraftViewModel,
    langViewModel: LanguageViewModel,
    startDestination: String = "mode_selection",
    isLoggedIn: Boolean,
    username: String? = null,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onArScannerClick: () -> Unit
) {
    val navController = rememberNavController()
    val crafts by viewModel.crafts.collectAsState()
    val context = LocalContext.current
    val currentLanguage by langViewModel.currentLanguage.collectAsState()

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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- ЛОГИКА ЗА ПРЕВОД НА ДИНАМИЧНОТО ЗАГЛАВИЕ ---
    val rawTitle = when {
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
        currentRoute == "leaderboard" -> "Класация"
        else -> "Старинни занаяти"
    }

    var translatedTitle by remember { mutableStateOf(rawTitle) }
    LaunchedEffect(currentLanguage, rawTitle) {
        translatedTitle = langViewModel.translate(rawTitle)
    }

    // --- ПРЕВОД НА ТЕКСТОВЕ В ДИАЛОЗИТЕ ---
    var exitTitle by remember { mutableStateOf("Изход") }
    var exitMsg by remember { mutableStateOf("Сигурни ли сте, че искате да затворите приложението?") }
    var logoutTitle by remember { mutableStateOf("Изход от профила") }
    var yesText by remember { mutableStateOf("Да") }
    var noText by remember { mutableStateOf("Не") }

    LaunchedEffect(currentLanguage) {
        exitTitle = langViewModel.translate("Изход")
        exitMsg = langViewModel.translate("Сигурни ли сте, че искате да затворите приложението?")
        logoutTitle = langViewModel.translate("Изход от профила")
        yesText = langViewModel.translate("Да")
        noText = langViewModel.translate("Не")
    }

    if (showExitAppDialog) {
        AlertDialog(
            onDismissRequest = { showExitAppDialog = false },
            title = { Text(exitTitle) },
            text = { Text(exitMsg) },
            confirmButton = {
                TextButton(onClick = { (context as? Activity)?.finish() }) { Text(yesText) }
            },
            dismissButton = {
                TextButton(onClick = { showExitAppDialog = false }) { Text(noText) }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(logoutTitle) },
            text = { Text(exitMsg) }, // Използваме същия въпрос за потвърждение
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogoutClick()
                }) { Text(yesText, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text(noText) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(translatedTitle) },
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
                    LanguageSelector(langViewModel)

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
                    langViewModel = langViewModel,
                    onSelectList = { navController.navigate("list") },
                    onSelectMap = { navController.navigate("map") }
                )
            }

            composable("list") {
                CraftListScreen(
                    langViewModel = langViewModel,
                    onCraftClick = { craft -> navController.navigate("detail/${craft.id}") }
                )
            }

            composable("map") {
                MapScreen(
                    langViewModel = langViewModel,
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
                        langViewModel = langViewModel,
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
                    langViewModel = langViewModel,
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
                        langViewModel = langViewModel,
                        onBack = { navController.popBackStack() },
                        isLoggedIn = isLoggedIn,
                        userId = null
                    )
                }
            }

            composable("leaderboard") {
                LeaderboardScreen(
                    onBack = { navController.popBackStack() },
                    langViewModel = langViewModel
                )
            }
        }
    }
}

@Composable
fun LanguageSelector(langViewModel: LanguageViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val currentLanguage by langViewModel.currentLanguage.collectAsState()

    // Дефинираме поддържаните езици
    val languages = listOf(
        "bg" to "Български",
        "en" to "English",
        "de" to "Deutsch"
    )

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Language, // Икона кълбо
                contentDescription = "Смяна на езика"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        langViewModel.setLanguage(code)
                        expanded = false
                    },
                    leadingIcon = {
                        if (currentLanguage == code) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}