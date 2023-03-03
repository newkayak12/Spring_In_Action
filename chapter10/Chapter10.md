# 10. 리액터 개요

크게 두 가지 형태로 코드를 작성할 수 있다. 명령형(imperative)와 리액티브(reactive)

- <strong>명령형</strong> 코드는 순차적으로 연속되는 작업이다. 각 작업은 한 번에 하나씩 그리고 이전 작업 다음에 실행된다. 테이터는 모아서 처리되고 이전 작업이 데이터 처리를 끝낸 후 
다음으로 넘어갈 수 있다.
- <strong>리액티브</strong> 코드는 데이터 처리를 위해서 작업들이 정의되기는 하지만 이 작업들이 병렬로 실행될 수 있다. 그리고 각 작업은 부분 집합의 데이터를 처리할 수 있으며, 처리가 끝난 후
끝난 데이터를 다음으로 넘기고 다른 부분 집합의 데이터로 계속 작업할 수 있다.

명령형은 한 번에 하나씩 만나는 순서대로 실행한다. 그리고 프로그램에서는 하나의 작업이 완전히 끝나기를 기다렸다가 다음 작업을 수행한다.
각 단계마다 처리되는 데이터는 전체를 처리할 수 있도록 사용할 수 있어야한다.
그러나 작업이 수행되는 동안 특히, 서버와 서버 사이를 오가는 동안 완료될 때까지는 놀고 있는다. 따라서 이는 자원 낭비가 된다.

물론, 자바를 비롯해서 대부분의 언어는 concurrent Programming을 지원한다. 하나의 쓰레드가 작업을 수행하는 동안 다른 쓰레드로 작업을 실행 시킬 수 있다.
또한 쓰레드를 만드는 것은 쉽지만 다중 쓰레드로 동시성을 관리하는 것은 쉽지 않다. 쓰레드가 많을수록 더 복잡해지기 때문이다. 

이해 반해 리액티브 프로그래밍은 본질적으로 함수적이며, 선언적이다. 즉 순차적으로 수행되는 작업 단위를 나타낸 것이 아닌 데이터가 흘러가는 파이프라인, 스트림을 포함한다.
그리고 이런 리액티브 스트림은 데이터 전체를 사용할 수 있을 때까지 기다리지 않고 사용 가능한 데이터가 있을 때마다 처리되므로 사실상 입력 되는 데이터는 무한할 수도 있다.

## 10.1.1 리액티브 스트림 정의하기
리액티브 스트림은 차단되지 않는 백프레셔(역압력, backPressure)를 갖는 비동기 스트림 처리의 표준을 제공하는 것이 목적이다. 여기서 백프레셔는
데이터를 소비하는 subscriber가 처리할 수 있을 만큼 전달 데이터를 제한함으로써 지나치게 빠른 데이터 소스로부터 데이터 전달 폭주를 피할 수 있는 수단이다.

> ### 자바 스트림 vs. 리액티브 스트림
> 자바 스트림과 리액티브 스트림은 유사성이 있다. streams라는 단어가 이름에 포함되며, 데이터로 작업하기 위한 API를 제공한다. 
> 그러나 자바 스트림은 대개 동기화되어 있고, 한정된 데이터로 작업을 수행한다.
> 리액티브 스트림은 무한 데이터셋을 비롯해서 어떤 크기의 데이터셋이건 비동기 처리를 지원한다. 그리고 실시간으로 데이터를 처리하며, 백프레셔를 사용해서 데이터 
> 전달 폭주를 막는다.


리액티브 스트림은 ```Publisher```, ```Subscriber```, ```Subscription```, ```Processor```로 요약할 수 있다. Publisher는 하나의 Susbscription 당 하나의 Subscriber에 발행하는
데이터를 생성한다.(Unicast) Publisher 인터페이스에는 Subscriber가 Publisher를 구독 신청할 수 있는 ```subscribe()``` 메소드 한 개가 선언되어 있다.

```java
public interface Publisher<T> {
    void subscribe(Subscribe<? super T> subscriber);    
}
```

