# 스프링 통합하기

이번 장에서는 스프링 통합(Spring Integration)으로 통합 패턴을 사용하는 방법을 볼 것이다. 각 통합 패턴은 하나의 컴포넌트로 구현되며,
이것을 통해서 파이프라인으로 메시지가 데이터를 운반한다. 스프링 구성을 사용하면 데이터가 이동하는 파이프라인으로 이런 컴포넌트들을 조합할 수 있다.


## 9.1 간단한 통합 플로우 선언하기
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-integration</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-file</artifactId>
</dependency>
```

첫 번쨰 의존성은 스프링 통합의 스프링 부트 스타터이다. 통합하려는 플로우와 무관하게 이 의존성은 스프링 통합 플로우의 개발 시 반드시 추가해야한다. 

두 번쨰 의존성은 스프링 통합의 파일 엔드포인트 모듈이다. 이 모듈은 외부 시스템 통합에 사용되는 24개 이상의 엔드포인트 모듈 중 하나이다. 일단 파일 시스템으로부터
통합 플로우로 파일을 읽거나, 통합 플로우로부터 파일 시스템으로 데이터를 쓸 수 있는 기능을 제공하는 역할을 한다.

이 다음은 파일에 데이터를 쓸 수 있도록 애플리케이션 통합 플로우로 데이터를 전송하는 게이트워이(gatewary)를 생성해야 한다.


```java
@MessagingGateway(defaultRequestChannel = "textIntChannel") // 메세지 게이트웨이를 선언한다.
public interface FileWriterGateway {
    void writeToFile (@Header("file_name") String fileName, String data);
}
```

FileWriterGateway는 간단한 자바 인터페이스이긴 하지만 알아볼 것이 많이 많다. 우선, ```FileWriterGateWay```에는 ```@MessageingGateway```가 지정되었다.
이 어노테이션은 FileWriterGateway 인터페이스의 구현체(클래스)를 런타임 시에 생성하라고 스프링 통합에 알려준다. 이는 레포지토리의 구현체를 스프링 데이터가 자동
생성하는 것과 유사하다. 이외의 다른 코드에서는 파일에 데이터 써야 할 때 FileWriterGateway 인터페이스를 사용할 것이다.

```@MessagingGateway```의 defaultRequestChannel 속성은 해당 인터페이스의 메소드 호출로 생성된 메시지가 이 속성에 지정된 메시지 채널로 전송된다는 것을
나타낸다. 여기서는 ``writeToFile()```의 호출로 생긴 메시지 textInChannel이라는 이름의 채널로 전송된다.

```writeToFile()``` 메소드는 두 개의 String 타입 매개변수를 갖는다. 파일 이름과 파일에 쓰는 텍스트를 포함하는 데이터다. 여기서 filename 매개변수에는 
```@Header```가 지정되었다. ```@Header```는 filename에 전달되는 값이 메시지 페이로드가 아닌 메시지 헤더에 있다는 것을 나타낸다.

이제는 메시지 게이트웨가 생성되었으므로 통합 플로우를 구성해야한다. 스프링 통합 스타터 의존성을 빌드에 추가했으므로 스프링 통합의 자동-구성이 수행될 수 있다.
그러나 애플리케이션의 요구를 충족하는 플로우를 정의하는 구성은 우리가 추가로 작성해야 한다. 통합 플로우는 세 가지 구성으로 정의할 수 있다.

- XML 구성
- 자바 구성
- DSL를 사용한 구성 

여기서는 Java로 구성할 것이다. (본 서에는 모두 있다.)

9.1.2 Java로 통합 플로우 구성하기 
현재는 대부분의 스프링 애플리케이션이 XML 구성으르 피하고 자바 구성을 사용한다. 실제로 스프링 부트 애플리케이션에서 자바 구성은 스프링의 자동-구성을 자연스럽게 보완해주는
방법이다. 따라서 스프링 부트 애플리케이션에 통합 플로우를 추가할 때는 XML 보다는 자바로 플로우를 정의하는 것이 좋다.

