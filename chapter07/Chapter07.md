# REST 서비스 사용하기



    1. RestTemplate: 스프링 프로엠워크에서 제공하는 간단하고 동기화된 REST 클라이언트
    2. Traverson: 스프링 HATEOAS에서 제공하는 하이퍼링크를 인식하는 동기화 REST 클래이언트로 같은 이름의 자바스크립트 라이브러리에서 비롯됨
    3. WebClient: 스프링 5에서 소개된 반응형 비동기 REST 클라이언트


## RestTemplate

 |       메소드        |                           기능                           |
|:----------------:|:------------------------------------------------------:|
|      delete      |                      DELETE 요청 수행                      |
|     exchange     |           지정된 HTTP 메소드 실행, ResponseEntity 반환           |
|     execute      |  지정된 HTTP 메소드를 URL에 대해서 실행하며 ResponseBody와 연결되는 객체 반환  |
|   getForEntity   | GET 수행, ResponseBody에 연결되는 객체를 포함하는 ResponseEntity를 반환 |
|   getForObject   |               GET 수행 ResponseBody의 객체 반환               |
| headerForHeaders |       HTTP HEAD를 전송하며 지정된 리소스의 URL, HTTP 헤더를 반환        |
|  optionForAllow  |      HTTP OPTIONS 요청을 전송하며 지정된 URL의 ALLOW 메소드 반환       |
|  patchForObject  |            HTTP PATCH를 전송하며 ResponseBody 반환            |
|  postForEntity   |     POST를 실행하며 ResponseBody에 있는 ResponseEntity를 반환     |
| postForLocation  |         URL에 데이터를 POST 하며 새로 생성된 리소스의 URL을 반환          |
|  postForObject   |       URL에 데이터를 POST 하며 ResponseBody와 연결되는 객체 반환       |
|       put        |                리소스 데이터를 지정된 URL에 PUT한다.                |


위 메소드들은 아래의 세 가지 형태로 오버로딩 되어 있다. 
    1. 가변 인자 리스트에 지정된 URL 매개변수에 URL 문자열을 받는다.
    2. Map<String,String>에 지정된 URL 매개변수에 URL 문자열을 받는다.
    3. java.net.URI를 URL에 대한 인자로 받으며, 매개변수화된 URL은 지원하지 않는다. 



## Traverson
Traverson은 스프링 데이터 HATEOAS와 같이 제공되며, 스프링 애플리케이션에서 하이퍼 미디어 API를 사용할 수 있는 솔루션이다. 