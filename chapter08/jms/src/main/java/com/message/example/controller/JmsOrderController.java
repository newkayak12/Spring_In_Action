package com.message.example.controller;

import com.message.example.repostiory.dto.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.JMSException;
import javax.jms.Message;

@RestController
@RequiredArgsConstructor
public class JmsOrderController {
    private final JmsTemplate jms;

    @GetMapping(value = "/convertAndSend/order")
    public String convertAndSendOrder(){
        Order order = new Order();
        jms.convertAndSend("tacocloud.order.queue", order, this::addOrderSource);
        return "CONVERT AND SENT ORDER";
    }

    private Message addOrderSource(Message message) throws JMSException {
        message.setStringProperty("X_ORDER_SOURCE", "WEB");
        return message;
    }
}
