package com.example.junittest.service;

import com.example.junittest.constant.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
public class TestServiceTest {
    @InjectMocks
    public TestService testService;



    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.invokeSetterMethod(new Constant(), "ID", "test");
    }

    @Test
    public void getIdTest(){
        assertThat(testService.getId()).isEqualTo("test");
    }
}
