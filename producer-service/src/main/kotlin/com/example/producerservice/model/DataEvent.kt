package com.example.producerservice.model
data class DataEvent(
    val id: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)