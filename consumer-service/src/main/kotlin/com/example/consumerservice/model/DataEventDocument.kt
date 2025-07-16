package com.example.consumerservice.model
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
@Document(collection = "events")
data class DataEventDocument(
    @Id val id: String,
    val content: String,
    val timestamp: Long,
    val processed: Boolean,
    val processedBy: String?
)
