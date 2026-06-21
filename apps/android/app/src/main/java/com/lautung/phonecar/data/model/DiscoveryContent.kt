package com.lautung.phonecar.data.model

data class DiscoveryContent(
    val id: String,
    val category: String,
    val title: String,
    val summary: String,
    val mediaId: String?,
    val mediaUrl: String?,
    val followed: Boolean,
)
