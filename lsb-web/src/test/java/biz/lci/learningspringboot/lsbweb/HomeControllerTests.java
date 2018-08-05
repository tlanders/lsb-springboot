package biz.lci.learningspringboot.lsbweb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = HomeController.class)
@Import({ThymeleafAutoConfiguration.class})
public class HomeControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private ImageService imageService;

    @Test
    public void baseRouteShouldListAllImages() {
        Image img1 = new Image("1", "test1.jpg");
        Image img2 = new Image("2", "test2.jpg");

        // setting up mock service
        given(imageService.findAllImages())
                .willReturn(Flux.just(img1, img2));

        // calling controller
        EntityExchangeResult<String> result = webClient
                .get().uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).returnResult();

        // verifying that image service was called only once
        verify(imageService).findAllImages();
        verifyNoMoreInteractions(imageService);

        // verify results match the expected mock data
        assertThat(result.getResponseBody())
                .contains("<title>Learning Spring Boot")
                .contains("<a href=\"/images/test1.jpg/raw\"")
                .contains("<a href=\"/images/test2.jpg/raw\"");
    }

    @Test
    public void fetchingImageShouldWork() {
        given(imageService.findOneImage(any()))
                .willReturn(Mono.just(new ByteArrayResource("data".getBytes())));

        webClient.get().uri("/images/xxx.png/raw")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("data");

        // verify image was called only once with the right name
        verify(imageService).findOneImage("xxx.png");
        verifyNoMoreInteractions(imageService);
    }

    @Test
    public void fetchingNullImageShouldFail() throws IOException {
        Resource res = mock(Resource.class);
        given(res.getInputStream()).willThrow(new IOException("Bad file"));
        given(imageService.findOneImage(any())).willReturn(Mono.just(res));

        webClient.get().uri("/images/xxx.png/raw")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class);

        verify(imageService).findOneImage("xxx.png");
        verifyNoMoreInteractions(imageService);
    }

    @Test
    public void deleteShouldWork() {
        given(imageService.deleteImage(any())).willReturn(Mono.empty());

        webClient.delete().uri("/images/del.jpg")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, "/");

        verify(imageService).deleteImage("del.jpg");
        verifyNoMoreInteractions(imageService);
    }
}
