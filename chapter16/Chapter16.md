# 16. 스프링 부트 액추에이터 사용하기

스프링 부트 액추에이터는 스프링 부터 애플리케이션의 모니터링이나 메트릭(metric)과 같은 기능을 HTTP와 JMX 엔드포인트(endpoint)를 통해서
제공한다.


## 16.1 액추에이터 개요
기계 장치에서 액추에이터는 메커니즘을 제어하고 작동시키는 부품이다. 스프링 부트 애플리케이션에서는 스프링 부트 액추에이터가 그와 같은 역할을 수행한다.
즉, 실행 중인 애플리케이션의 내부를 볼 수 있게 하고, 어느 정도까지는 애플리케이션의 작동 방법을 제어할 수 있게 한다.
액추에이터가 노출하는 엔드포인트를 사요하면 실행 중인 스프링 부트 애플리케이션의 내부 상태에 관한 것을 알 수 있다. 예를 들면

- 애플리케이션 환경에서 사용할 수 있는 구성 속성들
- 애플리케이션에 포함된 다양한 패키지의 로깅 레벨
- 애플리케이션이 사용 중인 메모리
- 지정된 엔드포인트가 받은 요청 횟수
- 애플리케이션의 건강 상태 정보

```xml
<dependency>
    <grouopId>org.springframework.boot</grouopId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

이처럼 액추에이터가 스타터 프로젝트 빌드의 일부가 되면 여러 가지 액추에이터 엔드포인트를 사용할 수 있다.

|   HTTP METHOD   |      PATH       |                                   DESCRIPTION                                   | 기본 활성 여부 |
|:---------------:|:---------------:|:-------------------------------------------------------------------------------:|:-------:|
|       GET       |  /auditevents   |                            호출된 감사(audit) 이벤트 리포트를 생성                            |    N    |
|       GET       |     /beans      |                          스프링 애플리케이션 컨텍스트의 모든 빈을 알려준다.                           |    N    |
|       GET       |   /conditions   |                         성공 또는 실패했던 자동-구성 조건의 내역을 생성한다.                          |    N    |
|       GET       |  /configprops   |                            모든 구성 속성들은 현재 값과 같이 알려준다.                            |    N    |
| GET,<br/>POST,<br/>DELETE |      /env       |                 스프링 애플리케이션에 사용할 수 있는 모든 속성 근원과 이 근원들의 속성을 알려준다.                 |    N    |
|       GET       | /env/{toMatch}  |                               특정 환경 속성의 값을 알려준다.                                |    N    |
|       GET       |     /health     |                             애플리케이션의 건강 상태 정보를 반환한다.                             |    Y    |
|       GET       |    /heapdump    |                                 힙의 덤프를 다운로드 한다.                                 |    N    |
|       GET       |   /httptrace    |                         가장 최근의 100개 요청에 대한 추적 기록을 생성한다.                         |    N    |
|       GET       |      /info      |                          개발자가 정의한 애플리케이션에 관한 정보를 반환한다.                          |    Y    |
|       GET       |    /loggers     |                    애플리케이션의 패키지 리스트(각 패키지의 로깅 레벨이 포함된)를 생성한다.                    |    N    |
|    GET,POST     | /loggers/{name} | 지정된 로거의 로깅 레벨(구성된 로깅 레벨과 유효 로깅 레벨 모두)을 반환한다. 유효 로깅 레벨은 HTTP POST 요청으로 설정될 수 있다. |    N    |
|       GET       |    /mappings    |                   모든 HTTP 매핑과 이 매핑들을 처리하는 핸들러 메소드들의 내역을 제공한다.                   |    N    |
|       GET       |    /metrics     |                                모든 메트릭 리스트를 반환한다.                                |    N    |
|       GET       | /metrics/{name} |                                지정된 메트릭의 값을 반환한다.                                |    N    |
|       GET       | /scheduledtasks |                             스케쥴링된 모든 태스크의 내역을 제공한다.                             |    N    |
|       GET       |   /threaddump   |                            모든 애플리케이션 쓰레드의 내역을 반환한다.                             |    N    |

## 16.1.1 엑추에이터의 기본 경로 구성하기
위 표의 경로 앞에는 기본적으로  '/actuator'가 붙는다. 예를 들어, 액추에이터로부터 애플리케이션 건강 상태 정보를 가져오고 싶을 때는 /actuator/health에 GET 요청을 하면 된다.
만약 `/actuator`를 바꾸고 싶다면
```yaml
management:
  endpoints:
    web:
      base-path: /management
