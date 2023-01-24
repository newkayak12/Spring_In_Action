package com.example.rabbitmq.service;

import com.example.rabbitmq.repository.dto.Order;

public interface OrderMessagingService {

    void sendOrder(Order order);

}
