# 11. 리액티브 API

## 11.1 스프링 WebFlux 사용하기
매 연결마다 하나의 쓰레드를 사용하는 스프링 MVC같은 Servlet 기반의 웹 프레임워크는 쓰레드 블로킹과 다중 쓰레드로 수행. 즉 요청이 처리될 떄
쓰레드 풀에서 작업 쓰레드를 가져와서 요청을 처리하며, 작업 쓰레드가 종료될 때까지 요청 쓰레드는 블로킹된다.

따라서 블로킹 기반 웹 프레임워크는 요청량 증가에 따른 확장이 어렵다. 게다가 처리가 느린 작업 쓰레드로 훨씬 더 심각한 상황이 발생한다. 해당 작업 쓰레드가 풀로 반환되어 또 다른
요청을 준비하는 데 시간이 많이 걸리기 때문이다. 

이에 반해서 비동기 웹 프레임워크는 더 적은 수의 쓰레드로 더 높은 확장성을 성취한다. 이벤트 루핑이라는 기법을 적용한 이런 프레임 워크는 한 쓰레드당 더 많은
요청을 처리할 수 있다.

                           ( 이벤트 푸시 -> )
    다중의 요청 ->  요청 핸들러       -        이벤트 루프   -  작업
                           (<- 콜백 호출 )

데이터 베이스, 네트워크 작업과 같은 집중적인 작업의 콜백과 요청을 비롯해서 이벤트 루프에서는 모든 것이 이벤트로 처리된다. 비요잉 드는 작업이 필요할 때 이벤트 루프는 해당
작업의 콜백을 등록하여 병행으로 수행하게되고 다른 이벤트 처리로 넘어간다.
그리고 작업이 완료될 때 이것을 요청과 동일하게 이벤트로 처리한다. 결과적으로 비동기 웹 프레임 워크는 소수의 쓰레드로 많은 요청을 처리할 수 있어서 쓰레드 관리 부담이 줄어들고
확장이 용이하다.


## 11.1.1 스프링 WebFlux의 개요
원래는 리액티브 모델을 MVC에 억지로 이식하려 했다. 그러나 많은 분기 코드와 이 작업의 결과는 패키징된 두 개의 웹프레임 워크가 됐다.
결국 별도의 리액티브 프레임워크를 만들기로 했고, 결과가 WebFlux이다.

<table>
    <tr>
        <td colspan="2">@Controller, @RequestMapping 등 </td>
        <td colspan="1">RouterFunction</td>
    </tr>
    <tr>
        <td colspan=""></td>
        <td colspan=""></td>
        <td colspan=""></td>
    </tr>
    <tr>
        <td colspan="1">스프링 MVC</td>
        <td colspan="2">스프링 WebFlux</td>
    </tr>
    <tr>
        <td colspan="1">서블릿 API</td>
        <td colspan="3">스프링 WebFlux</td>
    </tr>
    <tr>
        <td colspan="1">스프링 MVC</td>
        <td colspan="3">스프링 WebFlux</td>
    </tr>
</table>
 
WebFlux는 서블릿 API와 연계되지 않는다. 따라서 서블릿 API가 제공하는 것과 동일한 기능의 리액티브 버전인 리액티브 HTTP API의 상위 계층에 위치한다.
그리고 WebFlux는 서블릿 API와 연결되지 않으므로 실행하기 위해 서블릿 컨테이너를 필요로 하지 않는다. 대신 블로킹이 없는 어떤 웹 컨테이너에서도 실행될 수 있다.
이에는 Netty, Undertow, 톰캣, Jetty 또는 다른 서블릿 3.1 이상의 컨테이너가 포함된다.

