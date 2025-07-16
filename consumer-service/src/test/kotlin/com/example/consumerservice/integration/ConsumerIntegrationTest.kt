package com.example.consumerservice.integration

import com.example.consumerservice.model.DataEventDocument
import com.example.consumerservice.repository.DataEventRepository
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration // Import java.time.Duration

@Testcontainers
@SpringBootTest
class ConsumerIntegrationTest {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, DataEventDocument>

    @Autowired
    private lateinit var repository: DataEventRepository

    companion object {
        @Container
        val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))

        @Container
        val mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:7.0"))

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers)
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
        }
    }

    @Test
    fun `should consume message and save to mongodb`() {
        // Arrange
        val eventToProduce = DataEventDocument(
            id = "mongo-test-123",
            content = "final data",
            timestamp = System.currentTimeMillis(),
            processed = true,
            processedBy = "processor-service"
        )

        // Act: Produce a message to the Kafka topic
        kafkaTemplate.send("processed-data-topic", eventToProduce.id, eventToProduce)

        // Assert: Verify that the data appears in MongoDB asynchronously
        // CORRECTED LINE: Removed the explicit 'infix' keyword
        await atMost Duration.ofSeconds(6) until {
            repository.findById("mongo-test-123").isPresent
        }

        val savedDocument = repository.findById("mongo-test-123").get()
        assert(savedDocument.content == "final data")
        assert(savedDocument.processed)
    }
}