package biz.lci.learningspringboot.lsbquickstart;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChapterController {
    private final ChapterRepository repo;

    public ChapterController(ChapterRepository r) {
        repo = r;
    }

    @GetMapping("/chapters")
    public Flux<Chapter> listing() {
        return repo.findAll();
    }
}
