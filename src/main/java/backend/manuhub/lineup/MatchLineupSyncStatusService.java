package backend.manuhub.lineup;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.ManuHubException;
import backend.manuhub.match.Match;
import backend.manuhub.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchLineupSyncStatusService {

    private final MatchRepository matchRepository;

    @Transactional
    public void markFailed(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ManuHubException(ErrorCode.NOT_FOUND_ERROR));
        match.failLineupSync();
    }
}
