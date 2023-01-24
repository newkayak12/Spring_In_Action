package com.message.example.service;

import com.message.example.repostiory.dto.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Service;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@Service
@RequiredArgsConstructor
public class JmsOrderMessageService implements OrderMessagingService{
    private final JmsTemplate jms;
    private final Destination orderQueue;

    @Override
    public void sendOrder(Order order) {
// anonymous
//        jms.send(new MessageCreator() {
//            @Override
//            public Message createMessage(Session session) throws JMSException {
//                return session.createObjectMessage(order);
//            }
//        });

//Lambda //default destination
//        jms.send(session -> session.createObjectMessage(order));

//other destination #1
//        jms.send(orderQueue, session -> session.createObjectMessage(order));
//other destination #2
//        jms.send("tacocloud.order.queue", session -> session.createObjectMessage(order));


//        jms.convertAndSend("tacocloud.order.queue", order);


        //use post processor

        jms.convertAndSend("tacoCloud.order.queue", order, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws JMSException {
                message.setStringProperty("X_ORDER_SOURCE", "WEB");
                return message;
            }
        });
    }


}
