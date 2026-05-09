package com.vidyarthibus.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidyarthibus.data.model.*
import com.vidyarthibus.data.repository.CrowdRepository
import com.vidyarthibus.data.repository.RouteRepository
import com.vidyarthibus.service.LocationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val route: BusRoute? = null,
    val crowdStatus: CrowdStatus = CrowdStatus(),
    val alternatives: List<SharedAutoContact> = emptyList(),
    val isLoading: Boolean = true,
    val isReporting: Boolean = false,
    val hasActiveReport: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isCancelled: Boolean = false,
    val cancellationMessage: String = ""
)

class DashboardViewModel(
    private val routeId: String,
    private val crowdRepository: CrowdRepository,
    private val routeRepository: RouteRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _isReporting = MutableStateFlow(false)
    private val _hasActiveReport = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _successMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        routeRepository.observeRoute(routeId),
        crowdRepository.observeCrowdStatus(routeId),
        crowdRepository.observeAlternatives(routeId),
        _isReporting,
        _hasActiveReport,
        _errorMessage,
        _successMessage
    ) { values ->
        val route = values[0] as? BusRoute
        val crowd = values[1] as CrowdStatus
        val alts = values[2] as List<*>
        val reporting = values[3] as Boolean
        val hasReport = values[4] as Boolean
        val error = values[5] as? String
        val success = values[6] as? String

        DashboardUiState(
            route = route,
            crowdStatus = crowd,
            alternatives = alts.filterIsInstance<SharedAutoContact>(),
            isLoading = false,
            isReporting = reporting,
            hasActiveReport = hasReport,
            errorMessage = error,
            successMessage = success,
            isCancelled = route?.isCancelled ?: false,
            cancellationMessage = route?.cancellationMessage ?: ""
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardUiState()
    )

    /** FR-04 + FR-05: One-tap report with GPS proximity gate */
    fun submitReport(context: Context) {
        viewModelScope.launch {
            _isReporting.value = true
            _errorMessage.value = null

            // FR-05: Check location permission & proximity
            if (!locationService.hasLocationPermission()) {
                _errorMessage.value = "Location permission is required to report crowd status."
                _isReporting.value = false
                return@launch
            }

            val route = uiState.value.route
            if (route == null) {
                _errorMessage.value = "Route data not available. Please try again."
                _isReporting.value = false
                return@launch
            }

            val isNear = locationService.isNearRoute(route)

            val result = crowdRepository.submitReport(
                routeId = routeId,
                proximityVerified = isNear
            )

            if (result.isSuccess) {
                _hasActiveReport.value = true
                _successMessage.value = "Report submitted! Your data helps fellow students."
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
            _isReporting.value = false
        }
    }

    /** Remove own report when user gets off the bus */
    fun removeReport() {
        viewModelScope.launch {
            crowdRepository.removeReport(routeId)
            _hasActiveReport.value = false
            _successMessage.value = "Report removed."
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}