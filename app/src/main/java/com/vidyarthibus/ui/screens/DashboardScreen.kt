package com.vidyarthibus.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vidyarthibus.data.model.CrowdLevel
import com.vidyarthibus.ui.components.CancellationBanner
import com.vidyarthibus.ui.components.CrowdMeter
import com.vidyarthibus.ui.components.ReportButton
import com.vidyarthibus.ui.theme.BrandBlue
import com.vidyarthibus.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onViewAlternatives: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showPermissionRationale by remember { mutableStateOf(false) }

    // Location permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.submitReport(context)
        } else {
            showPermissionRationale = true
        }
    }

    // Snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text  = uiState.route?.routeName ?: "Loading…",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text  = "Route ${uiState.route?.routeNumber ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlue)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // FR-09: Cancellation banner
            AnimatedVisibility(
                visible = uiState.isCancelled,
                enter   = slideInVertically(),
                exit    = slideOutVertically()
            ) {
                CancellationBanner(message = uiState.cancellationMessage)
            }

            // FR-03: Live Crowd Meter
            CrowdMeter(crowdStatus = uiState.crowdStatus)

            // Last updated info
            LastUpdatedBadge(timestamp = uiState.crowdStatus.lastUpdated)

            // FR-04: One-tap Report Button (NFR-07: ≤ 3 taps)
            ReportButton(
                hasActiveReport = uiState.hasActiveReport,
                isReporting     = uiState.isReporting,
                onReport        = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                onRemoveReport  = viewModel::removeReport
            )

            // FR-05 info text
            Text(
                text  = "📍 Location is verified but never stored.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            // FR-07: Alternatives button when full
            AnimatedVisibility(visible = uiState.crowdStatus.level == CrowdLevel.RED) {
                OutlinedButton(
                    onClick   = onViewAlternatives,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Commute, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Bus is Full — View Alternatives",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Route stops
            uiState.route?.let { route ->
                if (route.stops.isNotEmpty()) {
                    RouteStopsList(stops = route.stops)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Permission rationale dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            icon  = { Icon(Icons.Default.LocationOn, null) },
            title = { Text("Location Required") },
            text  = {
                Text(
                    "Your location is needed to verify you are near the bus route. " +
                            "It is never stored — only a yes/no check is performed."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) { Text("Grant Permission") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LastUpdatedBadge(timestamp: Long) {
    if (timestamp == 0L) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text  = "Live  ·  ${java.text.SimpleDateFormat("HH:mm:ss").format(timestamp)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = androidx.compose.ui.unit.TextUnit(
                    11f, androidx.compose.ui.unit.TextUnitType.Sp
                )
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun RouteStopsList(stops: List<String>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = "Route Stops",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            stops.forEachIndexed { index, stop ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color    = BrandBlue,
                        shape    = RoundedCornerShape(50),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "${index + 1}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    Text(
                        text  = stop,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (index < stops.lastIndex) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .padding(start = 11.dp)
                            .width(2.dp)
                            .height(16.dp)
                    ) {
                        Divider(
                            color = BrandBlue.copy(alpha = 0.3f),
                            thickness = 2.dp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}