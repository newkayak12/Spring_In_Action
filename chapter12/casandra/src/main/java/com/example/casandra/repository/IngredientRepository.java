package com.example.casandra.repository;

import com.example.casandra.repository.entity.Ingredient;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

public interface IngredientRepository  extends ReactiveCassandraRepository<Ingredient, String> {
}
