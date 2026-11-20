package backend.manuhub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    // 모든 RestClient가 같은 장애 기준을 사용하도록 공통 연결·응답 타임아웃을 적용한다.
    @Value("${external.http.connect-timeout:5s}")
    private Duration connectTimeout;

    @Value("${external.http.read-timeout:30s}")
    private Duration readTimeout;

    @Bean
    public RestClient naverRestClient(@Value("${external.naver.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory())
                .build();
    }

    @Bean
    public RestClient apiFootballRestClient(@Value("${external.api-football.api-base-uri}") String apiBaseUri,
                                            @Value("${external.api-football.api-key}") String apiKey) {

        return RestClient.builder()
                .baseUrl(apiBaseUri)
                .defaultHeader("x-apisports-key", apiKey)
                .requestFactory(requestFactory())
                .build();
    }

    @Bean
    public RestClient youtubeRestClient(@Value("${external.youtube.base-uri}") String baseUri) {
        return RestClient.builder()
                .baseUrl(baseUri)
                .requestFactory(requestFactory())
                .build();
    }

    @Bean
    public RestClient defaultRestClient() {
        return RestClient.builder()
                .requestFactory(requestFactory())
                .build();
    }

    private SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return requestFactory;
    }
}
