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


## 8.2 RabbitMQ, AMQP 사용하기
기존 JMS는 자바 명세이므로 자바 애플리케이션에서만 사용할 수 있다는 것이 단점이 도니다. RabbitMQ, 카프카와 같은 메시징 시스템은 이런 단점을 해결하여 다른 언어와
JVM 외의 다른 플랫폼에서 사용할 수 있게 했다.

AMQP의 가장 중요한 구현이라고 할 수 있는 RabbitMQ는 JMS보다 더 진보된 메시지 라우팅 전략을 제공한다. JMS 메시지가 수신자가 가져갈 메시지 도착지의 이름을 주소로 
사용하는 반면, AMQP 메시지는 수신자가 리스닝하는 큐와 분리된 거래소 이름과 라우팅 키를 주소로 사용한다.

```
                <RabbitMQ 브로커>             
                |                            
                |           바인딩 -> 큐          -> 메시지 수신자 
메시지 전송자  ->     거래소  〈                      
                |           바인딩 -> 큐          -> 메시지 수신자
                |                           
```

메시지가 RabbitMQ 브로커에 도착하면 주소로 지정된 거래소에 들어간다. 거래소는 하나 이상의 큐에 메시지를 전달할 책임이 있다. 이때 거래소 타입, 거래소와 큐 간의 바인딩,
메시지의 라우팅 키 값을 기반으로 처리한다.  거래소는 아래와 같은 종류가 있다.

 * 기본(Default) : 브로커가 자동으로 생성하는 특별한 거래소, 해당 메시지의 라우팅 키와 이름이 같은 큐로 메시지를 전달한다. 모든 큐는 자동으로 기본 거래소와 
연결된다. 
 * 다이렉트(Direct) : 바인딩 키가 해당 메시지의 라우팅 키와 같은 큐에 메시지를 전달한다. (Unicast)
 * 토픽(Topic) : 바인딩 키(와일드 카드를 포함하는)가 해당 메시지의 라우팅 키와 일치하는 하나의 이상의 큐에 메시지를 전달한다. (Multicast)
 * 팬아웃(Fanout) : 바인딩 키나 라우팅 키에 상관 없이 모든 연결된 큐에 메시지를 전달한다. (Broadcast)
 * 헤더(Header) : 토픽 거래소와 유사하며, 라우팅 키 대신 메시지 헤더 값을 기반으로 한다는 것이 다르다. (Multicast)
 * 데드 레터(Dead letter) : 전달 불가능한 즉, 정의된 어떤 거래소-큐 바인딩과는 일치하지 않는 모든 메시지를 보관하는 잡동사니 거래소이다.


기본 형태는 팬아웃이며, 이들은 JMS의 큐 및 토픽과 거의 일치한다. 그러나 다른 거래소들을 사용하면 더 유연한 라우팅 스킴을 정의할 수 있다. 메시지는 라우팅 키를 
갖고 거래소로 전달되고 큐에서 읽혀져 소비된다는 것을 이해하는 것이 가장 중요하다. 메시지는 바인딩 정의를 기반으로 거래소로부터 큐로 전달된다. 

## 8.2.1 RabbitMQ를 스프링에 추가하기
```xml
<dependency>
    <groupId>org.springframwork.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```
이처럼 AMQP 스타터를 빌드에 추가하면 다른 지원 컴포넌트는 물론이고 AMQP 연결 팩토리와 RabbitTemplate 빈을 생성하는 자동-구성이 수행된다. 
따라서 스프링을 사용해서 RabbitMQ 브로커로부터 메시지를 전송 및 수신할 수 있다. 

### RabbitMQ 브로커의 위치와 인증 저보를 구성하는 속성
|            속성            |               설명                |
|:------------------------:|:-------------------------------:|
| spring.rabbitmq.address  | 쉼표로 구분된 리스트 형태의 RabbitMQ 브로커 주소 |
|   spring.rabbitmq.host   |            브로커의 호스트             |
|   spring.rabbitmq.port   |             브로커의 포트             |
| spring.rabbitmq.username |     브로커를 사용하기 위한 사용자 이름 (선)     |
| spring.rabbitmq.password |     브로커를 사용하기 위한 사용자 암호 (선)     |

이를 실무에 맞춰서 세팅하면 예를 들어 아래와 같다.
```yaml
spring:
  profiles: prod
  rabbitmq: 
    host: rabbit.tacocloud.com
    port: 5673
    username: tacoweb
    password: l3tm31n
```

