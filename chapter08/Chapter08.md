# 비동기 메시지 전송하기

이전 장에서는 REST를 사용한 동기 통신에 대해서 알아봤다. 그러나 꼭 이러한 통신만 사용해야하는 것은 아니다. 비동기 메시징은 애플리케이션 간에 
응답을 기다리지 않고 간접적으로 메시지를 전송하는 방법이다. 이렇게 되면 애플리케이션 간의 결합도를 낮추고 확장성은 높여준다.

이 경우 여러 가지의 비동기 메시징 옵션을 고려한 수 있다. JMS(Java Message Service), RabbitMQ, AMQP(Advanced Message Queueing Protocol),
아파치 카프카(Apache Kafka)이다. 그리고 기본적인 메시지 전송과 수신에 추가하여, 스프링 메시지 기반 POJO 지원에 대해서 알아볼 것이다.

## JMS로 메시지 전송

JMS는 두 개 이상의 클라이언트 간에 메시지 통신을 위한 공통 API를 정의하는 자바 표준이다. JMS가 나오기 전에는 클라이언트 간에 메시지 통신을 중개하는 메시지 브로커
들이 나름의 API를 가지고 있어서 애플리케이션의 메시징 코드가 브로커 간에 호환될 수 없었다. 그러나 JMS를 사용하면 이것을 준수하는 모든 구현 코드가 공통
인터페이스를 통해 함께 작동할 수 있다. 

스프링은 JmsTemplate이라는 템플릿 기반의 클래스를 통해 JMS를 지원한다. JmsTemplate을 사용하면 프로듀서(producer)가 큐와 토픽에 메시지를 전송하고 컨슈머
(consumer)는 그 메시지를 받을 수 있다. 또한, 스프링은 메시지 기반의 POJO도 지원한다. POJO는 큐나 토픽에 도착하는 메시지에 반응하여 비동기 방식으로 메시지를 
수신하는 간단한 자바 객체이다.

## 8.1.1 JMS 설정하기
JMS를 사용할 수 있으려면 JMS 클라이언트를 추가해야한다. 우선 아파치 ActiveMQ 또는 최신의 ActiveMQ Artemis 중 어떤 브로커를 사용할 지 결정해야한다.

```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <atrifactId>spring-boot-starter-artemis</atrifactId>
    </dependency>
```

기본적으로 스프링은 Artemis 브로커가 localhost 61616 포트를 리스닝하는 것으로 간주한다. 애플리케이션 개발 시에는 이렇게 해도 문제가 없다.
그러나 실무 환경으로 애플리케이션을 이양할 떄는 해당 브로커를 어떻게 사용하는지 스프링에게 알려주는 몇 가지 속성을 설정해야한다.

```yaml
## artemis
spring:
  artemis: 
    host: #브로커 호스트
    port: #브로커 포트
    user: #브로커를 위한 사용자 (선택)
    password: #브로커를 사용하기 위한 사용자 암호(선택)
## ActiveMQ
  activemq:
    broker-url: # 브로커의 URL
    user: # 브로커를 사용하기 위한 사용자 (선택)
    password: # 브로커를 사용하기 위한 사용자 암호 (선택)
    in-memory: # 인메모리 브로커로 시작할 것인지에 대한 여부 (기본 true)
```

## 8.1.2 JmsTemplate을 사용해서 메시지 전송
JMS 스타터 의존성이 우리 빌드에 지정되면, 메시지를 송수신하기 위해 주입 및 사용할 수 있는 JmsTemplate을 스프링 부트가 자동-구성 한다.
JmsTemplate은 스프링 JMS 통합 지원의 핵심이다. 스프링의 다른 템플릿 기반 컴포넌트와 마찬가지로, JmsTemplate는 JMS로 작업하는데 필요한 코드를 줄여준다. 
만일 JmsTemplate이 없다면 메시지 브로커와 연결 및 세션을 생성하는 코드는 물론이고, 메시지를 전송하는 도중 발생할 수 있는 예외를 처리하는 코드도 우리가 작성해야한다.
JmsTemplate는 '메시지 전송'에만 집중할 수 있게 해준다. 

JmsTemplate은 다음과 같은 유용한 메소드를 갖는다.