```
이러면 `/management/health`로 GET 요청해야 한다.

## 16.1.2 액추에이터 엔드포인트의 활성화와 비활성화
대부분의 액추에이터 엔드포인트는 민감한 정보를 제공하므로 보안 처리가 되어야 하기 때문에 default가 'N'이다. 물론 스프링 시큐리티로 액추에이터 보안 처리를 할 수 있다.
여튼 보안처리가 없고, 민감한 정보를 제공하므로 기본 값은 꺼져 있음이다.

엔드포인트의 노출 여부를 제어할 때는 ```management.endpoints.web.exposure.include```와 ```management.endpoints.web.exposure.include```
를 사용하면 노출을 원하는 엔드포인트를 지정할 수 있다.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, beans, conditions
```
또한 include 속성은 와일드카드(*)도 사용할 수 있다. 이는 include로 `*`를 놓고 exclude에서 노출하지 않고 싶은 것만 제거할 수 있음을 의미한다.

## 16.2 액추에이터 엔드포인트 소비하기
액추에이터는 실행 중인 애플리케이션의 흥미롭고 유용한 정보를 HTTP 엔드포인트를 통해서 제공한다. 그리고 HTTP 엔드포인트이므로 다른 REST API 처럼 브라우저 기반 
혹은 terminal에서 curl로도 소비할 수 있다.

엑추에이터가 제공하는 엔드포인트에 어떤 것이 있는지 알아보기 위해서 액추에이터의 기본 경로에 대해서 GET을 하면 HATEOAS 링크를 응답으로 받을 수 있다.

## 16.2.1 애플리케이션 기본 정보 가져오기

### 애플리케이션에 관한 정보 요구하기
실행 중인 애플리케이션에 대한 정보를 얻으려면 /info 엔드포인트에 요구하면 된다. 주로
```yaml
info:
  contact:
    email: ~
    phone: ~
```
과 같은 정보가 info에 담겨서 출력된다.

### 애플리케이션 건강 상태 살펴보기
/heath 엔드포인트에 HTTP GET 요청을 하면 건강 상태를 갖는 간단한 JSON을 응답받는다. 
`{"status":"UP"}`과 같이 말이다.
UP는 하나 이상의 건강 상태를 종합한 지표이다. 건강 지표는 애플리케이션이 상호 작용하는 외부 시스템(DB, 메시지 브로커, Eureka, configServer)의 건강 상태를
나타낸다.

- UP: 외부 시스템이 작동 중이고 접근 가능
- DOWN: 외부 시스템이 장도하지 않거나 접근 불가
- UNKNOWN: 외부 시스템 상태를 모름
- OUT_OF_SERVICE: 외부 시스템에 접근할 수 있지만 현재는 사용 불가

모든 건강 지표의 건강 상태는 아래의 규칙에 따라 애플리케이션의 전체 건강 상태로 종합된다.

- 모든 건강 지표가 UP이면 애플리케이션의 건강 상태도 UP
- 하나 이상의 건강 지표가 DOWN이면 건강 상태고 DOWN
- 하나 이상의 건강 지표가 OUT_OF_SERVICE이면 애플리케이션 건강 상태도 OUT_OF_SERVICE
- UNKNOWN은 무시되며 애플리케이션 종합 건강 상태에 고려되지 않는다.

```yaml
management:
  endpoint:
    health:
      show-details: always
```
로 상세 건강 지표를 볼 수 있다. 해당 속성의 기본값은 never이다. 또한   `when-authorized`로 설정하면 요청하는 클라이언트가 완벽하게 인가된 경우에 한해서 상세 내역을
보여준다.

