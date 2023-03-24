# 18. Junit

## 18.1 단위테스트란?
단위 테스트는 프로그램의 기본 단위인 모듈(Module)을 테스트하는 것이다. 구현 단계에서 각 모듈의 개발을 완료하고 명세서의 내용대로 정확히 구현되었는지를 테스트한다.

### 단위 테스트의 장점
1. 개발 초기에 문제를 발견할 수 있게 해준다.
2. 개발자가 나중에 코드를 리팩토링하거나 라이브러리 업데이트 등에서 기존 기능이 올바르게 작동하는지 확인할 수 있다.
3. 기능에 대한 불확실성을 감소시킬 수 있다.
4. 시스템에 대한 실제 문서를 제공한다.

### 단위 테스트를 하지 않는다면?
1. 개발 단계가 번거로워진다.
2. 개발자가 만들 기능을 보호할 수 없다.


 Junit는 자바 환경에서 손쉽게 단위 테스트를 할 수 있도록 도와준다.

### 메소드
#### Assertions
- `fail` : 무조건 실패
- `assertArrayEquals(a,b)` : 배열 A, B가 같은지 확인
- `assertIterableEquals(a, b)` : 두 Iterable이 같은지 
- `assertLinesMatch`: 두 stream이 같은지
- `assertThrow` : 에러가 발생하면 true
- `assertDoesNotThrow` : 에러가 발생하지 않으면 true
- `assertEquals(a,b)` : 객체 A와 B가 같은지 확인
- `assertEquals(a,b,c)` : 객체 A와 B 값이 일치함을 확인(a: 예상값, b: 결과값, c: 오차범위)
- `assertSame(a,b)`: 객체 A와 B가 같은 객체임을 확인
- `assertTrue(a)` : 조건 A가 참인지 확인
- `assertFalse(a)` : 조건 A가 거짓인지 확인
- `assertNull(a)` : 객체 A가 Null임을 확인
- `assertNotNull(a)` : 객체 A가 Null이 아님을 확인
- `assertTimeout` : 테스트가 지정한 시간보다 오래 걸리지 않으면 true, 지정한 시간보다 오래 걸려도 테스트 종료까지 대기
- `assertTimeoutPreemptively` : 테스트가 지정한 시간보다 오래 걸리지 않으면 true, 오래 걸리면 바로 종료
#### Assumption
- `assumeTrue` : 테스트 실패하면 에러 발생
- `assumeFalse` : 테스트 성공하면 에러 발생
- `assumingThat(boolean, excutable` : 첫 번째 인자가 true 이면 두 번쨰 인자로 들어온 함수 실행, 첫 번째 인자 값이 false인 경우에도 테스트를 스킵하지 않고 진행

### 기본 어노테이션
- `@Test` : 테스트를 만드는 모듈 역할
- `@SpringBootTest` : SpringBoot의 테스트 환경에서 의존성을 주입
- `@DisplayName` : 테스트 클래스 또는 테스트 메소드의 사용자 정의 표시 이름을 정의
- `@ExtendWith` : 사용자 정의 확장명을 등록하는데 사용
- `@BeforeEach` : 각 테스트 메소드 전에 실행됨
- `@AfterEach` : 각 테스트 메소드 후에 실행됨을 나타냄
- `@BeforeAll` : 현재 클래스의 모든 테스트 메소드 전에 실행됨을 나타냄
- `@AfterAll` : 현재 클래스의 모든 테스트 메소드 후에 실행됨을 나타냄
- `@Disable` : 테스트 클래스 또는 메소드를 비활성화

### ServiceLayer에서 의존성 주입이 실패하는 경우
@Value와 같이 주입이 실패하면 (SpringContext가 올라가지 않아서) `ReflectionTestUtils`를 사용해서 리플렉션으로 강제로 주입해주면 된다.

> ### 테스트코드 작성을 위한 FIRST 원칙
> - F : (Fast) : 테스트는 빠르게 수행되어야 한다.
> - I : (Independent) : 테스트는 독립적으로 수행되어야 한다.
> - R : (Repeatable) : 테스트는 반복적으로 수행해도 결과가 같아야 한다.
> - S : (Self-Validating) : 테스트는 자체적으로 검증이 가능해야 한다.
> - T : (Timely) : 테스트는 적시에 작성해야 한다.
> 

