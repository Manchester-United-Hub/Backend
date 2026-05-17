package backend.manuhub.news;

import backend.manuhub.news.dto.NewsItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NewsItem 테스트")
public class NewsItemTest {

    @Test
    @DisplayName("HTML 태그가 제거된다")
    void cleanHtmlTags() {
        NewsItem item = NewsItem.create("제목", "url", "url", "<b>내용</b>", "Wed, 29 Apr 2026 14:56:00 +0900");

        assertThat(item.description()).isEqualTo("내용");
    }

    @Test
    @DisplayName("&quot; 가 \" 로 변환된다")
    void cleanQuot() {
        NewsItem item = NewsItem.create("&quot;제목&quot;", "url", "url", "내용",
                "Wed, 29 Apr 2026 14:56:00 +0900");

        assertThat(item.title()).isEqualTo("\"제목\"");
    }

    @Test
    @DisplayName("날짜 파싱이 정상 동작한다")
    void parsePubDate() {
        NewsItem item = NewsItem.create("제목", "url", "url", "내용", "Wed, 29 Apr 2026 14:56:00 +0900");

        assertThat(item.publishedAt()).isEqualTo(LocalDateTime.of(2026, 4, 29, 14, 56));
    }

    @Test
    @DisplayName("날짜 파싱 실패 시 isValid()가 false를 반환한다")
    void invalidDate() {
        NewsItem item = NewsItem.create("제목", "url", "url", "내용", "invalid-date");

        assertThat(item.isValid()).isFalse();
    }

    @Test
    @DisplayName("title이 null이면 isValid()가 false를 반환한다")
    void nullTitle() {
        NewsItem item = NewsItem.create(null, "url", "url", "내용", "Wed, 29 Apr 2026 14:56:00 +0900");

        assertThat(item.isValid()).isFalse();
    }

    @Test
    @DisplayName("모든 필드가 정상이면 isValid()가 true를 반환한다")
    void validItem() {
        NewsItem item = NewsItem.create("제목", "url", "url", "내용", "Wed, 29 Apr 2026 14:56:00 +0900");

        assertThat(item.isValid()).isTrue();
    }
}
