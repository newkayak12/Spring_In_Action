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

## 17.3 Admin 서버의 보안

액추에이터에 보안이 중요하듯, Admin 서버에도 보안이 중요하다. 게다가 만일 액추에이터 엔드포인트에서 인증을 요구한다면 Admin 서버가 해당 엔드포인트에 접근하기 위해서 인증
정보를 알아야한다.

## 17.3.1 Admin 서버에 로그인 활성화하기
Admin 서버는 기본적으로 보안이 되지 않으므로 보안을 추가하는 것이 좋다. 역시 스프링 시큐리티로 처리할 수 있다.

## 17.3.2 액추에이터로 인증하기
```yaml
spring:
  application:
    name: test
  boot:
    admin:
      client:
        url:
          - http://localhost:8082
        username: admin
        password: qwer1234!
```
와 같이 spring.boot.admin.client.username, password에 admin의 security ID, Password를 기입하면 된다.

### 요약
- 스프링 부트 Admin 서버는 하나 이상의 스프링 부트 애플리케이션으로부터 엑추에이터 엔드포인트를 소비하고 사용자 인터페이스를 갖춘 웹 애플리케이션에서 데이터를 보여준다.
- 스프링 부트 애플리케이션은 자신을 클라이언트로 Admin에 서버를 등록할 수 있다. 또는 Admin 서버가 유레카를 통해서 클라이언트 애플리케이션을 찾게 할 수 있다.
- 애플리케이션 상태의 스냅샷을 갭쳐하는 엑추에이터 엔드포인트와 다르게, Admin 서버는 애플리케이션의 내부 작업에 관한 실시간 뷰를 보여줄 수 있다.
- Admin 서버는 액추에이터 엔드포인트의 결과를 쉽게 필터링해 주며, 겨웅에 따라서는 그래프로 데이터를 보여준다.
- Admin 서버는 스프링 부트 애플리케이션이므로 스프링 시큐리티를 사용해서 보안 처리를 할 수 있다.

