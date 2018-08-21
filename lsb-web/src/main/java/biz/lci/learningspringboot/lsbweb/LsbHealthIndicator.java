package biz.lci.learningspringboot.lsbweb;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class LsbHealthIndicator implements HealthIndicator
{
    @Override
    public Health health() {
        try {
            URL url = new URL("http://greglturnquist.com/books/learning-spring-boot");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int statusCode = conn.getResponseCode();
            if(statusCode >= 200 && statusCode < 300) {
                return Health.up().build();
            } else {
                return Health.down().withDetail("HTTP status code", statusCode).build();
            }
        } catch(IOException ioe) {
            return Health.down(ioe).build();
        }
    }
}
