package com.vidyarthibus.data.model

data class BusRoute(
    val routeId: String = "",
    val routeName: String = "",
    val routeNumber: String = "",
    val stops: List<String> = emptyList(),
    // Centre-point coordinates for proximity check
    val centerLat: Double = 0.0,
    val centerLng: Double = 0.0,
    // Radius in metres around route polyline
    val proximityRadius: Int = 500,
    val isCancelled: Boolean = false,
    val cancellationMessage: String = ""
)