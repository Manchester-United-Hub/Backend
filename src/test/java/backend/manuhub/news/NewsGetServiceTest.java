package backend.manuhub.news;

import backend.manuhub.exception.InvalidRequestException;
import backend.manuhub.news.dto.NewsListGetResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NewsGetService 테스트")
@ActiveProfiles("test")
@SpringBootTest
public class NewsGetServiceTest {

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsRepository newsRepository;

    @AfterEach
    void clear() {
        newsRepository.deleteAll();
    }

    @Test
    @DisplayName("첫 요청 시 최신 뉴스를 반환한다")
    void getNewsListFirstRequest() {
        newsRepository.save(News.create("뉴스1", "https://news1.com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, 15, 0)));
        newsRepository.save(News.create("뉴스2", "https://news2.com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, 14, 0)));

        NewsListGetResponse response = newsService.getNewsList(null, null, 10);

        assertThat(response.newsList()).hasSize(2);
    }

    @Test
    @DisplayName("커서 기반으로 다음 페이지를 조회한다")
    void getNewsListWithCursor() {
        newsRepository.save(News.create("뉴스1", "https://news1.com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, 15, 0)));
        newsRepository.save(News.create("뉴스2", "https://news2.com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, 14, 0)));
        newsRepository.save(News.create("뉴스3", "https://news3.com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, 13, 0)));

        NewsListGetResponse firstResponse = newsService.getNewsList(null, null, 2);
        NewsListGetResponse secondResponse = newsService.getNewsList(firstResponse.nextCursorAt(), firstResponse.nextCursorId(), 2);

        assertThat(secondResponse.newsList()).hasSize(1);
    }

    @Test
    @DisplayName("결과가 없으면 빈 리스트를 반환한다")
    void getNewsListEmpty() {
        NewsListGetResponse response = newsService.getNewsList(null, null, 10);

        assertThat(response.newsList()).isEmpty();
        assertThat(response.nextCursorAt()).isNull();
        assertThat(response.nextCursorId()).isNull();
    }

    @Test
    @DisplayName("size만큼 뉴스를 반환한다")
    void getNewsListWithSize() {
        newsRepository.save(News.create("뉴스1", "https://news1.com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, 15, 0)));
        newsRepository.save(News.create("뉴스2", "https://news2.com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, 14, 0)));
        newsRepository.save(News.create("뉴스3", "https://news3.com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, 13, 0)));

        NewsListGetResponse response = newsService.getNewsList(null, null, 2);

        assertThat(response.newsList()).hasSize(2);
    }

    @Test
    @DisplayName("cursorAt만 있으면 InvalidRequestException이 발생한다")
    void throwExceptionWhenOnlyCursorAt() {
        assertThatThrownBy(() -> newsService.getNewsList(LocalDateTime.of(2026, 4, 29, 15, 0), null, 10))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("cursorId만 있으면 InvalidRequestException이 발생한다")
    void throwExceptionWhenOnlyCursorId() {
        assertThatThrownBy(() -> newsService.getNewsList(null, 1L, 10))
                .isInstanceOf(InvalidRequestException.class);
    }
}
