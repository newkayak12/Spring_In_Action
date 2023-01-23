### 6.2 하이퍼미디어 사용하기

API 클라이언트 코드에서는 흔히 하드코딩된 URL 패턴을 사용하고 문자열로 처리한다. 그러나 API의 URL 스킴이 변경되면 어떻게 될까?
하드 코딩된 클라이언트 코드는 AP~~~~I를 잘못 인식하여 정상적으로 실행되지 않을 것이다. 따라서 API URL을 하드코딩하고 문자열로 처리하면
클라이언트 코드가 불안정해진다. 

REST API를 구현하는 또하는 방법으로는 HATEOAS(Hypermedia As The Engine Of Application State)가 있다. 
이 것은 API로부터 반환되는 리소스(데이터)에 해당 리소스와 관련된 하이퍼 링크들이 포함된다. 따라서 클라이언트가
최소한의 API URL 만 알면 반환되는 리소스와 관련하여 처리 가능한 다른 API URL들을 알아내어 사용할 수 있다.
```json

[
  {
    "id": 4,
    "name": "Veg-Out",
    "createdAt": "2018-01-31T20:15:53.219+0000",
    "ingredients": [
      {"id": "FLTO", "name": "Flour Tortilla", "type": "WRAP"},
      {"id": "COTO", "name": "Corn Tortilla", "type": "WRAP"},
      {"id": "TMTO", "name": "Diced Tomatoes", "type": "VEGGIES"},
      {"id": "LETC", "name": "Lettuce", "type": "VEGGIES"},
      {"id": "SLSA", "name": "Salsa", "type": "SAUCE"} 
    ]
  }
]
```
위 예시의 경우 클라이언트가 각 타코에 대해서 HTTP 작업을 수행하고 싶다면 해당 경로에 URL id를 추가해서 다시 던져야한다.
이와 다르게 API에 하이퍼미디어가 활성화되면 해당 API에는 자신과 관련된 URL이 나타나므로 클라이언트가 하드코딩하지 않아도 된다. 

```json
"_embedded": {
    "tacoResourceList": [ 
      {
          "name": "Veg-Out",
          "createdAt":
          "2018-01-31T20:15:53.219+0000",
          "ingredients": [ 
              { "name": "Flour Tortilla", "type": "WRAP", "_links": { "self": { "href": "http://localhost:8080/ingredients/FLTO" } } },
              { "name": "Corn Tortilla", "type": "WRAP", "_links": { "self": { "href": "http://localhost:8080/ingredients/COTO" } } },
              { "name": "Diced Tomatoes", "type": "VEGGIES", "_links": { "self": { "href": "http://localhost:8080/ingredients/TMTO" } } },
              { "name": "Lettuce", "type": "VEGGIES", "_links": { "self": { "href": "http://localhost:8080/ingredients/LETC" } } },
              { "name": "Salsa", "type": "SAUCE", "_links": { "self": { "href": "http://localhost:8080/ingredients/SLSA" } } }
            ],
          "_links": { "self": { "href": "http://localhost:8080/design/4" } } 
      }
  ] 
}
```
이런 형태의 HATEOAS를 HAL(Hypertext Application Language)라고 한다. 이것은 JSON 응답에 하이퍼링크를 포함시킬 때 주로 사용되는 형식이다.
스프링 HATEOAS 프로젝트는 하이퍼링크를 스프링에 지원한다. 구체적으로 말해서 스프링 MVC 컨트롤러에서 리소스를 반환하기 전에 해당 리소스에 링크를 추가하는 데
사용할 수 있는 클래스와 리소스 어셈블러들을 제공한다. 


### 6.2.1 하이퍼링크 추가하기
스프링 HATEOAS는 하이퍼링크 리소스를 나타내는 두 개의 기본 타입인 Resource와 Resources를 제공한다. 두 타입이 전달하는 링크는 스프링 MVC 컨트롤러 메소드에서 반환
될 떄 클라이언트가 받는 JSON (또는 XML)에 포함된다.

```java
@Controller
class HateosController {
    @GetMapping("/recent")
    public Resources<Resource<Taco>> recentTacos() {
        PageRequest page = PageRequest.of(0, 12, Sort.by("createdAt").descending());
        List<Taco> tacos = tacoRepo.findAll(page).getContent();
        Resources<Resource<Taco>> recentResources = Resources.wrap(tacos);
        recentResources.add(new Link("http://localhost:8080/design/recent", "recent"));
        return recentResources;
    }
}
```

여기서 거슬리는 점이 있다면 ``` http://localhost:8080/ ```이겠다. 이 부분을 HATEOAS 링크 빌더를 사용해서 커버할 수 있다.

