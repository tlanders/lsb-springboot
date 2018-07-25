package biz.lci.learningspringboot.lsbquickstart;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping
    public String home(@RequestParam(required = false, defaultValue = "") String name) {
        return "Hello " + name + "!";
    }
}
