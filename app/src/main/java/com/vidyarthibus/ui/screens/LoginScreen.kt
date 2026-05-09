package com.vidyarthibus.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidyarthibus.ui.theme.*
import com.vidyarthibus.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Navigate when login is successful
    LaunchedEffect(uiState.user) {
        if (uiState.user != null) onAuthSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF011C31),
                        Color(0xFF022E50),
                        Color(0xFF011C31)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(40.dp))

            // App Logo — using Surface + Icon (no file loading)
            Surface(
                modifier  = Modifier.size(100.dp),
                color     = BrandBlue,
                shape     = RoundedCornerShape(24.dp),
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Default.DirectionsBus,
                        contentDescription = "Vidyarthi-Bus Logo",
                        tint     = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            // App Name
            Text(
                text       = "Vidyarthi-Bus",
                style      = MaterialTheme.typography.headlineLarge,
                color      = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text      = "Crowdsourced Bus Alert System",
                style     = MaterialTheme.typography.bodyMedium,
                color     = OnSurfaceDim,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Form Card
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text       = if (uiState.isRegistering) "Create Account" else "Sign In",
                        style      = MaterialTheme.typography.headlineMedium,
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Email Field
                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        label         = { Text("College Email") },
                        leadingIcon   = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = BrandBlueLight
                            )
                        },
                        singleLine      = true,
                        modifier        = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction    = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape  = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = BrandBlueLight,
                            unfocusedBorderColor = OnSurfaceDim,
                            focusedLabelColor    = BrandBlueLight,
                            unfocusedLabelColor  = OnSurfaceDim,
                            focusedTextColor     = Color.White,
                            unfocusedTextColor   = OnSurface,
                            cursorColor          = BrandBlueLight
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value         = password,
                        onValueChange = { password = it },
                        label         = { Text("Password") },
                        leadingIcon   = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = BrandBlueLight
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible }
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = OnSurfaceDim
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        singleLine      = true,
                        modifier        = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (uiState.isRegistering)
                                    viewModel.register(email, password)
                                else
                                    viewModel.signIn(email, password)
                            }
                        ),
                        shape  = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = BrandBlueLight,
                            unfocusedBorderColor = OnSurfaceDim,
                            focusedLabelColor    = BrandBlueLight,
                            unfocusedLabelColor  = OnSurfaceDim,
                            focusedTextColor     = Color.White,
                            unfocusedTextColor   = OnSurface,
                            cursorColor          = BrandBlueLight
                        )
                    )

                    // Error Message
                    AnimatedVisibility(visible = uiState.errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = CrowdRed.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text     = uiState.errorMessage ?: "",
                                modifier = Modifier.padding(12.dp),
                                color    = CrowdRed,
                                style    = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Sign In / Register Button
                    Button(
                        onClick = {
                            if (uiState.isRegistering)
                                viewModel.register(email, password)
                            else
                                viewModel.signIn(email, password)
                        },
                        enabled  = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlue,
                            contentColor   = Color.White
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(22.dp),
                                color       = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text       = if (uiState.isRegistering) "Register" else "Sign In",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Toggle Login / Register
                    TextButton(
                        onClick  = viewModel::toggleMode,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text  = if (uiState.isRegistering)
                                "Already have an account? Sign In"
                            else
                                "New student? Register here",
                            color = BrandBlueLight,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}