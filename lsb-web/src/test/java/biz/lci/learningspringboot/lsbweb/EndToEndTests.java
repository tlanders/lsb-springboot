package biz.lci.learningspringboot.lsbweb;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.interactions.Actions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.chrome.ChromeDriverService.createDefaultService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EndToEndTests {
    static ChromeDriverService chromeDriverService;
    static ChromeDriver chromeDriver;

    @LocalServerPort
    int port;

    @BeforeClass
    public static void setUp() throws IOException {
        System.setProperty("webdriver.chrome.driver", "ext/chromedriver.exe");
        chromeDriverService = createDefaultService();
        chromeDriver = new ChromeDriver(chromeDriverService);

        Path testResults = Paths.get("build", "test-results");
        if(!Files.exists(testResults)) {
            Files.createDirectory(testResults);
        }
    }

    @AfterClass
    public static void tearDown() {
        chromeDriverService.stop();
    }

    @Test
    public void homePageShouldWork() throws IOException {
        chromeDriver.get("http://localhost:" + port);

        takeScreenshot("homePageShouldWork-1");

        assertThat(chromeDriver.getTitle()).isEqualTo("Learning Spring Boot: Spring-a-Gram");

        String pageContent = chromeDriver.getPageSource();

        assertThat(pageContent).contains("<a href=\"/images/seed2.jpg/raw\">");

        WebElement webEl = chromeDriver.findElement(By.cssSelector("a[href*=\"seed1.jpg\"]"));
        Actions actions = new Actions(chromeDriver);
        actions.moveToElement(webEl).click().perform();

        takeScreenshot("homePageShouldWork-2");
        chromeDriver.navigate().back();
    }

    private void takeScreenshot(String name) throws IOException {
        FileCopyUtils.copy(chromeDriver.getScreenshotAs(OutputType.FILE),
                new File("build/test-results/TEST-" + name + ".png"));
    }
}