그리고 Subscriber가 구독신청되면 Publisher로부터 이벤트를 수신할 수 있다. 이 이벤트들은 Subscriber 인터페이스의 메소드를 통해 전송된다.
```java
public interface Subscriber<T> {
    void onSubscribe(Subscription sub);
    void onNext(T item);
    void onError(Throwable ex);
    void onComplete();
}
```
Subscriber가 수신할 첫 이벤트는 ```onSubscribe()```를 통해서 이뤄진다. Publisher가 onSubscribe()를 호출할 때 이 메소드의 인자로 Subscription 객체를
Subscriber에게 전달한다. Subscriber는 Subscription 객체를 통해서 구독을 관리할 수 있다.

```java
public interface Subscription {
    void request( long n );
    void cancel();
}
```

Subscriber는 request()를 호출하여 전송되는 데이터를 요청하거나, 또는 더 이상 데이터를 수신하지 않고 구독을 취소한다는 것을 나타내기 위해서 cancel()을 호출할 수 있다.
request()를 호출할 떄 Subscriber는 받고자 하는 데이터 항목 수를 나타내는 long을 인자로 전달한다. 이것이 백프레셔이며, Subscriber 가 처리할 수 있는 것보다 더 많은 데이터를
Publisher가 전송하는 것을 막는다. 요청된 수의 데이터를 Publisher가 전송한 뒤, Subscriber는 다시 request()를 호출하여 더 많은 요청을 할 수 있다.

Subscriber의 데이터 요청이 완료되면 데이터가 스트림을 통해 전달되기 시작한다. 이때 onNext() 메소드가 호출되어 Publisher가 전송하는 데이터가 Subscriber에게 전달되며, 
만일 에러가 생길 때는 onError()가 호출된다. 그리고 Publisher에서 전송할 데이터가 없고 더 이상 데이터를 생성하지 않는다면 Publisher가 onCompleted() 
를 호출하여 작업이 끝났음을 Subscriber에게 전달한다.

Processor 인터페이스는 다음과 같이 Subscriber, Publisher를 결합한 것이다.(RxJava의 Subject와 유사해 보인다.)
```java
public interface Processor<T,R> extends Subsriber<T>, Publisher<R> { }
```
Subscriber 역할로 Processor는 데이터를 수신하고 처리한다. 그다음에 역할을 바꿔서 Publisher 역할로 처리 결과를 자신의 Subscriber들에게 발행한다. 
이와 같이 Publisher로부터 시작해서 0 또는 그 이상의 Processor를 통해 데이터를 끌어온 다음 최종 결과를 Subscriber에 전달한다.

그러나 리액티브 스트림 인터페이스는 스트림을 구성하는 기능이 없다. 이에 따라 프로젝트 리액터에서는 리액티브 스트림을 구성하는 API를 제공하여 리액티브 스트림 인터
페이스를 구현하였다. 

## 10.2 리액터 시작하기
리액티브 프로그래밍은 명령형과 다르게 작업 단계를 기술하는 것이 아니라. 데이터가 전달될 파이프라인을 구성하는 것이다. 그리고 이 파이프라인을 통해 데이터가 전달되는
동안 어떤 형태로든 변경 또는 사용될 수 있다.

예를 들어서 사람 이름을 가져와서 모두 대문자로 변경한 후 이것을 기반으로 인사말을 출력한다고 하면 
```java
String name = "Swift";
String capitalName = name.toUpperCase();
String greeting = "Hello, " + name + "!";
System.out.println(greeting);
```
이 된다. 각 줄이 한 단계씩 단계로 실행된다. 그리고 각 단계가 완료될 때까지 다음 단계로 이동하지 기다린다.

```java
Mono.just("Swift")
    .map(String::toUpperCase())
    .map(cn -> "Hello, "+cn+"!")
    .subscribe(System.out::println);
```
각 단계별로 실행되는 것처럼 보이겠지만, 실제로는 데이터가 전달되는 파이프라인을 구성하는 것이며, 파이프라인의 각 단계는 어떻게 하든 데이터가 변경된다. 또한, 각
작업은 같은 쓰레드로 동작할 수도 있고 다른 쓰레드로 동작시킬 수도 있다.

이 예의 Mono는 리액터의 두 가지 핵심 타입 중 하나이며, 다른 하나로는 Flux가 있다. 두 개 모두 리액티브 스트림의 Publisher 인터페이스를 구현한 것이다. 
```Flux```는 0, 1 또는 다수의(무한일 수도 있는) 데이터를 갖는 파이프라인을 나타낸다. 반면에 ```Mono```는 하나의 데이터 항목만 갖는 데이터셋에 최적화된 리액티브이다. 

