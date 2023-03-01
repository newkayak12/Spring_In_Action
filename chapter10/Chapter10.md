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