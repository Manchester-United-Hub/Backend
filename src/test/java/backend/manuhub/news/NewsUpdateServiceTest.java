package backend.manuhub.news;

import backend.manuhub.exception.ApiClientException;
import backend.manuhub.exception.ApiServerException;
import backend.manuhub.exception.ErrorCode;
import backend.manuhub.external.naver.NaverNewsClient;
import backend.manuhub.external.naver.NaverNewsResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@DisplayName("NewsUpdateService 테스트")
@ActiveProfiles("test")
@SpringBootTest
public class NewsUpdateServiceTest {

    @MockitoBean
    private NaverNewsClient naverNewsClient;

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsRepository newsRepository;

    @AfterEach
    void clear() {
        newsRepository.deleteAll();
    }

    private NaverNewsResponse.NaverNewsItem createNaverNewsItem(String title, String originalLink, String pubDate) {
        NaverNewsResponse.NaverNewsItem item = new NaverNewsResponse.NaverNewsItem(title, originalLink, "https://naver.com", "내용", pubDate);
        return item;
    }

    @Test
    @DisplayName("DB가 비어있을 때 전체 뉴스가 저장된다")
    void saveAllWhenDbEmpty() {
        given(naverNewsClient.fetchNews()).willReturn(List.of(
                createNaverNewsItem("뉴스1", "https://news1.com", "Wed, 29 Apr 2026 15:00:00 +0900"),
                createNaverNewsItem("뉴스2", "https://news2.com", "Wed, 29 Apr 2026 14:00:00 +0900")
        ));

        newsService.syncRecentNews();

        assertThat(newsRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("lastPublishedAt 이전 뉴스는 저장되지 않는다")
    void filterOldNews() {
        newsRepository.save(News.create("기존뉴스", "https://old.com", "https://naver.com", "내용",
                LocalDateTime.of(2026, 4, 29, 15, 0)));

        given(naverNewsClient.fetchNews()).willReturn(List.of(
                createNaverNewsItem("신규뉴스", "https://new.com", "Wed, 29 Apr 2026 16:00:00 +0900"),
                createNaverNewsItem("오래된뉴스", "https://older.com", "Wed, 29 Apr 2026 14:00:00 +0900")
        ));

        newsService.syncRecentNews();

        assertThat(newsRepository.count()).isEqualTo(2); // 기존 1 + 신규 1
    }

    @Test
    @DisplayName("중복 뉴스는 저장되지 않는다")
    void skipDuplicateNews() {
        newsRepository.save(News.create("기존뉴스", "https://news1.com", "https://naver.com", "내용",
                LocalDateTime.of(2026, 4, 29, 15, 0)));

        given(naverNewsClient.fetchNews()).willReturn(List.of(
                createNaverNewsItem("기존뉴스", "https://news1.com", "Wed, 29 Apr 2026 15:00:00 +0900")
        ));

        newsService.syncRecentNews();

        assertThat(newsRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("유효하지 않은 뉴스는 저장되지 않는다")
    void skipInvalidNews() {
        given(naverNewsClient.fetchNews()).willReturn(List.of(
                createNaverNewsItem(null, "https://news1.com", "Wed, 29 Apr 2026 15:00:00 +0900")
        ));

        newsService.syncRecentNews();

        assertThat(newsRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("네이버 API 서버 오류 시 ApiServerException이 발생한다")
    void throwApiServerExceptionWhenServerError() {
        given(naverNewsClient.fetchNews()).willThrow(new ApiServerException(ErrorCode.NAVER_API_SERVER_ERROR));

        assertThatThrownBy(() -> newsService.syncRecentNews())
                .isInstanceOf(ApiServerException.class);
    }

    @Test
    @DisplayName("네이버 API 클라이언트 오류 시 ApiClientException이 발생한다")
    void throwApiClientExceptionWhenClientError() {
        given(naverNewsClient.fetchNews()).willThrow(new ApiClientException(ErrorCode.NAVER_API_CLIENT_ERROR));

        assertThatThrownBy(() -> newsService.syncRecentNews())
                .isInstanceOf(ApiClientException.class);
    }
}
