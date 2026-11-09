package backend.manuhub.matchevent;

import backend.manuhub.match.Match;
import backend.manuhub.matchevent.dto.MatchEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MatchEventUpdateServiceTest {

    private static final Long MATCH_ID = 1557450L;

    @Mock private MatchEventRepository repository;
    @InjectMocks private MatchEventUpdateService updateService;

    @Captor private ArgumentCaptor<List<MatchEvent>> entitiesCaptor;

    private Match match;

    @BeforeEach
    void setUp() {
        match = mock(Match.class);
        given(match.getMatchId()).willReturn(MATCH_ID);
    }

    @Test
    @DisplayName("저장된 이벤트와 같으면 삭제·저장하지 않는다")
    void updateMatchEvents_sameEvents_noChange() {
        given(repository.findAllByMatch_MatchIdOrderByIdAsc(MATCH_ID))
                .willReturn(List.of(entity(34, MatchEventType.GOAL)));

        updateService.updateMatchEvents(match, List.of(response(34, MatchEventType.GOAL)));

        verify(repository, never()).deleteAllByMatchId(any());
        verify(repository, never()).saveAll(any());
    }

    @Test
    @DisplayName("저장된 이벤트가 없으면 새로 저장한다")
    void updateMatchEvents_noStoredEvents_saves() {
        given(repository.findAllByMatch_MatchIdOrderByIdAsc(MATCH_ID)).willReturn(List.of());

        updateService.updateMatchEvents(match, List.of(response(34, MatchEventType.GOAL)));

        verify(repository).saveAll(entitiesCaptor.capture());
        assertThat(entitiesCaptor.getValue()).hasSize(1);
        assertThat(entitiesCaptor.getValue().get(0).getElapsedMinute()).isEqualTo(34);
    }

    @Test
    @DisplayName("저장된 이벤트와 다르면 전부 지운 뒤 새 이벤트로 교체한다")
    void updateMatchEvents_differentEvents_replaces() {
        given(repository.findAllByMatch_MatchIdOrderByIdAsc(MATCH_ID))
                .willReturn(List.of(entity(34, MatchEventType.GOAL)));

        updateService.updateMatchEvents(match, List.of(
                response(34, MatchEventType.GOAL), response(50, MatchEventType.CARD)));

        InOrder inOrder = inOrder(repository);
        inOrder.verify(repository).deleteAllByMatchId(MATCH_ID);
        inOrder.verify(repository).saveAll(entitiesCaptor.capture());
        assertThat(entitiesCaptor.getValue()).hasSize(2);
        assertThat(entitiesCaptor.getValue()).extracting(MatchEvent::getType)
                .containsExactly(MatchEventType.GOAL, MatchEventType.CARD);
    }

    @Test
    @DisplayName("저장되는 엔티티에 경기와 이벤트 필드가 그대로 담긴다")
    void updateMatchEvents_mapsFields() {
        given(repository.findAllByMatch_MatchIdOrderByIdAsc(MATCH_ID)).willReturn(List.of());

        updateService.updateMatchEvents(match, List.of(response(90, MatchEventType.CARD)));

        verify(repository).saveAll(entitiesCaptor.capture());
        MatchEvent saved = entitiesCaptor.getValue().get(0);
        assertThat(saved.getMatch()).isSameAs(match);
        assertThat(saved.getTeamId()).isEqualTo(33L);
        assertThat(saved.getPlayerName()).isEqualTo("Matheus Cunha");
        assertThat(saved.getDetail()).isEqualTo("detail");
    }

    private MatchEventResponse response(int elapsed, MatchEventType type) {
        return MatchEventResponse.create(elapsed, null, 33L, "Manchester United",
                1165L, "Matheus Cunha", null, null, type, "detail", null);
    }

    private MatchEvent entity(int elapsed, MatchEventType type) {
        return MatchEvent.create(match, elapsed, null, 33L, "Manchester United",
                1165L, "Matheus Cunha", null, null, type, "detail", null);
    }
}
