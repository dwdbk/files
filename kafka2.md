To handle Kafka consumer error strategies for retry, recovery mechanisms, and delay queues in Spring Boot, combined with a producer acknowledgment mechanism and storing received timestamps in PostgreSQL, you can structure the code in multiple steps:

Key components:
Producer that sends messages to Kafka topics.
Consumer that consumes messages and applies retry, recovery, and delay queue mechanisms.
Database (PostgreSQL) to store the received timestamp of a message in case of successful consumption.
Here’s a structured code example for both producer and consumer sides:

1. Producer Code (with Acknowledgment)
The producer sends a message to a Kafka topic and verifies that the consumer has acknowledged receipt.
java
Copier le code
@Service
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public CompletableFuture<SendResult<String, String>> sendMessage(String topic, String key, String message) {
        logger.info(String.format("Producing message -> %s", message));
        return kafkaTemplate.send(topic, key, message)
            .completable()
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Message [{}] delivered with offset {}", message, result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to deliver message [{}]: {}", message, ex.getMessage());
                }
            });
    }
}
2. Consumer with Retry and Recovery Mechanism
The consumer needs:

Retry mechanism: Retry messages in case of failures (e.g., transient errors).
Recovery mechanism: If retries fail, it should send the message to a dead letter topic or perform a fallback action.
Delay queues: These can be implemented via a retry topic pattern where messages are sent with delays in case of errors.
Configuration for Kafka with Retry and Dead Letter Topic:

java
Copier le code
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // Add Retry & Recovery capabilities
        factory.setErrorHandler(new SeekToCurrentErrorHandler(deadLetterPublishingRecoverer(), new FixedBackOff(1000L, 3))); // Retry 3 times with 1 sec delay
        return factory;
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer() {
        return new DeadLetterPublishingRecoverer(kafkaTemplate()); // For handling dead letters (unprocessed messages)
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }
}
3. Consumer Logic with Retry, Delay, and PostgreSQL Integration
The consumer retries the message processing and, upon success, stores the message timestamp in PostgreSQL.

java
Copier le code
@Service
public class KafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private MessageRepository messageRepository;

    @KafkaListener(topics = "my_topic", groupId = "group_id")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            // Simulate processing
            logger.info(String.format("Consumed message -> %s", record.value()));

            // Save the received message with a timestamp to PostgreSQL
            MessageEntity messageEntity = new MessageEntity();
            messageEntity.setMessage(record.value());
            messageEntity.setReceivedAt(LocalDateTime.now());
            messageRepository.save(messageEntity);

            acknowledgment.acknowledge();  // Acknowledge the message after successful processing
        } catch (Exception e) {
            logger.error("Error processing message [{}]: {}", record.value(), e.getMessage());
            // Message will be retried due to SeekToCurrentErrorHandler configured
            throw e;  // Throw exception to trigger retries or dead letter mechanism
        }
    }
}
4. Entity and Repository for PostgreSQL
You need a JPA entity and repository for storing the received messages with timestamps.

java
Copier le code
@Entity
@Table(name = "messages")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    // Getters and Setters
}

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
}
5. PostgreSQL Configuration in application.properties
Make sure to configure PostgreSQL in your application.properties:

properties
Copier le code
spring.datasource.url=jdbc:postgresql://localhost:5432/yourdb
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
6. Error Handling and Dead Letter Queue
If the retries fail, the SeekToCurrentErrorHandler will route the message to a dead-letter topic using the DeadLetterPublishingRecoverer. You can listen to this dead-letter topic for further processing or alerting.

java
Copier le code
@KafkaListener(topics = "my_topic.DLT", groupId = "group_id")
public void consumeDeadLetter(ConsumerRecord<String, String> record) {
    logger.error("Received message in Dead Letter Topic -> {}", record.value());
    // Handle dead letter logic
}
Explanation of the Code:
Producer: Sends a message to a Kafka topic and logs the result. The CompletableFuture is used for non-blocking acknowledgment of message delivery.
Consumer with Retry: The consumer uses a SeekToCurrentErrorHandler for retrying failed messages. It tries three times with a fixed backoff of 1 second.
Dead Letter Queue: If the consumer exhausts retries, the message is forwarded to a dead-letter topic (DLT).
PostgreSQL Integration: On successful consumption, the message and the timestamp are stored in PostgreSQL.
Delay Queues: Can be handled via Kafka retry topic configurations or custom delay mechanisms by adjusting backoff.
This setup provides a robust solution for Kafka error handling with retries, recovery, and message tracking via PostgreSQL.






Vous avez dit :
Add store the received date of consumers in postgreSQL
ChatGPT a dit :
ChatGPT
To store the received timestamp of messages processed by the Kafka consumer in PostgreSQL, we can follow these steps:

Define the JPA entity representing the data structure to be saved in PostgreSQL.
Implement the repository to handle database operations.
Update the Kafka consumer to save the received timestamp when the message is successfully processed.
Let's go through the complete implementation:

1. Define JPA Entity to Store Received Messages
First, create an entity class to represent the Kafka message data and the timestamp when it was received.

java
Copier le code
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumer_messages")
public class ConsumerMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_key")
    private String messageKey;

    @Column(name = "message_value", nullable = false)
    private String messageValue;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageValue() {
        return messageValue;
    }

    public void setMessageValue(String messageValue) {
        this.messageValue = messageValue;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
ConsumerMessage is the entity mapped to a PostgreSQL table (consumer_messages).
It stores the messageKey, messageValue, and receivedAt timestamp.
2. Create JPA Repository
Define a Spring Data JPA repository to save and query the messages in the database.

java
Copier le code
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumerMessageRepository extends JpaRepository<ConsumerMessage, Long> {
}
3. Update Kafka Consumer to Store Received Timestamp
In the Kafka consumer service, once a message is successfully processed, store the message and its received timestamp into PostgreSQL.

java
Copier le code
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class KafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private ConsumerMessageRepository consumerMessageRepository;

    @KafkaListener(topics = "my_topic", groupId = "group_id")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            logger.info(String.format("Consumed message -> Key: %s, Value: %s", record.key(), record.value()));

            // Create a new message entity and store the received message with a timestamp to PostgreSQL
            ConsumerMessage consumerMessage = new ConsumerMessage();
            consumerMessage.setMessageKey(record.key());
            consumerMessage.setMessageValue(record.value());
            consumerMessage.setReceivedAt(LocalDateTime.now());

            // Save to the database
            consumerMessageRepository.save(consumerMessage);

            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();

            logger.info("Message successfully processed and stored with timestamp: {}", consumerMessage.getReceivedAt());

        } catch (Exception e) {
            logger.error("Error processing message [{}]: {}", record.value(), e.getMessage());
            // Message will be retried due to configured retry mechanism
            throw e;  // Rethrow to trigger retry or dead-letter mechanism
        }
    }
}
4. Database Table Structure in PostgreSQL
The above entity will map to the following PostgreSQL table structure. You can manually create this table in PostgreSQL, or let JPA handle it with ddl-auto=update in application.properties.

sql
Copier le code
CREATE TABLE consumer_messages (
    id SERIAL PRIMARY KEY,
    message_key VARCHAR(255),
    message_value TEXT NOT NULL,
    received_at TIMESTAMP NOT NULL
);
id: Primary key (auto-increment).
message_key: Key of the Kafka message (nullable).
message_value: The actual value of the Kafka message (not nullable).
received_at: The timestamp when the message was received (not nullable).
5. PostgreSQL Configuration in application.properties
Ensure you have your PostgreSQL database properly configured in the application.properties file:

properties
Copier le code
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update  # Auto-create or update tables based on entities
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
6. Handling Retries and Dead Letters
In case of any exceptions, the message will be retried, and the acknowledgment will only be sent if the message is successfully processed and saved in the PostgreSQL database. If all retries fail, the message will be sent to the dead-letter topic (DLT).

You already have the error handler in the Kafka configuration (SeekToCurrentErrorHandler) as shown earlier, which will handle retries and route messages to the dead-letter queue in case of repeated failure.

Summary of Workflow:
Producer sends messages to a Kafka topic.
Consumer listens to the topic and processes the messages.
On successful consumption, it saves the message's key, value, and received timestamp in the consumer_messages table in PostgreSQL.
If processing fails, retries are handled based on your error-handling configuration.
If all retries fail, the message is routed to a dead-letter topic.
This setup provides a robust solution for Kafka message processing with retry mechanisms and persistent storage of consumer acknowledgment using PostgreSQL.






Vous avez dit :
Add store the received date of mutiple consumers in postgreSQL
ChatGPT a dit :
ChatGPT
To store the received timestamps of multiple Kafka consumers in PostgreSQL, the approach involves creating a system where each consumer logs the messages it processes along with its metadata (like the consumer identifier, timestamp, etc.) into a common PostgreSQL table. This enables you to track which consumer received the message and when.

Key steps:
Extend the JPA Entity to support storing consumer-specific information.
Each consumer will store its metadata, including its unique identifier and the received timestamp, in the PostgreSQL database.
Modify the repository to handle multiple consumers' records.
Let’s implement this step by step:

1. Extend the JPA Entity to Store Consumer Metadata
You can extend the existing ConsumerMessage entity to include information about which consumer processed the message.