```java
@Controller
class HateosController {
    @GetMapping("/recent")
    public Resources<Resource<Taco>> recentTacos() {
        PageRequest page = PageRequest.of(0, 12, Sort.by("createdAt").descending());
        List<Taco> tacos = tacoRepo.findAll(page).getContent();
        Resources<Resource<Taco>> recentResources = Resources.wrap(tacos);
        recentResources.add(
//            ControllerLinkBuilder.linkTo(DesignTacoController.class).slash("recent").withRel("recents")
//                혹은
            ControllerLinkBuilder.linkTo(methodsOn(DesignTacoController.class).recentTacos()).withRel("recents")
                
        );
        return recentResources;
    }
}
```
```linkTo()```와 ```methodOn()```를 사용하면 해당 컨트롤러에서 URL의 모든 값을 얻을 수 있다. 이러면 하드 코딩량이 줄어든다.


### 6.2.2 리소스 어셈블러 생성하기
각 리스트에 포함된 각 타코 리소스에 대한 링크를 추가해야하는데 루프에서 Resources 객체가 가지는 Resource<Taco> 요소에 Link를 구하는 방법은 꽤나 번거
로운 방법이다. 대신 Resources.wrap()에서 리스트의 각 타코를 Resource 객체로 생성사는 대신 TacoResource 객체로 변환하는 유틸리티를 주는 것이 낫다.

```java
public class TacoResource extends  ResourceSupport {
    private final String name;
    private final Datae createdAt;
    private final List<Ingredient>  ingredients;
    
    public TacoResource(Taco taco){
        this.name = taco.getName();
        this.createdAt = taco.getCreatedAt();
        this.ingredients = taco.getIngredeints();
    }
}
```

타코 리소스를 구성하는 리소스 어셈블러
```java
import org.springframewok.hateoas.mvc.ResourceAssemblerSupport;
import tacos.Taco;

public class TacoResourceAssembler extends ResourceAssemblerSupport<Taco, TacoResource> {
    public TacoResourceAssembler() {
        super(DesignTacoController.class, TacoResource.class);
        
        @Override
        protected TacoResource instantiateResource(Taco taco){
            return new TacoResource(taco);
        }
        
        @Override
        public TacoResource toResource(Taco taco){
            return createResourceWithId(taco.getId(), taco);
        }
        
    }
}
```
TacoResourceAssembler는 super 생성자를 호출하며, TacoResource를 생성하면서 만들어지는 링크를 포함하는 URL의 기본 경로를 결정하기 위해서 DesignTacoController
를 사용한다.
instantiateResource()는 Taco 객체로 TacoResource 인스턴스를 생성하도록 오버라이드됐다. TacoResource가 기본 생성자를
가지도 있다면 이 메소드는 생략할 수 있다. 
마지막으로 toResource()는 ResourceAssemblerSupport로부터 상속받을 때 반드시 오버라이드르 해야한다. 여기서는 Taco 객체로 TacoResorce 인스턴스를
생성하면서 Taco 객체의 id 속성 값으로 생성되는 self 링크가 URL에 자동 지정된다.

외견상으로 toResource가 instantiateResource와 같은 것으로 보이지만, instantiateResource는 Resource를 생성하고 toResource는 Resource
인스턴스를 생성하면서 링크도 추가한다. 내부적으로 toResource()는 instantiateResource를 호출한다. 


이렇게되면 변겯된 recentTacos()에서는 새로운 TacoResource 타입을 사용하여 ```Resources<Resource<Taco>>``` 대신 ```Resource<TacoResource>```
를 반환한다. 


/// 중략 ///

### 6.3 데이터 기반 서비스 활성화 하기
위에서 본 바와 같이 스프링 데이터는 우리가 코드에 정의한 인터페이스를 기반으로 레포지토리 구현체를 자동으로 생성하고 필요한 기능을 수행한다.
그러나 스프링 데이터에는 애플리케이션의 API를 정의하는 데 도움을 줄 수 있는 기능도 있다.

스프링 데이터 REST는 스프링 데이터의 또 다른 모듈이며, 스프링 데이터가 생성하는 레포지토리의 REST API를 자동으로 생성한다. 
따라서 스프링 데이터 REST를 우리 빌드에 추가하면 각 레포지토리의 인터페이스를 사용하는 API를 얻을 수 있다.


```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-rest</artifactId>
    </dependency>
```

이렇게 의존성을 지정하면 이미 스프링 데이터를 사용 중인 프로젝트에서 REST API를 노출시킬 수 있다. 스프링 데이터  REST 스타터가 우리 빌드에 포함되어 있으므로
스프링 데이터가 생성한 모든 레포지토리(ex. JPA)의 REST API가 자동으로 생성될 수 있도록 스프링 데이터 REST가 자동-구성되기 때문이다.

스프링 데이터 REST가 생성하는 REST 엔드포인트는 우리가 직접 생성한 것만큼 좋다. 그리고 이 엔드포인트를 사용하려면 지금까지 생성했던 @RestController 어노테이션이
지정된 모든 클래스를 제거해야한다.

