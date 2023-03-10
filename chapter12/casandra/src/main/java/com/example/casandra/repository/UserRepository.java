package com.example.casandra.repository;

import org.apache.catalina.User;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends ReactiveCassandraRepository<User, UUID> {
    @AllowFiltering
    Mono<User> findUserByUsername(String username);
}
