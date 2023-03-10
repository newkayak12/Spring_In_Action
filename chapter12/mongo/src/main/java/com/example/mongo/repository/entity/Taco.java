package com.example.mongo.repository.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Date;
import java.util.List;

@Data
@RestResource(rel="tacos", path = "tacos")
@Document
public class Taco {
    @Id
    private String id;

    @NotNull
    @Size(min = 5, message = "Name must be a least 5 character long")
    private String name;

    private Date createdAt  = new Date();

    @Size(min = 1, message = "You must choose at least 1 ingredient")
    private List<Ingredient> ingredients;
}