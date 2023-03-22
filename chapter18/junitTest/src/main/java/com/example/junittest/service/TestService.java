package com.example.junittest.service;

import com.example.junittest.constant.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestService {
    public String getId() {
        return Constant.ID;
    }

    public String getPassword() {
        return Constant.PASSWORD;
    }

    public String getNickname() {
        return Constant.NICKNAME;
    }
}
