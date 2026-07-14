package backend.manuhub.rank;

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
@DisplayName("RankScheduler 테스트")
public class RankSchedulerTest {

    @MockitoBean
    private RankService rankService;

    @Autowired
    private RankScheduler rankScheduler;

    @Test
    @DisplayName("updateRank()가 정상 실행된다")
    void updateRank() {
        rankScheduler.updateRank();

        then(rankService).should(times(1)).updateRank();
    }

    @Test
    @DisplayName("ApiServerException 발생 시 retry 후 총 2번 호출된다")
    void retryWhenApiServerException() {
        willThrow(new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR))
                .given(rankService).updateRank();

        rankScheduler.updateRank();

        then(rankService).should(times(2)).updateRank();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 recover가 호출된다")
    void recoverWhenUnexpectedException() {
        willThrow(new RuntimeException("unexpected"))
                .given(rankService).updateRank();

        assertThatNoException().isThrownBy(() -> rankScheduler.updateRank());
    }
}
