package com.vidyarthibus.data.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val selectedRoute: String = "",
    val createdAt: Long = 0L
)