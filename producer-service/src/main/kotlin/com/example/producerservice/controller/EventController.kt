package com.example.producerservice.controller
import com.example.producerservice.model.DataEvent
import com.example.producerservice.service.EventProducer
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID
@RestController
@RequestMapping("/events")
class EventController(private val eventProducer: EventProducer) {
    data class CreateEventRequest(val content: String)
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun createEvent(@RequestBody request: CreateEventRequest) {
        val event = DataEvent(id = UUID.randomUUID().toString(), content = request.content)
        eventProducer.sendEvent(event)
    }
}