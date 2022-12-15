### 6.2 하이퍼미디아 사용하기

API 클라이언트 코드에서는 흔히 하드코딩된 URL 패턴을 사용하고 문자열로 처리한다. 그러나 API의 URL 스킴이 변경되면 어떻게 될까?
하드 코딩된 클라이언트 코드는 API를 잘못 인식하여 정상적으로 실행되지 않을 것이다. 따라서 API URL을 하드코딩하고 문자열로 처리하면
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