```java

@Configuration
public class FileWriterIntegrationConfig {
    @Bean
    @Transformer( inputChannel="textInChannel", outputChannel="fileWriterChannel")
    public GenericTransformer<String, String> upperCaseTransformer() {
        return text -> text.toUpperCase();
    }
    
    @Bean
    @ServiceActivator(inputChannel="fileWriterChannel")
    public FileWritingMessageHandler fileWriter() {
        FileWritingHandler handler = FileWriteringMessageHandler(new File('path'));
        
        handler.setExpectReply(false);
        handler.setFileExistsMode(FileExistsMode.APPEND);
        handler.setAppendNewLine(true);
        return handler;
    }
}

```

이 자바 구성에는 두 개의 빈을 정의한다 컨버터, 파일-쓰기 메시지 핸들러이다. 컨버터인 ```GenericTransformer```는 함수형 인터페이스이므로 메시지 텍스트에
toUpperCase()를 호출하는 람다로 구현할 수 있다. GenericTransformer에는 ```@Transformer```가 지정되었다. 이 어노테이션은 GenericTransformerrk
textInChannel의 메시지를 받아서 fileWriterChannel로 쓰는 통합 플로우 컨버터를 지정한다.

파일 쓰기 빈에는 ```@ServiceActivator```를 지정했다. 이 어노테이션은 fileWirterChannel으로부터 메시지를 받아서 FileWriterHandler의 인스턴스로
정의된 서비스에 넘겨주믈 나타낸다. FileWritingMessageHandler는 메시지 핸들러이며, 메시지 페이로드를 지정된 디렉토리의 파일에 쓴다. 이떄 파일 이름은 해당 
메시지의 file_name 헤더에 지정된 것을 사용한다. 

특이한점은 ```handler.setExpectReply(false)```이다. 이 메소드는 서비스에서 응답채널을 사용하지 않음을 나타낸다. 만일 이를 호출하지 않는다면 
통합 플로우가 정상적으로 작동하더라도 응답 채널이 구성되지 않았다는 로그가 출력된다. 자바 구성에서는 채널들을 별도로 선언하지 않았는데
textInChannel, fileWriterChannel 이라는 이름의 빈이 없으면 이 채널들은 자동으로 생성된다. 그러나 각 채널의 구성 방법을 더 제어하고 싶으면


```java
@Bean
public MessageChannel textInChannel() {
    return new DriectChannel();
}

@Bean
public MessageChannel fileWirterChannel() {
    return new DirectChannel();    
}
```

## 9.1.3 스프링 통합 DSL 구성 사용하기
이때는 각 플로우의 컴포넌트를 별도의 빈으로 선언하지 않고 전체 플로우를 하나의 빈으로 선언한다. 
```java
@Configuration
public class FileWriterIntegrationConfig {
    @Bean
    public IntegrationFlow fileWriterFlow() {
        return IntegrationFlows
                .from(MessageChannels.direct("textInChannel")) //인바운드
                .<String,String>transform(t -> t.toUpperCase()) //컨버터
                .handle(Files
                        .outboundAdapter(new File("/~path"))
                        .fileExistsMode(FileExistsMode.APPEND)
                        .appendNewLine(true))
                .get();
    }
}
```
이 구성은 전체 플로우를 하나의 빈 메소드에 담고 있어서 코드를 최대한 간결하게 작성할 수 있다는 이점이 있다.


## 9.2 스프링 통합의 컴포넌트 살펴보기
스프링 통합은 다수의 통합 시나리오를 갖는 많은 영역을 포함한다. 따라서 모든 것을 하나에 넣는 것은 바늘 구멍에 밧줄을 넣는 것과 같다.
통합 플로우는 하나 이상의 컴포넌트로 구성되며, 그 내역은 아래와 같다.

