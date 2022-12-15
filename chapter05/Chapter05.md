# Spring In Action

## 5장. 구성 속성 사용하기

- 자동 - 구성되는 빈 조정하기
- 구성 속성을 애플리케이션 컴포넌트에 적용하기
- 스프링 프로파일 사용하기

여러 면에서 스프링 부트의 자동-구성(autoConfiguration)도 이와 유사하다. 자동-구성은 스프링 애플리케이션 개발을 굉장히 단순화해준다. 스프링 애플리케이션 컨텍스트에서
구성 속성은 빈(bean)의 속성이다. 그리고 JVM 시스템 속성, 명령행 인자, 환경 변수 등 여러 가지 원천 속성 중에서 설정할 수 있다.

### 5.1 자동-구성 세부 조정하기
- 빈 연결(Bean wiring) : 스프링 애플리케이션 컨텍스트에서 빈으로 생성되는 애플리케이션 컴포넌트 및 상호 간에 주입되는 방법을 선언하는 구성
- 속성 주입(Property injection) : 스프링 애플리케이션 컨텍스트에서 빈의 속성 값을 설정하는 구성


### 5.1.1 스프링 환경 추상화 이해하기
스프링 환경 추상화(environment abstract)는 구성 가능한 모든 속성을 한 곳에서 관리하는 개념이다. 즉, 속성의 근원을 추상화하여 각 속성을 필요로하는 빈이
스프링 자체에서 해당 속성을 사용할 수 있게 해준다. 스프링 환경에서는 아래와 같은 속성의 근원으로부터 원천 속성을 가져온다.
- JVM 시스템 속성
- 운영체제의 환경 변수
- 명령행 인자(command-line argument)
- 애플리케이션의 속성 구성 파일

그런 다음에 스프링 환경에서 이 속성들을 한 군데로 모은 후 각 속성이 주입되는 스프링 빈을 사용할 수 있게 해준다.

스프링 부트에 의해 자동으로 구성되는 빈들은 스프링 환경으로부터 가져온 속성들을 사용해서 구성될 수 있다. 간단한 예로 포트를 설정하려면 ```src/main/resource/application.properties```
에 값을 설정하면 된다. 또한 애플리케이션을 실행할 때 명령어로 server.port를 지정할 수도 있다. ```java -jar tacoCloud.jar --server.port=9090```
과 같이 말이다. 

단, 환경 변수로 속성을 설정할 떄는 속성 이름의 형태가 약간 달라진다. 운영체제 나르맫로 환경 변수 규칙이 있기 떄문이다.


### 5.1.2 데이터 소스 구성하기
데이터 소스의 경우 우리 나름의 DataSource 빈을 명시적으로 구성할 수 있다. 그러나 스프링 부트 사용 시는 그럴 필요가 없으며, 대신에 구성 속성을 통해서 해당 
데이터베이스의 URL과 인증을 구성하는 것이 더 간단해진다.
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost/tacocloud
    username: tacodb
    password: tacopassword
    driver-class-name: com.mysql.jdbc.Driver
```
spring.datasource.driver-class-name 속성을 설정하지 않아도 문제는 없지만 문제가 생긴다면 설정하면 된다. 그러면 이 DataSource 빈을 자동-구성할 때
스프링 부트가 이런 속성 설정을 연결 데이터로 사용한다. 또한, 톰캣의 JDBC 커넥션 풀(connection pool)을 classpath에서 자동으로 찾을 수 있다면 
DataSource 빈이 그것을 사용한다. 그러나 그렇지 않으면
- HikariCP
- Commons DBCP 2

중 하나를 선택해서 사용한다. 이것이 스프링 부트의 자동-구성을 통해서 사용 가능한 커넥션 풀이다. 그러나 우리가 원하는 DataSource 빈을 명시적으로 구성하면
어떤 커넥션 풀도 사용할 수 있다. 

애플리케션이 시작될 때 데이터베이스를 초기화하는 SQL 스크립트의 실행 방법을 알아봤다. 이때 아래와 같이 ```spring.datasource.schema```와 
```spring.datasource.data```를 사용하면 더 간단하게 지정할 수 있다.

```yaml
spring:
  datasource:
    schema:
      - order-schema.sql
      - ingredient-schema.sql
      - taco-schema.sql
      - user-schema.sql
    data:
      -ingredient.sql
