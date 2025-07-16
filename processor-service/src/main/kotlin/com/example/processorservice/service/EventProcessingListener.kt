package com.example.processorservice.service
import com.example.processorservice.model.DataEvent
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
@Service
class EventProcessingListener(
    private val kafkaTemplate: KafkaTemplate<String, DataEvent>
) {
    @KafkaListener(topics = ["original-data-topic"], groupId = "processor-group")
    fun handleEvent(event: DataEvent) {
        println("Processing event: $event")
        val processedEvent = event.copy(
            processed = true,
            processedBy = "processor-service"
        )
        println("Publishing processed event: $processedEvent")
        kafkaTemplate.send("processed-data-topic", processedEvent.id, processedEvent)
    }
}
