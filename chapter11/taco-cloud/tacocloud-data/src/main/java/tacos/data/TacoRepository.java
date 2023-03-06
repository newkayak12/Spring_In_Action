package tacos.data;

import org.springframework.data.repository.PagingAndSortingRepository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import tacos.Taco;

/* extends PagingAndSortingRepository<Taco, Long>*/
public interface TacoRepository extends ReactiveCrudRepository<Taco, Long> {

}