```java

public interface JmsTemplate {
    //원시 메시지를 전송 
    void send(MessageCreator messageCreator) throws JmsException;
    void send(Destination destination, MessageCreateor messageCreateor) throws JmsException;
    void send(String destinationName, MessageCreator messageCreator) throws JmsException;

    //객체로부터 변환된 메시지를 전송한다. 
    void convertAndSend(Object message) throws JmsException;
    void convertAndSend(Destination destination, Object message) throws JmsException;
    void convertAndSend(String destination, Object message) throws JmsException;
    
    //객체로부터 변환되고 전송에 앞서 후처리(Post-Prcessing)되는 메시지를 전송한다.
    void convertAndSend(Object message, MessagePostProcessor postProcessor) throws JmsException;
    void convertAndSend(Destination destination, Object message, MessagePostProcessor postProcessor)  throws JmsException;
    void convertAndSend(String destinationName, Object message, MessagePostProcessor postProcessor)  throws JmsException;
}
```

실제로는 ```send()```, ```convertAndSend()``` 두 가지의 메소드만 있으며, 각 메소드는 서로 다른 매개변수를 지원하기 위해서 오버로딩되어 있다.

1. 맨 앞의 send()는 Message 객체를 생성하기 위해서 MessageCreator를 필요로 한다.
2. 중간의 convertAndSend()는 Object 타입의 객체를 Message로 바꾼다.
3. 마지막의 convertAndSend() 메소드는 Object 타입 객체를 Message 타입으로 변환한다. 그러나 메시지가 전송되기 전에 Message의 커스터마이징을
할 수 있도록 MessagePostProcessor도 인자로 받는다.

게다가 이들 3개의 메소드 부류 각각은 3개의 오버로딩된 메소드로 구성되며, 이 메소드들은 JMS 메시지의 도착지(destination), 즉 메시지를 쓰는 곳을 지정하는 법이
다르다.

1. 첫 번째 메소드는 도착지 매개 변수가 없으며, 해당 메시지를 기본 도착지로 전송한다.
2. 두 번째 메소드는 해당 메시지의 도착지를 나타내는 Destination 객체를 인자로 받는다.
3. 세 번쨰 메소드는 해당 도착지를 나타내는 문자열을 인자로 받는다.

```java
@Service
@RequiredArgsConstructor
public class JmsOrderMessageService implements OrderMessagingService{
    private final JmsTemplate jms;

    @Override
    public void sendOrder(Order order) {
        jms.send(session -> session.createObjectMessage(order));
    }
}
```
```yaml
spring:
  jms:
    template:
      default-destination: tacocloud.order.queue
```
이렇게 하면 메시지를 보내는 것 자체에 집중할 수 있다. 그러나 기본 도착지가 아닌 다른 곳에 메시지를 전송해야할 필요가 있다면
```send()``` 메소드에 매개변수로 도착지를 지정하면 된다. 
```java

@Component
public class Config {


    @Bean
    public Destination orderQueue(){
        return new ActiveMQQueue("tacocloud.order.queue");
    }
}
```

```java

@Service
@RequiredArgsConstructor
public class JmsOrderMessageService implements OrderMessagingService{
    private final JmsTemplate jms;
    private final Destination orderQueue;

    @Override
    public void sendOrder(Order order) {
        jms.send(orderQueue, session -> session.createObjectMessage(order));
        
        // 또는 
        
        //jms.send("tacocloud.order.queue", , session -> session.createObjectMessage(order));
        
    }

}
```

### 메시지 변환하고 전송하기
JmsTemplate의 convertAndSend()는  MessageCreator를 제공하지 않아도 되므로 메시지 전송이 간단하다. 즉, 전송될 객체를 ```convertAndSend()```
의 인지로 직접 전달하면 해당 객체가 Message로 변환되어 전송된다.

```java

@Override
public void sendOrder(Order order){
        jms.convertAndSend("tacocloud.order.queue",order);
}
```

이러한 POJO를 Message로 변환하는 일은 MessageConverter를 구현해서 처리할 수 있다. 


### 메시지 변환기 구현하기 
MessageConverter는 스프링에 정의된 인터페이스이며, 두 개의 메소드만 정의되어 있다. 
```java
public interface MessageConverter {
    Message toMessage(Object object, Session session) throws  JMSException, MessageConversionException;
    Object fromMessage(Message message);
}
```
이 인터페이스는 스프링에 이미 구현되어 있기 때문에 따로 구현할 필요까지는 없다.

