# 17. 스프링 관리하기

스프링부트 액추에이터가 리턴하는 값은 모두 JSON이다. 이러한 상황에서 엔드포인트를 더 쉽게 사용할 수 있도록 액추에이터 상위
계층에 사용자 인터페이스를 생성하고, 엑추에이터로부터 직접 사용하기 어려운 실시간 데이터를 캡쳐를 해보자.

## 17.1 스프링 부트 ADMIN 사용하기
스프링 부트 Admin은 관리용 프론트 웹 애플리케이션이며, 액추에이터 엔드포인트를 편리하게 소비할 수 있게 한다. 액추에이터 엔드포인트는 두 개의 주요 구성 요소로 나뉜다.

## 17.1.1 Admin 생성
Admin을 활성화하려면 새로운 스프링 부트 애플리케이션을 생성하고 Admin 서버 의존성을 프로젝트 빌드에 추가해야한다. 일반적으로 Admin 서버는 독립 실행형
애플리케이션으로 사용한다. 따라서 Initializr로 새롭게 생성하면서 'Spring Boot Admin(server)'를 선택해서 만들면 된다.

```xml
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-server</artifactId>
</dependency>
```
```java
@SpringBootApplication
@EnableAdminServer
public class AdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}

}
```
이후, Main에 `@EnableAdminServer`를 선언해서 서버를 활성화 한다.

## 17.1.2 Admin에 클라이언트 등록하기
Admin 서버는 다른 스프링 부트 애플리케이션의 액추에이터 데이터를 보여주는 별개의 애플리케이션이므로 다른 애플리케이션을 Admin 서버가 알 수 있도록 클라이언트로 등록해야한다.
스프링 부트 Admin 클라이언트를 Admin에 등록하는 방법은 두 가지이다.

1. 각 애플리케이션이 자신을 Admin에 등록
2. Admin 서버가 유레카 레지스트리를 통해서 서비스를 찾는다.


### Admin 클라이언트 애플리케이션 구성하기
스프링 부트 애플리케이션이 자신을 Admin에 등록하려면 해당 애플리케이션 빌드에 Admin 클라이언트 스타터를 포함시켜야 한다.
'Spring Boot Admin(Client)'를 추가하면 된다.

```xml
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
</dependency>
```
또한 YAML에 
```yaml
spring:
  application:
    name: test
  boot:
    admin:
      client:
        url:
          - http://localhost:8082
```

### Admin 클라이언트 찾기
서비스들을 찾을 수 있게 Admin 서버를 활성화할 때는 Admin 서버 프로젝트 빌드에 스프링 클라우드 Netflix 유레카 클라이언트 스타터를 추가하면 된다.
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```
이렇게 해서 Admin 서버가 유레카 클라이언트로 활성화되면 더 이상 추가해 줄 것은 없다. 그리고 모든 클라이언트에서 application.yaml 파일 구성을 하지 않아도
된다. 왜냐하면 유레카가 알아서 애플리케이션을 찾아서 ADMIN에 등록해주기 떄문이다. 이때 admin 서버 자신이 유레카 서버가 될 수도 있다.

## 17.2 Admin 서버 살펴보기
Admin에는 아래와 같은 사항이 포함된다.

- 애플리케이션의 건강 상태와 정보와 일반 정보
- Micrometer를 통해 발행되는 메트릭과 /metric 엔드포인트
- 환경 속성
- 패키지와 클래스 로깅 레벨
- 쓰레드 추적 기록 정보
- HTTP 요청의 추적 기록
- 감사 로그(auditLog)