> Reactor vs. RxJava(ReactiveX)
> RxJava의 Observable, Single과 Reactor의 Mono,Flux가 비슷하다는 것을 눈치챌 것이다. 실제로 개념적으로 거의 같으며, 여러 동일한 연산자를 제공한다.
> 또한 실제로 Reactor, RxJava 간 타입 변환도 가능하다. 

## 10.3 리액티브 오퍼레이션 적용하기
Flux와 Mono는 리액터가 제공하는 가장 가장 핵심적인 구성 요소이다. 그리고 Flux와 Mono가 제공하는 오퍼레이션들은 두 타입을 함께 결합하여 데이터가 전달될 수 있는
파이프라인을 생성한다. Flux, Mono는 500개 이상의 오퍼레이션이 있으며, 각 오퍼레이션은 아래와 같이 분류할 수 있다.

- 생성(creation) 오퍼레이션
- 조합(combination) 오퍼레이션
- 변환(transformation) 오퍼레이션
- 로직(logic) 오퍼레이션

## 10.3.1 리액티브 타입 생성하기
스프링에서 리액티브 타입을 사용할 때는 레포지토리나 서비스로부터 Flux, Mono가 제공되므로 굳이 리액티브 타입을 생성할 필요가 없지만, 데이터를 방출하는 새로운 리액티브 Publisher
를 생성해야 할 때가 있다.  리액터는 Flux나 Mono를 생성하는 오퍼레이션을 제공한다.

### 객체로부터 생성하기
Flux, Mono로 생성하려는 하나 이상의 객체가 있다면 just()와 같은 메소드로 리액티브 타입을 생성할 수 있다.
```java
@Test
public void createAFlux_just(){
    Flux<String> fruitFlux = Flux.just("Apple", "Orange", "Grape", "Banana", "Strawberry");
    fruitFlux.subscribe(System.out::println);
}
```
위의 경우 Flux(Observable)을 생성하고 구독자를 붙였다. 여기서 리액티브 타입을 구독한다는 것은 데이터가 흘러갈 수 있게 한다는 것이다.
subscribe()의 매개변수는 ```java.util.Consumer```이며, 이는 리액티브 스트림의 Subscriber 객체를 생성하기 위해서 사용된다. 위의 예시는 Subscribe를 
호출하는 즉시 데이터가 전달된다. 이는 중간에 다른 오퍼레이션이 없기 때문이다.

이처럼 Flux, Mono의 항목들을 콘솔로 출력하면 리액티브 타입이 실제 작동하는 것을 파악하는데 도움이 된다. 그러나 리액터의 StepVerifier를 사용하는 것이 Flux, Mono를
테스트하는 데 더 좋은 방법이다. Flux, Mono가 지정되면 StepVerifier는 해당 리액티브 타입을 구독한 다음에 스트림을 통해 전달되는 데이터에 대해 ```Assertion```을 제공한다.
그리고 해당 스트림이 기대한 대로 완전하게 작동하는지 검사한다.

```java
@Test
public void useStepVerifier(){
    Flux<String> fruitFlux = Flux.just("Apple", "Orange", "Grape", "Banana", "Strawberry");
    StepVerifier.create(fruitFlux)
            .expectNext("Apple")
            .expectNext("Orange")
            .expectNext("Grape")
            .expectNext("Banana")
            .expectNext("Strawberry")
            .verifyComplete();
}
```
이 경우 StepVerifier가 fruitFlux를 구독한 후 각 데이터 항목이 기대한 과일 이름과 일치하는 Assertion을 적용한다. 그리고 fruitFlux가 완전한지 검사한다.


### 컬렉션으로부터 생성하기
Flux는 배열, Iterable 객체, 자바 stream으로부터 생성될 수도 있다.

```java
@Test
public void createFlux_fromArray(){
    String[] fruits = new String[] {"Apple", "Orange", "Grape", "Banana", "Strawberry"};
    Flux<String>  fruitFlux = Flux.fromArray(fruits);
    StepVerifier.create(fruitFlux)
            .expectNext("Apple")
            .expectNext("Orange")
            .expectNext("Grape")
            .expectNext("Banana")
            .expectNext("Strawberry")
            .verifyComplete();
}
```
결과 값은 위의 예와 같다.```java.util.List```, ```java.util.Set``` 또는 ```java.lang.Iterable```의 다른 구현 컬렉션으로부터 Flux를 생성해야 한다면
해당 컬렉션을 인자로 전달하여 static 메소드인 fromIterable()을 호출하면 된다.