- 채널(channel) : 한 요소로부터 다른 요소로 메시지를 전달한다.
- 필터(Filter)  : 조건에 맞는 메시지가 플로우를 통과하게 해준다.
- 변환기(Transformer) : 메시지 값을 변경하거나 메시지 페이로드의 타입을 다른 타입으로 변환한다.
- 라우터(Router) : 여러 채널 중 하나로 메시지를 저낟ㄹ하며, 대개 메시지 헤더를 기반으로 한다.
- 분배기(Splitter) : 들어오는 메시지를 두 개 이상의 메시지로 분할하며, 분할된 각 메시지는 다른 채널로 전송된다.
- 집적기(Aggregator) : 분배기와 상반된 것으로 별개의 채널로부터 전달되는 다수의 메시지를 하나의 메시지로 결합한다.
- 서비스 액티베이터(Service activator) : 메시지를 처리하도록 자바 메소드에 메시지를 넘겨준 후 메소드의 반환 값을 출력 채널로 전송한다.
- 채널 어댑터(Channel adapter) : 외부 시스템에 채널을 연결한다. 외부 시스템으로부터 입력을 받거나 쓸 수 있다.
- 게이트 웨이(Gateway) : 인터페이스를 통해 통합 플로우로 데이터를 전달한다. 

지금부터는 통합 플로우 컴포넌트에 대해서 알아본다.


## 9.2.1 메시지 채널
메시지 채널은 통합 파이프라인을 통해서 메시지가 이동하는 수단이다. 즉, 채널은 스프링 통합에서 서로를 연결하는 통로가 된다. 스프링 통합은 여러 채널 구현체(클래스)
를 제공한다. 

- PublishSubscribeChannel: 이것으로 전송되는 메시지는 하나 이상의 컨슈머로 전달된다. 컨슈머가 여럿일 때는 모든 컨슈머가 해당 메시지를 수신한다.
- QueueChannel: 이것으로 전송되는 메시지는 FIFO 방식으로 컨슈머가 가져갈 때까지 큐에 저장된다. 컨슈머가 여럿일 때는 그중 하나의 컨슈머만 해당 메시지를 수신한다.
- PriorityChannel: QueueChannel과 유사하지만, FIFO 방식 대신 메시지의 priority 헤더를 기반으로 컨슈머가 메시지를 가져간다.
- RendezvousChannel: QueueChannel과 유사하지만, 컨슈머가 메시지를 수신할 때까지 메시지 전송자가 채너을 차단한다는 것이 다르다(Pub-Sub을 동기화)
- DirectChannel: PublishSubscribeChannel과 유사하지만, 전송자와 동일한 쓰레드로 실행되는 컨슈머를 호출하여 단일 컨슈머에게 메시지를 전송한다. 이 채널은 트랜잭션을 지원한다. 
- ExecutorChannel: DirectChannel과 유사하지만, TaskExecutor를 통해서 메시지가 전송된다.(전송자와 다른 쓰레드에서 처리된다.) 이 채널 타입은 트랜잭션을 지원하지 않는다.
- FluxMessageChannel: 프로젝트 리액터(Project Reactor)의 플럭스(Flux)를 기반으로 하는 리액티브 스트림즈 퍼블리셔(Reactive Streams Publisher) 채널이다.

자바 구성과 자바 DSL 구성 모두에서 입력 채널은 자동으로 생성되며, 기본적으로  Direct Channel이 사용된다. 그러나 다른 채널 구현체를 사용하고 싶다면 해당 채널을 별도의 빈으로 선언하고
통합플로우에서 참조해야한다.

```java
@Bean
public MessageChannel orderChannel(){
    return new PublishSubscribeChannel(); 
}   
```

그다음에 통합 플로우 정의에서 이 채널을 이름으로 참조한다. 이 채널을 서비스 액티베이터에서 소비한다면 ```@ServiceActivator``` 애노테이션의 inputChannel
속성에서 이 채널 이름으로 참조하면 된다.

```java
@ServiceActivator(inputChannel="orderChannel")
```
또는 자바 DSL 구성을 사용할 떄는 ```channel()```메소드 호출에서 참조한다.
```java
@Bean
public IntegratoinFlow orderFlow() {
    return IntegrationFlows
        .... 
        .channel("orderChannel")
        ....
        .get();
}
```


