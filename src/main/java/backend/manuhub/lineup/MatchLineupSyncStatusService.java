package backend.manuhub.lineup;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MatchLineupSyncStatusService {

    // 최초 시도를 포함해 세 번 실패하면 스케줄러 자동 재시도 대상에서 제외한다.
    private static final int MAX_ATTEMPTS = 3;

    private final MatchRepository matchRepository;
    private final Clock clock;

    @Transactional
    public void recordFailure(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));
        match.recordLineupSyncFailure(LocalDateTime.now(clock), MAX_ATTEMPTS);
    }

    @Transactional
    public void markCompleted(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));
        match.completeLineupSync();
    }
}
