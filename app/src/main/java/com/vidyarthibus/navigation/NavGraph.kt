package com.vidyarthibus.navigation

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vidyarthibus.data.repository.*
import com.vidyarthibus.service.LocationService
import com.vidyarthibus.ui.screens.*
import com.vidyarthibus.viewmodel.*

object Routes {
    const val LOGIN        = "login"
    const val ROUTE_SELECT = "route_select"
    const val DASHBOARD    = "dashboard/{routeId}"
    const val ALTERNATIVES = "alternatives/{routeId}"

    fun dashboard(routeId: String)    = "dashboard/$routeId"
    fun alternatives(routeId: String) = "alternatives/$routeId"
}

@Composable
fun VidyarthiBusNavGraph(context: Context) {
    val navController = rememberNavController()

    // Firebase instances
    val auth     = remember { FirebaseAuth.getInstance() }
    val database = remember { FirebaseDatabase.getInstance() }

    // Repositories
    val authRepo  = remember { AuthRepository(auth) }
    val routeRepo = remember { RouteRepository(database) }
    val crowdRepo = remember { CrowdRepository(database, auth) }

    // ViewModels
    val authViewModel = remember {
        AuthViewModel(authRepo)
    }
    val routeViewModel = remember {
        RouteViewModel(routeRepo)
    }

    val startDestination = if (auth.currentUser != null)
        Routes.ROUTE_SELECT else Routes.LOGIN

    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {

        // Login Screen
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel     = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Routes.ROUTE_SELECT) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Route Selection Screen
        composable(Routes.ROUTE_SELECT) {
            RouteSelectionScreen(
                viewModel       = routeViewModel,
                onRouteSelected = { routeId ->
                    navController.navigate(Routes.dashboard(routeId))
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard Screen
        composable(
            route     = Routes.DASHBOARD,
            arguments = listOf(
                navArgument("routeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments
                ?.getString("routeId") ?: return@composable

            val dashboardViewModel = remember(routeId) {
                DashboardViewModel(
                    routeId         = routeId,
                    crowdRepository = crowdRepo,
                    routeRepository = routeRepo,
                    locationService = LocationService(context)
                )
            }

            DashboardScreen(
                viewModel          = dashboardViewModel,
                onBack             = { navController.popBackStack() },
                onViewAlternatives = {
                    navController.navigate(Routes.alternatives(routeId))
                }
            )
        }

        // Alternatives Screen
        composable(
            route     = Routes.ALTERNATIVES,
            arguments = listOf(
                navArgument("routeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments
                ?.getString("routeId") ?: return@composable

            val dashboardViewModel = remember(routeId) {
                DashboardViewModel(
                    routeId         = routeId,
                    crowdRepository = crowdRepo,
                    routeRepository = routeRepo,
                    locationService = LocationService(context)
                )
            }

            AlternativesScreen(
                viewModel = dashboardViewModel,
                onBack    = { navController.popBackStack() }
            )
        }
    }
}