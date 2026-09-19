package com.example.demo.kafka;

import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
public class BillingEventProducer {

    private static final String TOPIC = "billing-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    public BillingEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            JsonMapper jsonMapper) {

        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
    }

    public void sendEvent(BillingEvent event) {

        String message = jsonMapper.writeValueAsString(event);

        kafkaTemplate.send(TOPIC, message);
    }
}