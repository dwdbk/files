Since the two microservices (producer and consumer) do not share the same database, we need to rethink how we implement the solution for tracking message acknowledgment. Here's how we can address this situation:

Producer Microservice:

The producer sends messages to Kafka.
It expects acknowledgments from multiple consumers, which will be sent back as Kafka messages to a specific acknowledgment topic that the producer will listen to.
When the producer receives acknowledgments, it stores the consumer ID and received timestamp in its own PostgreSQL database.
Consumer Microservice:

The consumer listens for messages from the producer's Kafka topic.
Upon successful processing of a message, the consumer sends an acknowledgment back to the producer's acknowledgment Kafka topic.
The consumer also stores its own records of message processing for internal tracking in its PostgreSQL database.
Key Components:
Kafka Topics:
A main topic for sending messages from producer to consumers.
An acknowledgment topic for consumers to send acknowledgment messages back to the producer.
Architecture Overview:
Producer sends messages → Consumer processes message → Consumer sends acknowledgment back → Producer stores acknowledgment.
Each microservice uses its own database for its internal tracking.
1. Producer Microservice Implementation
Dependencies in pom.xml (Producer)
xml
Copier le code
<dependencies>
    <!-- Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- PostgreSQL for storing consumer acks -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>

    <!-- Jackson for JSON serialization -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
</dependencies>
Kafka Producer Configuration (Producer)
java
Copier le code
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
Kafka Producer Service (Producer)
java
Copier le code
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Sends message to Kafka
    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
Consumer Acknowledgment Listener (Producer)
java
Copier le code
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class KafkaAckListener {

    private final ConsumerAckRepository consumerAckRepository;

    public KafkaAckListener(ConsumerAckRepository consumerAckRepository) {
        this.consumerAckRepository = consumerAckRepository;
    }

    @KafkaListener(topics = "acknowledgment_topic", groupId = "producer_ack_group")
    public void listenForAcknowledgment(AcknowledgmentMessage ackMessage) {
        // Save acknowledgment to PostgreSQL database
        ConsumerAck ack = new ConsumerAck();
        ack.setConsumerId(ackMessage.getConsumerId());
        ack.setMessageId(ackMessage.getMessageId());
        ack.setReceivedAt(LocalDateTime.now());

        consumerAckRepository.save(ack);
    }
}
PostgreSQL Entity for Consumer Acknowledgments (Producer)
java
Copier le code
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ConsumerAck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String consumerId;
    private String messageId;
    private LocalDateTime receivedAt;

    // Getters and setters
}

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumerAckRepository extends JpaRepository<ConsumerAck, Long> {
}
Acknowledgment Message Object (Producer & Consumer)
java
Copier le code
public class AcknowledgmentMessage {
    private String consumerId;
    private String messageId;

    // Getters and setters

    public AcknowledgmentMessage(String consumerId, String messageId) {
        this.consumerId = consumerId;
        this.messageId = messageId;
    }

    // default constructor, getters, setters
}
2. Consumer Microservice Implementation
Dependencies in pom.xml (Consumer)
xml
Copier le code
<dependencies>
    <!-- Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- PostgreSQL for tracking message receipts -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>

    <!-- Jackson for JSON serialization -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
</dependencies>
Kafka Consumer Configuration (Consumer)
java
Copier le code
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Component
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfigs()));
        return factory;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer_group");
        return props;
    }
}
Kafka Consumer Service (Consumer)
java
Copier le code
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class KafkaConsumerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConsumerAckRepository consumerAckRepository;

    public KafkaConsumerService(KafkaTemplate<String, Object> kafkaTemplate, 
                                ConsumerAckRepository consumerAckRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.consumerAckRepository = consumerAckRepository;
    }

    @KafkaListener(topics = "test_topic", groupId = "consumer_group")
    public void consume(String message) {
        // Simulate message processing
        System.out.println("Received message: " + message);

        // Send acknowledgment back to the producer via acknowledgment topic
        AcknowledgmentMessage ackMessage = new AcknowledgmentMessage("consumer1", message);
        kafkaTemplate.send("acknowledgment_topic", ackMessage);

        // Store consumer tracking data
        recordMessageReceipt("consumer1", message);
    }

    public void recordMessageReceipt(String consumerId, String messageId) {
        ConsumerAck ack = new ConsumerAck();
        ack.setConsumerId(consumerId);
        ack.setMessageId(messageId);
        ack.setReceivedAt(LocalDateTime.now());
        consumerAckRepository.save(ack);
    }
}
PostgreSQL Entity for Consumer Acknowledgments (Consumer)
java
Copier le code
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ConsumerAck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String consumerId;
    private String messageId;
    private LocalDateTime receivedAt;

    // Getters and setters
}

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumerAckRepository extends JpaRepository<ConsumerAck, Long> {
}
Key Concepts:
Producer Microservice:

Sends messages to a topic and listens for acknowledgment messages from the consumers via a dedicated acknowledgment topic.
The producer tracks message acknowledgment in its PostgreSQL database, which records the consumer ID and acknowledgment timestamp.
Consumer Microservice:

Consumes messages from the producer's Kafka topic.
After successful processing, the consumer sends an acknowledgment message back to the producer's acknowledgment topic.
The consumer also stores message receipt information in its own PostgreSQL database for tracking and internal purposes.
This architecture allows you to track which consumers have acknowledged the message without requiring the producer and consumer microservices to share the same database.
