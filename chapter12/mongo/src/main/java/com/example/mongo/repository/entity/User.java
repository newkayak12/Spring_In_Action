package com.example.mongo.repository.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document
public class User implements Serializable {

    private static final long serialVersionUID = -7049453858826698277L;

    private final String username;
    private final String password;
    private final String street;
    private final String city;
    private final String state;
    private final String zip;
    private final String phoneNumber;


}