### Given - When - Then Pattern
Given - When - Then은 [준비 - 실행 - 검증]이다. 테스트 코드를 작성 시에 준비/ 실행/ 검증의 세 부분으로 나누기만 하면 된다.

Given은 테스트를 위해 준비하는 과정이다. 테스트에 사용하는 변수, 입력 값 등을 정의하거나, Mock 객체를 정의하는 구문도 포함된다. 

When은 실제로 액션을 하는 과정이다. 보통 When은 한 줄이면 끝난다.

Then은 테스트를 검증하는 과정이다.




### 어노테이션
- `@BeforeClass` : 해당 클래스의 테스트 진행 전 딱 한 번만 실행한다.
- `@Sql`: Sql 파일을 실행해서 테스트 준비를 할 수 있다. `@Sql({"classpath:tesetdb/example.sql"})`와 같이 사용할 수 있다.
- `@Transaction` 으로 테스트 시 저장하는 경우를 롤백할 수 있다.
- `@Rollback(value="true")`로 롤백 여부를 결정할 수 있다.
- `@TestMethodOrder()` : 클래스 선언부에 사용할 수 있는 어노테이션으로 테스트 순서를 정해준다. 
>   1. MethodName: 메소드 명 ( MethodOrderer.MethodName.class )
>   2. DisplayName: @DisplayName 순 ( MethodOrderer.DisplayName.class )
>   3. OrderAnnotation: @Order 순서 ( MethodOrderer.OrderAnnotation.class )
>   4. Random: 랜덤 ( MethodOrderer.Random.class )
>
>   만약 원하는 순서가 없다면 MethodOrder를 구현하면 된다. 
- `@Order` @Test가 지정된 메소드들은 순서 보장이 되지 않는다. 따라서 순서를 지정할 필요가 생길 수 있다. 
예를 들어서 저장 -> 수정 -> 삭제 순으로 테스트하는 경우라면 '순서'가 꽤나 중요하다. @Order(순서)로 메소드 실행 순서를 정해줄 수 있다.
> *** primaryKey로 삭제하는 경우 아무리 롤백을 해도 primaryKey는 리셋되지 않으므로 신경 쓸 필요가 있다.
- `@TestInstance` : jUnit은 기본적으로 @Test 단위로 인스턴스가 생성된다. 이렇게 되면 테스트 간 영향이 없어서 단위 테스트에 최적화된다. 그러나 메소드 단위가 아닌 클래스 단위로 테스트 해야할 
때가 생긴다. @TestInstance는 테스트의 생명 주기를 결정한다. 기본 값은 `PER_METHOD`로 메소드 단위이며, `PER_CLASS`로 클래스 단위로 확장시킬 수 있다.
- `@Tag` : 테스트 클래스, 메소드에 테스트 구분을 태깅하기 위해 사용한다. 테스트 실행 시 TagExpression에 인자로 전달하면 해당 태그를 갖고 있는 테스트만 실행할 수 있다.
- `@RepeatTest` : 조건에 따라 반복 테스트를 할 수 있게 해준다.
> - value: 반복 횟수( 반드시 0보다 커야만 한다. (필수))
> - name : 반복할 때 나타나는 테스트명이다. (default: "repetition" + 현재 반복 횟수 + "of" + 총 반복 횟수)
> 
> TestInfo 
> - getDisplayName : @DisplayName 값과 동일
> - getTags : @Tag 배열 값
> - getTestClass : 패키지 + 테스트 클래스 명
> - getTestMethod : 패키지명 + 테스트 클래스 명 + 테스트 메소드
> 
> RepetitionInfo
> - getCurrentRepetition : 현재 반복 횟수
> - getTotalRepetitions : 총 반복 횟수
> - DISPLAY_NAME_PLACEHOLDER : @DisplayName 값
> - SHORT_DISPLAY_NAME : "repetition" + 현재 반복 횟수 + "of" + 총 반복 횟수
> - LONG_DISPLAY_NAME : DISPLAY_NAME_PLACEHOLDER + "::" + SHORT_DISPLAY_NAME
> - TOTAL_REPETITIONS_PLACEHOLDER : 현재 반복 횟수
> - CURRENT_REPETITION_PLACEHOLDER : 총 반복 횟수

