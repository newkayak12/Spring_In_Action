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
@MessagingGateway(defaultRequestChannel = "textIntChannel")
public interface FileWriterGateway {
    void writeToFile (@Header("file_name") String fileName, String data);
}
```