자동 구성에서는 애플리케이션과 관련된 건강 지표만 /heath에 노출된다. 그러나 건강 지표에 아래의 항목을 추가하여 건강 지표를 제공 받을 수도 있다.
- 카산드라
- 구성 서버
- Couchbase
- 유레카
- Hystrix
- JDBC 데이터 소스
- Elasticsearch
- InfluxDB
- JMS 메시지 브로커
- LDAP
- 이메일 서버
- Neo4j
- RabbitMQ
- Redis
- Solr

또한 서드 파티 라이브러리들도 건강 지표를 제공할 수 있다.


## 16.2.2 구성 상세 정보 보기
애플리케이션에 관한 일반 정보를 받는 것은 기본적으로 필요하지만, 이보다 더 유용한 정보를 알아야 할 필요가 있다. 예를 들면 애플리케이션이 어떻게 구성됐는지, 애플리케이션
컨텍스트에 어떤 빈이 있는지, 어떤 자동-구성이 실패/성공인지 어떤 환경 속성들을 애플리케이션에 사용할 수 있는지, HTTP 요청이 어떻게 컨트롤러와 연결되는지, 하나 이상의 
패키지나 클래스에 어떤 로깅 레벨이 설정됐는지 등이다.

이런 것들은 액추에이터의 /bean, /conditions, /env, /configprops, /mappings, /loggers 엔드포인트로 알 수 있다. /env, /loggers 같은 엔드포인드의
경우는 실행 중인 애플리케이션 구성을 실시간으로 조정할 수 있다.

### Bean 연결 정보 얻기
스프링 애플리케이션 컨텍스트를 살펴보는 데 가장 중요한 엔드포인트가 /beans 엔드포인트다. 이 엔드포인트는 애플리케이션 컨텍스트의 모든 빈을 나타내는 JSON을 반환한다.
응답의 최상위는 context 이며, 이는 애플리케이션에 있는 각 스프링 애플리케이션 컨텍스트의 하위 요소 하나를 포함한다. 그리고 각 스프링 애플리케이션 컨텍스트에는 bean 요소가 있으며,
이는 해당 애플리케이션 컨텍스트에 있는 모든 빈의 상세 정보를 갖는다.

### 자동-구성 내역 알아보기
자동-구성에 대해서 궁금할 수 있다. 왜 자동-구성이 됐는지, 또는 자동-구성이 됐을 것 같은데 왜 안됐는지 등이다. 이런 경우에  /conditions 엔드포인트를 GET 하여
알아볼 수 있다.

/conditions 요청에 대한 결과는 세 부분으로 나눈다. ```PositiveMatches (성공한 조건부 구성)```, ```NegativeMatches(실패한 조건부 구성)```, ```UnconditionalClasses(조건 없는 클래스)```
이다. positiveMatches는 자동-구성이 성공했음을, 여기서 message에 ```@ConditionalOnMissingBean```이 있다면 빈이 구성되지 않았으면 구성되게 한다.
neagetiveMatches는 자동-구성이 실패한 것을 의미한다. 마지막으로 unconditionalClasses는 조건 없이 구성됨을 의미한다. 이는 스프링 부트 작동에 기본이 되는 것이므로
구성 속성에 관련된 구성은 조건없이 자동-구성되기 때문이다.


### 환경 속성과 구성 속성 알아보기
애플리케이션의 빈들이 어떻게 연결되어 있는지 아는 것에 추가하여 어떤 환경 속성들이 사용 가능하고 어떤 구성 속성들이 각 빈에 주입되었는지 파악하는 것도 중요하다.
/env 엔드포인트에 GET을 하면 스프링 애플리케이션에 적용 중인 모든 속성 근원의 속성들을 포함하는 응답을 받을 수 있다.

