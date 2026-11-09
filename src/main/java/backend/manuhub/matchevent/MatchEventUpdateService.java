package backend.manuhub.matchevent;

import backend.manuhub.match.Match;
import backend.manuhub.matchevent.dto.MatchEventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MatchEventUpdateService {

    private final MatchEventRepository repository;

    public void updateMatchEvents(Match match, List<MatchEventResponse> events) {
        List<MatchEventResponse> existsMatchEvents = repository.findAllByMatch_MatchIdOrderByIdAsc(match.getMatchId()).stream()
                .map(this::toResponse)
                .toList();

        if (events.equals(existsMatchEvents)) return;

        repository.deleteAllByMatchId(match.getMatchId());
        repository.saveAll(events.stream()
                .map(r -> toEntity(match, r))
                .toList());
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