```QueueChannel```을 사용할 때는 consumer가 이 채널을 ```polling``` 하도록 구성하는 것이 중요하다. 
```java
@Bean
public MessageChannel orderChannel(){
    return new QueueChannel();    
}
```
이것을 입력 채널로 사용할 때 컨슈머는 도착한 메시지 여부를 폴링해야 한다. 컨슈머가 서비스 액티베이터인 경우는 다음과 같이  ```@ServiceActivator``` 어노테이션을 지정할 수 있다.
```java
@ServiceActivator(inputChannel="orderChannel", poller=@Poller(fixedRate="1000"))
```

## 9.2.2 필터 
필터는 통합 파이프라인의 중간에 위치할 수 있으며, 플로우의 전 단계로부터 다음 단계로의 메시지 전달을 필터링한다.
예를 들어 정수 값을 갖는 메시지가 numberChannel이라는 이름의 채널로 입력되고 짝수인 경우만 evenNumberChannel이라는 이름의 채널로 전달된다고 해보자
이 경우 ```@Filter``` 어노테이션이 지정된 필터를 선언할 수 있다.
```java
@Filter(inputChannel="numberChannel", outputChannel="evenNumberChannel")
public boolean evenNumberFilter(Integer number){
    return number % 2 == 0;    
}
```
```java
@Bean
public IntegratoinFlow orderFlow() {
    return IntegrationFlows
        .... 
        .<Integer>filter( p -> p % 2 == 0)
        ....
        .get();
}
```
예시는 람다로 구현했지만 실제로 filter() 메소드가 GenericSelector를 인자로 받는다. 이는 GenericSelector를 구현하여 다양한 조건으로 필터링할 수 있다는 것의 의미한다.

## 9.2.3 컨버터
컨버터는 메시지 값의 변경이나 타입을 변환하는 일을 수행한다. 변환 작업은 숫자 값의 연산이나 문자열 값 조작과 같은 간단한 것이 될 수 있다. 예를 들어 정수 값을 포함하는 
메시지가 numberChannel이라는 이름의 채널로 입력되고, 이 숫자를 로마 숫자를 포함하는 문자열로 변환한다고 해보자. 이 경우 ```@Transformer``` 어노테이션으로
```GenricTransformer```타입의 빈을 선언할 수 있다.
```java
@Bean
@Transformer(inputChannel="numberchannel", outputChannel="romanNumberChannel")
public GenericTransformer<Integer, String> romanNumTransformer(){
    return RomanNumbers::toRoman    
}
```

```@Transformer``` 어노테이션은 이 빈을 컨버터로 지정한다. 그리고 변환 결과는 'romanNumber'이라는 채널로 전송된다.
자바 DSL에서는 toRoman() 이라는 메소드의 메소드 참조를 인자로 전달하여 transform()을 호출한다.
```java
@Bean
public IntegratoinFlow transformerFlow() {
    return IntegrationFlows
        .... 
        .transform(RomanNumbers::toRoman)
        ....
        .get();
}
```

혹은 

```java
@Bean
public RomanNumberTransformer romanNumberTransformer() {    
    retrun new RomanNumberTransformer()
}
@Bean
public IntegrationFlow transformerFlow(RomanNumberTransformer romanNumberTransformer) {
    return IntegrationFlows
        ...
        .transform(romanNumberTransformer)
        ...
        .get();
}
```