MVC와 WebFlux간의 공통적인 컴포넌트는 주로 컨트롤러를 정의하는데 사용하는 어노테이션들이다. 
반대로 MVC와 WebFlux간 차이는 RouterFunction에서 나타난다. 이는 어노테이션을 사용하는 대신 함수형 프로그래밍 패러다임으로 컨트롤러를 정의하는 대안 프로그래밍 모델을
나타낸다. 또한 MVC와 WebFlux의 가장 중요한 차이는 빌드에 추가하는 의존성이다. WebFlux는 ```spring-boot-starter-web```등을 추가하는 것이 아닌 스프링 부트 WebFlux 스타터 의존성을 추가해야한다.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webFlux</artifactId>
</dependency>
```

또한, 스프링 MVC 대신 WebFlux를 사용하면 기본 내장 서버가 톰캣 대신 Netty가 된다. Netty는 몇 안되는 비동기적인 이벤트 중심의 서버 중 하나이며, 스프링 WebFlux와
같은 리액티브 웹 프레임워크에 잘 맞는다. 

다른 스타터 의존성을 사용하는 것 외에도 스프링 WebFlux의 컨트롤러 메소드는 대개 도메인 타입이나 컬렉션 대신 Mono, Flux같은 리액티브 타입을 인자로 받거나 반환한다.
또한, 스프링 WebFlux 컨트롤러는 Observable, Single, Complatable과 같은 RxJava 타입도 처리할 수 있다.


### 리액티브 스프링 MVC?
스프링 WebFlux가 Mono, Flux같은 리액티브 타입을 반환하지만, 그렇다고 해서 스프링 MVC가 리액티브를 전혀 사용하지 못하는 것은 아니다. 
단지 차이점은 그런 타입들이 사용되는 방법에 있다. 즉, 스프링 WebFlux는 요청이 이벤트 루프로 처리되는 진정한 리액티브 웹 프레임워크인 반면, 스프링 MVC는 다중 쓰레드에
의존하여 다수의 요청을 처리하는 서블릿 기반 프레임워크이다.


## 11.1.2 리액티브 컨트롤러 작성하기
가장 이상적인 모델은 리액티브 컨트롤러가 리액티브 엔드 to 엔드 스택의 제일 끝에 위치하며, 이 스택에는 컨트롤러, 레포지토리, DB, 여타 서비스가 포함되는 것이 이상적이다.

        CLIENT

          ┃

    WEBFLUX CONTROLLER  ← Flux/ Mono →   Service(Optional)  ← Flux/ Mono →  REPOSITORY
                                                                    
                                                                                 ┃
                                                                    
                                                                                DB

물론 이런 부분에서 대응이 되어 있다. 일단, 현 시점에서 중요한 것은 WebFlux 컨트롤러를 정의하기 위한 노력이 MVC과 크게 다르지 않다는 것에 있다.
이외 중요한 것은 Flux<Taco>와 같은 리액티브 타입을 받을 때 subscribe를 호출할 필요가 없다는 것이다. 프레임워크가 호출하기 때문이다.


### 단일 값 반환하기
```java
@GetMapping("/{id}")
public Mono<Taco> tacoById(@PathVariable("id") Long id) {
return tacoRepo.findById(id);
}
```
와 같이 ReactiveRepository를 채용하면 간단해진다.

### Rxjava 타입 사용하기
스프링 WebFlux에서 Flux나 Mono가 아닌 Observable, Single을 사용할 수도 있다.
```java
@RestController
@RequestMapping(path="/design", produces="application/json")
@CrossOrigin(origins="*")
public class DesignTacoController {
    @GetMapping("/recent")
    public Observable<Taco> recentTacos(){
        return tacoService.getRecentTacos();
    }
    
}
```
와 같이 말이다.

### 리액티브하게 입력 처리하기 
지금까지는 컨트롤러 메소드가 반환하는 리액티브 타입만 알아봤다. 그러나 스프링 WebFlux를 사용할 때 요청을 처리하는 핸들러 메소드의 입력으로 mono, flux를 받을 수 있다. 
```java
@PostMapping(consumes="application/json")
@ResponseStatus(HttpStatus.CREATED)
public Taco postTaco(@RequestBody Taco taco){
    return tacoRepo.save(taco);
}
```
위에서 requestBody로 받는 다는 것이 데이터가 완전하게 분석되어 Taco 객체를 생성하는 데 사용될 수 있어야 postTaco가 실행될 수 있음을 의미한다
또한, 레포지토리의 save() 메소드의 블로킹되는 호출이 끝나고 복귀되어야 postTaco()가 끝나고 복귀할 수 있다는 것을 의미한다.
결과적으로 두 번 블로킹 된다. 1. 파싱, 2. 저장

```java
@PostMapping(consumes="application/json")
@ResponseStatus(HttpStatus.CREATED)
public Mono<Taco> postTaco(@RequestBody Mono<Taco> taco) {
    return tacoRepo.saveAll(taco).next();
}
```
여기서 postTaco()는 Mono<Taco>를 인자로 받아 레포지토리의 saveAll() 메소드를 호출한다. saveAll은 Mono, Flux를 포함해서 리액티브 스트림의 Publisher
인터페이스를 구현한 모든 타입을 인자로 받을 수 있다. 또한 next()로 결과 값을 받을 수 있다. 

saveAll()은 Mono<Taco>를 인자로 받으므로 요청 몸체가 Taco 객체가 분석되는 것을 기다리지 않고 즉시 호출된다. 그리고 레포지토리 또한 리액티브이므로 Mono를 
받아서 즉시 Flux<Taco>를 반환한다. 그리고 next()로 Mono<Taco>로 반환된다. 

## 11.2 함수형 요청 핸들러 정의하기
MVC의 어노테이션 기반 프로그래밍 모델은 몇 가지 단점이 있다.

우선 어노테이션이 '무엇을 하는지'와 '어떻게 해야하는지'를 정의하는 데 괴리가 있다. 어노테이션 자체는 무엇을 정의하며 어떻게는 프레임워크 코드 어딘가에 정의되어 있다.
이로 인해 프로그래밍 모델을 커스터마이징하거나 확장할 때 복잡해진다. 이런 변경을 하려면 어노테이션 외부에 있는 코드로 작업해야하기 때문이다. 게다가 이런 코드의 디버깅은
까다롭다. 어노테이션을 breakpoint로 잡을 수 없기 때문이다. 또한 처음 스프링을 입문하면 어노테이션을 사용하는 환경이 적응하기 어렵다.

이런 이유로 WebFlux에서는 리액티브 API를 정의하기 위한 새로운 함수형 프로그래밍 모델이 소개됐다. 어노테이션이 아닌 요청을 핸들러 코드에 연관시킨다.

- RequestPredicate: 처리될 요청의 종류를 선언한다.
- RouterFunction: 일치하는 요청이 어떻게 핸들러에 전달되어야 하는지 선언한다.
- ServerRequest: HTTP 요청을 나타내며, 헤더와 바디 정보를 사용할 수 있다.
- ServerResponse: HTTP 응답을 나타내며 헤더와 바디 정보를 포함한다.

```java
@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<?> helloRouterFunction() {
        return route(GET("/hello"), request -> ok().body(Mono.just("HELLO WORLD!"), String.class));
    }
}
```
@Configuration이 지정된 RouterFunctionConfig 클래스에는 RouterFunction<?> 타입의 @Bean이 있다. RouterFunction은 요청을 나타내는 RequestPredicate 객체가 어떤 요청 처리 함수와
연관되는지를 선언한다. 

RouterFunction의 route()는 두 개의 인자를 받는다 하나는 RequestPredicate 객체이고, 다른 하나는 일치하는 요청을 처리하는 함수이다. 여기서는 "/hello" 경로의 GET 요청과 일치하는 
RequestPredicate을 RequestPredicates의 GET() 메소드가 선언된다.

두 번쨰 인자로 전달된 핸들러는 메소드 참조가 될 수도 있지만 위 예시는 람다로 처리했다. 요청 처리 람다에서는 ServerRequest를 인자로 받으며, ServerResponse의 ok()
메소드와 이 메소드에서 반환된 BodyBuilder의 body()를 사용해서 ServerResponse를 반환한다. 

이 코드에서 helloRouterFunction() 메소드는 한 종류의 요청만 처리하는 RouterFunction을 반환 타입으로 선언한다. 그러나 다른 종류의 요청을 처리해야 하더라도 또 다른 @Bean
을 작성할 필요가 없다. 대신에 andRoute()를 호출하여 또 다른 RequestPredicate 객체가 어떤 요청 처리 함수와 연관되는지만 선언하면 된다.

```java

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<?> helloRouterFunction() {
        return route(GET("/hello"), request -> ok().body(Mono.just("HELLO WORLD!"), String.class))
                .andRoute(GET("/bye"), request -> ok().body(Mono.just("SEE YA!"), String.class))
                .andRoute(GET("/TEST"), this::test);
    }

    public Mono<ServerResponse> test(ServerRequest request) {
//        request.bodyToMono()
//        request.bodyToFlux()
        return ServerResponse.ok().body("TEST", String.class);
    }
}
```

## 요약
- 스프링 WebFlux는 리액티브 웹 프레임워크를 제공한다. 이 프레임워크의 프로그래밍 모델은 스프링 MVC가 상당수 반영되었다.
- Webflux에서는 함수형 프로그래밍 모델을 제공한다.
- 리액티브 컨트롤러는 WebTestClient로 테스트할 수 있다.
- WebClient는 RestTemplate의 리액티브 버전이다.