- `@ParameterizedTest` : 파라미터를 넣어서 테스트를 반복적으로 실행할 수 있게 해주는 어노테이션
> - name : @DisplayName 설정
> - DISPLAY_NAME_PLACEHOLDER : @DisplayName과 동일
> - INDEX_PLACEHOLDER : 현재 실행 인덱스
> - ARGUMENTS_PLACEHOLDER : 현재 실행된 파라미터 값
> - ARGUMENTS_WITH_NAMES_PLACEHOLDER : 현재 실행된 파라미터명 + "=" + 값
> - DEFAULT_DISPLAY_NAME : "["+INDEX_PLACEHOLDER+"]" + ARGUMENTS_WITH_PLACEHOLDER
>
> @ParameterizedTest는 단독으로 사용되지 않고 어떤 파라미터를 사용하는지에 대해서 추가 선언을 해줘야한다.
> 

-------------------------
### 1. @ValueSource
지원 타입은 short[], byte[], int[], long[], float[], double[], char[], boolean[], String[], Class<?>[]

> 
> 각 타입의 소문자에 +"s"를 붙이면 파라미터 명이다. (strings)
> 
> 사용은 @ValueSource(strings = {"안녕", "잘가"})와 같이 한다.

 ** 마지막이 ~Test로 끝나면 @Test를 붙이지 않아도 된다.
```java
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void testWithValueSource(int intArg) {
        assertTrue(intArg > 0 && intArg < 4);
    }
```
### 2. @NullSource
메소드 인자에 null을 값을 넣어준다. 메소드 인자가 1개일 때만 사용할 수 있다. 
```java
@ParameterizedTest
@NullSource
void test(Integer arg){
    assertTrue(arg == null);    
}
```

### 3. @EmptySource
메소드 인자에 빈 객체 값을 넣어준다. 메소드 인자가 1개일 때만 사용할 수 있다. 
```java
@ParameterizedTest
@EmptySource
void test(Map arg){
    assertTrue(arg.isEmpty());    
}
```

### 4. @NullAndEmptySource
@NullSource와 @EmptySource를 합쳐놓은 어노테이션이다.

### 5. @EnumSource
Enum에 정의된 상수들을 테스트하기 위한 어노테이션이다. 
> - value : Enum 클래스 / 기본값은 NullEnum.class
> - names : 검색 조건(문자열, 정규식) -> mode에서 사용된다.
> - mode : INCLUDE(default), EXCLUDE, MATCH_ANY(stream의 anyMatch), MATCH_ALL(stream의 matchAll)
 
### 6. @MethodSource 
factory 메소드가 리턴해주는 값을 가지고 반복하는 테스트 어노테이션
#### factory 메소드 조건
1. 반드시 static, 테스트 클래스에 @TestInstance(Lifecycle.PER_CLASS)가 있을 경우에는 필요 없음
2. 인자가 없어야 한다.
3. stream 타입으로 리턴해야함
value : factory 메소드 명

### 7. @CsvSource
CSV( Comma Seperated Value ) 형식의 데이터로 반복 테스트를 한다.
> - value: String[] :: csv 형식의 데이터
> - delimiter : char :: delimiter를 변경( delimiterString과 공존 불가)
> - delimiterString : String :: String으로 delimiter 지정
> - emptyValue : cvs 중 빈 값일 경우 대체되는 값
> - nullValues: cvs 중 null 값으로 대체되는 값