주목할 만한 몇 가지를 보자면 activeProfiles, propertySource 등이 있다.
/env는 단순히 속성을 보는 것만이 아니라 속성을 설정할 수도 있다. 즉 key-value 쌍으로 이뤄진 JSON 문서를 담은 POST를 보내면 실행 중인 애플리케이션의 속성을 
설정할 수 있다.

```shell
 curl http://localhost:8080/actuator/env \
-d'{"name:"hello", "value":"bye"}'\
-H "Content-type: application/json"
```
필요 없는 속성은 DELETE 요청으로 삭제할 수 있다. 이러한 변경은 일시적이며 재시작시 적용되지 않는다.

### HTTP 요청 - 매핑 내역 보기
스프링 MVC의 프로그래밍 모델은 HTTP 요청을 쉽게 처리한다. 그러나 애플리케이션이 처리할 수 있는 모든 HTTP 요청, 그리고 이런 요청들을 어떤 종류의 컴포넌트가 처리하는지를
전체적으로 파악하기는 어려울 수 있다.
액츄에이터의 /mappings 엔드포인트는 애플리케이션의 모든 HTTP 요청 핸들러 내역을 제공한다.

### 로깅 레벨 관리하기
어떤 애플리케이션이든 로깅은 중요한 기능이다. 실행 중인 애플리케이션에서 어떤 로깅레벨이 설정됐는지 궁금하면 /loggers 엔드포인트에 GET을 하면 된다.
/env와 마찬가지로 POST로 configured 로깅 레벨을 변경할 수 있다.
```shell
curl localhost:8080/actuator/loggers/ROOT \
-d'{"configuredLevel":"WARN"}'\
-H "Content-type:application/json"
```
## 16.2.3 애플리케이션 활동 지켜보기
애플리케이션이 처리하는 HTTP 요청이나 애플리케이션에 있는 모든 쓰레드의 작동을 포함해서 실행 중인 애플리케이션의 활동을 지켜보는 것은 유용하다.
이를 위해서 /httptrace, /threaddump, /heapdump 엔드포인트를 제공한다.

/heapdump는 상세하게 나타내기 가장 어려운 액추에이터 엔드포인트이다. 메모리나 쓰레드 문제를 찾는 데 사용할 수 있는 gzip 압축 형태의 HPROF 힙 덤프 파일을 다운로드한다.


### HTTP 요청 추적하기
/httptrace 엔드포인트는 애플리케이션이 처리한 가장 최근의 100개 요청을 알려주며, HTTP 요청 메소드와 경로, 요청이 처리된 시점을 나타내는 타임스탬프,
요청과 응답 헤더들, 요청 처리 소요 시간 등이 출력된다.
```java
@Configuration
public class Config {
    @Bean
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository();
    }
}
```
(✔ 위와 같이 설정하지 않으면 자동으로 설정이 되지 않는 것 같다.)

### 쓰레드 모니터링
HTTP 요청 추적에 추가하여 실행 중인 애플리케이션에서 무슨 일이 생기는지 결정하는 데 쓰레드의 활동이 유용할 수 있다. /threaddump 엔드포인트는 현재 실행 중인 
쓰레드에 관한 스냅샷을 제공한다. 완전한 쓰레드 덤프 응답은 실행 중인 애플리케이션의 모든 쓰레드를 포함한다. 
쓰레드 덤프는 쓰레드의 블로킹, 로킹 과나련 상세 정보와 스택 기록이 포함된다.


## 16.2.4 런타임 메트릭 활용하기
/metric 엔드포인트는 실행 중인 애플리케이션에서 생성되는 온갖 종류의 메트릭을 제공할 수 있으며, 여기에는 메모리, 프로세스, 가비지 컬렉션, HTTP 요청 관련 메트릭 등이 포함된다.
엑추에이터의 기본으로 제공하는 메트릭의 종류는 굉장히 많다. 