## 8.2.2 RabbitTemplate을 사용해서 메시지 전송하기
RabbitMQ 메시징을 위한 스프링 지원은 핵심은 RabbitTemplate이다. RabbitTemplate은  JmsTemplate과 유사한 메소드들을 제공한다.
그러나 RabbitMQ 특유의 작동 방법에 따른 미세한 차이가 있다. RabbitTemplate을 사용한 메시지 전송의 경우에 send(), convertAndSend() 메소드는 JmsTemplate 메소드와 유사하다.
그러나 지정된 큐나 토픽에만 메시지를 전송했던 JmsTemplate 메소드와 달리 RabbitTemplate은 거래소와 라우팅 키의 형태로 메시지를 전송한다. 

```java
interface RabbitTemplate {
    //원시 메시지 전송
    void send(Message message) throws AmqpException;
    void send(String routingKey, Message message) throws AmqpException;
    void send(String exchange, String routingKey, Message message) throws AmqpException;
    
    //객체로부터 변환된 메시지를 전송한다.
    void convertAndSend(Object message) throws AmqpException;
    void convertAndSend(String routingKey, Object message) throws AmqpException;
    void convertAndSend(String exchange, String routingKey, Object message) throws AmqpException;
    
    // 객체로부터 변환되고 후처리(post-processing)되는 메시지를 전송
    void convertAndSend(Object message, MessagePostProcessor mPP) throws  AmqpException;
    void convertAndSend(String routingKey, Object message, MessagePostProcessor mPP) throws  AmqpException;
    void convertAndSend(String exchange, String routingKey, Object message, MessagePostProcessor mPP) throws  AmqpException;
}
```

볼 수 있듯이 JmsTemplate과 유사한 패턴을 따른다. 이 메소드들은 도착지 이름(Destination) 대신 거래소와 라우팅 키를 지정하는 문자열 값을 인자를 받는다는 점이 
JmsTemplate과 다르다. 거래소를 인자로 받지 않는 메소드들은 기본 거래소로 메시지를 전송한다. 마찬가지로 라우팅 키를 인자로 받지 않는 메소드들은 기본 라우팅 키로 전송되는
메시지를 갖는다.

```java
@Service
@RequiredArgsConstructor
public class RabbitOrderMessagingService implements  OrderMessagingService {
    private final RabbitTemplate rabbit;

    @Override
    public void sendOrder(Order order) {
        MessageConverter converter = rabbit.getMessageConverter();
        MessageProperties props = new MessageProperties();
        Message message = converter.toMessage(order, props);
        rabbit.send("tacocloud.order", message);

    }
}
```
이와 같이 ```MessageConverter```가 있으면 Order 객체를 Message 객체로 변환하기 쉽다. 메시지 속성은  ```MessageProperties```를 사용해서 제공하면 된다.
그러나 메시지 속성을 설정할 필요가 없다면 ```MessageProperties```의 기본 인스턴스면 족하다. 그리고 모든 준비가 완료되면 ```send()```를 호출할 수 있다. 
이때 메시지와 함께 거래소 및 라우팅 키를 인자로 전달한다. (선택적) 기본 거래소의 이름은 ```""```이며, 이는 RabbitMQ가 자동으로 생성하는 기본 거래소와 일치한다. 
이와 동일하게 기본 라우팅 키도 ```""```이다.

이러한 기본값은 ```spring.rabbitmq.template.exchange```와 ```spring.rabbitmq.template.routing-key```로 변경할 수 있다.
```yaml
spring:
  rabbitmq:
    template:
      exchange: tacocloud.orders
      routing-key: kitchens.central
```
이렇게 되면 거래소를 지정하지 않은 모든 메시지는 이름이 ```tacocloud.orders```인 거래소로 자동 전송된다. 만일 send(), convertAndSend()를 호출할 때 
라우팅 키도 지정하지 않으면 메시지는 ```kitchens.central```을 라우팅 키로 갖는다. 

메시지 변환기로 Message 객체를 생성하는 것은 손쉽지만, 모든 변환을 RabbitTemplate이 처리하도록 ```convertAndSend()```를 이용하는 것도 한 방법이다.

```java
@Service
@RequiredArgsConstructor
public class RabbitOrderMessagingService implements  OrderMessagingService {
    private final RabbitTemplate rabbit;

    @Override
    public void sendOrder(Order order) {
        rabbit.convertAndSend("tacocloud.order", message);
    }
}
```

