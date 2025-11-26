package com.halicare.halicare.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halicare.halicare.viewModel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(
    onSignUpClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()
) {
    var phonenumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }


    val loginState by authViewModel.loginState.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }

    LaunchedEffect(loginState) {
        val currentState = loginState
        when (currentState) {
            is AuthViewModel.LoginUiState.Success -> {
                val response = currentState.response
                val prefs = context.getSharedPreferences("HALICARE_PREFS", android.content.Context.MODE_PRIVATE)
                with(prefs.edit()) {
                    putString("ACCESS_TOKEN", response.token)
                    putString("USER_ID", response.userId)
                    putString("USER_TYPE", response.userType)
                    putString("FIRST_NAME", response.firstName)
                    putString("LAST_NAME", response.lastName)
                    putString("PHONE_NUMBER", response.phoneNumber)
                    apply()
                }
                dialogTitle = "Success"
                dialogMessage = "Login successful!"
                showDialog = true
            }
            is AuthViewModel.LoginUiState.Error -> {
                dialogTitle = "Login Failed"
                dialogMessage = currentState.errorMessage
                showDialog = true
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF001E6B), shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign In",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Phone Number",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001E6B),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, end = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = phonenumber,
                onValueChange = {
                    phonenumber = it
                    if (errorText.isNotEmpty()) errorText = ""
                },
                label = { Text("Enter phone number", fontStyle = FontStyle.Italic) },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
                    .padding(start = 16.dp, end = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                  isError = errorText.contains("Phone number", ignoreCase = true),
                supportingText = {
                    if (errorText.contains("Phone number", ignoreCase = true)) {
                        Text(
                            text = errorText,
                            color = Color.Red
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF001E6B),
                    unfocusedIndicatorColor = Color.Gray,
                    errorIndicatorColor = Color.Red,
                    errorTextColor = Color.Black,
                    errorContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001E6B),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
                    .padding(start = 16.dp, end = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (errorText.isNotEmpty()) errorText = ""
                },
                label = { Text("Enter Password", fontStyle = FontStyle.Italic) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                isError = errorText.contains("Password", ignoreCase = true),
                supportingText = {
                    if (errorText.contains("Password", ignoreCase = true)) {
                        Text(
                            text = errorText,
                            color = Color.Red
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF001E6B),
                    unfocusedIndicatorColor = Color.Gray,
                    errorIndicatorColor = Color.Red,
                    errorTextColor = Color.Black,
                    errorContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
               onClick = {
                    if (phonenumber.isBlank()) {
                        errorText = "Phone number is required"
                    } else if (password.isBlank()) {
                        errorText = "Password is required"
                    } else {
                        errorText = ""
                        authViewModel.loginUser(phonenumber, password)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001E6B)),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .height(50.dp)
                    .width(280.dp)
            ) {
                if (loginState is AuthViewModel.LoginUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(text = "Sign In", color = Color.White, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account? ", color = Color(0xFF000080))
                TextButton(onClick = onSignUpClick) {
                    Text("Sign Up", color = Color(0xFF000080),textDecoration = TextDecoration.Underline)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(dialogTitle, fontWeight = FontWeight.Bold, color = Color(0xFF000080)) },
                text = { Text(dialogMessage) },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        if (dialogTitle == "Success") {
                            onLoginSuccess()
                        }
                    }) {
                        Text("OK", fontWeight = FontWeight.Bold, color = Color(0xFF000080))
                    }
                },
                containerColor = Color.White
            )
        }

    }
}





