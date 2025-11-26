package com.halicare.halicare.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun SignUpScreen(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()
) {
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var phonenumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsChecked by remember { mutableStateOf(false) }
    var termsError by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }

    val signUpState by authViewModel.signUpState.collectAsState()

    LaunchedEffect(signUpState) {
        val currentState = signUpState
        when (currentState) {
            is AuthViewModel.SignUpUiState.Success -> {
                dialogTitle = "Success"
                dialogMessage = "Registration successful!"
                showDialog = true
            }
            is AuthViewModel.SignUpUiState.Error -> {
                dialogTitle = "Registration Failed"
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
                    .height(70.dp)
                    .background(
                        Color(0xFF001E6B),
                        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign Up",
                    color = Color.White,
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "First Name",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001E6B),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, end = 16.dp)
            )
            OutlinedTextField(
                value = firstname,
                onValueChange = {
                    firstname = it
                    if (termsError.isNotEmpty()) termsError = ""
                },
                label = { Text("Enter first name", fontStyle = FontStyle.Italic) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                isError = firstname.isBlank() && termsError.isNotEmpty(),
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

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Last Name",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001E6B),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, end = 16.dp)
            )
            OutlinedTextField(
                value = lastname,
                onValueChange = {
                    lastname = it
                    if (termsError.isNotEmpty()) termsError = ""
                },
                label = { Text("Enter last name", fontStyle = FontStyle.Italic) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                isError = lastname.isBlank() && termsError.isNotEmpty(),
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

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Phone Number",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001E6B),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, end = 16.dp)
            )
            OutlinedTextField(
                value = phonenumber,
                onValueChange = {
                    phonenumber = it
                    if (termsError.isNotEmpty()) termsError = ""
                },
                label = { Text("Enter phone number", fontStyle = FontStyle.Italic) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phonenumber.isBlank() && termsError.isNotEmpty(),
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

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001E6B),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (termsError.isNotEmpty()) termsError = ""
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
                isError = (password.isBlank() || (password != confirmPassword)) && termsError.isNotEmpty(),
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

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Confirm Password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001E6B),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (termsError.isNotEmpty()) termsError = ""
                },
                label = { Text("Confirm password", fontStyle = FontStyle.Italic) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (confirmPasswordVisible) "Hide confirm password" else "Show confirm password"
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                isError = (confirmPassword.isBlank() || (password != confirmPassword)) && termsError.isNotEmpty(),
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

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 7.dp, start = 6.dp)
            ) {
                Checkbox(
                    checked = termsChecked,
                    onCheckedChange = {
                        termsChecked = it
                        if (it) termsError = ""
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF001E6B),
                        uncheckedColor = Color(0xFF001E6B),
                        checkmarkColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I agree to the Terms and Conditions",
                    modifier = Modifier.clickable {
                        showTermsDialog = true
                    },
                    color = Color(0xFF000080),
                    fontWeight = FontWeight.Bold
                )
            }

            if (termsError.isNotEmpty()) {
                Text(
                    text = termsError,
                    color = Color.Red,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (!termsChecked) {
                        termsError = "You must agree to the Terms and Conditions"
                    } else if (firstname.isBlank()
                        || lastname.isBlank()
                        || phonenumber.isBlank()
                        || password.isBlank()
                    ) {
                        termsError = "All fields are required"
                    } else if (password != confirmPassword) {
                        termsError = "Passwords do not match"
                    } else {
                        termsError = ""
                        authViewModel.registerUser(
                            firstname,
                            lastname,
                            phonenumber,
                            password,
                            confirmPassword
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001E6B)),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .height(50.dp)
                    .width(280.dp)
            ) {
                if (signUpState is AuthViewModel.SignUpUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(text = "Sign Up", color = Color.White, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account? ", color = Color(0xFF000080))
                TextButton(onClick = onSignInClick) {
                    Text("Sign In", color = Color(0xFF000080), textDecoration = TextDecoration.Underline)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                            onSignUpClick()
                        }
                    }) {
                        Text("OK", fontWeight = FontWeight.Bold, color = Color(0xFF000080))
                    }
                },
                containerColor = Color.White
            )
        }
        if (showTermsDialog) {
            TermsAndConditionsDialog(onDismiss = { showTermsDialog = false })
        }
    }
}