### 8. @CsvFileSource
.cvs 파일을 읽어서 테스트할 수 있게 해주는 어노테이션
@CsvSource와 파라미터 값이 거의 비슷하다.
> - resource: String[] :: .csv 파일 경로 (/resources/~)
> - files : String[] :: .csv파일 경로(/src/)
> - encoding : String :: 인코딩 값
> - lineSeparator: String :: 줄 바꿈 구분자.
> - delimiter : char :: delimiter 변경
> - delimiterString: String : delimiter를 String으로 변경
> - numLinesToSkip: int :: csv 파일 라인 스킵 수 (주로 csv 컬럼명이 첫째 줄에 있기 때문에 거의 1 사용)
> - emptyValue: String :: 빈 값인 경우 
> - nullValue: String :: null 인 경우

### 9. @ArgumentSrouce(value = "")
다른 어노테이션처럼 정해진 데이터 주입 방법 말고 커스텀하게 주입 데이터 값을 정할 수 있는 어노테이션
(top 클래스가 아니고 inner class라면 static이 있어야함)
---------------------

- `@EnabledOnOs, @DisabledOnOs` : OS 환경 별 테스트를 할 수 있게 해주는 어노테이션이다.  
> - value : String[] :: 테스트할 OS (LINUX, MAC, SOLARIS, WINDOWS, OTHER)
> - disabledReason : String :: disabled 이유
> 
> EnabledOnOs(OS.Window) + @Test = @TestOnWindows

- `@EnabledOnJre, @DisabledOnJre`
> - value : Jre[] :: JAVA_8....
> - disabledReason: String::disabled 이유

- `@EnabledOnJreRange`
> - min : JRE :: 테스트할 최소 버전
> - max : JRE :: 테스트할 최대 버전 
> - disabledReason : String::disabled 이유

- `@EnabledIfSystemProperty, @DisabledIfSystemProperty`
JVM의 System Property 별 테스트를 할 수 있게 해준다. 만약 2개 이상의 조건으로 테스트하고 싶다면 @EnalbedIfSystemProperties, @DisabledIfSystemProperties로 변경할 수도 있다.
> - named: String :: JVM의 System Property 명
> - matches: String :: 찾고자하는 이름, 정규식
> - disabledReason

- `@EnabledifEnvironmentVariable, @DisabledIfEnvironmentVariable`
환경 변수 별로 테스트 할 수 있게 해주는 어노테이션. 만약 2개 이상의 조건으로 테스트하고 싶으면 @EnabledifEnvironmentVariables, @DisabledIfEnvironmentVariables
> - named : String :: 환경 변수명
> - matches: String :: 찾고자 하는 이름, 정규식
> - disabledSReason : String :: disabled 이유
> 

- `@EnabledIf, @DisabledIf`
> - value: String :: 로직이 있는 메소드 명
> - disabledReason : String :: disabled 이유


## Mockito
Mock은 "모의"라는 의미로 테스트할 때 필요한 실제 객체와 동일한 모의 객체를 만들어 테스트 효용성을 높이기 위해서 사용한다. 
만약 DB에서 데이터를 읽어 객체를 리턴하는 메소드를 테스트한다고 하면 테스트 실행 때마다 DB를 읽어오는 것이 부하가 많이 걸리고, 시간도 걸린다.
따라서 DB에서 읽어오는 것이 아니라 mock을 만들어서 테이블 접근을 최소화 할 수 있다. 

### 그럼 Mockito란?
mock을 쉽게 만들고 mock의 행동을 정의하는 stubbing, 정상적으로 작동하는 지에 대한 verify 등 다양한 기능을 제공해주는 프레임 워크

### 어노테이션
- `@Mock` : @Mock으로 만든 mock 객체는 가짜 객체이며, 그 안에 메소드를 호출해서 사용하려면 반드시 stubbing을 해야한다. 만약, stubbing 없이 호출한다면 primitive type은 0, reference type은 null을 반환
- `@Spy` : @Spy로 만든 mock 객체는 진짜 객체이며, 메소드 실행 시 스터빙을 하지 않으면 기존 객체의 로직을 실행한 값을, stubbing을 한 경우에는 stubbing 값을 리턴
- `@InjectMock` : @InjectMock은 DI(DependencyInjection)를 @Mock, @Spy로 생성된 @Mock객체를 자동으로 주입해주는 어노테이션이다. @InjectMcoks를 사용한 mock 객체를 주입받을 수 있는 형태는 contructor, propertySetter, feildInjection 등이 있다.

