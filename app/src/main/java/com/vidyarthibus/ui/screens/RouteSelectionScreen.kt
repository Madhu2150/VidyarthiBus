package com.vidyarthibus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vidyarthibus.data.model.BusRoute
import com.vidyarthibus.ui.theme.BrandBlue
import com.vidyarthibus.ui.theme.CrowdRed
import com.vidyarthibus.viewmodel.RouteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSelectionScreen(
    viewModel: RouteViewModel,
    onRouteSelected: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Your Route",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor       = Color(0xFF022540),
                    titleContentColor    = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.Logout, "Sign out")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // Search bar
            OutlinedTextField(
                value         = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label         = { Text("Search routes…") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                trailingIcon  = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, "Clear search")
                        }
                    }
                },
                modifier   = Modifier.fillMaxWidth(),
                singleLine = true,
                shape      = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    ErrorCard(message = uiState.errorMessage!!)
                }

                uiState.routes.isEmpty() -> {
                    // FIXED: replaced DirectionsBusFilledRounded with DirectionsBus
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text  = "No routes found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            if (uiState.searchQuery.isNotBlank()) {
                                Text(
                                    text  = "Try a different search term",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.routes,
                            key   = { it.routeId }
                        ) { route ->
                            RouteCard(
                                route   = route,
                                onClick = { onRouteSelected(route.routeId) }
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteCard(route: BusRoute, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment      = Alignment.CenterVertically,
            horizontalArrangement  = Arrangement.spacedBy(14.dp)
        ) {
            // Route number badge
            Surface(
                color = BrandBlue,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text     = route.routeNumber,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color    = Color.White,
                    style    = MaterialTheme.typography.titleLarge
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = route.routeName,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (route.stops.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text     = route.stops.take(3).joinToString(" → "),
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
                if (route.isCancelled) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = CrowdRed,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text     = "CANCELLED",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color    = Color.White,
                            style    = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Icon(
                imageVector        = Icons.Default.ChevronRight,
                contentDescription = "Open route ${route.routeNumber}",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text     = message,
            modifier = Modifier.padding(16.dp),
            color    = MaterialTheme.colorScheme.onErrorContainer,
            style    = MaterialTheme.typography.bodyMedium
        )
    }
}