```java
@Test
public void createFlux_fromIterable(){
    List<String> fruitList = new ArrayList<>();
    fruitList.add("Apple");
    fruitList.add("Orange");
    fruitList.add("Grape");
    fruitList.add("Banana");
    fruitList.add("Strawberry");
    Flux<String> fruitFlux = Flux.fromIterable(fruitList);
    StepVerifier.create(fruitFlux)
            .expectNext("Apple")
            .expectNext("Orange")
            .expectNext("Grape")
            .expectNext("Banana")
            .expectNext("Strawberry")
            .verifyComplete();
}
```
Stream은 fromStream()을 호출하면 된다.
```java
    @Test
    public void createFlux_fromStream(){
       Stream<String> fruitStream = Stream.of("Apple", "Orange", "Grape", "Banana", "Strawberry");
        Flux<String> fruitFlux = Flux.fromStream(fruitStream);
        StepVerifier.create(fruitFlux)
                .expectNext("Apple")
                .expectNext("Orange")
                .expectNext("Grape")
                .expectNext("Banana")
                .expectNext("Strawberry")
                .verifyComplete();
    }
```

### Flux 데이터 생성하기
때로는 데이터 없이 매번 새로운 값으로 증가하는 숫자를 방출하는 카운터 역할의 Flux만 필요한 경우가 있다. 이와 같은 카운터 Flux를 생성할 때는 static 메소드인 range()
를 사용할 수 있다.

```java
@Test
public void createAFlux_Range(){
    Flux<Integer> intervalFlux = Flux.range(1, 5);
    StepVerifier.create(intervalFlux)
                .expectNext(1)
                .expectNext(2)
                .expectNext(3)
                .expectNext(4)
                .expectNext(5)
                .verifyComplete();
}
```

유사한 예로 interval()이 있다. range() 처럼  interval()도 증가값을 방출하는 Flux를 생성한다. 그러나 시작~종료 값이 아닌 방출 주기를 지정한다.


```java
@Test
public void createAFlux_interval(){
    Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1))
                                  .take(5);
    StepVerifier.create(intervalFlux)
            .expectNext(1L)
            .expectNext(2L)
            .expectNext(3L)
            .expectNext(4L)
            .expectNext(5L)
            .verifyComplete();
}
```
 위의 예는 take()로 무한정 방출되는 것을 막는다.
 
## 10.3.2 리액티브 타입 조합하기
두 개의 리액티브 타입을 결합하거나 Flux 두 개를 이상의 리액티브 타입으로 분할해야 필요가 생길 수 있다. 

### 리액티브 타입 결합하기
두 개의 Flux를 하나의 Flux로 결합하려면 mergeWith(Rx에서 concat?) 오퍼레이션을 사용하면 된다.
```java
 @Test
public void mergeFluxes() {
    Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbosa").delayElements(Duration.ofMillis(500));
    Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples").delaySubscription(Duration.ofMillis(250)).delayElements(Duration.ofMillis(500));

    Flux<String> mergedFlux = characterFlux.mergeWith(foodFlux);
    StepVerifier.create(mergedFlux)
            .expectNext("Garfield")
            .expectNext("Lasagna")
            .expectNext("Kojak")
            .expectNext("Lollipops")
            .expectNext("Barbosa")
            .expectNext("Apples")
            .verifyComplete();
}
```
일반적으로 Flux는 가능한 빨리 데이터를 방출하지만 위 예시에서는 delayElements로 500밀리초 늦췄다. 또한 foodFlux가 characterFlux 다음 스트리밍을 시작하도록
foodFlux에 delaySubscription을 사용해서 250밀리초가 지난 후 구독 및 데이터를 방출하도록 했다. 

