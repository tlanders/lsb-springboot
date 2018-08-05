package biz.lci.learningspringboot.lsbweb;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataMongoTest
public class EmbeddedImageRepositoryTests {
    @Autowired
    private ImageRepository repo;

    @Autowired
    private MongoOperations mongoOps;

    @Before
    public void setUp() {
        mongoOps.dropCollection(Image.class);

        mongoOps.insert(new Image("1", "test1.jpg"));
        mongoOps.insert(new Image("2", "test2.jpg"));
        mongoOps.insert(new Image("3", "test3.jpg"));

        mongoOps.findAll(Image.class).forEach(image -> System.out.println(image.toString()));
    }

    @Test
    public void findAllShouldWork() {
        Flux<Image> images = repo.findAll();
        StepVerifier.create(images)
                .recordWith(ArrayList::new)
                .expectNextCount(3)
                .consumeRecordedWith(results -> {
                    assertThat(results).hasSize(3);
                    assertThat(results)
                            .extracting(Image::getName)
                            .contains("test1.jpg", "test2.jpg", "test3.jpg");
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void findByNameShouldWork() {
        Mono<Image> image = repo.findByName("test1.jpg");
        StepVerifier.create(image)
                .expectNextMatches(next -> {
                    assertThat(next.getName()).isEqualTo("test1.jpg");
                    assertThat(next.getId()).isEqualTo("1");
                    return true;
                });

        // TODO - how to test that it doesn't find an item?
//        Mono<Image> notAnImage = repo.findByName("notInDb");
//        StepVerifier.create(notAnImage)
//                .expectNextMatches(next -> {
//                    assertThat(next.getName()).isNotBlank();
//                    return true;
//                })
//                .expectComplete();
    }
}
