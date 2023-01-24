package com.example.rabbitmq.mqReceive;

import com.example.rabbitmq.repository.dto.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RabbitOrderReceiver {
    private final RabbitTemplate rabbit;
    private MessageConverter converter;

    public Order receiveOrder(){
        Optional<Message> message = Optional.ofNullable(rabbit.receive("tacocloud.orders"));
        return message.map(mes -> (Order) converter.fromMessage(mes)).orElseGet(() -> null);
    }
}
