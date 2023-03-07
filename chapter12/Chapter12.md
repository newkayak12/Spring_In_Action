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