### 메시지 변환기 구성하기
기본적으로 메시지 변환은 SimpleMessageConverter로 수행되며, 이는 String과 같은 간단한 타입과 Serializable 객체를 Message로 변환할 수 있다. 그러나 스프링은
다음을 포함해서 RabbitTemplate에서 사용할 수 있는 여러 개의 메시지 변환기를 제공한다. 

 * Jackson2JsonMessageConverter: Jackson2JSONProcessor를 사용해서 객체를 JSON으로 변환
 * MarshallingMessageConvert: 스프링 Marshaller와 Unmarshaller를 사용해서 변환
 * SerializerMessageConverter: 스프링의 Serializer와 Deserializer를 사용해서 String과 객체를 변환한다.
 * SimpleMessageConverter: String, byte 배열, Serializable 타입을 변환한다.
 * ContentTypeDelegatingMessageConverter: contentType 헤더를 기반으로 다른 메시지 변환기에 변환을 위임한다. 


메세지 변환기를 변경해야 할 때는 MessageConverter 타입의 빈을 구성하면 된다. 예를 들어, JSON 기반 메시지 변환의 경우는 Jackson2JsonMessageConverter를
구성하면 된다.

```java
@Component
public class Configuration {
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
```

이렇게 하면 스프링 부트 자동-구성에서 이 빈을 찾아서 기본 메시지 변환기 대신 이 빈을 RabbitTemplate으로 주입한다. 


### 메시지 속성 설정하기 
JMS에서처럼 전송하는 메시지의 일부 헤더를 설정해야하는 경우가 있다. 이때는 Message 객체를 생성할 떄 메시지 변환기에 제공하는 MessageProperties 인스턴스로 헤더를 설정할 수 있다.
```java
@Service
@RequiredArgsConstructor
public class RabbitOrderMessagingService implements  OrderMessagingService {
    private final RabbitTemplate rabbit;

    @Override
    public void sendOrder(Order order) {
        MessageConverter converter = rabbit.getMessageConverter();
        MessageProperties props = new MessageProperties();
        props.setHeader("X_ORDER_SOURCE", "WEB");
        Message message = converter.toMessage(order, props);
        rabbit.send("tacocloud.order", message);

    }
}
```
그러나 convertAndSend()에서는 ```MessageProperties```를 직접 사용할 수 없으므로 아래와 같이 MessagePostProcessor에서 진행해야 한다. 

```java
@Service
@RequiredArgsConstructor
public class RabbitOrderMessagingService implements  OrderMessagingService {
    private final RabbitTemplate rabbit;

    @Override
    public void sendOrder(Order order) {
        rabbit.convertAndSend("tacocloud.order", order, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws  AmqpException {
                MessageProperties props = message.getMessageProperties();
                props.setHeader("X_ORDER_SOURCE", "WEB");
                return message;
            }
        });
    }
}
```


## 8.2.3 RabbitMQ로부터 메시지 수신하기
RabbitTemplate을 사용한 메시지 전송은 JmsTemplate을 사용한 메시지 전송과 크게 다르지 않다. RabbitMQ 큐로부터의 메시지 수신도 JMS로부터의 메시지
수신과 크게 다르지 않다. JMS에서처럼 RabbitMQ의 경우도 아래의 두 가지를 선택할 수 있다. 
* RabbitTemplate을 사용해서 큐로부터 메시지를 가져온다.
* ```@RabbitListener```가 지정된 메소드로 메시지가 푸시된다. 


### RabbitTemplate을 사용해서 메시지 수신하기
```java
interface RabbitTemplate {
    //메시지 수신
    Message receive() throws AmqpException;
    Message receive(String queueName) throws AmqpException;
    Message receive(Long timeoutMillis) throws AmqpException;
    Message receive(String queueName, Long timeoutMillis) throws AmqpException;
    
    // 메시지로부터 변환된 객체를 수신
    Object receiveAndConvert()  throws AmqpException;
    Object receiveAndConvert(String queueName)  throws AmqpException;
    Object receiveAndConvert(Long timeMillis)  throws AmqpException;
    Object receiveAndConvert(String queueName, Long timeoutMillis)  throws AmqpException;
    
    //메시지로부터 변환된 type-safe 객체를 수신한다.
    <T> T receiveAndConvert(parameterizedTypeReference<T> type) throws  AmqpException;
    <T> T receiveAndConvert(String queueName, parameterizedTypeReference<T> type) throws  AmqpException;
    <T> T receiveAndConvert(Long timeoutMillis, parameterizedTypeReference<T> type) throws  AmqpException;
    <T> T receiveAndConvert(String queueName, Long timeoutMillis, parameterizedTypeReference<T> type) throws  AmqpException;
}
```

