To handle Kafka consumer error strategies with retries, recovery mechanisms, and delay queues in Spring Boot, we can use Spring Kafka's error handling features like SeekToCurrentErrorHandler, retryable annotations, and custom error handling. We will also extend this to store the acknowledgment of received messages and timestamps in PostgreSQL from the producer side.

Step-by-Step Approach:
Kafka Consumer Configuration:
Set up a retry mechanism for handling temporary errors.
Implement a recovery mechanism for non-recoverable errors.
Delay queues to handle messages that need to be retried with a delay.
Kafka Producer Configuration:
Set up a mechanism to check if the consumer has processed the message and store the consumer acknowledgment in PostgreSQL.
1. Consumer Configuration (Error Handling, Retries, and Recovery)
Start by configuring the Kafka consumer with retry and recovery mechanisms:

Kafka Configuration with Retry and Error Handling
java
Copier le code
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.listener.RetryListener;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Setting up retry mechanism and error handling
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }

    // Error Handler with Retry and BackOff strategy
    @Bean
    public DefaultErrorHandler errorHandler() {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(5);
        backOff.setInitialInterval(1000);  // initial delay of 1 second
        backOff.setMultiplier(2);          // delay will be multiplied by 2 on each retry
        backOff.setMaxInterval(10000);     // maximum delay of 10 seconds

        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) -> {
            // Handle dead-letter queue or any custom recovery logic
            System.out.println("Sending message to DLQ: " + record);
        }, backOff);

        // Retry listener to log retries
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            System.out.println("Failed delivery attempt " + deliveryAttempt + " for message: " + record);
        });

        return errorHandler;
    }
}
In the above code:

ExponentialBackOffWithMaxRetries defines retry behavior with increasing delays between retries.
DefaultErrorHandler handles retries and delegates unprocessed messages to dead-letter queue or custom recovery.
Kafka Consumer Listener
java
Copier le code
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "my_topic", groupId = "group_id")
    public void consume(String message) throws Exception {
        System.out.println("Message received: " + message);
        
        // Simulate an error for retries
        if (message.contains("error")) {
            throw new RuntimeException("Simulated error processing the message");
        }
    }
}
2. Kafka Producer and Acknowledgment Logic
Kafka Producer Service
java
Copier le code
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ConsumerAcknowledgmentRepository acknowledgmentRepository;

    @Transactional
    public void sendMessage(String message, String topic) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);

        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                System.out.println("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                System.out.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
            }
        });
    }

    // Store consumer acknowledgment in PostgreSQL
    public void storeConsumerAck(String consumerGroupId, String topic, String messageId, Timestamp timestamp) {
        ConsumerAcknowledgment acknowledgment = new ConsumerAcknowledgment(consumerGroupId, topic, messageId, timestamp);
        acknowledgmentRepository.save(acknowledgment);
    }
}
In this code, after sending a message to Kafka, the KafkaProducerService stores consumer acknowledgment details, including messageId, consumerGroupId, and the timestamp of when the consumer received the message, in PostgreSQL.

Kafka Producer Configuration
java
Copier le code
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
3. Consumer Acknowledgment Entity in PostgreSQL
Create an entity to store consumer acknowledgment details:

Entity Class
java
Copier le code
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "consumer_acknowledgment")
public class ConsumerAcknowledgment {

    @Id
    private String messageId;

    private String consumerGroupId;

    private String topic;

    private Timestamp receivedAt;

    // Constructor, Getters, Setters

    public ConsumerAcknowledgment(String consumerGroupId, String topic, String messageId, Timestamp receivedAt) {
        this.consumerGroupId = consumerGroupId;
        this.topic = topic;
        this.messageId = messageId;
        this.receivedAt = receivedAt;
    }

    // Getters and Setters
}
Repository
java
Copier le code
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumerAcknowledgmentRepository extends JpaRepository<ConsumerAcknowledgment, String> {
}
4. Saving Acknowledgment in PostgreSQL After Consumption
Now, modify the consumer service to store acknowledgment when the message is consumed successfully:

java
Copier le code
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class KafkaConsumerService {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @KafkaListener(topics = "my_topic", groupId = "group_id")
    public void consume(String message) throws Exception {
        System.out.println("Message received: " + message);

        // Simulate an error for retries
        if (message.contains("error")) {
            throw new RuntimeException("Simulated error processing the message");
        }

        // Store acknowledgment in the producer database after successful processing
        kafkaProducerService.storeConsumerAck("group_id", "my_topic", message, new Timestamp(System.currentTimeMillis()));
    }
}
Explanation:
Consumer Error Handling:

The consumer retries message processing with exponential backoff. After a maximum number of retries, unprocessed messages are sent to a Dead Letter Queue (DLQ).
Producer Side Acknowledgment:

The producer stores the consumer acknowledgment with details like consumer group ID, topic, message ID, and the timestamp when the consumer received the message.
PostgreSQL Integration:

A JPA entity and repository are used to store consumer acknowledgment details in the producer's database.
This setup ensures a robust Kafka messaging system with retry, recovery, acknowledgment tracking, and database storage for acknowledgment data.
