package com.agentsbackend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Creates Kafka topics used by order fulfillment retry flow.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.retry:order-fulfillment-retry}")
    private String retryTopic;

    @Value("${kafka.topic.dlq:order-fulfillment-dlt}")
    private String deadLetterTopic;

    @Bean
    public NewTopic orderRetryTopic() {
        return TopicBuilder.name(retryTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic orderDeadLetterTopic() {
        return TopicBuilder.name(deadLetterTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