### Stubbing 이란?
Stubbin은 Stub이라고 하며, 토막, 남은 부분이라는 뜻을 가지고 있다.
TestStub은 테스트 호출 중 TestStub은 테스트 중에 만들어진 호출에 대해 미리 준비된 답변을 제공하는 것이다. 
만들어진 mock 객체의 메소드 실행시 어떤 리턴 값을 리턴할 지 정의하는 것이라고 보면 된다.

### Mockito에서 Stubbing
mockito에서는 `when()`으로 스터빙을 지원한다. when에 스터빙할 메소드를 넣고 그 이후에 어떤 동작을 어떻게 제어할 지를 메소드 체이닝 형태로 작성하면 된다.
스터빙 방법은 OngoinStubbing, Stubber를 사용하는 방법 두 가지가 있다. 

#### OngoingStubbing 메소드
OngoingStubbing 메소드란 when 에 넣은 메소드의 리턴 값을 정의해주는 메소드이다. 
`when({stubbing target method}).{ongoinstubbing Method};`

|메소드 명| 설명 |
|:---:|:---:|
|thenReturn| 스터빙한 메소드 호출 후 어떤 객체를 리턴할 지 정의|
|thenThrow| 스터빙한 메소드 호출 후 어떤 Exception을 throw할 건지 정의|
|thenAnswer| 스터빙한 메소드 호출 후 어떤 작업을 할지 정의 <br/> 사용 권장하지 않음|
|thenCallRealMethod| 실제 메소드 호출 |


#### stubber 메소드
`{Stubber method}.when({stubbing class}).{stubbing method}`
| 메소드 | 설명 |
|:---:|:---:|
|doReturn|스터빙 메소드 호출 후 어떤 행동을 할 건지 정의|
|doThrow|스터빙 메소드 호출 후 어떤 Exception을 throw 할지 정의|
|doAnswer|서터빙 메소드 호출 후 어떤 작업을 할지 정의|
|doNothing|스터빙 메소드 호출후 어떤 행동도 하지 않도록 정의|
|doCallRealMethod|실제 메소드 호출 |

스터빙 메소드는 계속해서 체이닝이 가능한데 이는 assert 순서에 맞춰서 체이닝이 진행된다.



#### Stubbing 한 메소드를 검증하는 방법

##### 1. verify
verify를 통해서 스터빙한 메소드가 실행 됐는지, n 번 실행 됐는지, 실행이 초과되지 않았는지를 다양하게 검증해볼 수 있다. 
`verify(T mock, VerificationMode mode)`

|메소드 명 | 설명(테스트 내에서 ~) |
|:---:|:---:|
|times(n)| 몇 번 호출됐는지 검증|
|never| 한 번도 호출되지 않았는지 검증|
|alLeastOne| 최소 한 번은 호출됐는지검증|
|atLeast(n)|최소 n 번이 호출됐는지 검증|
|atMostOnce|최대 한 번 호출됐는지 검증|
|atMost(n)|최대 n 번 호출됐는지 검증|
|calls(n)|n 번 호출됐는지 검증(InOrder와 함꼐 사용해야한다.)|
|only|해당 검증 메소드만 실행됐는지 검증|
|timeout(long mills)|n ms 이상 걸리면 Fail, 검증 종료|
|after(long mills)|n ms 이상 걸리는지 확인 timeout이 지나도 종료 X|
|description|실패한 경우에 출력될 문구|

##### 2. 메소드 호출 순서 검증
메소드 호출 순서 검증을 위해서는 inOrder를 사용하면 된다.

```java
    @Mock
    UserService userService;

    @Test 
    void testInOrder(){
        
        userService.getUser();
        userService.getLoginErrNum();
        
        Inroder inorder = inOrder(userService);
        
        inOrder.verify(userService).getUser();
        inOrder.verify(userService).getLoginErrNum();
        
        
    }
```


[출처 : https://effortguy.tistory.com/118]
