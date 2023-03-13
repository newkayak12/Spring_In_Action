package com.example.mongo.repository;

import com.example.mongo.repository.entity.Taco;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TacoRepository extends ReactiveMongoRepository<Taco, String> {
    Flux<Taco> findTacoOrderByCreatedAtDesc();

}