```

또는 명시적인 데이터 소스 구성 대신 JNDI(Java Naming and Directory Interface)에 구성하는 것을 원할 수 있다. 이때는 다음과 같이 ```spring.datasource.jndi-name```
속성을 구성하면 스프링이 찾아준다.

```yaml
spring:
  datasource:
    jndi-name: java:/comp/env/jdbc/tacoCloudDS
```

단, spring.datasource.jndi-name 속성을 설정하면 기존에 설정된 다른 데이터 소스 구성 속성은 무시된다.


### 5.1.4 로깅 구성하기
대부분의 애플리케이션은 어떤 형태로든 로깅(logging)을 제공한다. 설사 우리 애플리케이션이 직접 로깅하지 않더라도 애플리케이션에서 사용하는 라이브러리가
자신의 활동을 로깅할 것이다. 

기본적으로 스프링 부트는 INFO 수준(level)으로 콘솔에 로그 메시지를 쓰기 위해 Logback을 통해 로깅을 구성한다. 애플리케이션을 실행할 때 이미 많은 양의
INFO 수준 항목(메시지)들을 콘솔의 애플리케이션 로그에서 보았을 것이다. 

로깅 구성을 제어할 때는 classpath의 루트에 logback.xml 파일을 생성할 수 있다.

```xml

<configuration>
    <appender name="STDOUT" class="cn.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>
                %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
            </pattern>
        </encoder>
    </appender>
    <logger name="root" level="INFO"/>
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

로깅에서 사용되는 패턴을 제외하면 이 Logback 구성은 logback.xml 파일이 없을 떄의 기본 로깅 구성과 동일하다. 그러나 logback.xml 파일을 수정하면 우리가 원하는 
형태로 애플리케이션 로그 파일을 제어할 수 있다.

로깅 구성에서 가장 많이 변경하는 것은 로깅 수준과 로그를 수록할 파일이다. 스프링부트에서는 위의 파일을 생성하지 않고 변경이 가능하다.

```yaml
logging:
  level:
    root: WARN
    org:
      springframework:
        security: DEBUG
```

위와 같이 로깅할 대상과 레벨을 지정할 수 있다. 만약 로그를 파일로 만들고자 한다면

```yaml
logging:
  path: /var/logs/
  file: TacoCloud.log
  level:
    root: WARN
    org:
      springframework:
        security: DEBUG
```


### 5.1.5 다른 속성의 값 가져오기 
하드 코딩된 String과 숫자 값으로만 속성 값을 설정해야 하는 것은 아니다. 대신에 다른 구성 속성으로부터 값을 가져올 수도 있다.

예를 들어, greeting.welcome이라는 속성을 또 다른 속성인 ```spring.application.name```의 값으로 설정하고 싶다고 해보자. 이때 다음과 같이 ${}를
사용해서 'greeting, welcome'을 설정할 수 있다. 

```yaml
greeting:
  welcome: ${spring.application.name}
```

또한, 다른 텍스트 속에 ${}를 포함시킬 수도 있다. 

```yaml
greeting
  welcome: You are using ${spring.application.name}
```

### 5.2 커스텀 구성 속성 생성하기
구성 속성은 빈의 속성일 뿐이며, 스프링의 환경 추상화로부터 여러 가지 구성을 받기 위해서 설계되었다. 그런데 그런 구성들을 사용한다는 것을
어떻게 빈에 나타낼 수 있을까?

