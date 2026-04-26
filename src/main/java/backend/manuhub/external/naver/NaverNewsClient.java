package backend.manuhub.external.naver;

import backend.manuhub.exception.naver.NaverApiClientException;
import backend.manuhub.exception.naver.NaverApiServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

import static backend.manuhub.external.naver.NaverNewsResponse.NaverNewsItem;

@Component
@RequiredArgsConstructor
public class NaverNewsClient {

    @Value("${external.naver.client-id}")
    private String clientId;
    @Value("${external.naver.client-secret}")
    private String clientSecret;

    @Qualifier("naverRestClient")
    private final RestClient restClient;

    public List<NaverNewsItem> fetchNews(){

        return restClient.get()
                .uri("/v1/search/news.json?query=맨체스터유나이티드")
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new NaverApiClientException();})
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new NaverApiServerException();})
                .body(NaverNewsResponse.class)
                .getItems();
    }
}
