package backend.manuhub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient naverRestClient(@Value("${external.naver.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public RestClient apiFootballRestClient(@Value("${external.api-football.api-base-uri}") String apiBaseUri,
                                            @Value("${external.api-football.api-key}") String apiKey) {

        return RestClient.builder()
                .baseUrl(apiBaseUri)
                .defaultHeader("x-apisports-key", apiKey)
                .build();
    }

    @Bean
    public RestClient defaultRestClient() {
        return RestClient.builder().build();
    }
}
