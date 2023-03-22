package com.example.junittest.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.assertj.core.api.Assertions.*;
@SpringBootTest
@RequiredArgsConstructor
public class TestControllerTest {
//    private final MockMvc mockMvc;
    @SpyBean
    public TestController testController;

    @Test
    public void getIdTest(){
        System.out.println(testController);
        assertThat(testController.getId()).isEqualTo("test");
    }
}
