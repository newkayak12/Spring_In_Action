# 14. 클라우드 구성 관리

## 14.1 구성 공유하기
몇 가지 속성 근원에 있는 속성들을 설정하여 스프링 애플리케이션을 구성할 수 있음을 우리는 여러 차례 지켜봐왔다. 만일 구성 속성을 런타임 환경을 변경하거나 런타임 환경에 
고유한 것이어야 한다면, 자바 시스템 속성 혹은 OS 환경 변수를 구성 속성으로 사용하는 것이 좋다. 그러나 값이 변경될 가능성이 거의 없고 애플리케이션에 특정되는 속성의
경우는 애플리케이션 패키지에 포함되어 배포되는 application.yml에 구성 속성을 지정하는 것이 좋은 선택이다.

간단한 애플리케이션에서는 큰 문제가 없다. 그러나 자바 시스템 속성이나 운영체제의 환경 변수에 구성 속성을 설정하는 경우는 해당 속성의 변경으로 인해 애플리케이션이
다시 시작되어야 한다는 것을 감안해야 한다. JAR/ WAR 파일 내부에 구성 속성을 포함시키는 경우는 해당 속성을 변경하거나 원래 값으로 돌려 놓으려면 빌드, 배포를 반복해야한다.

물론 예외가 있겠지만 대다수의 애플리케이션은 이런 과정을 수행함에 따라 리스크가 뒤따른다. 또한 실행 중인 애플리케이션에 이런 작업을 하는 것은 꽤나 문제가 있다.
또한, DB 비밀번호와 같은 일부 속성들은 보안에 민감한 값을 갖는다. 이런 속성 값은 각 애플리케이션의 속성에 지정될 때 암호화될 수 있지만, 사용 전에 해당 속성 값을 복호화
하는 애플리케이션을 수반해야한다. 

이런 이유로 중앙 집중식으로 구성을 관리할 떄는 어떤 이점이 있을지 알아보자.

- 구성이 더 이상 애플리케이션 코드에 패키징되지 않는다. 따라서 애플리케이션을 빌드, 배포하는 과정 없이 실행 중에 구성을 변경할 수 있다.
- 공통적인 구성을 공유하는 마이크로서비스가 자신의 속성 설정으로 유지/관리하지 않고도 동일한 속성을 공유할 수 있다. 그리고 속성 변경이 필요하면 한 곳에서
한 번만 수정하면 모든 곳에 적용된다.
- 보안에 민감한 구성은 애플리케이션 코드와는 별도로 암호화하고 유지/관리 할 수 있다. 그리고 복호화된 속성 값을 언제든지 애플리케이션에서 사용할 수 있으므로 복호화
를 하는 코드가 불필요해진다.

스프링 클라우드 구성 서버는 애플리케이션의 모든 마이크로서비스가 구성에 의존할 수 있는 서버를 사용해서 중앙 집중식 구성을 제공한다. 따라서 모든 서비스에 공통된 구성은
물론이고, 특정 서비스에 국한된 구성도 한 곳에서 관리할 수 있다.

스프링 클라우드 구성 서버는 애플리케이션의 모든 마이크로서비스가 구성에 의존할 수 있는 서버를 사용해서 중앙 집중식 구성을 제공한다. 따라서 모든 서비스에 공통된 구성은
물론이고, 특정 서비스에 국한된 구성도 한 곳에서 관리할 수 있다.


## 14.2 구성 서버 실행하기
스프링 클라우드 구성 서버는 집중화된 구성 데이터 소스를 제공한다. 구성 서버는 유레카처럼 더 큰 애플리케이션의 마이크로서비스로 생각할 수 있으며, 같은 애플리케이션에
있는 다른 서비스들의 구성 데이터를 제공하는 역할을 수행한다.
구성서버는 클라이언트가 되는 다른 서비스들이 구성 속성을 사용할 수 있도록 REST API를 제공한다. 구성 서버를 통해 제공되는 구성 데이터는 구성 서버 외부에 저장된다.

## 14.2.1 구성 서버 활성화 하기
더 큰 애플리케이션 시스템 내부의 또 다른 마이크로서비스인 구성 서버는 별개의 애플리케이션으로 개발되어 배포된다. 따라서 새로운 구성 서버 프로젝트를 생성해야 한다.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-config-server</artifactId>
    </dependency>
</dependencies>
```
이 의존성의 버전은 스프링 클라우드 릴리즈 트레인에 의해 결정된다. 
이후 구성 서버를 활성화시키면 된다. 구성 서버의 main에 ```@EnableConfigServer```를 추가하면 구성 서버를 활성화하여 자동-구성한다.
```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationName.class, args);
    }
}
```
애플리케이션을 실행하고 구성 서버가 작동하는 것을 알아보기전, 한 가지 더 할 일이 있다. 구성 서버가 처리할 구성 속성들이 있는 곳을 알려줘야한다.
깃 혹은 네이티 위치를 사용할 수 있다. 일단 깃으로 살펴보면
```yaml
server:
  port: 8888
spring:
  cloud:
    config:
      server:
        git:
          uri: github url
```

혹은 리모트가 아닌 서버 파일로 진행하려면

```yaml
server:
  port: 8888
spring:
  cloud:
    config:
      server:
        native:
          search-locations: path
```
로 진행하면 된다.

이후 만약 구성 서버에 구성을 등록했다면, `curl localhost:8888/application/default` 로 접근하면 해당 구성 속성을 불러올 것이다.
여기서 구성 서버의 /application/default 경로에 대한 HTTP GET을 수행한다.

                구성 서버의 호스트, 포트                 스프링 프로필   
            `http://localhost:8888 / application / default  [/master]`
                                    애플리케이션 이름

첫 경로의 application은 구성 서버에 요청하는 애플리케이션의 이름이다. 
두 번째 부분은 애플리케이션에 활성화된 스프링 프로필 이름이다. 
요청 경로의 세 번째는 생략 가능하며, 구성 속성을 가져올 백엔드 GIT의 라벨이나 branch를 지정한다.

필요하다면 하위 경로에 구성 속성을 저장할 수도 있다. 이 때는 uri아래에 ```search-paths```를 지정하면 된다. 이는 쉼표로 복수로 지정할 수 있다.
또한, 와일드 카드(*)를 지정할 수도 있다.

### Git 레포지토리의 분기나 라벨에 구성 속성 저장하고 제공하기 -생략
### Git 백엔드를 사용한 인증


## 14.3 공유되는 구성 데이터 사용하기