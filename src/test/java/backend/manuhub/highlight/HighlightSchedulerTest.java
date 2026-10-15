package backend.manuhub.highlight;

import backend.manuhub.common.util.SeasonProvider;
import backend.manuhub.exception.ApiServerException;
import backend.manuhub.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;


@SpringBootTest
@ActiveProfiles("test")
@DisplayName("HighlightScheduler 테스트")
public class HighlightSchedulerTest {

    @MockitoBean
    private HighlightService highlightService;

    @MockitoBean
    private SeasonProvider seasonProvider;

    @Autowired
    private HighlightScheduler highlightScheduler;

    @Test
    @DisplayName("updateHighlights()가 정상 실행된다")
    void updateHighlights() {
        given(seasonProvider.getCurrentSeason()).willReturn(2026);

        highlightScheduler.updateHighlights();

        then(highlightService).should(times(1)).syncHighlights(2026);
    }

    @Test
    @DisplayName("ApiServerException 발생 시 retry 후 총 2번 호출된다")
    void retryWhenApiServerException() {
        given(seasonProvider.getCurrentSeason()).willReturn(2026);
        willThrow(new ApiServerException(ErrorCode.YOUTUBE_SERVER_ERROR))
                .given(highlightService).syncHighlights(2026);

        highlightScheduler.updateHighlights();

        then(highlightService).should(times(2)).syncHighlights(2026);
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 recover가 호출된다")
    void recoverWhenUnexpectedException() {
        given(seasonProvider.getCurrentSeason()).willReturn(2026);
        willThrow(new RuntimeException("unexpected"))
                .given(highlightService).syncHighlights(2026);

        assertThatNoException().isThrownBy(() -> highlightScheduler.updateHighlights());
    }
}
