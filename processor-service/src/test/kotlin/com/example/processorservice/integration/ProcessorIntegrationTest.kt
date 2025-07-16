package com.example.processorservice.integration
import com.example.processorservice.model.DataEvent
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
@Testcontainers
@SpringBootTest
class ProcessorIntegrationTest {
    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, DataEvent>
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
        kafkaConsumer.subscribe(listOf("processed-data-topic"))
    }
    @AfterEach
    fun tearDown() {
        kafkaConsumer.close()
    }
    @Test
    fun `should consume, process, and produce an event`() {
        // Arrange
        val originalEvent = DataEvent(id = "event1", content = "needs processing", timestamp = 123L)
        // Act: Send an event to the input topic
        kafkaTemplate.send("original-data-topic", originalEvent.id, originalEvent)
        // Assert: Verify a processed event is produced to the output topic
        val record = KafkaTestUtils.getSingleRecord(kafkaConsumer, "processed-data-topic", 10.seconds.toJavaDuration())
        val processedEvent = objectMapper.readValue(record.value(), DataEvent::class.java)
        assert(processedEvent.id == "event1")
        assert(processedEvent.processed)
        assert(processedEvent.processedBy == "processor-service")
    }
}
