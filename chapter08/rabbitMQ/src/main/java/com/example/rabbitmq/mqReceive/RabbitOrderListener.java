package com.example.rabbitmq.mqReceive;

import com.example.rabbitmq.repository.dto.KitchenUi;
import com.example.rabbitmq.repository.dto.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitOrderListener {
    private final KitchenUi ui;

    @RabbitListener(queues = "tacocloud.order.queue")
    public void receiveOrder(Order order) {
        ui.displayOrder(order);
    }
}