## 9.2.4 라우터
라우터는 전달 조건을 기반으로 통합 플로우 내부를 분기한다. 일전과 같이 짝/홀수로 구분해서 메시지를 각기 다른 채널로 전송해야한다고 가정해보자. 이런 경우 라우터를 통해서
플로우를 분기할 수 있다. ```@Router```가 지정된 AbstractMessageRouter 타입의 빈을 선언하면 된다.
```java
@Bean
@Router(inputChannel="numberChannel")
public AbstractMessageRouter evenOddRouter() {
    return new AbstractMessageRouter() {
        @Override
        protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
            Integer number = (Integer) message.getPayload();
            if ( number % 2 == 0 ){
                return Collections.singletone(evenChannel());    
            }  
            return Collections.singletone(oddChannel());
        }
    }   
}

@Bean
public MessageChannel evenChannel(){
    return new DriectChannel();    
}

@Bean
public MessageChannel oddChannel(){
        return new DriectChannel();
        }
```
```AbstractMessageRouter``` 빈은 numberChannel이라는 이름의 입력 채널로부터 메시지를 받는다. 그리고 이 빈을 구현한 익명 구현 클래스에서는 메시지 페이로드를 검사하여
짝수면 even 홀수면 odd를 반환한다. 같은 것을 DSL 구성으로 정의하면
```java
@Bean
public IntegrationFlow numberRoutingFlow(AtomicInteger source) {
    return IntegrationFlows
        ...
        .<Integer, String>route(n -> n % 2 == 0 > "EVEN" : "ODD",
                                mapping -> mapping.subFlowMapping("Even",
                                    sf -> sf.<Integer, Integer>transform( n -> n * 10).handle( (i,h) -> {...})
                                )
                                                .subFlowMapping("Even",
                                    sf -> sf.transform( RomanNumbers::toRoman ).handle( (i,h) -> {...})
                                )
        )
        .get();
}
```

## 9.2.5 분배기
때로는 통합 플로우에서 하나의 메시지를 여러 개로 분할하여 독립적으로 처리하는 것이 유용할 수 있다. 
1. <strong>메시지 페이로드가 같은 타입의 컬렉션 항목들을 포함하며, 각 메시지 페이로드 별로 처리하고자 할 때다.</strong>
   
-> 예를 들어, 여러 가지 종류의 제품이 있으며, 제품 리스트를 전달하는 메시지는 각가 한 종류 제품의 페이로드를 갖는 다수의 메시지로 분할될 수 있다.

2. <strong>연관된 정보를 전달하는 하나의 메시지 페이로드는 두 개 이상의 서로 다른 타입 메시지로 분할될 수 있다.</strong>

-> 예를 들어, 주문 메시지는 배달 정보 등을 전달할 수 있으며, 각 저보는 서로 다른 하위 플로우에서 처리될 수 있다. 이 경우는 일반적으로 분배기 다음에 페이로드
타입 별로 메시지를 전달하는 라우터가 연결된다.

하나의 메시지 페이로드를 두 개 이상의 서로 다른 타입 메시지로 분할할 때는 수신 페이로드의 각 부분을 추출하여 POJO로 정의하면 된다.

```java
import java.util.ArrayList;

public class OrderSplitter {
    public Collection<Object> splitOrderIntoParts(PurchaseOrder po) {
        ArrayList<Object> parts = new ArrayList<>();
        parts.add(po.getBiilingInfo());
        parts.add(po.getLineItem());
        return parts;
    }
}
```
```@Spllitter``` 어노테이션으로 통합 플로우의 일부로 OrderSplitter 빈을 선언할 수 있다.
```java
@Bean
@Splitter(inputChannel="poChannel", outputChannel="splitOrderChannel")
public OrderSplitter orderSplitter() {
    return new OrderSplitter();    
}
```
여기서는 주문 메시지가 poChannel이라는 이름의 채널로 도착하며 OrderSplitter에 의해서 분할 된다. 그 다음 컬렉션으로 반환되는 각 항목은 splitOrderChannel이라는
채널에 별도의 메시지로 전달된다. 플로우의 이 지점에서 PayloadTypeRouter를 선언하여 대금 청구 정보와 주문 항목 정보를 각 정보에 적합한 하위 플로우로 전달할 수 있다.
```java
@Bean
@Router(inputChannel="splitOrderChannel")
public MessageRouter splitOrderRouter() {
    PayloadTypeRouter router = new PayloadTypeRouter();
    
    router.setChannelMapping(BillingInfo.class.getName(), "billingInfoChannel");
    router.setChannelMapping(List.class.getName(), "lineItemsChannel");
    
    return router;
}
```
PayloadTypeRouter는 각 페이로드 타입을 기반으로 서로 다른 채널에 메시지를 전달한다. 즉, BillingInfo 타입의 페이로드는 billingInfoChannel로 전달되어 처리되며,
List에 저장된 항목들은 List 타입으로 lineItemsChannel에 전달된다. 

