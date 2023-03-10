package com.example.casandra.repository.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

@UserDefinedType("user")
@Data
public class UserUDT {
    private final String username;
    private final String fullName;
    private final String phoneNumber;
}
