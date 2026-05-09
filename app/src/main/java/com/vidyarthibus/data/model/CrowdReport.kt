package com.vidyarthibus.data.model

data class CrowdReport(
    val reportId: String = "",
    val routeId: String = "",
    val userId: String = "",          // hashed — no PII
    val timestamp: Long = 0L,
    // Proxy boolean — actual coords never stored (NFR-10)
    val proximityVerified: Boolean = false
)

/**
 * Aggregated view computed client-side from active reports.
 */
data class CrowdStatus(
    val routeId: String = "",
    val activeReporters: Int = 0,
    val capacity: Int = 60,           // default bus capacity
    val lastUpdated: Long = 0L
) {
    val percentage: Float
        get() = (activeReporters.toFloat() / capacity.toFloat()).coerceIn(0f, 1f)

    val level: CrowdLevel
        get() = when {
            percentage <= 0.40f -> CrowdLevel.GREEN
            percentage <= 0.70f -> CrowdLevel.YELLOW
            else               -> CrowdLevel.RED
        }
}

enum class CrowdLevel { GREEN, YELLOW, RED }