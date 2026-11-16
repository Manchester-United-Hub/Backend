package backend.manuhub.sse;

import backend.manuhub.exception.MatchNotFoundException;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class MatchSseServiceTest {

    @InjectMocks
    private MatchSseService matchSseService;

    @Mock
    private MatchSseRegistry matchSseRegistry;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private Match match;

    @Test
    @DisplayName("경기가 없으면 예외가 발생하고 등록하지 않는다")
    void connect_matchNotFound() {
        given(matchRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> matchSseService.connect(1L))
                .isInstanceOf(MatchNotFoundException.class);
        verifyNoInteractions(matchSseRegistry);
    }

    @Test
    @DisplayName("종료된 경기는 빈 Optional을 반환하고 등록하지 않는다")
    void connect_finishedMatch() {
        given(matchRepository.findById(1L)).willReturn(Optional.of(match));
        given(match.isFinished()).willReturn(true);

        Optional<SseEmitter> result = matchSseService.connect(1L);

        assertThat(result).isEmpty();
        verifyNoInteractions(matchSseRegistry);
    }

    @Test
    @DisplayName("종료되지 않은 경기는 emitter를 반환하고 레지스트리에 등록한다")
    void connect_success() {
        given(matchRepository.findById(1L)).willReturn(Optional.of(match));
        given(match.isFinished()).willReturn(false);

        Optional<SseEmitter> result = matchSseService.connect(1L);

        assertThat(result).isPresent();
        verify(matchSseRegistry).register(eq(1L), eq(result.get()));
    }
}