|             메시지 컨버터             |                                                       하는 일                                                       |
|:-------------------------------:|:----------------------------------------------------------------------------------------------------------------:|
| MappingJackson2MessageConverter |                                     jackson2 JSON 라이브러리를 이용해서 메시지는 JSON으로 변환                                     |
|   MarshallingMessageConverter   |                                                JAXB를 사용해서 XML로 변환                                                |
|    MessagingMessageConverter    | 수신된 메시지의 MessageConberter를 사용해서 해당 메시지를 Message 객체로 상호 변환 <br/> 또는 JMS 헤더와 연관된 JmsHeaderMapper를 표준 메시지 헤더로 상호 변환 |
|     SimpleMessageConverter      |         문자열을 TextMessage로, byte 배열을 BytesMessage로 Map을 MapMessage로 Serializable 객체를 ObjectMessage로 상호 변환         |


기본적으로 SimpleMessageConverter를 사용하며, 이 경우 전송될 객체가 Serializable을 구현해야한다. 이러한 제약을 피하려면 MappingJackson2MessageConverter와
같은 메시지 변환기를 사용할 수도 있다.

이 경우 MappingJackson2MessageConverter의 setTypeIdPropertyName() 메소드를 호출한 뒤 이 메시지 변환기 인스턴스를 반환한다. 수신된 메시지의 변환 타입을 메시지
수신자가 알아야 하기 때문이 이 부분이 중요하다. 여기서는 변환되는 타입의 클래스 이름이 포함된다. 그러나 이는 유연성이 다소 떨어진다. 메시지 수신자도 같은 클래스와 타입을 가져야하기 때문이다.

따라서 유연성을 높이기 위해서 메시지 컨버터의 ```setTypeIdMappings()```를 호출하여 실제 타입에 임의의 타입 이름을 매핑시킬 수 있다. 

```java
   @Bean
public MappingJackson2MessageConverter messageConverter() {
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setTypeIdPropertyName("_typeId");

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("order", Order.class);
        messageConverter.setTypeIdMappings(typeIdMappings);
        return messageConverter;
        }
```

이러면 발신지와 수신지가 꼭 같은 Order를 갖고 있을 필요는 없게 된다.


### 후처리 메소드
때떄로 커스텀 헤더를 메시지에 추가하는 것이 필요할 때가 있다. 만일 ```send()```를 사용해서 타코 주문을 전송한다면 Message 객체의 setStringProperty()를 호출하면 된다.

```java
    jms.send("tacocloud.order.queue", session -> {
            Message message = session.createObjectMessage(order);
            message.setStringProperty("X_ORDER_SOURCE", "WEB");
        })
    
```

하지만 다행스럽게도 내부적으로 생성된 Message 객체를 전송 전에 변경할 수 있는 방법이 있다. 즉, ```convertAndSend()```의 마지막 인자로
MessagePostProcessor를 전달하면 된다. 
```java

jms.convertAndSend("tacoCloud.order.queue", order, new MessagePostProcessor() {
@Override
public Message postProcessMessage(Message message) throws JMSException {
        message.setStringProperty("X_ORDER_SOURCE", "WEB");
        return message;
        }
})
```

여기서 MessagePostProcessor는 함수형 인터페이스이다. 따라서 익명구현을 람다로 교체할 수도 있다.
```java
@RestController
@RequiredArgsConstructor
public class JmsOrderController {
    private final JmsTemplate jms;

    @GetMapping(value = "/convertAndSend/order")
    public String convertAndSendOrder(){
        Order order = new Order();
        jms.convertAndSend("tacocloud.order.queue", order, this::addOrderSource);
        return "CONVERT AND SENT ORDER"
    }

    private Message addOrderSource(Message message) throws JMSException {
        message.setStringProperty("X_ORDER_SOURCE", "WEB");
        return message;
    }
}
```


## 8.1.3 JMS 메시지 수신하기

메세지를 수신하는 방식에는 두 가지가 있다. 우리 코드에서 메시지를 요청하고 도착할 때까지 기다리는 풀 모딜(Pull Model)과 메시지가 수신 가능하게 되면 우리 
코드로 자동 전달하는 푸시 모델(Push Model)이다.

JmsTemplate은 메시지를 수신하는 여러 개의 메소드를 제공하지만, 코든 메시드가 풀 모델을 사용한다. 따라서 이 메소드 중 하나를 호출하여 메시지를 요청하면
쓰레드에서 메시지를 수신할 수 있을 때까지 기다린다.
이와는 달리 푸시 모델을 사용할 수도 있으며, 이때는 언제든 메시지가 수신 가능할 떄 자동으로 호출되는 메시지 리스너를 정의한다.

