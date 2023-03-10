## 12.1 스프링 데이터의 리액티브 개념 이해하기

스프링 데이터 Kay 릴리즈 트레인부터 스프링 데이터는 리액티브 레포지토리의 지원을 제공하기 시작했다. 여기에 카산드라, 몽고DB, 카우치 베이스, 레디스로 데이터를 
저장할 때 리액티브 프로그래밍 모델을 지원하는 것이 포함된다.

그러나 관계형 DB나 JPA는 리액티브 레포지토리가 지원되지 않는다. 스프링 데이터JPA로 리액티브 프로그래밍 모델을 지원하려면 관계형 데이터베이스와 JDBC 드라이버 역시
논블로킹이어야만 한다. 

## 12.1.1 스프링 데이터 리액티브 개요
스프링 데이터 리액티브의 핵심은 다음과 같이 요약할 수 있다.
 
> 리액티브 레포지토리는 도메인타입이나 컬렉션 대신 Mono, Flux를 인자로 받거나 반환한다.

DB를 탐색할 떄도,
```java
Flux<Ingredient> findByType(Ingredient.Type type);
```
DB에 작성할 떄도
```java
Flux<Taco> saveAll(Publisher<Taco> tacoPublisher);
```

간단히 말해서, 스프링 데이터의 리액티브 레포지토리는 스프링 데이터의 리액티브가 아닌 레포지토리와 거의 동일한 프로그래밍 모델을 공유한다. 단, 리액티브 레포지토리는
도메인 타입이나 컬렉션 대신 Mono, Flux를 인자로 받거나 반환한다.

## 12.1.2 리액티브, 논 리액티브 간 타입 변환
과연 논리액티브와 리액티브 간 이전은 영영 불가능한 것일까?
리액티브 프로그래밍의 장점은 클라이언트부터 DB까지 리액티브 모델을 가질 때 비로소 의미가 있다. 물론 DB가 블로킹일 때도 여전히 일부 장점을 살릴 수는 있다.
심지어 DB가 블로킹 없는 리액티브 쿼리를 지원하지 않더라도 블로킹 되는 방식으로 데이터를 가져와서 가능한 빨리 리액티브 타입으로 캐스팅 해서 상위 컴포넌트들이 
남아있는 장점을 살릴 수 있다. 

단순하다. 블로킹 방식의 메소드를 호출할 수 밖에 없다면 가능한 빨리 리액티브가 아닌 결과를 리액티브로 변환하면 된다.
```java
List<Order> orders = repo.findByUser(somUser);
Flux<Order> orderFlux = Flux.fromIterable(orders);

Order order = repo.findById(Long id);
Mono<Order> orderMono = Mono.just(order);
```

이처럼 Mono.just() 혹은 Flux의 fromIterable(), fromArray(), fromStream() 메소드를 사용하면 레포지토리의 리액티브가 아닌 블로킹 코드를 격리시키고 애플리케이션의
어디에서든 리액티브 타입으로 처리하게 할 수 있다.

반대로 저장한다면?
```java
Taco taco = tacoMono.block();
tacoRepo.save(taco);
```
block()은 추출작업을 위해서 블로킹 오퍼레이션을 실행한다. Flux의 데이터를 추출할 떄는 toIterable()을 사용할 수 있다. WebFlux 컨트롤러가 Flux<Taco>를 받은 후 이것을
스프링 데이터 JPA 레포지토리가 save() 메소드로 저장한다고 하면 아래와 같이 하면 된다.

```java
Iterable<Taco> tacos = tacoFlux.toIterable();
tacoRepo.saveAll(tacos);
```

Mono.block()과 마찬가지로 Flux.toIterable()은 Flux가 발행하는 모든 객체를 모아서 Iterable 타입으로 추출해준다. 그러나 Mono.block(), Flux.toIterable()은 
추출 작업을 할 때 블로킹 되므로 리액티브 프로그래밍 모델을 벗어난다. 따라서 이런 식으로 사용하는 것은 지양하는 것이 좋다.

