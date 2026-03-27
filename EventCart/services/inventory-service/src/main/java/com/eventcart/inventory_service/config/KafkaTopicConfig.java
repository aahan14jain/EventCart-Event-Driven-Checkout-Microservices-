package com.eventcart.inventory_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Ensures compensation topic exists at startup. Unlike other saga topics, nothing consumes
 * {@code inventory.released}, so the broker may not create it before the first publish.
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic inventoryReleasedTopic() {
        return TopicBuilder.name("inventory.released")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
