package backend.manuhub.sse;

import backend.manuhub.exception.MatchNotFoundException;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import backend.manuhub.lineup.MatchLineupService;
import backend.manuhub.lineup.MatchLineupRefreshEvent;
import backend.manuhub.lineup.dto.MatchLineupResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;
import java.util.List;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchSseServiceTest {

    @InjectMocks
    private MatchSseService matchSseService;

    @Mock
    private MatchSseRegistry matchSseRegistry;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchLineupService matchLineupService;

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
        given(matchLineupService.getLineup(1L)).willReturn(new MatchLineupResponse(
                1L, MatchLineupResponse.LineupStatus.NOT_AVAILABLE, null, null));

        Optional<SseEmitter> result = matchSseService.connect(1L);

        assertThat(result).isPresent();
        verify(matchSseRegistry).register(eq(1L), eq(result.get()));
        verify(matchLineupService).getLineup(1L);
    }

    @Test
    @DisplayName("구독자가 없으면 전송용 라인업을 조회하지 않는다")
    void skipsSnapshotQueryWithoutSubscribers() {
        matchSseService.onLineupRefresh(new MatchLineupRefreshEvent(1L));

        verifyNoInteractions(matchLineupService);
    }

    @Test
    @DisplayName("구독자 수와 관계없이 라인업은 한 번만 조회하고 전체 응답을 전송한다")
    void broadcastsSingleSnapshotToAllSubscribers() throws IOException {
        SseEmitter first = mock(SseEmitter.class);
        SseEmitter second = mock(SseEmitter.class);
        given(matchSseRegistry.getEmitters(1L)).willReturn(List.of(first, second));
        given(matchLineupService.getLineup(1L)).willReturn(new MatchLineupResponse(
                1L, MatchLineupResponse.LineupStatus.NOT_AVAILABLE, null, null));

        matchSseService.onLineupRefresh(new MatchLineupRefreshEvent(1L));

        verify(matchLineupService, times(1)).getLineup(1L);
        verify(first).send(any(SseEmitter.SseEventBuilder.class));
        verify(second).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("한 연결의 전송 실패는 정리하고 다음 구독자에게 계속 전송한다")
    void isolatesFailedSubscriber() throws IOException {
        SseEmitter failed = mock(SseEmitter.class);
        SseEmitter healthy = mock(SseEmitter.class);
        IOException failure = new IOException("Disconnected client");
        given(matchSseRegistry.getEmitters(1L)).willReturn(List.of(failed, healthy));
        given(matchLineupService.getLineup(1L)).willReturn(new MatchLineupResponse(
                1L, MatchLineupResponse.LineupStatus.NOT_AVAILABLE, null, null));
        doThrow(failure).when(failed).send(any(SseEmitter.SseEventBuilder.class));

        matchSseService.onLineupRefresh(new MatchLineupRefreshEvent(1L));

        verify(matchSseRegistry).unregister(1L, failed);
        verify(failed).completeWithError(failure);
        verify(healthy).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("heartbeat는 접속 중인 경기에만 전송하고 DB를 조회하지 않는다")
    void heartbeatSkipsDatabaseQueries() throws IOException {
        SseEmitter emitter = mock(SseEmitter.class);
        given(matchSseRegistry.getSubscribedMatchIds()).willReturn(List.of(1L));
        given(matchSseRegistry.getEmitters(1L)).willReturn(List.of(emitter));

        matchSseService.sendHeartbeats();

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
        verifyNoInteractions(matchRepository, matchLineupService);
    }
}