다시, 조금 더 리액티브스럽게 처리하는 방법이 있다. 즉, Mono, Flux를 구독하고 요소에서 원하는 오퍼레이션을 처리하는 것이다.
예를 들어, Flux<Taco>가 발행하는 Taco 객체를 리액티브가 아닌 레포지토리에 저장할 떄는
```java
tacoFlux.subscribe( taco -> tacoRepo::save );
```
와 같이 하면 된다. 물론 save는 블로킹이다. 하지만 Flux, Mono가 발행하는 데이터를 소비하고 처리하는 리액티브 방식의 subscribe()를 사용하므로 블로킹 방식의 일괄처리보다는 낫다.

## 12.1.3 리액티브 레포지토리 개발하기
JPA는 인터페이스를 선언하면 이것을 스프링 데이터가 런타임 시에 자동으로 구현해준다. 결과적으로 카산드라, 몽고DB 같은 관계형이 아닌 데이터베이스도 역시 가능하다.
리액티브가 아닌 레포지토리 지원 위에 구축된 스프링 데이터 카산드라와 스프링 데이터 몽고DB는 리액티브 모델도 지원한다. 따라서 데이터 퍼시스턴스를 제공하는 백엔드로
이 데이터베이스들을 사용하면, 스프링 애플리케이션 웹 계층부터 DB까지 엔드 - to - 엔드 리액티브로 구성할 수 있다.

## 12.2 리액티브 카산드라 레포지토리 사용하기
카산드라는 분산처리, 고성능, 상시 가용, 궁극적인 일관성을 갖는 NoSQL DB이다. 카산드라는 데이터를 테이블에 저장된 row로 처리하며, 각 row는 1:N 관계의 많은 분산 
노드에 걸쳐 분할된다. 즉, 한 노드가 모든 데이터를 갖지 않지만, 특정 row는 다수의 노드에 걸쳐 복제될 수 있으므로 단일 장애점(single point of failure: 한 노드에 문제가 생기면
전체가 사용 불가능)을 없애준다.

스프링 데이터 카산드라는 카산드라 DB의 자동화된 레포지토리 지원을 제공하는데 이것은 관계형 DB의 스프링 데이터 JPA가 제공하는 것과 유사하면서 다르다. 또한, 스프링
데이터 카산드라는 애플리케이션의 도메인 타입을 DB 구조에 매핑하는 어노테이션을 제공한다.

## 12.2.1 스프링 데이터 카산드라 활성화 하기

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-cassandra-reactive</artifactId>
</dependency>
```
이 의존성은 스프링 데이터 JPA 스타터 대신 필요하다. 그리고 이런 라이브러리들이 classpath에 저장되므로 런타임 시에 리액티브 카산드라 라이브러리들을 생성하는
자동 구성이 수행된다. 

단, 일부 구성은 제공해야하는데 최소한 레포지토리가 운용되는 key space의 이름을 구성해야하며, 이렇게 하기 위해서 해당 키 공간을 생성해야한다.
키 공간을 자동으로 생성하게 할 수 있지만, 우리가 직접 생성하는 것이 훨씬 쉽다.

```shell
 docker exec -it cassandra cqlsh "-u cassandra -p cassandra"
```
```cassandraql
create keyspace reactive with replication={'class':'SimpleStrategy', 'replication_factor':1};
```

yaml은 아래와 같이 설정한다.
```yaml
spring:
  data:
    cassandra:
      keyspace-name: reactive
      schema-action: recreate_drop_unused
      port: 9042
      local-datacenter: datacenter1
      username: cassandra
      password: cassandra
```


## 12.2.2 카산드라 데이터 모델링 이해하기
이미 얘기했듯이, 카산드라는 관계형 데이터베이스와 많이 다르다. 따라서 도메인 타입을 카산드라의 테이블로 매핑하기 전게 알아 둘 중요한 것이 있다. 즉, 카산드라 데이터
모델링은 관계형 DB의 모델링과 다르다.

카산드라 데이터 모델링에 관해서 알아 둘 몇 가지 중요한 사항이 있다. 카산드라 테이블은 얼마든지 많은 column을 가질 수 있다. 그러나 모든 row가 같은 column
을 가질 필요는 없다.

카산드라 DB는 다수의 파티션에 걸쳐 분할된다. 테이블의 어떤 row도 하나 이상의 파티션에서 관리될 수 있다. 그러나 각 파티션은 모든 row를 갖지 않고 
서로 다른 row를 가질 수 있다.

카산드라는 읽기 오퍼레이션에 최적화되어 있다. 따라서 테이블이 비정규화되고 데이터가 다수의 테이블에 걸쳐 중복되는 경우가 흔다. 예를 들어, 고객 정보는 고객 테이블에 저장
되지만, 각 고객의 주문 정보를 포함하는 테이블에도 중복 저장될 수 있다. 

## 12.2.3 카산드라 퍼시스턴스의 도메인 타입 매핑
카산드라에서는 JPA와 같이 진행할 수 없다 대신 카산드라는 유사한 목적의 어노테이션을 제공한다.

```java
package com.example.casandra.repository;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Table("ingredient")

