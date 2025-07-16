package com.example.consumerservice.service
import com.example.consumerservice.model.DataEventDocument
import com.example.consumerservice.repository.DataEventRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
@Service
class EventMongoConsumer(private val repository: DataEventRepository) {
    @KafkaListener(topics = ["processed-data-topic"], groupId = "consumer-group")
    fun handleEvent(event: DataEventDocument) {
        println("Saving event to MongoDB: $event")
        repository.save(event)
    }
}
