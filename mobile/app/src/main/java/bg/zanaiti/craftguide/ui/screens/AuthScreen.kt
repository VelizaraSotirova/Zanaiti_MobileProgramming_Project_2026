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
import bg.zanaiti.craftguide.utils.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    tokenManager: TokenManager,
    onAuthSuccess: () -> Unit
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(tokenManager)
    )
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()

    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLoginMode) "Вход" else "Регистрация") }
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
                label = { Text("Потребителско име") },
                modifier = Modifier.fillMaxWidth()
            )

            if (!isLoginMode) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Имейл") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Пълно име") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Парола") },
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
                    Text(if (isLoginMode) "Вход" else "Регистрация")
                }
            }

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { isLoginMode = !isLoginMode }
            ) {
                Text(if (isLoginMode) "Нямаш акаунт? Регистрирай се" else "Вече имаш акаунт? Влез")
            }
        }
    }
}