public class Ingredient {

    @PrimaryKey
    private final String id;
    private final String name;
    private final Type type;

    public static enum Type {
        WRAP, PROTEIN, VEGGIES, CHEESE, SAUCE;
    }
}
```
```@Entity``` 대신 ```@Table```, ```@Id``` 대신 ```@PrimaryKey```를 지정했다.

```java
package com.example.casandra.repository;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@RestResource(rel = "tacos", path = "tacos")
@Table("tacos") //taco 테이블에 저장, 유지
public class Taco {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED) //파티션 키를 정의
    private UUID id = Uuids.timeBased();

    @NotNull
    @Size(min = 5, message = "Name must be at least 5 characters long")
    private String name;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING) //클러스터링 키를 정의
    private Date createdAt = new Date();

    @Size(min = 1, message = "You must choose at least 1 ingredient")
    @Column("ingredients") //List를 ingredients열에 매핑
    private List<IngredientUDT> ingredients;
}
```
id 속성은 기본키이다. ```PrimaryKeyType.PARTITIONED``` 타입으로 지정되어 있다. 이것은 데이터의 각해잉 저장되는 카산드라 파티션을 결정하기 위해서 사용하는 파티션 키를 선언한 것이다.

>   ## PrimaryKey
>    A primary key in Cassandra consists of one or more partition keys and zero or more clustering key components
> 
>  The order of these components always puts the partition key first and then the clustering key.
> 
>   ## PartitionKey
> 
>   The primary goal of a partition key is to distribute the data evenly across a cluster and query the data efficiently.
>
>  A partition key is for data placement apart from uniquely identifying the data and is always the first value in the primary key definition.
> 
>   ## ClusteringKey
> 
>   As we've mentioned above, partitioning is the process of identifying the partition range within a node the data is placed into.
> 
>   In contrast, clustering is a storage engine process of sorting the data within a partition and is based on the columns defined as the clustering keys.
>
(대략, partitionKey는  clusteringKey는 인덱스 비스무리해보임)

마지막 ~UDT가 조금 낯선데, Ingredeint가 아닌 IngredientUDT로 정의됐다. 카산드라 테이블은 비정규화되어 중복 데이터를 가질 수 있다. 식재료와 타코 안의 재료 데이터가 중복 저장될 수 있다. 
그리고 ingredients 테이블은 하나 이상의 행을 참조하는 대신 ingredients는 선택된 각 식재료의 전체 데이터를 포함한다.

점점 더 왜 재사용이 안될까?라는 의문이 생긴다. ingredients 같이 컬렉션을 포함하는 열은 네이브 타입(정수, 문자열)의 컬렉션이거나 사용자 정의 타입( UDT_ User Defined Type )의 컬렉션이어야 하기 떄문이다.

```java
package com.example.casandra.repository;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@UserDefinedType("ingredient")
public class IngredientUDT {
    private final String name;
    private final Ingredient.Type type;
}
```

카산드라에서 사용자 정의 타입은 단순한 네이티브 타입보다 더 다채로운 테이블 열을 선언 할 수 있게 해준다. 그리고 비정규화된 관계영 DB 외래키처럼 사용된다.
단, 다른 테이블의 한 행에 대한 참조만 갖는 외부 키와는 대조적으로, 사용자 정의 타입의 열은 다른 테이블의 한 행으로부터 복사될 수 있는 데이터를 실제로 갖는다. 

Ingredient 클래스는 사용자 정의 타입으로 사용할 수 없다. 왜냐하면 @Table 어노테이션이 이미 Ingredient를 카산드라에 저장하는 엔티티(도메인 타입)으로 매핑했기 때문이다.
따라서 taco 테이블의 ingredients 컬럼에 식재료 데이터가 어떻게 저장되는지 정의하기 위해서 새로운 클래스를 생성해야한다. 그게 IngredientUDT이다.

IngredientUDT는 Ingredient클래스와 매우 유사하지만, 엔티티에 매핑하는 데 필요한 요구사항은 더 간단하다. 
1. @UserDefinedType이 지정되서 사용자 정의 타입임을 알린다.
2. 해당 클래스는 id를 사용하지 않는다. (사용자 정의 타입은 우리가 원하는 어떤 속성도 가질 수 있지만, 테이블 정의와 갖지 않아도 된다.)

## 12.2.4 리액티브 카산드라 레포지토리 작성하기
스프링 데이터로 리액티브가 아닌 레포지토리를 작성할 때는 JPARepository의 도움을 받기 위해서 기본 레포지토리 인터페이스 중 하나를 확장하는 인터페이스를 선언하면 된다.
그리고 커스텀 쿼리를 추가할 수도 있다. 리액티브 레포지토리도 다르지 않다. 가장 큰 차이점은 다른 종류의 기본 레포지토리를 확장하는 것과
도메인 타입이나 컬랙션 대신 Mono, Flux 같은 리액티브 타입을 메소드에서 처리하는 거싱다.
리액티브 카산드라 레포지토리를 작성할 때는 두 개의 기본 인터페이스인 ```ReactiveCassandraRepository``` 혹은 ```ReactiveCrudRepository```를 선택할 수 있다.
둘 중 어떤 것을 선택하는 가는 레포지토리를 어떻게 사용하느냐에 따라 달렸다. ReactiveCassandraRepository는 ReactiveCrudRepository를 확장하여 새 객체가 저장될 떄
사용되는 insert()의 몇 가지 변형 버전을 제공하며, 이외에는 ReactiveCrudRepository와 동일한 메소드를 제공한다. 만약 많은 데이터를 추가한다면 
```ReactiveCassandraRepository```를 선택하면 되고, 그게 아니라면 ```ReactiveCrudRepository```를 선택할 수 있다.

> #### 카산드라 레포지토리는 반드시 리액티브해야하는가?
> 꼭 그렇지 않다. CassandraRepository,  CrudRepository를 사용하면 된다. 그 다음 Flux, Mono 대신 카산드라 어노테이션이 지정된 도메인
> 타입이나 도메인 타입이 저장된 컬렉션을 레포지토리 메소드에서 반환하면 된다.


```java
public interface IngredientRepository  extends ReactiveCassandraRepository<Ingredient, String> {
}
```

Reactive가 붙으면 Flux, Mono를 받고 반환한다는 것을 잊으면 안된다.
```java
@RestController
@RequestMapping(value = "/ingredient")
@RequiredArgsConstructor
public class IngredientController {
    private IngredientRepository ingredientRepository;