구성 속성의 올바른 주입을 지원하기 위해서 스프링 부트는 @ConfigurationProperties 어노테이션을 제공한다. 그리고 어떤 스프링 빈이건
이 어노테이션이 지정되면, 해당 빈의 속성들이 스프링 환경의 속성으로 주입될 수 있다.
```yaml
taco:
  orders:
      pageSize: 10
```
```java
 import java.beans.ConstructorProperties;

@Component
@ConstructorProperties(prefix="tacao.orders")
@Data
public class OrderProps {
    private int pageSize = 20;
}
```
OrderProps 클래스는 접두어로 taco.orders를 갖는 ```@ConfigurationProperties```가 지정되었다. 
또한, ```@Component```가 지정되었으므로, 스프링 컴포넌트 검색에서 OrderProps를 자동으로 찾은 후 스프링 애플리케이션
컨텍스트의 빈으로 생성해준다.


### 5.3 프로파일로 구성하기
애플리케이션이 서로 다른 런타임 환경에서 배포, 설치될 떄는 대개 구성 명세가 달라진다. 예를 들어 데이터베이스 연결 명세가 개발 환경과
다를 것이고, 프로덕션 환경과도 여전히 다르다. 이때는 각 환경의 속성들을 application.properties나 application.yml에 
정의하는 대신, 운영체제의 환경 변수를 사용해서 구성하는 것이 한 가지 방법이다. 

그러나 하나 이상의 구성 속성을 환경 변수로 지정하는 것은 번거롭다. 게다가 환경 변수의 변경을 추적 관리하거나 오류가 있을 경우에 변경 전으로 바로
되돌릴 수 있는 방법이 마땅치 않다. 따라서 런타임에 활성화되는 프로파일에 따라 서로 다른 빈, 구성클래스, 구성 속성들이 적용 또는 
무시되도록 하는 것이 프로파일이다. 


### 5.3.1 프로파일 특정 속성 정의
프로파일에 특정한 속성을 정의하는 한 가지 방법은 프로덕션 환경의 속성들만 포함하는 또는 다른 .yml이나 .properties를 생성하는 것이다.
이때 파일 이름은 다음 규칙을 따라야한다. ```application-{프로파일 이름}.yml``` 또는 ```application-{프로파일 이름}.properties```이다.
또 YAML 구성에서만 또 다른 방법으로 프로파일 특정 속성을 정의할 수도 있다. 이때는 프로파일에 특정되지 않고 공통으로 적용되는
기본 속성과 함께 프로파일 특정 속성을 application.yml에 지정할 수 있다. 즉, 프로파일에 특정되지 않은 기본 속성 다음에 3개의 -(하이픈)을 추가하고
그 다음에 해당 프로파일의 이름을 나타내는 ```spring.profiles```속성을 지정하면 된다.



### 5.3.2 프로파일 활성화하기
프로파일 특정 속성들의 설정은 해당 프로파일이 활성화되어야 유효하다. 이는 ```spring.profile.active```에 지정하면된다.
그러나 이렇게 고정하면 환경에 맞춰 대응하기 어렵다. 이 때는 ```java -jar [application_name].jar --spring.profiles.active=prod```
와 같이 명령행 인자로 활성화할 프로필을 설정할 수도 있다. 여기서 위 명령행으로 실행하기 전에
```yaml
spring:
 profiles:
  active: prod,audit,ha
```
만일 스프링 애플리케이션을 클라우드 파운드리(cloudFoundry)에 배포할 때는 cloud라는 이름의 프로파일이 자동으로 활성화하면 된다.


### 5.3.3 프로파일을 이용해서 조건별로 빈생성하기
서로 다른 프로파일 각각에 적합한 빈들을 제공하는 것이 유용할 떄가 있다. 일반적으로 자바 구성 클래스에 선언된 빈은 활성화되는 프로파일과는 무관하게 생성된다.
그러나 특정 프로파일이 활성화될 때만 생성되어야하는 빈이 있다면 ```@Profile``` 어노테잇ㄴ을 이용하면된다.

```java
class example {

    @Bean
    @Profile({"dev", "qa"}) // @Profile("!prod")와 같이도 사용할 수 있다. 
    public CommandLinerRunner dataLoader(IngredientRepository repo, UserRepository userRepo,
                                         PasswordEncoder encoder) {
//        ...
    }
}
```

```@Profile```은 ```@Configuration```이 지정된 클래스 전체에 대해서 사용할 수도 있다. 