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
