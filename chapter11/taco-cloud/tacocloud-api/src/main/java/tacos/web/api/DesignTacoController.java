//tag::recents[]
package tacos.web.api;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityLinks;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
//end::recents[]
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
//tag::recents[]
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.Taco;
import tacos.data.TacoRepository;

@RestController
@RequestMapping(path="/design",
                produces="application/json")
@CrossOrigin(origins="*")
public class DesignTacoController {
  private TacoRepository tacoRepo;
  
  @Autowired
  EntityLinks entityLinks;

  public DesignTacoController(TacoRepository tacoRepo) {
    this.tacoRepo = tacoRepo;
  }

  @GetMapping("/recent")
  public Flux<Taco> recentTacos() {                 //<3>
//    PageRequest page = PageRequest.of(
//            0, 12, Sort.by("createdAt").descending());
    return tacoRepo.findAll().take(12);
  }
  //end::recents[]

  //tag::postTaco[]
  @PostMapping(consumes="application/json")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Taco> postTaco(@RequestBody Mono<Taco> taco) {
    return tacoRepo.saveAll(taco).next();
  }
  //end::postTaco[]
  
  
  @GetMapping("/{id}")
  public Mono<Taco> tacoById(@PathVariable("id") Long id) {
    return tacoRepo.findById(id);
  }
  


}

