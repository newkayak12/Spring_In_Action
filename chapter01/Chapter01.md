# Spring In Action

```java
package toacos;

@SpringBootApplication
public class TacoCloudApplication {
    public static void main(String[] args) {
        SpringApplication.run(TacaCloudApplication.class, args)
    }    
}
/**
 * @SpringBootApllication은 아래 세 개의 어노테이션을 합친 것이다.
 * 
 *  1. @SpringBootConfiguration: 현재 클래스를 구성 클래스로 지정한다. 
 *  2. @EnableAutoConfiguration> 스프링 부트 자동-구성을 활성화 한다.
 *  3. @ComponentScan : 컴포넌트 스캔을 활성화한다. @Component, @Controller, @Serivice 등을 컨텍스트에
 *  컴포넌트로 등록할 수 있게 찾아준다. 
 * 
 */
```

## 1.3.3 테스트 하기 (23pg)
이 테스트에서 주목할 것은 ```@SpringBooTest``` 대신 ```@WebMvcTest```가 붙는 다는 것이다. 이것은 스프링 부트에서
제공하는 특별한 테스트 어노테이션이며 스프링 MVC 애플리케이션의 형태로 테스트가 되도록 한다. 또한 ```@WebMvcTest```는 또한
스프링 MVC를 테스트하기 위한 스프링 지원을 설정한다. 우리는 실제 서버를 시작하는 것이 아닌 스프링 MVC 모의(Mocking) 메커니즘을 사용해도
충분하므로 모의 테스트를 하기 위해 MovcMvc 객체를 주입한다. 