mergeWith은 소스 Flux 들이 완벽하게 번갈아 방출되도록 할 수 없으므로 zip()을 대신 사용할 수 있다.
```java
@Test
public void zipFluxes() {
    Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbosa").delayElements(Duration.ofMillis(500));
    Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples").delaySubscription(Duration.ofMillis(250)).delayElements(Duration.ofMillis(500));

    Flux<Tuple2<String, String>> zippedFlux = Flux.zip(characterFlux, foodFlux);
    StepVerifier.create(zippedFlux)
            .expectNextMatches(p ->
                        p.getT1().equals("Garfield") &&
                        p.getT2().equals("Lasagna")
                    )
            .expectNextMatches(p ->
                    p.getT1().equals("Kojak") &&
                            p.getT2().equals("Lollipops")
            )
            .expectNextMatches(p ->
                    p.getT1().equals("Barbosa") &&
                            p.getT2().equals("Apples")
            )
            .verifyComplete();
}
```
zip은 정적인 연산자고 번갈아가며 조합된다. zip으로 방출되는 요소들은 Tuple(컨테이너)이며, 각 소스 Flux가 순서대로 방출하는 항목을 포함한다. Tuple이 싫다면
마지막인자로 BiFunction을 전달하면 원하는 대로 연산하여 방출할 수 있다.
```java
@Test
public void zipFluxesToObj() {
    Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbosa").delayElements(Duration.ofMillis(500));
    Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples").delaySubscription(Duration.ofMillis(250)).delayElements(Duration.ofMillis(500));

    Flux<Object> zippedFlux = Flux.zip(characterFlux, foodFlux, (s1, s2) -> String.format("%s eats %s", s1, s2));
    StepVerifier.create(zippedFlux)
            .expectNext("Garfield eats Lasagna")
            .expectNext("Kojak eats Lollipops")
            .expectNext("Barbosa eats Apples")
            .verifyComplete();
}
```

### 먼저 값을 방출하는 리액티브 타입 선택
두 개의 Flux 객체가 있는데, 이것을 결합하는 대신 먼저 값을 방출하는 소스 Flux의 값을 발행하는 새로운 Flux를 생성하고 싶다고 해보자. fisrt() 연산자는 두 FLux
중에서 먼저 값을 방출하는 Flux의 값을 선택해서 이 값을 발행한다.

```java
@Test
public void firstFlux() {
    Flux<String> slowFlux = Flux.just("tortoise", "snail", "sloth").delaySubscription(Duration.ofMillis(100));
    Flux<String> fastFlux = Flux.just("hare", "cheetah", "squirrel");

    Flux<String> firstFlux = Flux.firstWithSignal(slowFlux, fastFlux);
    StepVerifier.create(firstFlux)
            .expectNext("hare")
            .expectNext("cheetah")
            .expectNext("squirrel")
            .verifyComplete();
}
```
first()를 이용하여 새로운 Flux를 사용한다. 이 Flux는 먼저 값을 방출하는 소스 Flux의 값만 발행한다. first()는 느린 Flux는 무시하고 빠른 Flux만 방출한다.

## 10.3.3 리액티브 스트림의 변환과 필터링
데이터가 흐르는 동안 필터링/ 매핑해야할 경우가 있다.

### 리액티브 타입으로부터 데이터 필터링하기
Flux로부터 데이터가 전달될 때 이를 필터링하는 가장 기본적인 방법은 맨 앞부터 원하는 개수의 항목을 무시하는 것이다. 이때 ```skip()```을 사용한다.
skip()은 지정된 수만큼 Flux 방출을 건너띈 후 나머지 항목을 방출하는 새로운 Flux를 생성한다.

```java
@Test
public void skipAFew(){
Flux<String> skipFlux = Flux.just("one", "two", "skip a few", "ninety nine", "hundred").skip(3);

StepVerifier.create(skipFlux)
        .expectNext( "ninety nine")
        .expectNext("hundred")
        .verifyComplete();
}
```

특정 개수가 아닌 일정 시간 경과로도 건너뛸 필요가 있는 경우가 있다. 이때도 skip을 쓴다.

```java
@Test
public void skipAFewSeconds(){
    Flux<String> skipFlux = Flux.just("one", "two", "skip a few", "ninety nine", "hundred")
            .delayElements(Duration.ofSeconds(1))
            .skip(Duration.ofSeconds(4));

    StepVerifier.create(skipFlux)
            .expectNext( "ninety nine")
            .expectNext("hundred")
            .verifyComplete();
}
```
skip() 연산자 반대 기능이 필요하다면 take()를 고려해볼만 하다.

