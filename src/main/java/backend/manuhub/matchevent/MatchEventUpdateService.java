package backend.manuhub.matchevent;

import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import backend.manuhub.lineup.MatchLineupRefreshEvent;
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
    private final MatchRepository matchRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 변경 여부를 반환하고 커밋 후 갱신을 요청해, 동일한 이벤트를 불필요하게 재전송하지 않는다.
    public boolean updateMatchEvents(Match match, List<MatchEventResponse> events) {
        List<MatchEventResponse> existsMatchEvents = repository.findAllByMatch_MatchIdOrderByIdAsc(match.getMatchId()).stream()
                .map(this::toResponse)
                .toList();

        // 이벤트가 없는 경기라도 마지막 수집 성공을 같은 트랜잭션에 기록해 반복 수집을 끝낸다.
        if (match.isFinished()) {
            matchRepository.findById(match.getMatchId()).orElseThrow().completeEventSync();
        }

        if (events.equals(existsMatchEvents)) return false;

        repository.deleteAllByMatchId(match.getMatchId());
        repository.saveAll(events.stream()
                .map(r -> toEntity(match, r))
                .toList());
        // 실제 전송은 AFTER_COMMIT 리스너가 처리하므로 롤백된 이벤트가 화면에 노출되지 않는다.
        eventPublisher.publishEvent(new MatchLineupRefreshEvent(match.getMatchId()));
        return true;
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
