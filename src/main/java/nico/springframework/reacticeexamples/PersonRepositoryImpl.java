package nico.springframework.reacticeexamples;

import nico.springframework.reacticeexamples.domain.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PersonRepositoryImpl implements PersonRepository {

    Person michael = new Person(1, "Michael", "Weston");
    Person fionna = new Person(2, "Fiona", "Glenanne");
    Person sam = new Person(3, "Sam", "Axe");
    Person jesse = new Person(4, "Jesse", "Porter");

    @Override
    public Mono<Person> getById(Integer id) {
        //next() returns an empty mono if not found
        return findAll().filter(person -> person.getId().equals(id)).next();
    }

    @Override
    public Flux<Person> findAll() {
        return Flux.just(michael, fionna, sam, jesse);
    }
}
