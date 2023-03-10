package com.example.casandra.repository;

import com.example.casandra.repository.entity.Taco;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface TacoRepository extends ReactiveCrudRepository<Taco, UUID> {
}
