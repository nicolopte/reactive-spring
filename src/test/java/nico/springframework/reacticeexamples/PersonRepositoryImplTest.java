package nico.springframework.reacticeexamples;

import nico.springframework.reacticeexamples.domain.Person;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class PersonRepositoryImplTest {

    PersonRepositoryImpl personRepository;

    @BeforeEach
    void setUp() {
        personRepository = new PersonRepositoryImpl();
    }

    @Test
    void getByIdBlock() {
        Mono<Person> personMono = personRepository.getById(1);

        Person person = personMono.block();

        System.out.println(person.toString());
    }

    @Test
    void getByIdSubscribe() {
        Mono<Person> personMono = personRepository.getById(4);
        //We expect to count 1
        StepVerifier.create(personMono).expectNextCount(1).verifyComplete();
        personMono.subscribe(person -> {
            System.out.println(person.toString());
        });
    }

    @Test
    void getByIdMap() {
        Mono<Person> personMono = personRepository.getById(1);

        personMono.map(person -> {
            System.out.println(person.toString()); //nothing here, we have to subscribe
            return person.getFirstName();
        }).subscribe(firstName -> { //When we subscribe its called backpressure
            System.out.println("FirstName : " + firstName);
        });

        System.out.println("-------------------------------");
        personMono.subscribe(person -> {
            System.out.println(person.getFirstName());
        });
    }

    @Test
    void fluxTestBlockTest() {
        /* A flux can return many values, i.e a list of persons */
        Flux<Person> personFlux = personRepository.findAll();

        Person person = personFlux.blockFirst(); //We should not use block to retrieve value

        System.out.println(person.toString());
    }

    @Test
    void testFluxSubscribe() {
        Flux<Person> personFlux = personRepository.findAll();

        StepVerifier.create(personFlux).expectNextCount(4).verifyComplete();
        /*Stream of events coming back from the flux*/
        personFlux.subscribe(person -> { //creates backpressure
            System.out.println(person.toString());
        });
    }

    @Test
    void fluxToListMono() {
        Flux<Person> personFlux = personRepository.findAll();

        /*Convert Flux to list*/
        Mono<List<Person>> personListMono = personFlux.collectList();

        /*Retrieves the list of persons and subscribe to it*/
        personListMono.subscribe(list -> {
            list.forEach(person -> {
                System.out.println(person.toString());
            });
        });
    }

    @Test
    void shouldContainFiona() {
        List<Person> personList = new ArrayList<>();
        Flux<Person> personFlux = personRepository.findAll();

        /*Convert Flux to list*/
        Mono<List<Person>> personListMono = personFlux.collectList();

        /*Retrieves the list of persons and subscribe to it*/
        personListMono.subscribe(list -> {
            list.forEach(person -> {
                personList.add(person);
                System.out.println(person.toString());
            });
        });

        assertFalse(personList.isEmpty());
        assertEquals(personList.get(1).getFirstName(), "Fiona");
    }

    @Test
    void testFindPersonById() {
        Flux<Person> personFlux = personRepository.findAll();

        final Integer id = 3;
        //Filters the desire object
        Mono<Person> personMono = personFlux.filter(person -> person.getId().equals(id)).next();
        StepVerifier.create(personMono).expectNextCount(1).verifyComplete();

        //Prints the object we found
        personMono.subscribe(person -> {
            System.out.println(person.toString());
        });
    }

    @Test
    void testFindPersonByIdNotFound() {
        Flux<Person> personFlux = personRepository.findAll();

        final Integer id = 8;
        //Filters the desire object
        Mono<Person> personMono = personFlux.filter(person -> person.getId().equals(id)).next();

        //Same result as the mono is empty. Tip! dont ned expectNextCount(0)
        StepVerifier.create(personMono).verifyComplete();
        StepVerifier.create(personMono).expectNextCount(0).verifyComplete();

        //Prints the object we found
        personMono.subscribe(person -> {
            System.out.println(person.toString());
        });
    }

    @Test
    void testFindPersonByIdNotFoundWithException() {
        Flux<Person> personFlux = personRepository.findAll();

        final Integer id = 8;
        //We expect at least one object to be found else throw an exception
        Mono<Person> personMono = personFlux.filter(person -> person.getId().equals(id)).single();

        //We handle the exception and return another person object
        personMono.doOnError(throwable -> {
            System.out.println("Person not found");
        }).onErrorReturn(new Person()).subscribe(person -> {
            System.out.println(person.toString());
        });
    }
}