    @GetMapping(value = "/")
    public Flux<Ingredient> allIngredients(){
        return ingredientRepository.findAll();
    }

}
```

```java
public interface UserRepository extends ReactiveCassandraRepository<User, UUID> {
    @AllowFiltering
    Mono<User> findUserByUsername(String username);
}
```
위 예시는 리액티브 레포지토리이므로 findUserByUsername()에서 User를 반환하면 안된다. 따라서 Mono<User>를 반환하도록 변경하면 된다.
또한 카산드라의 특성상 관계형 DB에서 하듯 'where ~'로하는 필터링은 느리게 처리될 수 있다. 물론 필요할 떄가 있다.
이때, ```@AllowFiltering``` 어노테이션을 사용하면 된다.

@AllowFiltering을 지정하지 않은 findUserByUsername()의 경우

>  SELECT * FROM USER WHERE username = '사용자명';

그라나 카산으라는 where을 사용하지 않는다. 따라서 @AllowFiltering 어노테이션을 findUserByUsername()에 지정하면

> SELECT * FROM USER WHERE username = '사용자명' allow filtering;

쿼리 끝의 ```allow filtering``` 절은 '쿼리 성능에 잠재적인 영향을 준다는 것을 알고 있지만 어쨋든 수행해야 한다'는 것을 카산드라에 알린다.


## 12.3 리액티브 몽고 DB 레포지토리 작성하기
카산드라가 테이블의 row로 데이터를 저장하는 반면, 몽고DB는 문서형 데이터베이스이다. 더 자세히 말해서, 몽고 DB는 BSON(Binary JSON) 형식의 문서로 데이터를
저장하며, 다른 DB에서 데이터를 질의하는 것과 유사한 방법으로 쿼리할 수 있다.

역시 몽고 DB도 관계형 DB는 아니다. 따라서 데이터를 모델링하는 방법은 물론이고 관리하는 방법도 타 DB와 다르다. 그렇기는 하지만 몽고 DB를 스프링 데이터로 사용하는 것은
별반 다르지 않다. 즉, 도메인 타입을 문서 구조로 매핑하는 어노테이션을 도메인 클래스에 지정한다. 그리고 JPA 방식대로 인터페이스를 작성하면 된다.

```shell
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-mongodb-reactive'
	implementation 'org.springframework.boot:spring-boot-starter-webflux'
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testImplementation 'io.projectreactor:reactor-test'
}
```
와 같이 의존성을 추가하면 된다. 기본적으로 스프링 데이터 몽고 DB는 기본 포트를 27017로 잡는다. H2같이 내장 몽고 DB를 사용할 수도 있는데, 
```Flapdoodle``` 내장 몽고 DB를 의존성에 추가하면 된다.

```yaml
spring:
  data:
    mongodb:
      host: ##기본값 localhost
      port: ##기본값 27017
      username: ##ID
      password: ##PASSWORD
      database: ## 기본값 test
