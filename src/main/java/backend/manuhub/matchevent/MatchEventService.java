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
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchEventService {

    private final MatchEventUpdateService matchEventUpdateService;
    private final MatchEventRepository matchEventRepository;
    private final MatchEventClient matchEventClient;
    private final MatchRepository matchRepository;
    private static final Set<String> ACTIVE_STATUSES = Set.of(
            "1H", "HT", "2H", "ET", "BT", "P", "SUSP", "INT", "LIVE"
    );

    /** 외부 API 대기 중 DB 연결을 점유하지 않도록 수집과 짧은 저장 트랜잭션을 분리한다. */
    public void syncMatchEvents(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(ErrorCode.MATCH_NOT_FOUND_ERROR));
        if ((match.getStatus() == null || !ACTIVE_STATUSES.contains(match.getStatus())) && !match.isFinished()) {
            return;
        }
        // 종료 후 마지막 수집이 성공한 경기는 더 이상 외부 API를 호출하지 않는다.
        if (match.isFinished() && match.isEventSyncCompleted()) {
            return;
        }
        matchEventUpdateService.updateMatchEvents(match, fetchMatchEvents(matchId));
    }

    public MatchEventListGetResponse getMatchEvents(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(ErrorCode.MATCH_NOT_FOUND_ERROR));

        List<MatchEvent> events = matchEventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(matchId);
        // 이벤트가 원래 없는 종료 경기도 성공한 수집으로 기억해 상세 조회마다 재호출하지 않는다.
        if (events.isEmpty() && match.isFinished() && !match.isEventSyncCompleted()) {
            List<MatchEventResponse> newEvents = fetchMatchEvents(matchId);
            matchEventUpdateService.updateMatchEvents(match, newEvents);
            events = matchEventRepository.findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(matchId);
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
