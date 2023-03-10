package com.example.casandra.controller;

import com.example.casandra.repository.IngredientRepository;
import com.example.casandra.repository.entity.Ingredient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/ingredient")
@RequiredArgsConstructor
public class IngredientController {
    private IngredientRepository ingredientRepository;


    @GetMapping(value = "/")
    public Flux<Ingredient> allIngredients(){
        return ingredientRepository.findAll();
    }

}
