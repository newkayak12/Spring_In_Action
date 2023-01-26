package com.example.kafka.service;

import com.example.kafka.repository.dto.Order;

public interface OrderMessagingService {
    void sendOrder(Order order);
}
