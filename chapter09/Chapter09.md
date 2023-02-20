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


## 9.2.1