추가로 스프링 데이터 REST가 자동 생성한 API와 관련해서 한 가지 할 일은 해당 API의 기본 경로를 설정하는 것이다. 해당 API의 엔드포인트가 우리가 작성한
모든 다른 컨트롤러와 충돌하지 않게 하기 위함이다. (실제로 일전에 설정해놓은 엔드포인트를 제거하지 않으면 스프링 데이터 REST가 생성한 것과 충돌을 일으킨다.)
스프링 데이터 REST가 자동 생성한 API의 기본 경로는 ```spring.data.rest.base-path```에 설정하면 된다.

```yaml
    spring:
      data:
        rest:
          base-path: '/api'
          
```

## 6.3.1 리소스 경로와 관계 이름 조정하기
실제로는 스프링 데이터 REST가 tacos라는 엔드포인트를 제공한다. 그러나 엔드포인트를 노출하는 방법이 문제이다. 즉, 스프링 데이터 레포지토리의
엔드포인트를 생성할 때 스프링 데이터 REST는 해당 엔드포인트와 관련된 엔티티 클래스 이름의 복수형을 사용한다. 

따라서 Ingredient는 /ingredients, Order는 /orders가, User는 /users가 된다. 또한 스프링 데이터 REST는 노출된 모든 엔드포인트 링크를 갖는 HOME
리스트도 노출한다. (전체 API list)

$ curl localhost:8080/api
```json lines
{
    "_links" : {
        "orders" : {
            "href" : "http://localhost:8080/api/orders"
        },
        "ingredients" : {
            "href" : "http://localhost:8080/api/ingredients" 
        },
        "tacoes" : {
            "href" : "http://localhost:8080/api/tacoes{?page,size,sort}",
            "templated" : true 
        },
        "users" : {
            "href" : "http://localhost:8080/api/users" 
        },
        "profile" : {
            "href" : "http://localhost:8080/api/profile" 
        } 
    }
}




```



여기서 복수형 관련 문제점을 해결하려면 Taco 클래스에 어노테이션을 설정해주면 된다.

```java
@Data
@Entity
@RestResource(rel="tacos", path="tacos")
public class Taco {
    ...
}
```
위와 같이 ```@RestResource``` 어노테이션을 설정하면 관계이름과 경로를 커스터마징할 수 있다.



## 6.3.2 페이징과 정렬
홈 리소스의 모든 링크는 선택적 매개변수인 page, size, sort를 제공한다. /api/tacos와 같은 컬렉션 리소스를 요청하면 기본적으로 
한 페이지당 20개의 항목이 반환된다. 그러나 page와 size 매개변수를 지정하면 요청에 포함될 페이지 번호와 페이지 크기를 조정할 수 있다.

예를 들어 페이지가 5인 첫 번째 페이지는 ```curl "localhost:8080/api/tacos?size=5``` 가 된다.
두 번쨰 페이지는 ```curl "localhost:8080/api/tacos?size=5&page=1```가 되는 식이다. page는 0부터 시작한다. 
또한 HATEOAS는 ```fisrt```, ```last```, ```next```, ```previous```페이지 링크를 요청 응답에 제공한다.

sort 매개변수는 엔티티의 속성을 기준으로 결과 리스트를 정렬할 수 있다. ```curl "localhost:8080/api/tacos?size=12&page=0&sort=createAt,desc```

그러나 문제가 하나 있다 앞의 매개변수들을 사용해서 타코 리스트를 요청하기 위한 UI가 하드코딩되어야 한다.따라서 클라이언트가 링크 리스트에서 URL을
찾을 수 있으면 좋을 것이다. 

## 6.3.3 커스텀 엔드포인트 추가하기
스프링 데이터 REST는 스프링 데이터 레포지토리의 CRUD 작업을 수행하는 엔드포인트 생성을 잘 하도록 한다. 그러나 때로는 기본적인 CRUD API로부터
탈피하여 커스터마이징할 필요가 있다.

이때 ```@RestController``` 어노테이션이 지정된 빈을 구현하여 스프링 REST가 자동 생성하는 엔드포인트에 보충할 수도 있다. 예를 들어, 이번 장 앞에
나왔던 DesignTacoController를 다시 사용하면서 이것이 스프링 데이터  REST가 제공하는 엔드포인트와 함께 동작하도록 할 수 있다.
그러나 이때는 두 가지 사안을 고려하여 API를 작성해야한다.

    1. 우리의 엔드포인트 컨트롤러는 스프링 REST의 기본 경로로 매핑되지 않는다. 따라서 이때는 스프링 데이터 REST의 기본 경로를 포함하여
    우리가 원하는 기본 경로가 앞에 붙도록 매핑시켜야한다. 그러나 기본 경로가 변경될 떄는 해당 컨트롤러의 매핑이 일치되도록 수정해야한다.

    2. 우리 컨트롤러에 정의한 엔드포인트는 스프링 데이터 REST 엔드포인트에서 반환되는 리소스의 하이퍼링크에 자동으로 포함되지 않는다. 이것은 
    클라이언트가 관계 이름을 사용해서 커스텀 엔드포인트를 찾을 수 없다는 것을 의미한다.


