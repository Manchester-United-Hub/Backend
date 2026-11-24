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

    /** 전체 이벤트로 매번 다시 조합하여 득점 취소·정정과 재연결에도 동일한 현재 상태를 제공한다. */
    public MatchLineupResponse compose(MatchLineupResponse lineup) {
        if (lineup.lineupStatus() != LineupStatus.AVAILABLE) {
            return lineup;
        }

        var playerIds = Stream.of(lineup.home(), lineup.away())
                .flatMap(team -> Stream.concat(team.startPlayers().stream(), team.substitutes().stream()))
                .map(PlayerResponse::playerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 선수마다 조회하지 않도록 사진 정보는 한 번에 읽고, 누락된 사진은 외부 호출 없이 null로 둔다.
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
                    // 자책골의 득점 인정 팀이 선수 소속 팀과 달라도 실제 선수에게 기록을 붙인다.
                    Long teamId = type == GoalType.OWN_GOAL ? null : event.getTeamId();
                    records(records, teamId, event.getPlayerId()).goals.add(
                            new GoalResponse(event.getElapsedMinute(), event.getExtraMinute(), type));
                }
                // 실제 득점의 assist만 도움으로 집계하고, 교체 상대·실축·자책골은 도움으로 세지 않는다.
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
                // 교체 이벤트의 player는 OUT, assist는 IN으로 해석해 양쪽 선수에게 각각 기록한다.
                addSubstitution(records, event, event.getPlayerId(), SubstitutionDirection.OUT,
                        event.getAssistPlayerId(), event.getAssistPlayerName());
                addSubstitution(records, event, event.getAssistPlayerId(), SubstitutionDirection.IN,
                        event.getPlayerId(), event.getPlayerName());
            }
            case VAR -> { /* 판정 문구를 득점으로 세지 않고, 이후 수집된 전체 이벤트의 정정을 반영한다. */ }
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
        // 교체 후에도 최초 선발 배치와 후보 명단을 유지해 각자의 득점·카드가 다른 선수에게 옮겨가지 않게 한다.
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
            default -> null; // 페널티킥 실축 등 실제 득점이 아닌 이벤트에는 골 아이콘을 붙이지 않는다.
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