이 메소드들은 앞의 send(), convertAndSend()와 대칭된다. 그러나 시그니쳐 부분이 조금 다르다. 우선, 수신 메소드의 어느 것도 거래소, 라우팅 키를 매개변수로 갖지 
않는다. 왜냐하면 거래소와 라우팅 키는 메시지를 큐로 전달되는데 사용하지만, 일단 메시지가 큐로 들어오면 다음 메시지 도착은 큐로부터 메시지를 소비하는
consumer이기 때문이다. 따라서 메시지를 소비하는 애플리케이션은 거래소 및 라우팅 키에 신경 쓸 필요가 없다.
또한, 수신 메소드에 수신 타입아웃 파라미터가 있다. 즉, 호출된 즉시 receive가 결과를 반환하며 수신할 메시지가 없으면 ```null```을 반환한다. 
이것이 JmsTemplate의 ```receive()```와 다른 점이다. 

```java

@Component
@RequiredArgsConstructor
public class RabbitOrderReceiver {
    private final RabbitTemplate rabbit;
    private MessageConverter converter;

    public Order receiveOrder(){
        Optional<Message> message = Optional.ofNullable(rabbit.receive("tacocloud.orders"), 3000);
        return message.map(mes -> (Order) converter.fromMessage(mes)).orElseGet(() -> null);
    }
}
```
이렇게하면 ```RabbitTemplate```의  ```receive()```를 호출하여 주문 데이터를 가져온다. 위 코드는 3초의 지연을 용인하며, 3초 이상 지연된ㄷ다면
```null```을 반환할 것이다. 만약 위의 '3000'이라는 값이 보기 불편하다면

```yaml
spring:
  rabbitmq:
    template:
      receive-timeout: 3000
```
과 같이 설정해줄 수 있다. 또한 자동 변환에 대해서 의구심을 갖는다면


```java

@Component
@RequiredArgsConstructor
public class RabbitOrderReceiver {
    private final RabbitTemplate rabbit;
    private MessageConverter converter;

    public Order receiveOrder(){
        
        return (Order) rabbit.receiveAndConvert("tacocloud.order.queue");
    }
}
```
로 간단하게 처리할 수 있다. 여기서 타입 캐스팅도 문제가 된다고 생각한다면

```java

import java.lang.reflect.ParameterizedType;

@Component
@RequiredArgsConstructor
public class RabbitOrderReceiver {
    private final RabbitTemplate rabbit;
    private MessageConverter converter;

    public Order receiveOrder() {
        return rabbit.receiveAndConvert("tacocloud.order.queue", new ParameterizedTypeReference<Order>() {});
    }
}
```
로 처리하면 된다. 단, receiveAndConvert()에 ```ParameterizedTypeReference```를 사용하려면 메시지 변환기가 
```SmartMessageConverter``` 인터페이스를 구현한 클래스```(Jackson2JsonMessageConverter)```이어야 한다.

### 리스너를 사용해서 RabbitMQ 메시지 처리 
메시지 기반의 RabbitMQ 빈을 위해 스프링은 RabbitListener를 제공한다. 이것은 JmsListener에 대응하는 RabbitMQ 리스너이다. 메시지가 큐에 도착할 때 
메소드가 자동호출 되도록 지정하기 위해서는 ```@RabbitListener``` 어노테이션을 RabbitMQ 빈의 메소드에 지정해야한다.

```java
@Component
@RequiredArgsConstructor
public class RabbitOrderListener {
    private final KitchenUi ui;

    @RabbitListener(queues = "tacocloud.order.queue")
    public void receiveOrder(Order order) {
        ui.displayOrder(order);
    }
}
```
이는 ```@JmsListener```와 거의 동일하게 작동한다. 따라서 서로 다른 메시지 브로커인 RabbitMQ, Artemis, ActiveMQ를 사용하는 코드를 작성할 때
크게 달라지는 것이 없다.


