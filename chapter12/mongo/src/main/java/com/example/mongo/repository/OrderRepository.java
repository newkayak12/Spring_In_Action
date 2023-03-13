package com.example.mongo.repository;

import com.example.mongo.repository.entity.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderRepository extends ReactiveCrudRepository<Order,String> {
}
