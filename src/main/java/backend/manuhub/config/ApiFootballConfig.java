package backend.manuhub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class ApiFootballConfig {

    @Value("${external.api-football.api-key}")
    private String apiKey;

    @Value("${external.api-football.api-base-uri}")
    private String apiBaseUri;

    @Bean
    public RestClient apiFootballRestClient() {

        return RestClient.builder()
                .baseUrl(apiBaseUri)
                .defaultHeader("x-apisports-key", apiKey)
                .build();
    }
}