여기서는 하나의 플로우가 두 개의 하위 플로우로 분할된다. 그러나 List<LineItem>을 처리하는 대신 각 LineItem을 별도로 처리하고 싶다면 어떻게 할까? 이때는 
List<LineItem>을 다수의 메시지로 분할하기 위해서 ```@Splitter``` 어노테이션을 지정한 메소드를 작성하고 이 메소드에서 처리된 LineItem이 저장된 컬렉션을 반환하면 된다.

```java
@Splitter(inputChannel="lineItemsChannel", outputChannel="lineItemChannel")
public List<LineItem> lineItemSplitter(List<LineItem> lineItems) {
    return lineItems;    
}
```
자바 DSL을 사용해서 이와 동일한 분배기/라우터 구성을 선언할 때는 split()과 route() 메소드를 호출하면 된다.
```java
return IntegrationFlows
        ...
            .split(orderSplitter()) 
            .<Object, String> route(
                    p -> {
                        if (p.getClass().isAssignableFrom(BillingInfo.class)) {
                            return "BILLING_INFO";
                        } else { 
                            return "LINE_ITEMS";
                        }
                    }, mapping -> mapping
                        .subFlowMapping("BILLING_INFO",
                                        sf -> sf.<BillingInfo> handle((billingInfo, h) -> { 
                            ...
                        }))
                        .subFlowMapping("LINE_ITEMS",
                                        sf -> sf.split() .<LineItem> handle((lineItem, h) -> {
                            ...
                        }))
            )
            .get();
```

## 9.2.6 서비스 액티베이터
서비스 액티베이터는 입력 채널로부터 메시지를 수신하고 이 메시지를 MessageHandler 인터페이스를 구현한 클래스에 전달한다.
스프링 통합은 MessageHandler를 구현한 여러 클래스를 제공한다. 그러나 서비스 액티베이터의 기능을 수행하기 위해 커스텀 클래스를 제공해야 할 때가 있다.
예를 들어, 다음 코드에서는 서비스 액티베이터로 구성된 MessageHandler 빈을 선언하는 방법을 보여준다.

```java
@Bean
@ServiceActivator(inputChannel="someChannel")
public MessageHandler sysoutHander() {
    return message -> { System.out.println("Message payload: " + message.getPayload()); };
}
```

someChannel이라는 이름의 채널로부터 받은 메시지를 처리하는 서비스액티베이터로 지정하기 위해서 이 빈은 ```@ServiceActivator``` 어노테이션이 지정되었다.
여기서 MessageHandler 자체는 람다를 사용해서 구현했으며, 메시지를 받으면 이것의 페이로드를 표준 출력 스트림으로 내보낸다.
또는 받는 메시지의 데이터를 처리한 후 새로운 페이로드를 받환하는 서비스 액티베이터를 선언할 수도 있다. 이 빈은 MessageHandler가 아닌 GenericHandler를 구현한 것이어야 한다.

```java
@Bean
@ServiceActivator(inputChannel="orderChannel", outputChannel="completeChannel")
public GenericHandler<Order> orderHandler(OrderRepository orderRepo) {
    return (payload, headers) -> orderRepo.save(payload)    
}
```
여기서는 handle() 메소드의 인자로 전달되는 MessageHandler로 람다를 사용했다. 그러나 메소드 참조 또는 MessageHandler 인터페이스를 구현하는 클래스 인스턴스까지도 
handle() 메소드의 인자로 제공할 수 있다. 단, 람다나 메소드 참조의 경우는 메시지를 매개변수로 받는다는 것을 명심하자.

만일 서비스 액티베이터를 플로우의 제일 끝에 두지 않으면 MessageHandler의 겨우와 유사하게 handle() 메소드에서 GenricHandler를 인자로 받을 수도 있다.
DSL로 구성하면

```java
public IntegrationFlow orderFlow(OrderRepository orderRepo){
    return IntegrationFlows
        ...
        .<Order>handle((payload, headers) -> orderRepo.save(payload))
        ...
        .get();
}
```

