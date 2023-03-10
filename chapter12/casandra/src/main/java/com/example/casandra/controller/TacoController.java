package com.example.casandra.controller;

import com.example.casandra.repository.TacoRepository;
import com.example.casandra.repository.entity.Taco;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/taco")
@RequiredArgsConstructor
public class TacoController {
    private final TacoRepository tacoRepository;

    @GetMapping(value = "/")
    public Flux<Taco> getAllTacos(){
        return tacoRepository.findAll();
    }
}