## 8.3 카프카 사용하기 
아파치 카프카는 가장 새로운 메시징 시스템이며, ActiveMQ, Artemis, RabbitMQ와 유사한 메시지 브로커다. 그러나 카프카 특유의 아키텍쳐를 갖고 있다. 

카프카는 높은 확장성을 제공하는 클러스터(cluster)로 실행되도록 설계됐다. 그리고 클러스터의 모든 카프카 인스턴스에 걸쳐 토픽(topic)을 파티션(partition)
으로 분할하여 메시지를 관리한다. RabbitMQ가 거래소와 큐를 사용해서 메시지를 처리하는 반면, 카프카는 토픽만 사용한다.

카프카의 토픽은 클러스터의 모든 브로커에 걸쳐 복제된다(replicated). 클러스터의 각 노드는 하나 이상의 토픽에 대한 리더(leader)로 동작하며,
토픽 데이터를 관리하고 클러스터의 다른 노드로 데이터를 복제한다.


                   |                  카프카 클러스터              | 
    producer1  ->  |  [브로커 [파티션 0] / [파티션 1] / [파티션 2]]   |  -> consumer1                           
                   |                                           | 
    producer2  ->  |  [브로커 [파티션 0] / [파티션 1] / [파티션 2]]   |  -> consumer2                               
                   |                                           | 
    producer3  ->  |  [브로커 [파티션 0] / [파티션 1] / [파티션 2]]   |  -> consumer3                   
                   |                                           |

    *** 카프카 클러스터는 여러 개의 브로커로 구성되며, 각 브로커는 토픽의 파티션 리더로 동작한다.


각 토픽은 여러 개의 파티션으로 분할될 수 있다. 이 경우 클러스터의 각 노드는 한 토픽에 대해서 하나 이상의 파티션의 리더가 된다.

## 8.3.1 카프카를 사용해서 스프링 설정하기

카프카를 사용해서 메시지를 처리하려면 이에 적합한 의존성을 빌드에 추가해야한다.
```xml
<dependency>
    <groupId>org.springframwork.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

이처럼 의존성을 추가하면 스프링 부트가 카프카 사용을 위한 자동-구성을 해준다. (스프링 애플리케이션에서 사용할 KafkaTemplate을 준비한다.) 따라서 우리는 
KafkaTemplate을 주입하고 메시지를 전송, 수신하면 된다.

그러나 메시지를 전송 및 수신하기에 앞서 카프카를 사용할 떄 편리한 몇 가지 속성을 알아야 한다. 특히 KafkaTemplate은 기본적으로 localhost에서 실행되면서 
9092 포트를 리스닝하는 카프카 브로커를 사용한다. 애플리케이션을 개발할 떄는 로커의 카프카 브로커를 사용하면 좋다. 그러나 실무로 이양한다면
호스트와 포트를 구성해야한다. 
```yaml
spring:
  kafka:
    bootstap-servers:
    - kafka.tacocloud.com: 9092 
    - kafka.tacocloud.com: 9093 
    - kafka.tacocloud.com: 9094 
```
    
## 8.3.2 KafkaTemplate을 사용해서 메시지 전송하기
```java
interface KafkaTemplate {
    ListenableFuture<SendResult<K,V>> send(String topic, V data);
    ListenableFuture<SendResult<K,V>> send(String topic, K key, V data);
    ListenableFuture<SendResult<K,V>> send(String topic, Integer partition, K key,  V data);
    ListenableFuture<SendResult<K,V>> send(String topic, Integer partition, Long timestamp, K key,  V data);
    
    ListenableFuture<SendResult<K,V>> send(ProducerRecord<K,V> record);
    ListenableFuture<SendResult<K,V>> send(Message<?> message);
    
    ListenableFuture<SendResult<K,V>> sendDefault(V data);
    ListenableFuture<SendResult<K,V>> sendDefault(K key, V data);
    ListenableFuture<SendResult<K,V>> sendDefault(Integer partition, K key, V data);
    ListenableFuture<SendResult<K,V>> sendDefault(Integer partition, Long timestamp, K key, V data);
}

