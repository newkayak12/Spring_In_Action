package com.example.security.controller;

import com.example.security.repository.entity.User;
import com.example.security.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService service;

    /**
     * 사용자가 누구인지 결정하는 방법은
     * Principal :  객체를 컨트롤러 메소드에 주입
     * Authentication : 객체를 컨트롤러 메소드에 주입
     * SecurityContextHolder: 보안 컨텍스트를 얻는다.
     * @AuthenticateionPrincipal: 어노테이션을 메소드에 지정
     */

    @GetMapping(value = "/user")
    public String user (Principal principal, Authentication authentication,  @AuthenticationPrincipal User user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principalData = authentication.getPrincipal(); //Object

        return principal.getName();
    }
}