```json
{
  "names": [
    "application.ready.time",
    "application.started.time",
    "disk.free",
    "disk.total",
    "executor.active",
    "executor.completed",
    "executor.pool.core",
    "executor.pool.max",
    "executor.pool.size",
    "executor.queue.remaining",
    "executor.queued",
    "http.server.requests",
    "jvm.buffer.count",
    "jvm.buffer.memory.used",
    "jvm.buffer.total.capacity",
    "jvm.classes.loaded",
    "jvm.classes.unloaded",
    "jvm.gc.live.data.size",
    "jvm.gc.max.data.size",
    "jvm.gc.memory.allocated",
    "jvm.gc.memory.promoted",
    "jvm.gc.overhead",
    "jvm.gc.pause",
    "jvm.memory.committed",
    "jvm.memory.max",
    "jvm.memory.usage.after.gc",
    "jvm.memory.used",
    "jvm.threads.daemon",
    "jvm.threads.live",
    "jvm.threads.peak",
    "jvm.threads.states",
    "logback.events",
    "process.cpu.usage",
    "process.files.max",
    "process.files.open",
    "process.start.time",
    "process.uptime",
    "system.cpu.count",
    "system.cpu.usage",
    "system.load.average.1m",
    "tomcat.sessions.active.current",
    "tomcat.sessions.active.max",
    "tomcat.sessions.alive.max",
    "tomcat.sessions.created",
    "tomcat.sessions.expired",
    "tomcat.sessions.rejected"
  ]
}
```
등이 있다. 
예를 들어 http.server.requests를 보면
`http://localhost/actuator/metrics/http.server.requests` 와 같이 접근하고 특정 태그 이름과 값을 지정하면 해당 값을 포커싱한다.
`http://localhost/actuator/metrics/http.server.requests?tag=status:500` 와 같이 말이다.


## 16.3 액추에이터 커스터마이징
액추에이터의 가장 큰 특징은 애플리케이션의 특정 요구를 충족하기 위해서 커스터마이징할 수 있다는 것이다.

## 16.3.1 /info 엔드포인트 정보ㅔㅈ공하기
yaml에 info.으로 시작하는 속성을 생성하면 쉽게 커스텀 데이터를 추가할 수 있다. 
하지만 그 이외에도 다른 방법이 있다. 스프링 부트는 InfoContributor라는 인터페이스를 제공하며, 이 인터페이스는 우리가 원하는 어떤 정보도 /info 엔드포인트 응답에
추가할 수 있게 한다. 

### 커스텀 정보 제공자 생성하기
```java
@Component
public class InfoConfig implements InfoContributor {
    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("TEST", Map.of("KEY", "VALUE"));
    }
}
```
위와 같이 구현하면 커스텀 엔드포인트로 지정할 수 있다. 이렇게 하면 /info 엔드포인트에 원하는 정보를 동적으로 추가할 수 있다. 반면 info.는 정적 속성을 추가할 수 있다.

### 빌드 정보를 /info 엔드포인트에 주입하기

스프링 부트에는 /info 엔드포인트 응답에 자동으로 정보를 추가해 주는 몇 가지 InfoContributor 구현체가 포함되어 있다. 이 중에서 BuilderInfoContributor가 
있는데 이는 프로젝트 빌드 파일의 정보를 /info에 추가해준다.

Maven
```xml
<build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>build-info</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```
Gradle
```shell
springBoot {
  buildInfo()
}
```
메이븐, 그래들 중 어떤 방법을 사용하든 빌드가 끝나면 WAR, JAR에 build-info.properties가 생성된다. 그리고 /info 엔드포인트에 응답이 반환된다.

## 16.3.2 커스텀 건강 지표 정의하기
스프링 부트에는 몇 가지 건강 지표가 포함되어 있으며, 이 건강 지표들은 스프링 애플리케이션에 통합할 수 있는 많은 외부 시스템의 건강 상태 정보를 제공한다. 그러나 
때도로는 스프링 부트에서 지원하지 않거나 건강 지표를 제공하지 않는 외부 시스템을 사용하는 경우가 생길 수 있다.

