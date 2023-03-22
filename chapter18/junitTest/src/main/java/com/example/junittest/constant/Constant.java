package com.example.junittest.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Constant {

    public static String ID;
    public static String PASSWORD;
    public static String NICKNAME;

    @Value("${Constant.id}")
    public  void setID(String ID) {
        System.out.println("ID : "+ ID);
        this.ID = ID;
    }
    @Value("${Constant.password}")
    public  void setPASSWORD(String PASSWORD) {
        this.PASSWORD = PASSWORD;
    }
    @Value("${Constant.nickname}")
    public  void setNICKNAME(String NICKNAME) {
        this.NICKNAME = NICKNAME;
    }
}
