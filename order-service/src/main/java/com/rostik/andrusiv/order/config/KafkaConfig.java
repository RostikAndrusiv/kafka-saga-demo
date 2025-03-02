package com.rostik.andrusiv.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Value("${order.events.topic.name}")
    private String ordersEventTopicName;
    @Value("${order.commands.topic.name}")
    private String ordersCommandsTopicName;
    @Value("${product.commands.topic.name}")
    private String productCommandsTopicName;
    @Value("${payment.commands.topic.name}")
    private String paymentCommandsTopicName;
    private static final Integer REPLICATION_FACTOR = 3;
    private static final Integer TOPIC_PARTITIONS = 3;

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    NewTopic createOrdersEventTopic() {
        return TopicBuilder
                .name(ordersEventTopicName)
                .partitions(TOPIC_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic createProductCommandsTopic() {
        return TopicBuilder
                .name(productCommandsTopicName)
                .partitions(TOPIC_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic createPaymentCommandsTopic() {
        return TopicBuilder
                .name(paymentCommandsTopicName)
                .partitions(TOPIC_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic createOrderCommandsTopic() {
        return TopicBuilder
                .name(ordersCommandsTopicName)
                .partitions(TOPIC_PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .build();
    }
}
