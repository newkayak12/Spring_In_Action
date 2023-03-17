package com.example.hystrix.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService{
    @Override
    @HystrixCommand(fallbackMethod = "testDefault", commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "500")})
    public String test() {

        if(true){
            throw new NullPointerException();
        }

        return "TEST";
    }

    @Override
    @HystrixCommand(fallbackMethod = "testDefault", commandProperties = {
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value="30"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "25"),
            @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "20000")
        }
    )
    public String testHystrix() {
        if(true){
            throw new NullPointerException();
        }

        return "TEST";
    }


    public String testDefault(){
        return "TestDefault";
    }
}