java
Copier le code
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumer_messages")
public class ConsumerMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_key")
    private String messageKey;

    @Column(name = "message_value", nullable = false)
    private String messageValue;

    @Column(name = "consumer_id", nullable = false)
    private String consumerId;  // To store which consumer processed the message

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageValue() {
        return messageValue;
    }

    public void setMessageValue(String messageValue) {
        this.messageValue = messageValue;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
consumerId: Identifies the specific Kafka consumer instance that processed the message.
receivedAt: The timestamp when the message was consumed.
2. Create a Repository to Handle Database Operations
This repository will be used by multiple consumers to store their message processing records.

java
Copier le code
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumerMessageRepository extends JpaRepository<ConsumerMessage, Long> {
}
3. Consumer Service to Store Messages in PostgreSQL
Each Kafka consumer will need to store its own metadata in the ConsumerMessage table.

Let’s assume you have multiple consumers distinguished by a unique consumer ID.

Here is an example of how you can implement multiple Kafka consumers:

Consumer 1:
java
Copier le code
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class KafkaConsumer1 {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer1.class);
    private static final String CONSUMER_ID = "consumer_1";

    @Autowired
    private ConsumerMessageRepository consumerMessageRepository;

    @KafkaListener(topics = "my_topic", groupId = "group_1")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            logger.info(String.format("Consumer 1 - Consumed message -> Key: %s, Value: %s", record.key(), record.value()));

            // Create a new message entity and store the received message with a timestamp to PostgreSQL
            ConsumerMessage consumerMessage = new ConsumerMessage();
            consumerMessage.setMessageKey(record.key());
            consumerMessage.setMessageValue(record.value());
            consumerMessage.setConsumerId(CONSUMER_ID);  // Store the consumer identifier
            consumerMessage.setReceivedAt(LocalDateTime.now());

            // Save to the database
            consumerMessageRepository.save(consumerMessage);

            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();

            logger.info("Consumer 1 - Message successfully processed and stored with timestamp: {}", consumerMessage.getReceivedAt());

        } catch (Exception e) {
            logger.error("Consumer 1 - Error processing message [{}]: {}", record.value(), e.getMessage());
            // Message will be retried due to configured retry mechanism
            throw e;
        }
    }
}
Consumer 2:
Similarly, you can have another consumer with a different consumer ID and potentially a different Kafka group, but storing its information in the same table.

java
Copier le code
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class KafkaConsumer2 {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer2.class);
    private static final String CONSUMER_ID = "consumer_2";

    @Autowired
    private ConsumerMessageRepository consumerMessageRepository;

    @KafkaListener(topics = "my_topic", groupId = "group_2")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            logger.info(String.format("Consumer 2 - Consumed message -> Key: %s, Value: %s", record.key(), record.value()));

            // Create a new message entity and store the received message with a timestamp to PostgreSQL
            ConsumerMessage consumerMessage = new ConsumerMessage();
            consumerMessage.setMessageKey(record.key());
            consumerMessage.setMessageValue(record.value());
            consumerMessage.setConsumerId(CONSUMER_ID);  // Store the consumer identifier
            consumerMessage.setReceivedAt(LocalDateTime.now());

            // Save to the database
            consumerMessageRepository.save(consumerMessage);

            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();

            logger.info("Consumer 2 - Message successfully processed and stored with timestamp: {}", consumerMessage.getReceivedAt());

        } catch (Exception e) {
            logger.error("Consumer 2 - Error processing message [{}]: {}", record.value(), e.getMessage());
            // Message will be retried due to configured retry mechanism
            throw e;
        }
    }
}
4. PostgreSQL Table Structure
Each consumer will insert its data into the same consumer_messages table. Here’s the structure for the table:

sql
Copier le code
CREATE TABLE consumer_messages (
    id SERIAL PRIMARY KEY,
    message_key VARCHAR(255),
    message_value TEXT NOT NULL,
    consumer_id VARCHAR(50) NOT NULL,  -- Identifies which consumer processed the message
    received_at TIMESTAMP NOT NULL
);
5. PostgreSQL Configuration in application.properties
Ensure that the PostgreSQL connection configuration in application.properties is correctly set up:

properties
Copier le code
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
6. Multiple Consumers in KafkaListener
You can have as many consumers as needed, with each consumer storing its metadata (consumer ID and timestamp) in the same PostgreSQL table. You can differentiate each consumer by its groupId in the @KafkaListener annotation and by assigning each a unique consumerId.

7. Tracking Messages
By checking the consumer_id and received_at columns in the consumer_messages table, you can track which consumer processed the message and when.

Summary of Workflow:
Multiple Kafka Consumers listen to the same or different topics and process messages.
Each consumer stores its metadata (consumer ID, message details, and timestamp) in the common consumer_messages table in PostgreSQL.
The consumers acknowledge the message only after saving the information in PostgreSQL.
This setup enables you to store and differentiate between messages processed by multiple consumers in PostgreSQL.