```java
@Test
public void take(){
    Flux<String> nationalParkFlux = Flux.just("YellowStone", "Yosemite", "Grand Canyon", "Zion", "Grand Teton").take(3);
    StepVerifier.create(nationalParkFlux).expectNext("YellowStone", "Yosemite", "Grand Canyon").verifyComplete();
}
```
take()도 시간을 기준으로 할 수 있다. 
```java
@Test
public void takeSeconds(){
    Flux<String> nationalParkFlux = Flux.just("YellowStone", "Yosemite", "Grand Canyon", "Zion", "Grand Teton")
            .delayElements(Duration.ofSeconds(1))
            .take(Duration.ofMillis(3500));
    StepVerifier.create(nationalParkFlux).expectNext("YellowStone", "Yosemite", "Grand Canyon").verifyComplete();
}
```
이는 일종의 필터링으로도 볼 수 있는데 더 범용적 필터링은 ```filter()```로 진행하면 된다. filter의 파리미터(Predicate)을 기반으로 필터링할 수 있다.
```java
@Test
public void filter(){
    Flux<String> nationalParkFlux = Flux.just("YellowStone", "Yosemite", "Grand Canyon", "Zion", "Grand Teton")
                    .filter(n -> !n.contains(" "));
    StepVerifier.create(nationalParkFlux).expectNext("YellowStone", "Yosemite", "Zion").verifyComplete();
}
```

경우에 따라서 이미 발행되어 수신된 항목을 필터링할 필요도 있을 것이다. ```distinct()```로 발행된 적이 없는 Flux만 발행할 수도 있다.

````java
@Test
public void distinct(){
    Flux<String> animalFlux = Flux.just("dog", "cat", "bird", "dog", "bird", "anteater").distinct();
    StepVerifier.create(animalFlux)
            .expectNext("dog", "cat", "bird", "anteater")
            .verifyComplete();
}
````

### 리액티브 데이터 매핑하기
Flux, Mono에 가장 많이 사용하는 연산자 중 하나는 발행된 항목을 다른 형태, 타입으로 매핑하는 것이다. 이런 경우 ```flat()```, ```flatMap()``` 연산자를 제공한다.
map() 오퍼레이션은 변환을 수행하는 Flux를 생성한다.

```java
@Test
public void map(){
    Flux<List<String>> playerFlux = Flux.just("Michael Jordan", "Scottie Pippen", "Steven Kerr")
            .map(n -> Arrays.asList(n.split(" ")[0], n.split(" ")[1]));
    StepVerifier.create(playerFlux)
            .expectNext(Arrays.asList("Michael", "Jordan"))
            .expectNext(Arrays.asList("Scottie", "Pippen"))
            .expectNext(Arrays.asList("Steven", "Kerr"))
            .verifyComplete();
}
```

map()에 지정된 함수로 가공을 해서 List<String>을 발행한다. map()에서 중요한 것은 각 항목이 소스 Flux로부터 발행될 때 동기적으로 매핑이 수행된다는 것이다.
따라서 비동기적으로 매핑을 수행하고 싶다면 flatMap()을 사용해야 한다.

그러나 flatMap()은 각 객체를 새로운 Mono, Flux로 매핑하며, 해당 Mono나 Flux들의 결과는 하나의 새로운 Flux가 된다.
flatMap()을 subscribeOn()과 함께 사용하면 리액터 타입의 변환을 비동기적으로 수행할 수 있다.

```java
@Test
public void flatMap() {
    Flux<List<String>> playerFlux = Flux.just("Michael Jordan", "Scottie Pippen", "Steven Kerr")
            .flatMap(a -> Mono.just(a)
                    .map(n -> Arrays.asList(n.split(" ")[0], n.split(" ")[1]))
                    .subscribeOn(Schedulers.parallel())
            );
    List<List<String>> playerList = Arrays.asList(
            Arrays.asList("Michael", "Jordan"),
            Arrays.asList("Scottie", "Pippen"),
            Arrays.asList("Steven", "Kerr")
    );

    StepVerifier.create(playerFlux)
            .expectNextMatches(p -> playerList.contains(p))
            .expectNextMatches(p -> playerList.contains(p))
            .expectNextMatches(p -> playerList.contains(p))
            .verifyComplete();
}
```

다시 언급하지만 map은 1:1로 동기적으로 flatMap은 1:N으로 비동기적으로 작동한다. map은 원소를 하나씩 받으며, flatMap은 array, object의 원소를 단일 원소 스트림으로 반환한다.
subscribeOn()은 구독이 동시적으로 처리되어야 한다는 것을 지정한다. 리액터는 어떤 특정 동시성 모델도 강요하지 않으며 우리가 사용하기 원하는 동시성 모델을 subscribeOn()의
인자로 지정할 수 있다. 이 때 Schedulers의 static 메소드 중 하나를 사용한다.

