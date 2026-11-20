package backend.manuhub.lineup;

import backend.manuhub.coach.Coach;
import backend.manuhub.coach.CoachRepository;
import backend.manuhub.team.Team;
import backend.manuhub.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LineupReferenceQueryService {

    private final TeamRepository teamRepository;
    private final CoachRepository coachRepository;

    /** 일반 라인업 조회에서는 외부 동기화 없이 저장된 팀과 감독 정보만 읽는다. */
    @Transactional(readOnly = true)
    public LineupReferenceData load(List<MatchLineup> lineups) {
        Set<Long> teamIds = lineups.stream()
                .map(MatchLineup::getTeamId)
                .collect(Collectors.toSet());
        Set<Long> coachIds = lineups.stream()
                .map(MatchLineup::getCoachId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Team> teams = teamRepository.findAllById(teamIds).stream()
                .collect(Collectors.toMap(Team::getId, Function.identity()));
        Map<Long, Coach> coaches = coachRepository.findAllById(coachIds).stream()
                .collect(Collectors.toMap(Coach::getId, Function.identity()));
        return new LineupReferenceData(teams, coaches);
    }
}
