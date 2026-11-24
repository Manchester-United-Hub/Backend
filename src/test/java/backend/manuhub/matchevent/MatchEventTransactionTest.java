package backend.manuhub.matchevent;

import backend.manuhub.external.matchevent.MatchEventClient;
import backend.manuhub.lineup.MatchLineupService;
import backend.manuhub.lineup.dto.MatchLineupResponse;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import backend.manuhub.matchevent.dto.MatchEventResponse;
import backend.manuhub.sse.MatchSseRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

// 실제 H2 트랜잭션으로 저장 성공과 전송 실패, 저장 롤백의 경계를 확인한다.
@SpringBootTest
@ActiveProfiles("test")
class MatchEventTransactionTest {

    private static final Long MATCH_ID = 1378985L;

    @Autowired private MatchRepository matchRepository;
    @Autowired private MatchEventUpdateService updateService;
    @Autowired private MatchEventService eventService;
    @MockitoSpyBean private MatchEventRepository eventRepository;
    @MockitoBean private MatchEventClient eventClient;
    @MockitoBean private MatchLineupService lineupService;
    @MockitoBean private MatchSseRegistry sseRegistry;

    @AfterEach
    void clear() {
        eventRepository.deleteAll();
        matchRepository.deleteAll();
    }

    @Test
    @DisplayName("이벤트가 없는 종료 경기의 수집 완료를 DB에 기록해 반복 요청을 막는다")
    void emptySuccessPersistsCompletionAndSkipsRepeatedApiCalls() {
        matchRepository.save(match());
        when(eventClient.fetchEvents(MATCH_ID)).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return List.of();
        });

        assertThat(eventService.getMatchEvents(MATCH_ID).events()).isEmpty();
        assertThat(eventService.getMatchEvents(MATCH_ID).events()).isEmpty();
        eventService.syncMatchEvents(MATCH_ID);

        assertThat(matchRepository.findById(MATCH_ID).orElseThrow().isEventSyncCompleted()).isTrue();
        verify(eventClient, times(1)).fetchEvents(MATCH_ID);
    }

    @Test
    @DisplayName("저장 실패 시 기존 이벤트와 완료 상태를 롤백하고 SSE를 전송하지 않는다")
    void persistenceFailureRollsBackEventsAndCompletionWithoutBroadcast() {
        Match match = matchRepository.save(match());
        eventRepository.save(MatchEvent.create(match, 39, null, 33L, "Manchester United",
                747L, "Casemiro", null, null, MatchEventType.CARD, "Yellow Card", null));
        doThrow(new IllegalStateException("DB failure")).when(eventRepository).saveAll(any());

        assertThatThrownBy(() -> updateService.updateMatchEvents(match, List.of(goal())))
                .isInstanceOf(IllegalStateException.class).hasMessage("DB failure");

        assertThat(matchRepository.findById(MATCH_ID).orElseThrow().isEventSyncCompleted()).isFalse();
        assertThat(eventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(MATCH_ID))
                .singleElement().extracting(MatchEvent::getType).isEqualTo(MatchEventType.CARD);
        verifyNoInteractions(sseRegistry, lineupService);
    }

    @Test
    @DisplayName("커밋 후 SSE 전송이 실패해도 저장한 이벤트와 수집 완료 상태는 유지한다")
    void failedBroadcastDoesNotRollBackCommittedData() throws IOException {
        Match match = matchRepository.save(match());
        SseEmitter disconnected = mock(SseEmitter.class);
        when(sseRegistry.getEmitters(MATCH_ID)).thenReturn(List.of(disconnected));
        when(lineupService.getLineup(MATCH_ID)).thenAnswer(invocation -> {
            // 전송용 조회가 실제 커밋 이후에 실행되는지 확인한다.
            assertThat(matchRepository.findById(MATCH_ID).orElseThrow().isEventSyncCompleted()).isTrue();
            assertThat(eventRepository.findAll()).hasSize(1);
            return new MatchLineupResponse(MATCH_ID, MatchLineupResponse.LineupStatus.NOT_AVAILABLE, null, null);
        });
        doThrow(new IOException("Disconnected")).when(disconnected).send(any(SseEmitter.SseEventBuilder.class));

        assertThat(updateService.updateMatchEvents(match, List.of(goal()))).isTrue();

        assertThat(eventRepository.findAll()).hasSize(1);
        assertThat(matchRepository.findById(MATCH_ID).orElseThrow().isEventSyncCompleted()).isTrue();
        verify(disconnected).send(any(SseEmitter.SseEventBuilder.class));
        verify(sseRegistry).unregister(MATCH_ID, disconnected);
    }

    private Match match() {
        return Match.create(MATCH_ID, LocalDateTime.of(2025, 8, 24, 23, 0), "FT",
                "Regular Season - 2", "Craven Cottage", "London",
                36L, "Fulham", null, false, 33L, "Manchester United", null, false, 1, 1);
    }

    private MatchEventResponse goal() {
        return MatchEventResponse.create(73, null, 36L, "Fulham", 1161L, "E. Smith Rowe",
                1455L, "A. Iwobi", MatchEventType.GOAL, "Normal Goal", null);
    }
}
