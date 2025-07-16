package com.example.producerservice.integration
import com.example.producerservice.model.DataEvent
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class EventProducerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    private lateinit var kafkaConsumer: Consumer<String, String>
    private val objectMapper = jacksonObjectMapper()
    companion object {
        @Container
        val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers)
        }
    }
    @BeforeEach
    fun setUp() {
        val consumerProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "test-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringDeserializer",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )
        kafkaConsumer = DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer()
        kafkaConsumer.subscribe(listOf("original-data-topic"))
    }
    @AfterEach
    fun tearDown() {
        kafkaConsumer.close()
    }
    @Test
    fun `POST events should produce a message to Kafka`() {
        // Act: Perform an HTTP POST to the controller endpoint
        mockMvc.post("/events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"content": "hello integration test"}"""
        }.andExpect {
            status { isAccepted() }
        }
        // Assert: Check that a message was received on the Kafka topic
        val record = KafkaTestUtils.getSingleRecord(kafkaConsumer, "original-data-topic", 10.seconds.toJavaDuration())
        val event = objectMapper.readValue(record.value(), DataEvent::class.java)
        assert(event.content == "hello integration test")
    }
}