Vous avez dit :
make the store in producer side
ChatGPT a dit :
ChatGPT
To store the message's sent timestamp in PostgreSQL from the Producer side, you'll need to modify the producer's logic so that it records the timestamp of when the message was successfully sent to Kafka. This can be achieved by:

Extending the producer logic to store metadata (including the sent timestamp) in PostgreSQL.
Creating an entity and repository to store the message metadata (key, value, timestamp) in the database.
Handling acknowledgment from the producer and saving the data in PostgreSQL after the message is successfully sent to Kafka.
Here’s the step-by-step implementation:

1. JPA Entity to Store Producer Message Metadata
This entity will represent the messages that the producer sends to Kafka, along with the sent timestamp.

java
Copier le code
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "producer_messages")
public class ProducerMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_key")
    private String messageKey;

    @Column(name = "message_value", nullable = false)
    private String messageValue;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageValue() {
        return messageValue;
    }

    public void setMessageValue(String messageValue) {
        this.messageValue = messageValue;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
messageKey: The Kafka message key.
messageValue: The Kafka message content.
sentAt: The timestamp of when the message was sent to Kafka.
2. Repository for Saving Producer Data
Create a Spring Data JPA repository for storing the message data from the producer into PostgreSQL.

java
Copier le code
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProducerMessageRepository extends JpaRepository<ProducerMessage, Long> {
}
3. Kafka Producer Logic
Update the producer logic to store the message and its sent timestamp in the PostgreSQL database after the message has been successfully sent to Kafka.

java
Copier le code
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;

@Service
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ProducerMessageRepository producerMessageRepository;

    public void sendMessage(String topic, String key, String message) {
        logger.info(String.format("Producing message -> Key: %s, Value: %s", key, message));

        // Send the message to Kafka
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);

        // Add callback to handle success and failure scenarios
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                logger.info(String.format("Message [{}] delivered with offset {}", message, result.getRecordMetadata().offset()));

                // Save the sent message with timestamp to PostgreSQL
                ProducerMessage producerMessage = new ProducerMessage();
                producerMessage.setMessageKey(key);
                producerMessage.setMessageValue(message);
                producerMessage.setSentAt(LocalDateTime.now());

                // Save to the database
                producerMessageRepository.save(producerMessage);

                logger.info("Message successfully saved in the database with sent timestamp: {}", producerMessage.getSentAt());
            }

            @Override
            public void onFailure(Throwable ex) {
                logger.error("Failed to deliver message [{}]: {}", message, ex.getMessage());
                // Handle message send failure
            }
        });
    }
}
Explanation:
The producer sends the message using the KafkaTemplate.
The ListenableFutureCallback handles the result:
On success, it logs the message and its offset and saves the message data (key, value, and sent timestamp) in the producer_messages table in PostgreSQL.
On failure, it logs an error.
4. PostgreSQL Table Structure for Producer Messages
Here’s how you can create the table for storing producer messages in PostgreSQL:

sql
Copier le code
CREATE TABLE producer_messages (
    id SERIAL PRIMARY KEY,
    message_key VARCHAR(255),
    message_value TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL
);
This structure:

Stores each message sent by the producer.
Includes the message key (optional), value, and the timestamp (sent_at) indicating when the message was successfully sent to Kafka.
5. PostgreSQL Configuration in application.properties
Ensure that PostgreSQL is correctly configured in your Spring Boot application properties:

properties
Copier le code
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update  # Auto-create or update tables based on entities
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
6. Full Example of Usage
Now you can invoke the KafkaProducer service to send a message to Kafka and simultaneously log the message in the PostgreSQL database with a timestamp.

java
Copier le code
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kafka")
public class KafkaProducerController {

    @Autowired
    private KafkaProducer kafkaProducer;

    @PostMapping("/publish")
    public String publishMessage(@RequestParam("topic") String topic, 
                                 @RequestParam("key") String key, 
                                 @RequestParam("message") String message) {
        kafkaProducer.sendMessage(topic, key, message);
        return "Message published successfully!";
    }
}
Summary of the Producer Workflow:
Send the message to Kafka: The producer sends a message to Kafka with a specified key and value.
Handle Kafka response: The producer listens for a callback to determine if the message was sent successfully.
Store metadata in PostgreSQL: On successful delivery, the producer stores the message key, value, and the timestamp when it was sent into the producer_messages table in PostgreSQL.
Handle errors: In case of message delivery failure, an error is logged but you could also implement a retry or failure-handling mechanism.
This ensures that each message sent by the Kafka producer is logged in PostgreSQL with a timestamp, which can be useful for auditing or tracking purposes.
