package com.example.mongo.repository.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Document(collection = "ingredients") // 지정된 도메인 타입을 몽고 DB 저장 문서로 선언된다.
                                      //collection을 지정하면 컬렉션 명을 지정할 수 있다.
public class Ingredient {

    @Id //문서 ID
    private final String id;
    @Field
    private final String name;
    @Field
    private final Type type;

    public static enum Type {
        WRAP, PROTEIN, VEGGIES, CHEESE, SAUCE
    }
}
