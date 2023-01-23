package com.message.example.receiver;

import com.message.example.repostiory.dto.Order;

public class KitchenUI {
    public void displayOrder(Order order){
        System.out.println(order.toString());
    }
}
