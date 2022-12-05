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