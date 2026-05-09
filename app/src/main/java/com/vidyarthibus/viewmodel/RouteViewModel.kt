package com.vidyarthibus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidyarthibus.data.model.BusRoute
import com.vidyarthibus.data.repository.RouteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RouteUiState(
    val routes: List<BusRoute> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class RouteViewModel(
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RouteUiState> = combine(
        routeRepository.observeAllRoutes()
            .onStart { _isLoading.value = true }
            .onEach { _isLoading.value = false }
            .catch { e ->
                _errorMessage.value = e.message
                _isLoading.value = false
                emit(emptyList())
            },
        _searchQuery,
        _isLoading,
        _errorMessage
    ) { routes, query, loading, error ->
        val filtered = if (query.isBlank()) routes
        else routes.filter {
            it.routeName.contains(query, ignoreCase = true) ||
                    it.routeNumber.contains(query, ignoreCase = true)
        }
        RouteUiState(
            routes = filtered,
            isLoading = loading,
            searchQuery = query,
            errorMessage = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RouteUiState())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}