package backend.manuhub.matchevent;

import backend.manuhub.match.Match;
import backend.manuhub.lineup.MatchLineupRefreshEvent;
import backend.manuhub.matchevent.dto.MatchEventListGetResponse;
import backend.manuhub.matchevent.dto.MatchEventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MatchEventUpdateService {

    private final MatchEventRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public void updateMatchEvents(Match match, List<MatchEventResponse> events) {
        List<MatchEventResponse> existsMatchEvents = repository.findAllByMatch_MatchIdOrderByIdAsc(match.getMatchId()).stream()
                .map(this::toResponse)
                .toList();

        if (events.equals(existsMatchEvents)) return;

        repository.deleteAllByMatchId(match.getMatchId());
        repository.saveAll(events.stream()
                .map(r -> toEntity(match, r))
                .toList());
        // 이벤트가 정정되면 선수별 기록을 다시 조합해 커밋 후 구독자에게 보낸다.
        eventPublisher.publishEvent(new MatchLineupRefreshEvent(match.getMatchId()));
    }

    public MatchEventListGetResponse updateLiveMatchEvents(List<MatchEventResponse> events, Match match) {
        List<MatchEvent> matchEventList = events.stream()
                .map(e -> toEntity(match, e))
                .toList();

        repository.deleteAllByMatchId(match.getMatchId());
        List<MatchEvent> savedMatchEvents = repository.saveAll(matchEventList);

        // 진행 중 경기의 골·카드·교체 변경을 선수별 라인업 SSE에도 반영한다.
        eventPublisher.publishEvent(new MatchLineupRefreshEvent(match.getMatchId()));

        return MatchEventListGetResponse.of(match.getMatchId(), savedMatchEvents);
    }

    private MatchEvent toEntity(Match match, MatchEventResponse r) {
        return MatchEvent.create(
                match, r.elapsedMinute(), r.extraMinute(),
                r.teamId(), r.teamName(),
                r.playerId(), r.playerName(),
                r.assistPlayerId(), r.assistPlayerName(),
                r.type(), r.detail(), r.comments()
        );
    }
    
    private MatchEventResponse toResponse(MatchEvent e) {
        return MatchEventResponse.create(
                e.getElapsedMinute(), e.getExtraMinute(),
                e.getTeamId(), e.getTeamName(),
                e.getPlayerId(), e.getPlayerName(),
                e.getAssistPlayerId(), e.getAssistPlayerName(),
                e.getType(), e.getDetail(), e.getComments()
        );
    }
}
