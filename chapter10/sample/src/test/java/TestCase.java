import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TestCase {

    @Test
    public void createAFlux_just(){
        Flux<String> fruitFlux = Flux.just("Apple", "Orange", "Grape", "Banana", "Strawberry");
        fruitFlux.subscribe(System.out::println);
    }

    @Test
    public void useStepVerifier(){
        Flux<String> fruitFlux = Flux.just("Apple", "Orange", "Grape", "Banana", "Strawberry");
        StepVerifier.create(fruitFlux)
                .expectNext("Apple")
                .expectNext("Orange")
                .expectNext("Grape")
                .expectNext("Banana")
                .expectNext("Strawberry")
                .verifyComplete();
    }

    @Test
    public void createFlux_fromArray(){
        String[] fruits = new String[] {"Apple", "Orange", "Grape", "Banana", "Strawberry"};
        Flux<String>  fruitFlux = Flux.fromArray(fruits);
        StepVerifier.create(fruitFlux)
                .expectNext("Apple")
                .expectNext("Orange")
                .expectNext("Grape")
                .expectNext("Banana")
                .expectNext("Strawberry")
                .verifyComplete();
    }

    @Test
    public void createFlux_fromIterable(){
        List<String> fruitList = new ArrayList<>();
        fruitList.add("Apple");
        fruitList.add("Orange");
        fruitList.add("Grape");
        fruitList.add("Banana");
        fruitList.add("Strawberry");
        Flux<String> fruitFlux = Flux.fromIterable(fruitList);
        StepVerifier.create(fruitFlux)
                .expectNext("Apple")
                .expectNext("Orange")
                .expectNext("Grape")
                .expectNext("Banana")
                .expectNext("Strawberry")
                .verifyComplete();
    }

    @Test
    public void createFlux_fromStream(){
       Stream<String> fruitStream = Stream.of("Apple", "Orange", "Grape", "Banana", "Strawberry");
        Flux<String> fruitFlux = Flux.fromStream(fruitStream);
        StepVerifier.create(fruitFlux)
                .expectNext("Apple")
                .expectNext("Orange")
                .expectNext("Grape")
                .expectNext("Banana")
                .expectNext("Strawberry")
                .verifyComplete();
    }


    @Test
    public void createAFlux_Range(){
        Flux<Integer> intervalFlux = Flux.range(1, 5);
        StepVerifier.create(intervalFlux)
                    .expectNext(1)
                    .expectNext(2)
                    .expectNext(3)
                    .expectNext(4)
                    .expectNext(5)
                    .verifyComplete();
    }

    @Test
    public void createAFlux_interval(){
        Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1))
                                      .take(5);
        StepVerifier.create(intervalFlux)
                .expectNext(1L)
                .expectNext(2L)
                .expectNext(3L)
                .expectNext(4L)
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    public void mergeFluxes() {
        Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbosa").delayElements(Duration.ofMillis(500));
        Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples").delaySubscription(Duration.ofMillis(250)).delayElements(Duration.ofMillis(500));

        Flux<String> mergedFlux = characterFlux.mergeWith(foodFlux);
        StepVerifier.create(mergedFlux)
                .expectNext("Garfield")
                .expectNext("Lasagna")
                .expectNext("Kojak")
                .expectNext("Lollipops")
                .expectNext("Barbosa")
                .expectNext("Apples")
                .verifyComplete();
    }

    @Test
    public void zipFluxes() {
        Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbosa").delayElements(Duration.ofMillis(500));
        Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples").delaySubscription(Duration.ofMillis(250)).delayElements(Duration.ofMillis(500));

        Flux<Tuple2<String, String>> zippedFlux = Flux.zip(characterFlux, foodFlux);
        StepVerifier.create(zippedFlux)
                .expectNextMatches(p ->
                            p.getT1().equals("Garfield") &&
                            p.getT2().equals("Lasagna")
                        )
                .expectNextMatches(p ->
                        p.getT1().equals("Kojak") &&
                                p.getT2().equals("Lollipops")
                )
                .expectNextMatches(p ->
                        p.getT1().equals("Barbosa") &&
                                p.getT2().equals("Apples")
                )
                .verifyComplete();
    }

    @Test
    public void zipFluxesToObj() {
        Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbosa").delayElements(Duration.ofMillis(500));
        Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples").delaySubscription(Duration.ofMillis(250)).delayElements(Duration.ofMillis(500));

        Flux<Object> zippedFlux = Flux.zip(characterFlux, foodFlux, (s1, s2) -> String.format("%s eats %s", s1, s2));
        StepVerifier.create(zippedFlux)
                .expectNext("Garfield eats Lasagna")
                .expectNext("Kojak eats Lollipops")
                .expectNext("Barbosa eats Apples")
                .verifyComplete();
    }

    @Test
    public void firstFlux() {
        Flux<String> slowFlux = Flux.just("tortoise", "snail", "sloth").delaySubscription(Duration.ofMillis(100));
        Flux<String> fastFlux = Flux.just("hare", "cheetah", "squirrel");

        Flux<String> firstFlux = Flux.firstWithSignal(slowFlux, fastFlux);
        StepVerifier.create(firstFlux)
                .expectNext("hare")
                .expectNext("cheetah")
                .expectNext("squirrel")
                .verifyComplete();
    }

    @Test
    public void skipAFew(){
        Flux<String> skipFlux = Flux.just("one", "two", "skip a few", "ninety nine", "hundred").skip(3);

        StepVerifier.create(skipFlux)
                .expectNext( "ninety nine")
                .expectNext("hundred")
                .verifyComplete();
    }

    @Test
    public void skipAFewSeconds(){
        Flux<String> skipFlux = Flux.just("one", "two", "skip a few", "ninety nine", "hundred")
                .delayElements(Duration.ofSeconds(1))
                .skip(Duration.ofSeconds(4));

        StepVerifier.create(skipFlux)
                .expectNext( "ninety nine")
                .expectNext("hundred")
                .verifyComplete();
    }

    @Test
    public void take(){
        Flux<String> nationalParkFlux = Flux.just("YellowStone", "Yosemite", "Grand Canyon", "Zion", "Grand Teton").take(3);
        StepVerifier.create(nationalParkFlux).expectNext("YellowStone", "Yosemite", "Grand Canyon").verifyComplete();
    }

    @Test
    public void takeSeconds(){
        Flux<String> nationalParkFlux = Flux.just("YellowStone", "Yosemite", "Grand Canyon", "Zion", "Grand Teton")
                .delayElements(Duration.ofSeconds(1))
                .take(Duration.ofMillis(3500));
        StepVerifier.create(nationalParkFlux).expectNext("YellowStone", "Yosemite", "Grand Canyon").verifyComplete();
    }

    @Test
    public void filter(){
        Flux<String> nationalParkFlux = Flux.just("YellowStone", "Yosemite", "Grand Canyon", "Zion", "Grand Teton")
                        .filter(n -> !n.contains(" "));
        StepVerifier.create(nationalParkFlux).expectNext("YellowStone", "Yosemite", "Zion").verifyComplete();
    }

    @Test
    public void distinct(){
        Flux<String> animalFlux = Flux.just("dog", "cat", "bird", "dog", "bird", "anteater").distinct();
        StepVerifier.create(animalFlux)
                .expectNext("dog", "cat", "bird", "anteater")
                .verifyComplete();
    }

    @Test
    public void map(){
        Flux<List<String>> playerFlux = Flux.just("Michael Jordan", "Scottie Pippen", "Steven Kerr")
                .map(n -> Arrays.asList(n.split(" ")[0], n.split(" ")[1]));
        StepVerifier.create(playerFlux)
                .expectNext(Arrays.asList("Michael", "Jordan"))
                .expectNext(Arrays.asList("Scottie", "Pippen"))
                .expectNext(Arrays.asList("Steven", "Kerr"))
                .verifyComplete();
    }

    @Test
    public void flatMap() {
        Flux<List<String>> playerFlux = Flux.just("Michael Jordan", "Scottie Pippen", "Steven Kerr")
                .flatMap(a -> Mono.just(a)
                        .map(n -> Arrays.asList(n.split(" ")[0], n.split(" ")[1]))
                        .subscribeOn(Schedulers.parallel())
                );
        List<List<String>> playerList = Arrays.asList(
                Arrays.asList("Michael", "Jordan"),
                Arrays.asList("Scottie", "Pippen"),
                Arrays.asList("Steven", "Kerr")
        );

        StepVerifier.create(playerFlux)
                .expectNextMatches(p -> playerList.contains(p))
                .expectNextMatches(p -> playerList.contains(p))
                .expectNextMatches(p -> playerList.contains(p))
                .verifyComplete();
    }

    @Test
    public void buffer(){
        Flux<String> fruitFlux = Flux.just("apple", "orange", "banana", "kiwi", "strawberry");
        Flux<List<String>> bufferedFlux = fruitFlux.buffer(3);

        StepVerifier.create(bufferedFlux)
                .expectNext(Arrays.asList("apple", "orange", "banana"))
                .expectNext(Arrays.asList("kiwi", "strawberry"))
                .verifyComplete();
    }

    @Test
    public void buffer2(){
        Flux.just("apple", "orange", "banana", "kiwi", "strawberry")
            .buffer(3)
            .flatMap( x ->
                Flux.fromIterable(x).map(y -> y.toUpperCase())
                    .subscribeOn(Schedulers.parallel())
                    .log()
            ).subscribe();
    }

    @Test
    public void collectList() {
        Flux<String> fruitFlux = Flux.just("apple", "orange", "banana", "kiwi", "strawberry");
        Mono<List<String>> fruitListMono = fruitFlux.collectList();

        StepVerifier.create(fruitListMono).expectNext(Arrays.asList("apple", "orange", "banana", "kiwi", "strawberry")).verifyComplete();
    }

    @Test
    public void collectMap(){
        Flux<String> animalFlux = Flux.just("aardvark", "elephant", "koala", "eagle", "kangaroo");
        Mono<Map<Character, String>> animalMono = animalFlux.collectMap(a -> a.charAt(0));

        StepVerifier.create(animalMono).expectNextMatches(map ->
                    map.size() == 3
                    && map.get('a').equals("aardvark")
                    && map.get('e').equals("eagle")
                    && map.get('k').equals("kangaroo")
        ).verifyComplete();
    }
}