```

주의할 것은 ```convertAndSend()```가 없다는 것이다. 왜냐하면 KafkaTemplate은 제네릭을 사용하고 메시지를 전송할 때 직접 도메인 타입을 처리할 수 있기 떄문이다.
따라서 send에 convertAndSend가 있다고 생각하면 된다. 또한, 이전에 사용했던 것들과 같이 다른 매개변수들이 있다. 카프카에서 메시지를 전송할 때는 메시지가 
전송되는 방법을 알려주는 다음 매개변수를 지정할 수 있다.

* 메시지가 전송될 토픽(send()에 필요)
* 토픽 데이터를 사용하는 파티션 (선)
* 레코드 전송 키 (선)
* 타임스탬프 (선, 기본값 System.currentTimeMillis())
* 페이로드 (필)

토픽과 페이로드는 가장 중요한 매개변수들이다. 파티션과 키는 send()와 sendDefault()에 매개변수로 제공되는 추가 정보일 뿐 KafkaTemplate을 사용하는 방법에는
거의 영향을 주지 않는다. 
```java
@Service
@RequiredArgsConstructor
public class KafkaOrderMessagingService implements OrderMessagingService{
    private final KafkaTemplate<String, Order> kafkaTemplate;

    @Override
    public void sendOrder(Order order) {
        kafkaTemplate.send("tacocloud.order.topic", order);
    }
}

```
여기서 ```sendOrder()```는 ```KafkaTemplate```의 ```send()```를 이용해서 ```tacocloud.order.topic```라는 토픽으로 
Order 객체를 전송한다. 이는 JMS, RabbitMQ와 굉장히 유사하다. 만일 기본 토픽을 설정하면 더 간단해진다.

```yaml
spring:
  kafka:
    template:
      default-topic: tacocloud.orders.topic
```

이 이후에 ```sendDefault()```를 호출하면 된다. 이 때는 토픽을 인자로 전달하지 않는다.


## 8.3.3 카프카 리스너 작성하기
send(), sendDefault() 특유 메소드 시그니처 외에도 KafkaTemplate은 메시지를 수신하는 메소드를 일체 제공하지 않는다. 이 점이 JmsTemplate, RabbitTemplate
과는 다르다. 

카프카의 경우 메시지 리스너는 ```@KafkaListener``` 어노테이션이 지정된 메소드에 정의된다. 이는 ```@JmsListener```, ```@RabbitListener```와 유사하며
동일한 방법으로 사용된다. 

```java

@Component
@RequiredArgsConstructor
public class Config {
    private final KitchenUi ui;

    @KafkaListener(topics = {"tacocloud.orders.topic"})
    public void handle(Order order) {
        ui.displayOrder(order);
    }

}
```

"tacocloud.orders.topic"이라는 이름의 토픽에 메시지가 도착해야 자동 호출된다. 그리고 ```handle()```에 페이로드만 인자로 받는다. 
그러나 메시지 메타데이터가 필요하다면 ```ConsumerRecord```, ```Message```객체도 인자로 받을 수 있다.

```java

@Component
@RequiredArgsConstructor
public class Config {
    private final KitchenUi ui;

    @KafkaListener(topics = {"tacocloud.orders.topic"})
    public void handle(Order order, ConsumerRecord<String,Order> record, Message message) {
        record.partition();
        record.timestamp();
        
        MessageHeaders headers = message.getHeaders();
        ui.displayOrder(order);
    }

}
```

메시지 페이로드는 ```ConsumerRecord.value()``` 혹은 ```Message.getPayload()```로도 받을 수 있다. 
이는 일전의 ```handle()``` 메소드에 Order 객체를 직접 요청하는 대신 ConsumerRecord, Message를 통해서 Order를 얻을 수 있음을 의미한다.


*** 요약

1. 애플리케이션 간 비동기 메시지 큐를 이용한 통신 방식은 간접 계층을 제공하므로 애플리케이션 간의 결합도는 낮추면서 확장성은 높인다.
2. 스프링은 JMS, RabbitMQ 또는 아파치 카프카를 사용해서 비동기 메시징을 지원한다.
3. 스프링 애플리케이션은 템플릿 기반의 클라이언트인 JmsTemplate, RabbitTemplate 또는 KafkaTemplate을 사용해서 메시지 브로커를 통한 메시지 전송을 할 수 있다.
4. 메시지 수신 애플리케이션은 같은 템플릿 기반의 클라이언트들은 사용해서 풀 모델 형태의 메시지를 소비할 수 있다.
5. 메시지 리스너 어노테이션인  ```@JmsListener```, ```@RabbitListener``` 또는 ```@KafkaListener```를 빈 메소드에 지정하면 푸시 모델의 형태로 컨슈머에게 메시지가 전송될 수 있다.