GenericHandler를 사용할 때는 람다나 메소드 참조에서 메시지 페이로드와 헤더를 매개변수로 받는다. 또한, GenericHandler를 플로우의 제일 끝에 사용하면다면 null을 반환해야한다. 
그렇지 않으면 지정된 출력 채널이 없다는 에러가 발생한다.

## 9.2.7 게이트웨이
게이트웨이는 애플리케이션이 통합 플로우로 데이터를 제출하고 선택적으로 플로우의 처리 결과인 응답을 받을 수 있는 수단이다. 스프링 통합에 구현된 게이트웨이는 애플리케이션이
통합 플로우로 메시지를 전송하기 위해 호출할 수 있는 인터페이스로 구체화되어 있다.

    [ APPLICATION    <GateWay]> -> [ -> {CHANNEL} FLOW -> ...]

이미 FileWriterGateway라는 단방향 게이트웨이를 본 적이 있다. 파일에 쓰기 위해 문자열을 인자로 받고 void를 반환하는 메소드를 갖고 있다.
양방향 게이트웨이의 작성도 어렵지 않으며, 이때는 게이트웨이 인터페이스를 작성할 때 통합 플로우로 전송할 값을 메소드에서 반환해야 한다.

예를 들어, 문자열을 받아서 모두 대문자로 변환하는 간단한 통합플로우 앞 쪽에 있는 게이트웨이를 상상해보자.
```java
package  com.exampe.demo;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Component;

@Compnent
@MessagingGateway(defaultRequestChannel="inChannel", defaultReplyChannel="outChannel")
public interface UpperCaseGateway {
    String uppercase(String in);
}
```
신기한 사실은 이 인터페이스를 따로 구현할 필요가 없다는 것이다. 지정된 채널을 통해 데이터를 전송하고 수신하는 구현체를 스프링 통합이 런타임 시에 자동으로 제공하기 때문이다.
uppercase() 가 호출되면 지정된 문자열이 통합 플로우의 inChannel로 전달된다. 그리고 플로우가 어떻게 정의되고 무슨 일을 하는 지와 상관없이, 데이터 outChannel로
도착하면 uppercase() 메소드로부터 반환한다. DSL로 작성하면 아래와 같다.
```java
@Bean
public IntegrationFlow uppercaseFlow(){
    return IntegrationFlows
        .from("inChannel")
        .<String,String> transform(s -> s.toUpperCase())
        .channel("outChannel")
        .get();
}
```

여기서는 inChannel로 데이터가 입력되면서 플로우가 시작된다. 그다음에 대문자로 변환하기 위해 람다로 정의된 변환기에 의해 메시지 페이로드가 변환된다. 그리고 결과 메시지는
outChannel로 전달된다. 이것은 UpperCaseGateway 인터페이스의 응답 채널로 선언했던 채널이다. 


## 9.2.8 채널 어댑터
채널 어댑터는 통합 플로우의 입구와 출구를 나타낸다. 데이터는 inbound 채널 어댑터를 통해 들어오고 outbound 채널 어댑터를 통해서 나간다. inbound는 플로우에 지정된
데이터 소스에 따라 여러 가지 형태를 갖는다.
```java
@Bean
@InboundChannelAdapter( poller=@Poller(fixedRate="1000"), channel="numberChannel")
public MessageSource<Integer> numberSource(AtomicInteger source){
    return () -> new GenericMessage<>(source.getAndIncrement());
}
```
와 같이 말이다. 이 @Bean은 ```@InboundChannelAdapter```으로 지정됐으므로 inbound 어댑터로 작동한다. 이 bean은 주입된 AtomicInteger로 부터
numberChannel이라는 이름으로 매초 한 번씩 폴링한다. DSL에서는 @InboundChannelAdapter 역할을 ```from()```이 한다.

