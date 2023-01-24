package com.message.example.service;

import com.message.example.repostiory.dto.Order;

public interface OrderMessagingService {
    public void sendOrder(Order order);
}
