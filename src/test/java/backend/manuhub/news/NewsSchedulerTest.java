package backend.manuhub.news;

import backend.manuhub.exception.ApiServerException;
import backend.manuhub.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("NewsScheduler 테스트")
class NewsSchedulerTest {

    @MockitoBean
    private NewsService newsService;

    @Autowired
    private NewsScheduler newsScheduler;

    @Test
    @DisplayName("syncRecentNews()가 정상 실행된다")
    void syncNews() {
        newsScheduler.syncNews();

        then(newsService).should(times(1)).syncRecentNews();
    }

    @Test
    @DisplayName("ApiServerException 발생 시 retry 후 총 2번 호출된다")
    void retryWhenApiServerException() {
        willThrow(new ApiServerException(ErrorCode.NAVER_API_SERVER_ERROR))
                .given(newsService).syncRecentNews();

        newsScheduler.syncNews();

        then(newsService).should(times(2)).syncRecentNews();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 recover가 호출된다")
    void recoverWhenUnexpectedException() {
        willThrow(new RuntimeException("unexpected"))
                .given(newsService).syncRecentNews();

        assertThatNoException().isThrownBy(() -> newsScheduler.syncNews());
    }
}