```
```java
@Data
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Document(collection = "ingredients") // 지정된 도메인 타입을 몽고 DB 저장 문서로 선언된다.
                                      //collection을 지정하면 컬렉션 명을 지정할 수 있다.
public class Ingredient {

    @Id //문서 ID //Serializable 타입인 어떤 속성도 사용 가능
    private final String id;
    @Field
    private final String name;
    @Field
    private final Type type;

    public static enum Type {
        WRAP, PROTEIN, VEGGIES, CHEESE, SAUCE
    }
}
```
카산드라에서 사용자 정의 타입 사용하기는 꽤나 까다로왔지만 몽고DB는 수월하다.

```java

@Data
@RestResource(rel="tacos", path = "tacos")
@Document
public class Taco {
    @Id
    private String id;
    
    @NotNull
    @Size(min = 5, message = "Name must be a least 5 character long")
    private String name;
    
    private Date createdAt  = new Date();
    
    @Size(min = 1, message = "You must choose at least 1 ingredient")
    private List<Ingredient> ingredients;
}
```
그냥 Pojo를 선언하듯 선언하면 기본키처리, 사용자 정의 타입 모두 해결된다. 그러나 일단 명심해야할 것은 ```@Id```의 타입은 ```Serializable```을 구현해야 한다.
여기서 String을 사용한 것은 몽고 DB가 자동으로 이 부분을 처리해주기 때문이다.
두 번째는 JPA 같이 List<Ingredient> 이지만 별도의 몽고 DB 컬렉션에 저장하는 것이 아닌 카산드라와 유사하게 비정규화된 상태로 문서에 직접 저장된다.

## 12.3.3 리액티브 몽고DB 레포지토리 인터페이스 작성하기
스프링 데이터 몽고 DB는 스프링 데이터 JPA 및 스프링 데이터 카산드라가 제공하는 것과 유사한 자동 레포지토리 지원을 제공한다. 몽고 DB의 리액티브 레포지토리를 작성할
떄는 ReactiveCrudRepository나 ReactiveMongoRepository를 선택할 수 있다. 둘 간의 차이는 ReactiveCrudRepository가 새로운 문서, 기존 문서의
save() 메소드에 의존하는 반면, ReactiveMongoRepository는 새로운 문서 저장에 최적화된 소수의 특별한 insert를 제공한다.

> ### 리액티브가 아닌 몽고DB 레포지토리는 어떨까?
> 