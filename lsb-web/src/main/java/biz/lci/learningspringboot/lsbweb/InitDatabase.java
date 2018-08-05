package biz.lci.learningspringboot.lsbweb;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

@Component
public class InitDatabase {
    @Bean
    CommandLineRunner init(MongoOperations ops) {
        return args -> {
            ops.dropCollection(Image.class);
            ops.insert(new Image("1", "seed1.jpg"));
            ops.insert(new Image("2", "seed2.jpg"));
            ops.insert(new Image("3", "seed3.jpg"));

            ops.findAll(Image.class).forEach(image -> System.out.println(image.toString()));
        };
    }
}
