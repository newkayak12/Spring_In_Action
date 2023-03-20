package com.example.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class HealthConfig implements HealthIndicator {

    @Override
    public Health health() {
        Random random = new Random(10);
        Integer randomNumber = random.nextInt();
        if(randomNumber >= 0 && randomNumber < 5){
            return Health.up().build();
        } else {
            return Health.down().withDetail("reason", "test").build();
        }
    }
}
