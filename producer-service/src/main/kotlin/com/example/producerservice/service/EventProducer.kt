package com.example.producerservice.service
import com.example.producerservice.model.DataEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
@Service
class EventProducer(private val kafkaTemplate: KafkaTemplate<String, DataEvent>) {
    fun sendEvent(event: DataEvent) {
        println("Producing event: $event")
        kafkaTemplate.send("original-data-topic", event.id, event)
    }
}