#### Schedulers의 동시성 모델
| Schedulers 메소드 |                                            개요                                            |
|:--------------:|:----------------------------------------------------------------------------------------:|
|  .immediate()  |                                    현재 쓰레드에서 구독을 실행한다.                                    |
|   .single()    |                  단일의 재사용 가능한 쓰레드에서 구독을 실행한다. 모든 호출자에 대해 동일한 쓰레드를 재사용한다.                  |
|  .newSingle()  |                                매 호출마다 전용 쓰레드에서 구독을 실행한다.                                 |
|   .elastic()   | 무한하고 신축성 있는 풀에서 가져온 작업 쓰레드에서 구독을 실행한다. 필요 시 새로운 작업 쓰레드가 생성되며, 유휴 쓰레드는 제거된다.(기본적으로 60초 후) |
|  .parallel()   |                  고정된 크기의 풀에서 가져온 작업 쓰레드에서 구독을 실행하며, CPU 코어의 개수가 크기가 된다.                  |

flatMap()이나 subscribe()을 사용할 떄의 장점은 다수의 병행 쓰레드에 작업을 분할하여 스트림의 처리량을 증가시킬 수 있다. 그러나 작업이 병행으로 수행되므로
어떤 작업이 먼저 끝날지 보장이 안된다.

### 리액티브 스트림의 데이터 버퍼링하기
Flux를 통해 전달되는 데이터를 처리하는 동안 데이터 스트림을 작은 덩어리로 분할하면 도움이 될 수 있다.  ```buffer()``` 연산자를 사용한다.
```java
@Test
public void buffer(){
    Flux<String> fruitFlux = Flux.just("apple", "orange", "banana", "kiwi", "strawberry");
    Flux<List<String>> bufferedFlux = fruitFlux.buffer(3);

    StepVerifier.create(bufferedFlux)
            .expectNext(Arrays.asList("apple", "orange", "banana"))
            .expectNext(Arrays.asList("kiwi", "strawberry"))
            .verifyComplete();
}
```
지정한 버퍼만큼 새로운 Flux로 버퍼링한다. 이렇게 buffer를 사용하고 flatMap과 같이 사용하면 List 컬렉션을 병행 처리할 수 있다.
```java
@Test
public void buffer2(){
    Flux.just("apple", "orange", "banana", "kiwi", "strawberry")
        .buffer(3)
        .flatMap( x ->
            Flux.fromIterable(x).map(y -> y.toUpperCase())
                .subscribeOn(Schedulers.parallel())
                .log()
        ).subscribe();
}
```

buffer를 사용하면 새로운 Flux로 버퍼링하지만 이 Flux는 List를 포함한다. 그러나 그 다음 flatMap으로 풀어헤쳐서 새로운 Flux를 만들고 map을 적용한다.
만약 Flux가 방출하는 모든 항목을 List로 모을 필요가 있다면 인자를 전달하지 않고 ```buffer()```를 호출하면 된다.
이 경우 소스 Flux가 발행한 모든 항목을 포함하는 List를 방출하는 새로운 Flux가 생성된다. ```collectList()```를 사용해도 같은 결과를 얻을 수 있다.
```collectList()```는 List를 발행하는 Flux 대신 Mono를 생성한다.

```java
@Test
public void collectList() {
    Flux<String> fruitFlux = Flux.just("apple", "orange", "banana", "kiwi", "strawberry");
    Mono<List<String>> fruitListMono = fruitFlux.collectList();
    
    StepVerifier.create(fruitListMono).expectNext(Arrays.asList("apple", "orange", "banana", "kiwi", "strawberry")).verifyComplete();
}
```

Flux 방출 항목을 모으는 다른 방법으로 collectMap()이 있다.
```java
@Test
public void collectMap(){
    Flux<String> animalFlux = Flux.just("aardvark", "elephant", "koala", "eagle", "kangaroo");
    Mono<Map<Character, String>> animalMono = animalFlux.collectMap(a -> a.charAt(0));

    StepVerifier.create(animalMono).expectNextMatches(map ->
                map.size() == 3
                && map.get('a').equals("aardvark")
                && map.get('e').equals("eagle")
                && map.get('k').equals("kangaroo")
    ).verifyComplete();
}
```
## 10.3.4 리액티브 타입에 로직 오퍼레이션 수행하기
