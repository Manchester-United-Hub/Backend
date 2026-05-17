package backend.manuhub.news;

import backend.manuhub.news.dto.NewsRecentGetResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NewsRecentGetService 테스트")
@ActiveProfiles("test")
@SpringBootTest
public class NewsGetRecentServiceTest {

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsRepository newsRepository;

    @AfterEach
    void clear() {
        newsRepository.deleteAll();
    }

    @Test
    @DisplayName("최신 뉴스 5개가 반환된다")
    void getRecentNewsList() {
        for (int i = 1; i <= 5; i++) {
            newsRepository.save(News.create("뉴스" + i, "https://news" + i + ".com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, i, 0)));
        }

        List<NewsRecentGetResponse> result = newsService.getRecentNewsList();

        assertThat(result).hasSize(5);
    }

    @Test
    @DisplayName("뉴스가 5개 미만이면 있는 만큼만 반환된다")
    void getRecentNewsListLessThanFive() {
        newsRepository.save(News.create("뉴스1", "https://news1.com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, 15, 0)));
        newsRepository.save(News.create("뉴스2", "https://news2.com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, 14, 0)));

        List<NewsRecentGetResponse> result = newsService.getRecentNewsList();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("뉴스 데이터가 없으면 빈 리스트를 반환한다")
    void getRecentNewsListEmpty() {
        List<NewsRecentGetResponse> result = newsService.getRecentNewsList();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("최신순으로 정렬되어 반환된다")
    void getRecentNewsListOrderByPublishedAt() {
        newsRepository.save(News.create("오래된뉴스", "https://news1.com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, 10, 0)));
        newsRepository.save(News.create("최신뉴스", "https://news2.com", "https://naver.com", "내용", LocalDateTime.of(2026, 4, 29, 15, 0)));

        List<NewsRecentGetResponse> result = newsService.getRecentNewsList();

        assertThat(result.get(0).title()).isEqualTo("최신뉴스");
        assertThat(result.get(1).title()).isEqualTo("오래된뉴스");
    }
}
