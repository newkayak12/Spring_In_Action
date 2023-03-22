package com.example.junittest.controller;

import com.example.junittest.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/test")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @GetMapping("/id")
    public String getId(){
        return testService.getId();
    }

    @GetMapping("/password")
    public String getPassword(){
        return testService.getPassword();
    }

    @GetMapping("/nickname")
    public String getNickname(){
        return testService.getNickname();
    }


}
