package com.example.processorservice.model
data class DataEvent(
    val id: String,
    val content: String,
    val timestamp: Long,
    val processed: Boolean = false,
    val processedBy: String? = null
)
