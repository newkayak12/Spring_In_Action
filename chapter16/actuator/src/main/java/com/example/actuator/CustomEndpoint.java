package com.example.actuator;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Endpoint(id = "test", enableByDefault = true)
public class CustomEndpoint {
    private List<String> array = new ArrayList<>();

    @ReadOperation
    public String readTest(){
        return array.stream().collect(Collectors.joining(", "));
    }

    @WriteOperation
    public String writeTest(String text){
        array.add(text);
        return array.stream().collect(Collectors.joining(", "));
    }

    @DeleteOperation
    public String deleteTest(){
        array.remove(array.size() - 1);
        return array.stream().collect(Collectors.joining(", "));
    }
}
