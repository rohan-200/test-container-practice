package com.example.processorservice.unit
import com.example.processorservice.model.DataEvent
import com.example.processorservice.service.EventProcessingListener
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
class EventProcessingListenerTest {
    private val kafkaTemplate: KafkaTemplate<String, DataEvent> = mockk()
    private val listener = EventProcessingListener(kafkaTemplate)
    @Test
    fun `handleEvent should process and send a new event`() {
        val originalEvent = DataEvent(id = "123", content = "original", timestamp = 1L)
        every { kafkaTemplate.send(any(), any(), any()) } returns mockk()
        listener.handleEvent(originalEvent)
        verify(exactly = 1) {
            kafkaTemplate.send(
                "processed-data-topic",
                "123",
                match { it.processed && it.processedBy == "processor-service" }
            )
        }
    }
}
