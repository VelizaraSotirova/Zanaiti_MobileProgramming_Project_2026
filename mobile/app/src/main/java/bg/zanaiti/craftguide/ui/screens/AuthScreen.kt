package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bg.zanaiti.craftguide.ui.AuthViewModel
import bg.zanaiti.craftguide.ui.AuthViewModelFactory
import bg.zanaiti.craftguide.ui.LanguageViewModel
import bg.zanaiti.craftguide.utils.TokenManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    tokenManager: TokenManager,
    langViewModel: LanguageViewModel, // Вече е разпознат
    onAuthSuccess: () -> Unit
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(tokenManager)
    )
    val coroutineScope = rememberCoroutineScope()
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val currentLanguage by langViewModel.currentLanguage.collectAsState()

    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }

    // Текстове за превод
    var tTitle by remember { mutableStateOf("") }
    var tUserLabel by remember { mutableStateOf("") }
    var tPassLabel by remember { mutableStateOf("") }
    var tEmailLabel by remember { mutableStateOf("") }
    var tFullNameLabel by remember { mutableStateOf("") }
    var tErrorMsg by remember { mutableStateOf("") }
    var tToggleMode by remember { mutableStateOf("") }

    // LaunchedEffect за сигурно извикване на suspend функцията translate
    LaunchedEffect(currentLanguage, isLoginMode, error) {
        tTitle = langViewModel.translate(if (isLoginMode) "Вход" else "Регистрация")
        tUserLabel = langViewModel.translate("Потребителско име")
        tPassLabel = langViewModel.translate("Парола")
        tEmailLabel = langViewModel.translate("Имейл")
        tFullNameLabel = langViewModel.translate("Пълно име")
        tToggleMode = langViewModel.translate(
            if (isLoginMode) "Нямаш акаунт? Регистрирай се" else "Вече имаш акаунт? Влез"
        )
        error?.let {
            tErrorMsg = langViewModel.translate(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tTitle) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(tUserLabel) },
                modifier = Modifier.fillMaxWidth()
            )

            if (!isLoginMode) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(tEmailLabel) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(tFullNameLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(tPassLabel) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (isLoginMode) {
                            authViewModel.login(username, password) {
                                onAuthSuccess()
                            }
                        } else {
                            authViewModel.register(username, email, password, fullName) {
                                onAuthSuccess()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(tTitle)
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tErrorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { isLoginMode = !isLoginMode }
            ) {
                Text(tToggleMode)
            }
        }
    }
}