예를 들어, 현 애플리케이션이 기존 레거시 메인프레임 애플리케이션과 통합될 수 있으며, 이 경우 우리 애플리케이션의 건강 상태는 레거시 시스템의 건강 상태에
영향을 받을 수 있다. 커스텀 건강 지표를 생성할 때는 HealthIndicator 인터페이스를 구현하는 빈만 생성하면 된다.

```java
@Component
public class HealthConfig implements HealthIndicator {

    @Override
    public Health health() {
        Random random = new Random(10);
        Integer randomNumber = random.nextInt();
        if(randomNumber >= 0 && randomNumber < 5){
            return Health.up().build();
        } else {
            return Health.down().withDetail("reason", "test").build();
        }
    }
}
```

## 16.3.3 커스텀 메트릭 등록하기
/metrics는 내장된 메트릭에만 국한되지 않는다. 궁극적으로 액추에이터 매트릭은 Micrometer에 의해서 구현된다. 이는 벤더 중립적인 메트릭이며, 애플리케이션이 원하는 어떤
메트릭도 발행하여 서드파티 모니터링 시스템에서 보여줄 수 있다.

Micrometer로 메트릭을 발행하는 가장 기본적인 방법은 Micrometer의 MeterRegistry를 사용하는 것이다. 스프링 부트 애플리케이션에서 메트릭을 발행할 때는 어디든
필요한 곳에 MetricRegistry만 주입하면 된다.

```java
@Component
public class TacoMetrics extends AbstractRepositoryEventListener<Taco>{
    private MeterRegistry meterRegistry;
    public TacoMetrics(MeterRegistry meterRegistry){
        this.meterRegistry = meterRegistry;
    }
    
    @Override
    protected void onAfterCreate(Taco taco){
        List<Ingredient> ingredients = taco.getIngredients();
        for( Ingredient ingredient : ingredients ){
            meterRegistry.counter("tacoCloud", "ingredient", ingredient.getId()).increament();
        }
    }
}
```

등과 같이 구현하면 된다.(메트릭도 사용할 수 있는 부분이 따로 있는 것으로 보인다.)

## 16.3.4 커스텀 엔드포인트 생성하기
엔드포인트는 HTTP 요청을 처리하는 것은 물론이고 JMXMBeans로도 노출되어 사용될 수 있다. 따라서 엔드포인트는 컨트롤러 클래스 이상의 것이다.
실제로 액추에이터 엔드포인트는 컨트롤러와 매우 다르게 정의된다. `@Controller`, `@RestController` 어노테이션으로 지정되는 클래스 대신, 액추에이터
엔드포인트는 `@Endpoint`로 지정되는 클래스로 정의된다. 또는 GET, POST, PUT, DELETE 등과 달리 액추에이터 엔드포인트 오퍼레이션은 `@ReadOperation`,
`@WriteOperation`, `@DeleteOperation` 어노테이션이 지정된 메소드로 정의된다. 또한, 이 어노테이션들은 어떤 특정 통신 메커니즘도 수반하지 않으므로 액추
에이터가 다양한 통신 메커니즘(HTTP, JMX)으로 통신할 수 있다. 
```java

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Endpoint(id = "test", enableByDefault = true)
public class CustomEndpoint {
    private List<String> array = new ArrayList<>();

    @ReadOperation
    public String readTest(){
        return array.stream().collect(Collectors.joining(", "));
    }

    @WriteOperation
    public String writeTest(String text){
        array.add(text);
        return array.stream().collect(Collectors.joining(", "));
    }

    @DeleteOperation
    public String deleteTest(){
        array.remove(array.size() - 1);
        return array.stream().collect(Collectors.joining(", "));
    }
}

```
커스텀 엔드포인트는 @Component가 선언되어 있어서 스프링 애플리케이션 컨텍스트의 빈으로 생성된다. 이 클래스는 또한 @EndPoint가 지정되었다. 따라서 
ID가 test인 액추에이터 엔드포인트가 된다.
/actuator/{id}로, 메소드에 따라 각 메소드가 반응한다.

