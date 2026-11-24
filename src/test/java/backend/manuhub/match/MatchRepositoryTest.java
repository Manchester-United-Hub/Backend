package backend.manuhub.match;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MatchRepositoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 17, 12, 0);
    private static final Set<String> ACTIVE_STATUSES = Set.of(
            "1H", "HT", "2H", "ET", "BT", "P", "SUSP", "INT", "LIVE"
    );

    @Autowired
    private MatchRepository matchRepository;

    @Test
    @DisplayName("NS 경기는 하루 이내만 갱신하고 진행 중 경기는 시간 하한선을 적용하지 않는다")
    void findsMatchesInRequestedUpdateWindow() {
        matchRepository.saveAll(List.of(
                match(1L, NOW.minusHours(2), "NS"),
                match(2L, NOW.minusDays(2), "NS"),
                match(3L, NOW.minusDays(2), "2H"),
                match(4L, NOW.plusHours(1), "NS")
        ));

        List<Match> result = matchRepository.findAllToUpdateBeforeAndDuringMatch(
                "NS",
                NOW.minusDays(1),
                NOW.plusMinutes(10),
                ACTIVE_STATUSES
        );

        assertThat(result)
                .extracting(Match::getMatchId)
                .containsExactly(3L, 1L);
    }

    @Test
    @DisplayName("라인업 백필은 다음 재시도 시각이 지난 pending 경기만 조회한다")
    void findsOnlyRetryableLineupSyncTargets() {
        Match ready = match(10L, NOW.minusDays(2), "FT");
        Match waiting = match(11L, NOW.minusDays(1), "FT");
        waiting.recordLineupSyncFailure(NOW, 3);
        Match failed = match(12L, NOW.minusHours(12), "FT");
        failed.recordLineupSyncFailure(NOW, 3);
        failed.recordLineupSyncFailure(NOW, 3);
        failed.recordLineupSyncFailure(NOW, 3);
        matchRepository.saveAll(List.of(ready, waiting, failed));

        List<Match> result = matchRepository.findLineupSyncTargets(
                "pending",
                3,
                NOW,
                Set.of("FT", "AET", "PEN"),
                PageRequest.of(0, 5)
        );

        assertThat(result)
                .extracting(Match::getMatchId)
                .containsExactly(10L);
    }

    @Test
    @DisplayName("경기 전 라인업 조회는 NS 경기의 45분, 30분, 15분 전 1분 구간만 선택한다")
    void findsPreMatchLineupSyncTargets() {
        matchRepository.saveAll(List.of(
                match(20L, NOW.plusMinutes(45).plusSeconds(30), "NS"),
                match(21L, NOW.plusMinutes(30), "NS"),
                match(22L, NOW.plusMinutes(15).plusSeconds(59), "NS"),
                match(23L, NOW.plusMinutes(46), "NS"),
                match(24L, NOW.plusMinutes(14).plusSeconds(59), "NS"),
                match(25L, NOW.plusMinutes(30), "1H")
        ));

        List<Match> result = matchRepository.findPreMatchLineupSyncTargets(
                "NS",
                NOW.plusMinutes(45), NOW.plusMinutes(46),
                NOW.plusMinutes(30), NOW.plusMinutes(31),
                NOW.plusMinutes(15), NOW.plusMinutes(16)
        );

        assertThat(result)
                .extracting(Match::getMatchId)
                .containsExactly(22L, 21L, 20L);
    }

    @Test
    @DisplayName("종료 이벤트 재수집은 최근 경기 중 수집 미완료인 경기만 선택한다")
    void findsOnlyRecentUncompletedEventSyncTargets() {
        Match pending = match(30L, NOW.minusHours(2), "FT");
        Match completed = match(31L, NOW.minusHours(1), "FT");
        completed.completeEventSync();
        matchRepository.saveAll(List.of(pending, completed,
                match(32L, NOW.minusDays(2), "FT"), match(33L, NOW.minusHours(1), "2H")));

        assertThat(matchRepository.findFinishedEventSyncTargets(Set.of("FT", "AET", "PEN"), NOW.minusDays(1)))
                .extracting(Match::getMatchId).containsExactly(30L);
    }

    private Match match(Long matchId, LocalDateTime date, String status) {
        return Match.create(
                matchId,
                date,
                status,
                "Regular Season - 12",
                "Old Trafford",
                "Manchester",
                33L,
                "Manchester United",
                "home-logo",
                null,
                40L,
                "Liverpool",
                "away-logo",
                null,
                null,
                null
        );
    }
}