```java
@Bean
public IntegrationFlow someFlow(AtomicInteger integerSource){
    return IntegerFlows
        .from(integerSource, "getAndIncrement",
            c -> c.poller(Pollers.fixedRate(1000))
        )
        ...
        .get();
}
```
종종 채널 어댑터는 스프링 integratoin의 여러 엔드포인트 모듈 중 하나에서 제공된다. 예를 들어, 지정된 디렉토리를 모니터링하여 해당 디텍토리에 저장하는 파일을
file-channel이라는 이름의 채널에 메시지로 전달하는 인바운드 채널 어댑터가 필요하다고 해보자. 이 경우 스프링 통합 파일 엔드포인트 모듈의 FileReadingMessageSource
를 사용하는 자바 구성으로 사용할 수 있다.
```java
@Bean
@InboundChannelAdapter(channel= "file-channel", poller= @Poller(fixedDelay="1000"))
public MessageSource<File> fileReadingMessageSource() {
    FileReadingMessageSource sourceReader = new FileReaderingMessageSource();
    sourceReader.setDirectory(new File(INPUT_DIR));
    sourceReader.setFilter(new SimplePatternFileListFilter(FILE_PATTERN));
    return sourceReader
}
```
DSL에서는 Files 클래스의 inboundAdapter() 메소드를 사용할 수 있다. 아웃바운드 채널 어댑터는 통합 플로우의 말단이며, 최종 메시지를 애플리케이션 혹은 다른 시스템에 넘긴다.
```java
@Bean
public IntegrationFlow fileReaderFlow() {
    return IntegrationFlows
        .from(Files.inboundAdapter(new File(INPUT_DIR)).patternFilter(FILE_PATTERN))
        .get();
}
```

메시지 핸들러로 구현되는 서비스 액티베이터는 아웃바운드 채널 어댑터로 자주 사용된다. 특히, 데이터가 애플리케이션 자체에 전달될 필요가 있을 때다. 

## 9.2.9 엔드포인트 모듈
스프링 통합은 커스텀 어댑터를 생성할 수 있게 해준다. 아래 그 이상의 다양한 엔드포인트 모듈을 스프링 integration이 제공한다.

|       모듈       | 의존성 ID(GroupId : org.springframework.integration) (prefix : spring-integration-) |
|:--------------:|:--------------------------------------------------------------------------------:|
|      AMQP      |                             spring-integration-amqp                              |
| 스프링 어플리케이션 이벤트 |                                      event                                       |
|   RSS/ Atom    |                                       feed                                       |
|     파일 시스템     |                                       file                                       |
|    FTP/FTPS    |                                       ftp                                        |
|    GemFire     |                                     gemfire                                      |
|      HTTP      |                                       http                                       |
|      JDBC      |                                       jdbc                                       |
|      JPA       |                                       jpa                                        |
|      JMS       |                                       jms                                        |
|      이메일       |                                       mail                                       |
|    MongoDB     |                                     mongodb                                      |
|      MQTT      |                                       mqtt                                       |
|     Redis      |                                      redis                                       |
|      RMI       |                                       rmi                                        |
|      SFTP      |                                       sftp                                       |
|     STOMP      |                                      stomp                                       |
|      스트림       |                                      stream                                      |
|     Syslog     |                                      syslog                                      |
|    TCP/UDP     |                                        ip                                        |
|    Twitter     |                                     twitter                                      |
|      웹서비스      |                                        ws                                        |
|    WebFlux     |                                     webflux                                      |
|   WebSocket    |                                    websocket                                     |
|      XMPP      |                                       xmpp                                       |
|   ZooKeeper    |                                    zookeeper                                     |

# 요약
- 스프링 통합은 플로우를 정의할 수 있게 해준다. 데이터는 애플리케이션으로 들어오거나 나갈 때 플로우를 통해서 처리할 수 있다.
- 통합 플로우는 XML, Java, Java DSL로 정의할 수 있다.
- 메시지 게이트웨이와 채널 어댑터는 통합플로우의 입구나 출구가 된다.
- 메시지는 플로우 내부에서 변환, 분할, 집적, 전달될 수 있으며, 서비스 액티베이터에 의해서 처리될 수 있다.
- 메시지 채널은 통합 플로우의 컴포넌트들을 연결한다.