두 가지 방식 모두 용도에 맞게 사용할 수 있다. 그러나 쓰레드의 실행을 막지 않으므로 일반적으로 푸시 모델이 좋은 선택이다. 단, 많은 메시지가 너무 빨리 도착한다면
리스너에 과부하가 걸리는 경우가 생길 수 있다.

### JmsTemplate을 사용해서 메시지 수신하기
다음 메소드를 포함해서 JmsTemplate은 브로커로부터 메시지를 가져오는 여러 개의 메소드를 제공한다.

```java
interface receive{
    Message receive() throws JmsException;
    Message receive(Destination destination) throws JmsException;
    Message receive(String destinationName) throws JmsException;
    
    Object receiveAndConvert() throws JmsException;
    Object receiveAndConvert(Dstination destination) throws JmsException;
    Object receiveAndConvert(String destinationName) throws JmsException;
}
```
메소드들은 메시지를 전송하는 JmsTemplate의 send(), convertAndSend() 메소드와 대응한다. receive는 원시 메시지를 수신하는 반면,
receiveAndConvert() 메소드는 메시지를 도메인 타입으로 변환하기 위해서 구성된 메시지 변환기를 사용한다. 그리고 각 메소드에는 도착지 이름을 갖는
Destination 객체나 문자열을 지정하거나 기본 도착지를 사용할 수 있다.

실제 사용하는 방법을 알기 위해서 tacocloud.order.queue 도착지로부터 Order 객체를 가져오는 코드를 보자.

```java

@Component
@RequiredArgsConstructor
public class Config implements OrderReceiver{
    private final JmsTemplate jms;
    private MessageConverter converter;

    @Override
    public Order receiveOrder() {
        try {
            return (Order) converter.fromMessage(jms.receive("tacocloud.order.queue"));
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
```
여기서는 주문 데이터를 가져올 도착지를 문자열로 지정하였다. ```receive()```는 변환되지 않은 메시지를 반환한다. 그러나 여기서 필요한 것은 메시지 내부의  Order가 될 것이다.
따라서 주입된 메시지 변환기로 변환을 한다. 이 경우는 메시지의 속성과 헤더를 살펴봐야할 때 필요하다. 그러나 그런 메타 데이터는 필요 없고 페이로드만 필요하다면 
```receiveAndConvert()```가 더 적절할 것이다.

```java
@Component
@RequiredArgsConstructor
public class Config implements OrderReceiver{
    private final JmsTemplate jms;
    private MessageConverter converter;

    @Override
    public Order receiveOrder() {
        return (Order) jms.receiveAndConvert("tacocloud.order.queue");
    }
}
```
이렇게하면 메시지 변환이 ```receiveAndConvert``` 안에서 시작된다. 


### 메시지 리스너 선언하기
receive()나 receiveAndConvert()를 호출해야하는 풀 모델과는 달리, 메시지 리스너는 메시지가 도착할 때까지 대기하는 수동적 컴포넌트이다.

JMS 메시지에 반응하는 메시지 리스너를 생성하려면 컴포넌트의 메소드에 ```@JmsListener```를 지정해야한다. 이러면 능동적으로 메시지를 요구하는 것이 아닌 수동적으로 
리스닝하는 새로운 컴포넌트를 보여준다.

```java

@Component
@RequiredArgsConstructor
public class PushConfig {
    private final KitchenUI ui;

    @JmsListener(destination = "tacocloud.order.queue")
    public void receiveOrder(Order order){
        ui.displayOrder(order);
    }
}

```
이러면 JmsTemplate을 사용하지 않으며, 우리 애플리케이션 코드에서도 호출되지 않느다. 대신 스프링 프레임워크 코드가 특정 도착지에 메시지가 도착하는 것을 기다리다가
도착하면 해당 메시지에 적재된 Order 객체가 인자로 전달되면서 receiveOrder() 메소드가 자동 호출된다. 메시지 리스너는 중단 없이 다수의 메시지를 빠르게 처리할 수 있어서
좋은 선택이 될 때가 있다. 그러나 우리 애플리케이션은 사람이 직접 조리하는 시간으로 심각한 병목을 초래할 가능성이 있다. 주방이 과부하 걸리지 않도록 도착 주문을 버퍼링해야 한다.
그렇다고 메시지 리스너가 나쁘다는 것은 아니다. 오히려 메시지가 빠르게 처리될 수 있을 때 딱 맞는다. 그러나 메시지 처리기가 자신의 시간에 맞춰 더 많은 메시지를 요청할 수 
있어야 한다면 JmsTemplate이 제공하는 풀 모델이 더 적합할 것이다. 