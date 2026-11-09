package backend.manuhub.matchevent;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.MatchNotFoundException;
import backend.manuhub.external.matchevent.MatchEventApiResponse;
import backend.manuhub.external.matchevent.MatchEventClient;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import backend.manuhub.matchevent.dto.MatchEventListGetResponse;
import backend.manuhub.matchevent.dto.MatchEventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchEventService {

    private final MatchEventUpdateService matchEventUpdateService;
    private final MatchEventRepository matchEventRepository;
    private final MatchEventClient matchEventClient;
    private final MatchRepository matchRepository;


    public MatchEventListGetResponse getMatchEvents(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(ErrorCode.MATCH_NOT_FOUND_ERROR));

        List<MatchEvent> events = matchEventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(matchId);
        if (events.isEmpty() ) { // + 종료된 경기인지 판별
            List<MatchEventResponse> newEvents = fetchMatchEvents(matchId);
            if (!newEvents.isEmpty()) {
                matchEventUpdateService.updateMatchEvents(match, newEvents);
                events = matchEventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(matchId);
            }
        }
        return MatchEventListGetResponse.of(matchId, events);
    }
    private List<MatchEventResponse> fetchMatchEvents(Long matchId) {
        return matchEventClient.fetchEvents(matchId).stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .toList();
    }
    private MatchEventResponse toResponse(MatchEventApiResponse.Event e) {
        MatchEventType type = toMatchEventType(e.type());
        if (type == null) {
            return null;
        }
        return MatchEventResponse.create(
                e.time().elapsed(), e.time().extra(),
                e.team().id(), e.team().name(),
                e.player() != null ? e.player().id() : null,
                e.player() != null ? e.player().name() : null,
                e.assist() != null ? e.assist().id() : null,
                e.assist() != null ? e.assist().name() : null,
                type, e.detail(), e.comments()
        );
    }

    private MatchEventType toMatchEventType(String type) {
        return switch (type) {
            case "Goal" -> MatchEventType.GOAL;
            case "Card" -> MatchEventType.CARD;
            case "subst" -> MatchEventType.SUBSTITUTION;
            case "Var" -> MatchEventType.VAR;
            default -> {
                log.warn(">>> MatchEventService --> unknown event type: {}", type);
                yield null;
            }
        };
    }
}
