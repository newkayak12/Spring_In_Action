package com.example.mongo.repository.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Document
public class Order implements Serializable {

    private static final long serialVersionUID = -6549378668751347373L;

    @Id
    private String id;
    private Date placedAt = new Date();

    @Field("customer")
    private User user;
    private List<Taco> tacos = new ArrayList<>();

}