여기서는 HTTP 엔드포인트에 한정되어 있다. 그러나 엔드포인트는 MBeans도 노출될 수 있으므로 어떤 JMX 클라이언트에서도 이 엔드포인트를 사용할 수 있다. 만약 HTTP 엔드포인트로만
제한하고 싶다면 @Endpoint 대신 @WebEndPoint로 지정하면 HTTP 엔드포인트로만 작동한다. 반대로 MBeans로만 제한하고 싶다면 @JmxEndpoint로 지정하면 된다.


## 16.4 액추에이터 보안 처리하기
액추에이터는 다양한 일을 할 수 있다. 따라서 유효한 접근 권한을 갖는 클라이언트만이 엔드포인트를 소비할 수 있도록 액추에이터를 보안 처리하는 것이 좋은 생각이다.
물론 보안이 중요하지만 보안 자체는 액추에이터의 책임 범위를 벗어난다. 대신에 스프링 시큐리티로 액추에이터 보안 처리를 해야한다.

액추에이터 엔드포인트들은 공통 기본 경로인 /actuator 아래 모여 있으므로 모든 액추에이터 엔드포인트 전반에 인증 규칙을 적용하기 쉽다.

- <strike>WebSecurityConfigurerApdater</strike> (deprecated)

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
public class SecurityOld extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/actuator/**")
                .hasRole("ADMIN").and().httpBasic();
    }
}
```
- Bean으로 등록

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityLatest {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/actuator/**")
                .hasRole("ADMIN").and().httpBasic();

        return http.build();
    }
}
```

이 경우 액추에이터에서 엔드포인트를 사용하려면 ROLE_ADMIN 권한을 갖도록 인가한 사용자로부터 요청되어야 한다. 여기서는 또한 클라이언트 애플리케이션이 요청이 
Authroization 헤더에 암호화된 인증 정보를 제출할 수 있도록 HTTP 기본 인증도 구성하였다.

이처럼 액추에이터 보안을 처리할 때 유일한 문제점은 엔드포인트의 경로가 /actuator/**로 하드 코딩됐다는 점이다. 이런 상황에서 스프링 부트는 EndpointRequest 클래스도 제공한다.
```java
@Configuration
@EnableWebSecurity
public class SecurityLatest {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.requestMatcher(
                        EndpointRequest.toAnyEndpoint()
                                .excluding("health")
                )
                .authorizeRequests()
                .anyRequest()
                .hasRole("ADMIN").and().httpBasic();

        return http.build();
    }
}
```
EndpointRequest.toAnyEndPoint()로 엑추에이터 엔드포인트와 일치하는 요청을 반환한다. 또한 excluding()으로 제외를 할 수도 있다.
혹은 특정 엔드포인트에만 적요아혹 싶다면 `EndpointRequest.to("beans")`와 같이 이름을 인자로 보내면 된다.


### 요약

- 스프링 부트 액추에이터는 HTTP, JMX MBeans 모두의 엔포인트를 제공하며 엔드포인트는 스프링 부트 애플리케이션의 내부 활동을 볼 수 있다.
- 대부분의 액추에이터 엔드포인트는 기본 비활성이나 `management.endpoint.web.exposure.include`, `management.endpoint.web.exposure.exclude`
로 선택적으로 노출 시킬 수 있다.
- /loggers, /env 등은 실행 중인 애플리케이션의 구성을 실시간으로 변경하는 쓰기 오퍼레이션을 제공한다. 
- 애플리케이션의 건강 상태는 외부에 통합된 애플리케이션 건강 상태를 추적하는 커스텀 건강 지표에 의해 영향을 받을 수 있다.
- 커스텀 애플리케이션 매트릭은 Micrometer를 통해서 등록할 수 있다. Micrometer는 벤더 중립적인 매트릭이며, 애플리케이션이 원하는 어떤 메트릭도
발행하여 서드 파티 모니터링 시스템에서 보일 수 있다.
- 스프링 웹 애플리케이션의 다른 엔드포인트와 마찬가지로 엑추에이터 엔드포인트는 스프링 시큐리티를 사용해서 보안을 처리할 수 있다.
