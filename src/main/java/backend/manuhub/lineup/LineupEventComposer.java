package backend.manuhub.lineup;

import backend.manuhub.lineup.dto.MatchLineupResponse;
import backend.manuhub.lineup.dto.MatchLineupResponse.*;
import backend.manuhub.matchevent.MatchEvent;
import backend.manuhub.matchevent.MatchEventRepository;
import backend.manuhub.player.Player;
import backend.manuhub.player.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class LineupEventComposer {

    private final MatchEventRepository matchEventRepository;
    private final PlayerRepository playerRepository;

    // 저장된 전체 이벤트에서 매번 다시 조합해 이벤트 정정과 재조회에도 같은 결과를 반환한다.
    public MatchLineupResponse compose(MatchLineupResponse lineup) {
        if (lineup.lineupStatus() != LineupStatus.AVAILABLE) {
            return lineup;
        }

        var playerIds = Stream.of(lineup.home(), lineup.away())
                .flatMap(team -> Stream.concat(team.startPlayers().stream(), team.substitutes().stream()))
                .map(PlayerResponse::playerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 선수 사진은 한 번에 읽고, 없는 사진은 외부 호출 없이 null로 둔다.
        Map<Long, Player> profiles = playerRepository.findAllById(playerIds).stream()
                .collect(Collectors.toMap(Player::getPlayerId, player -> player));
        Map<PlayerKey, PlayerEvents> records = new HashMap<>();
        List<MatchEvent> events = matchEventRepository
                .findAllByMatch_MatchIdOrderByElapsedMinuteAscExtraMinuteAscIdAsc(lineup.matchId());
        events.stream().sorted(Comparator.comparing(MatchEvent::getElapsedMinute)
                        .thenComparing(event -> Objects.requireNonNullElse(event.getExtraMinute(), 0)))
                .forEach(event -> addEvent(records, event));

        return new MatchLineupResponse(lineup.matchId(), lineup.lineupStatus(),
                composeTeam(lineup.home(), profiles, records),
                composeTeam(lineup.away(), profiles, records));
    }

    private void addEvent(Map<PlayerKey, PlayerEvents> records, MatchEvent event) {
        if (event.getType() == null) {
            return;
        }
        switch (event.getType()) {
            case GOAL -> {
                GoalType type = goalType(event.getDetail());
                if (type != null && event.getPlayerId() != null) {
                    // 자책골은 득점 인정 팀과 실제 선수의 소속 팀이 다를 수 있다.
                    Long teamId = type == GoalType.OWN_GOAL ? null : event.getTeamId();
                    records(records, teamId, event.getPlayerId()).goals.add(
                            new GoalResponse(event.getElapsedMinute(), event.getExtraMinute(), type));
                }
                // 실축과 자책골의 도움 정보는 선수 도움 기록에 포함하지 않는다.
                if (type != null && type != GoalType.OWN_GOAL && event.getAssistPlayerId() != null) {
                    records(records, event.getTeamId(), event.getAssistPlayerId()).assists.add(
                            new AssistResponse(event.getElapsedMinute(), event.getExtraMinute(),
                                    event.getPlayerId(), event.getPlayerName()));
                }
            }
            case CARD -> {
                CardType type = cardType(event.getDetail());
                if (type != null && event.getPlayerId() != null) {
                    records(records, event.getTeamId(), event.getPlayerId()).cards.add(
                            new CardResponse(event.getElapsedMinute(), event.getExtraMinute(), type));
                }
            }
            case SUBSTITUTION -> {
                // 교체 이벤트의 player는 OUT, assist는 IN 선수로 해석한다.
                addSubstitution(records, event, event.getPlayerId(), SubstitutionDirection.OUT,
                        event.getAssistPlayerId(), event.getAssistPlayerName());
                addSubstitution(records, event, event.getAssistPlayerId(), SubstitutionDirection.IN,
                        event.getPlayerId(), event.getPlayerName());
            }
            case VAR -> { /* 판정 자체는 득점으로 세지 않는다. */ }
        }
    }

    private void addSubstitution(Map<PlayerKey, PlayerEvents> records, MatchEvent event,
                                 Long playerId, SubstitutionDirection direction,
                                 Long counterpartId, String counterpartName) {
        if (playerId != null) {
            records(records, event.getTeamId(), playerId).substitutions.add(new SubstitutionResponse(
                    direction, event.getElapsedMinute(), event.getExtraMinute(), counterpartId, counterpartName));
        }
    }

    private PlayerEvents records(Map<PlayerKey, PlayerEvents> records, Long teamId, Long playerId) {
        return records.computeIfAbsent(new PlayerKey(teamId, playerId), ignored -> new PlayerEvents());
    }

    private TeamLineupResponse composeTeam(TeamLineupResponse team, Map<Long, Player> profiles,
                                          Map<PlayerKey, PlayerEvents> records) {
        // 교체 후에도 최초 선발과 후보 위치를 유지한다.
        return new TeamLineupResponse(team.teamId(), team.name(), team.logo(), team.formation(), team.coach(),
                composePlayers(team.teamId(), team.startPlayers(), profiles, records),
                composePlayers(team.teamId(), team.substitutes(), profiles, records));
    }

    private List<PlayerResponse> composePlayers(Long teamId, List<PlayerResponse> players,
                                               Map<Long, Player> profiles, Map<PlayerKey, PlayerEvents> records) {
        return players.stream().map(player -> {
            Player profile = profiles.get(player.playerId());
            PlayerEvents events = records.getOrDefault(new PlayerKey(teamId, player.playerId()), new PlayerEvents());
            PlayerEvents ownGoals = records.get(new PlayerKey(null, player.playerId()));
            List<GoalResponse> goals = new ArrayList<>(events.goals);
            if (ownGoals != null) {
                goals.addAll(ownGoals.goals);
            }
            goals.sort(Comparator.comparing(GoalResponse::elapsedMinute)
                    .thenComparing(goal -> Objects.requireNonNullElse(goal.extraMinute(), 0)));
            return new PlayerResponse(player.playerId(), player.name(), player.number(), player.position(),
                    player.grid(), profile == null ? null : profile.getPhoto(),
                    List.copyOf(goals), List.copyOf(events.assists),
                    List.copyOf(events.cards), List.copyOf(events.substitutions));
        }).toList();
    }

    private GoalType goalType(String detail) {
        if (detail == null) return null;
        return switch (detail) {
            case "Normal Goal" -> GoalType.NORMAL;
            case "Penalty" -> GoalType.PENALTY;
            case "Own Goal" -> GoalType.OWN_GOAL;
            default -> null; // 실제 득점이 아닌 이벤트는 제외한다.
        };
    }

    private CardType cardType(String detail) {
        if (detail == null) return null;
        return switch (detail) {
            case "Yellow Card" -> CardType.YELLOW;
            case "Red Card", "Red card" -> CardType.RED;
            case "Second Yellow card", "Second Yellow Card", "Yellow-Red Card" -> CardType.SECOND_YELLOW;
            default -> null;
        };
    }

    private record PlayerKey(Long teamId, Long playerId) {}

    private static class PlayerEvents {
        private final List<GoalResponse> goals = new ArrayList<>();
        private final List<AssistResponse> assists = new ArrayList<>();
        private final List<CardResponse> cards = new ArrayList<>();
        private final List<SubstitutionResponse> substitutions = new ArrayList<>();
    }
}
