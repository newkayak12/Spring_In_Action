package com.message.example.receiver;

import com.message.example.repostiory.dto.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullConfig implements OrderReceiver{
    private final JmsTemplate jms;
    private MessageConverter converter;

    @Override
    public Order receiveOrder() {
//  Receive raw Message
//        try {
//            return (Order) converter.fromMessage(jms.receive("tacocloud.order.queue"));
//        } catch (JMSException e) {
//            throw new RuntimeException(e);
//        }

        return (Order) jms.receiveAndConvert("tacocloud.order.queue");
    }
}
