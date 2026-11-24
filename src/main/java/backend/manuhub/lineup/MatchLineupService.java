package backend.manuhub.lineup;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.external.lineup.LineupApiResponse;
import backend.manuhub.external.lineup.LineupClient;
import backend.manuhub.lineup.dto.MatchLineupResponse;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchLineupService {

    private static final String HOME = "home";
    private static final String AWAY = "away";
    private final MatchRepository matchRepository;
    private final LineupClient lineupClient;
    private final MatchLineupRepository matchLineupRepository;
    private final LineupPlayerRepository lineupPlayerRepository;
    private final LineupReferenceSyncService lineupReferenceSyncService;
    private final LineupReferenceQueryService lineupReferenceQueryService;
    private final MatchLineupPersistenceService matchLineupPersistenceService;
    private final MatchLineupSyncStatusService matchLineupSyncStatusService;
    private final LineupEventComposer lineupEventComposer;

    /** 일반 조회는 저장된 라인업만 반환하며 외부 API 호출, 참조 동기화, DB 저장을 수행하지 않는다. */
    // 커밋 후 콜백에서도 이전 트랜잭션에 참여하지 않고, 확정된 라인업과 이벤트를 새 읽기 트랜잭션으로 조합한다.
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public MatchLineupResponse getLineup(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));

        List<MatchLineup> savedLineups = matchLineupRepository.findAllByMatchId(matchId);
        if (!hasHomeAndAway(savedLineups)) {
            return new MatchLineupResponse(
                    matchId,
                    MatchLineupResponse.LineupStatus.NOT_AVAILABLE,
                    null,
                    null
            );
        }

        List<Long> lineupIds = savedLineups.stream()
                .map(MatchLineup::getId)
                .toList();
        List<LineupPlayer> savedPlayers =
                lineupPlayerRepository.findAllByLineupIdInOrderByLineupIdAscDisplayOrderAsc(lineupIds);
        LineupReferenceData references = lineupReferenceQueryService.load(savedLineups);
        return lineupEventComposer.compose(
                MatchLineupMapper.toResponseFromEntities(match, savedLineups, savedPlayers, references));
    }

    /** 경기 전·종료 후 스케줄러가 공통 사용하며, DB 선확인 후 필요한 경우에만 외부 라인업을 동기화한다. */
    public boolean backfillLineup(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));

        List<MatchLineup> savedLineups = matchLineupRepository.findAllByMatchId(matchId);
        if (hasHomeAndAway(savedLineups)) {
            matchLineupSyncStatusService.markCompleted(matchId);
            return true;
        }

        List<LineupApiResponse.Response> lineups = lineupClient.getLineups(matchId);
        LineupApiResponse.Response home = findTeamLineup(lineups, match.getHomeTeamId());
        LineupApiResponse.Response away = findTeamLineup(lineups, match.getAwayTeamId());

        if (home == null || away == null) {
            return false;
        }

        // 양 팀 라인업이 모두 준비된 경우에만 팀·감독 이미지 동기화와 최종 저장을 수행한다.
        lineupReferenceSyncService.syncApiReferences(List.of(home, away));
        matchLineupPersistenceService.replaceLineups(matchId, home, away);
        return true;
    }

    private boolean hasHomeAndAway(List<MatchLineup> lineups) {
        boolean hasHome = lineups.stream().anyMatch(lineup -> HOME.equals(lineup.getTeamType()));
        boolean hasAway = lineups.stream().anyMatch(lineup -> AWAY.equals(lineup.getTeamType()));
        return hasHome && hasAway;
    }

    private LineupApiResponse.Response findTeamLineup(
            List<LineupApiResponse.Response> lineups,
            Long teamId
    ) {
        return lineups.stream()
                .filter(lineup -> lineup.team() != null && teamId.equals(lineup.team().id()))
                .findFirst()
                .orElse(null);
    }

}