첫번 째로  기본 경로에 대한 문제를 해결하면 스프링 데이터 REST는 @RepositoryRestController를 포함한다. 이는 스프링 데이터 REST 엔드포인트에 구성되는
것과 동일한 기본 경로로 매핑되는 컨트롤러 클래스에 지정하는 새로운 어노테이션이다. 쉽게 설명하면 ```@RepositoryRestController```가 지정된 컨트롤러의 모든 경로 
매핑은 ```spring.data.rest.base-path``` 속성의 값이 앞에 붙은 경로이다.

```java
@RepositoryRestController
public class RecentTacoController {
    
    private TacoRepository tacoRepo;
    
    public RecentTacoController(TacoRepository tacoRepo){
        this.tacoRepo = tacoRepo;
    }
    
    @GetMapping(path="/tacos/recent", produces="application/hal+json")
    pubic ResponseEntity<Resource<TacoResource>> recentTaco () {
        ...
    }
    
}
```

여기서 ```@GetMapping```은 /taco/recent로 매핑되지만, RecentTacosController 클래스에 @RepositoryRestController 어노테이션이 지정되어 있으므로
맨 앞 스프링 데이터 REST의 기본 경로가 추가된다. recentTacos()는 /api/tacos/recent의 GET을 처리하게 된다.

! 여기서 중요한 점은 ```@RepositoryRestController```, ```@RestController```는 유사하지만 동일한 기능을 수행하지는 않는다.
```@RestController```는 핸들러 메소드의 반환값을 요청 응답의 몸체에 자동으로 수록하지 않는다. 따라서 해당 메소드에 @ResponseBody
를 선언하거나 해당 메소드에서 응답 데이터를 포함하는 ResponseEntity를 반환해야한다.

추가적으로 /api/tacos를 요청할 때 /api/tacos/recent는 노출되지 않는다. 이 문제를 해결해보자.

## 6.3.4 커스텀 하이퍼링크를 스프링 데이터 엔드포인트에 추가하기
/api/tacos에서 반환된 하이퍼링크 중 없다면 어떻게 해야할까? 리소스 프로세서 빈을 선언하면 스프링 데이터 REST가 자동으로 포함시키는 링크 리스트에
해당 링크를 추가할 수 있다. 스프링 데이터 HATEOAS는 ResourceProcessor를 제공한다. 이는 API를 통해 리소스가 반환됙 전에 리소스를 조작하는 인터페이스이다.

여기에서는 ```PagedResources<Resource<Taco>>``` 타입의 리소스에 recents 링크를 추가하는 ResourceProcessor를 구현해야한다. 

```java
@Bean
public ResourceProcessor<PagedResources<Resource<Taco>>> tacoProcessor(EntityLinks links){
    
    return new ResourceProcessor<PagedResources<Resource<Taco>>>() {
        @Override
        public PagedResources<Resource<Taco>> process (PagedResources<Resource<Taco>> resource) {
            
            resource.add( links.linkFor(Taco.class).slash("recent").withRel("recents") );
            
            return resource;
        }
    }
}
```

위 예시에서 ResourceProcessor는 익명 내부 클래스로 정의되었고 스프링 애플리케이션 컨텍스트에 생성되는 빈으로 선언되었다. 따라서 스프링 HATEOAS가
자동으로 이 빈을 찾을 후, 해당하는 리소스에 적용한다.




***
Summary

    1. REST 엔드포인트는 스프링 MVC, 그리고 브라우저 지향의 컨트롤러와 동일한 프로그래밍 모델을 따르는 컨트롤러로 생성할 수 있다.
    2. 모델과 뷰를 거치지 않고 요청 응답 몸체에 직접 데이터를 쓰기 위해 컨트롤러의 핸들러 메소드에는 @ResponseBody 어노테이션을 지정할 수 있으며, 
    ResponseEntity 객체를 반환할 수 있다.
    3. @RestController 어노테이션을 컨트롤러에 지정하면 해당 컨트롤러의 각 핸들러 메소드에 @ResponseBody를 지정하지 않아도 되므로 컨트롤러를 단순화 해준다.
    4. 스프링 HATEOAS는 스프링 MVC에서 반환되는 리소스의 하이퍼링크를 추가할 수 있게 한다.
    5. 스프링 데이터 레포지토리는 스프링 데이터 REST를 사용하는 REST API로 노출시킬 수 있다.