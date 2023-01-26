package com.example.kafka.listener;

import com.example.kafka.repository.dto.KitchenUi;
import com.example.kafka.repository.dto.Order;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.protocol.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Config {
    private final KitchenUi ui;

    @KafkaListener(topics = {"tacocloud.orders.topic"})
    public void handle(Order order) {
        ui.displayOrder(order);
    }

}
