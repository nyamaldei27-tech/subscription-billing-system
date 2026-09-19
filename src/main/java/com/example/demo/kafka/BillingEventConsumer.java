package com.example.demo.kafka;

import com.example.demo.entity.SubscriptionActivity;
import com.example.demo.repository.SubscriptionActivityRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class BillingEventConsumer {

    private final JsonMapper jsonMapper;
    private final SubscriptionActivityRepository subscriptionActivityRepository;

    public BillingEventConsumer(
            JsonMapper jsonMapper,
            SubscriptionActivityRepository subscriptionActivityRepository) {

        this.jsonMapper = jsonMapper;
        this.subscriptionActivityRepository = subscriptionActivityRepository;
    }

    @KafkaListener(
            topics = "billing-events",
            groupId = "billing-service-group"
    )
    public void consumeEvent(String message) {

        BillingEvent event = jsonMapper.readValue(
                message,
                BillingEvent.class
        );

        SubscriptionActivity activity = new SubscriptionActivity(
                event.getSubscriptionId(),
                event.getCustomerId(),
                event.getEventType(),
                event.getTimestamp()
        );

        subscriptionActivityRepository.save(activity);

        System.out.println(
                "Activity saved: " +
                        event.getEventType() +
                        " | Subscription: " +
                        event.getSubscriptionId() +
                        " | Customer: " +
                        event.getCustomerId()
        );
    }
}