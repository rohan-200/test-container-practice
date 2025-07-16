package com.example.consumerservice.unit
import com.example.consumerservice.model.DataEventDocument
import com.example.consumerservice.repository.DataEventRepository
import com.example.consumerservice.service.EventMongoConsumer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
class EventMongoConsumerTest {
    private val repository: DataEventRepository = mockk()
    private val consumer = EventMongoConsumer(repository)
    @Test
    fun `handleEvent should save the event to repository`() {
        val event = DataEventDocument("1", "data", 1L, true, "proc")
        every { repository.save(any()) } returns event
        consumer.handleEvent(event)
        verify(exactly = 1) { repository.save(event) }
    }
}
