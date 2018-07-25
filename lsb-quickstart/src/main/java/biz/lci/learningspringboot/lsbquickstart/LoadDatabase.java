package biz.lci.learningspringboot.lsbquickstart;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration
public class LoadDatabase {
    @Bean
    CommandLineRunner init(ChapterRepository repo) {
        return args ->
            Flux.just(
                    new Chapter("Quick Start with Java"),
                    new Chapter("Reactive Web with Spring Boot"),
                    new Chapter("... and more!"))
                    .flatMap(repo::save)
                    .subscribe(System.out::println);
    }
}
