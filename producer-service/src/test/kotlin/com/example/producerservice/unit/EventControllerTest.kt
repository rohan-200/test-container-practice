package com.example.producerservice.unit
import com.example.producerservice.controller.EventController
import com.example.producerservice.model.DataEvent
import com.example.producerservice.service.EventProducer
import io.mockk.*
import org.junit.jupiter.api.Test
class EventControllerTest {
    private val eventProducer: EventProducer = mockk()
    private val eventController = EventController(eventProducer)
    @Test
    fun `createEvent should call event producer`() {
        val request = EventController.CreateEventRequest("test content")
        // We need every a call to the mock
        every { eventProducer.sendEvent(any()) } just Runs
        eventController.createEvent(request)
        // Verify sendEvent was called exactly once with a matching DataEvent
        verify(exactly = 1) {
            eventProducer.sendEvent(match { it.content == "test content" })
        }
    }
}
