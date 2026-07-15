package backend.manuhub.match;

import backend.manuhub.exception.ApiServerException;
import backend.manuhub.external.match.MatchApiResponse;
import backend.manuhub.external.match.MatchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchInitializeService {

    private final MatchRepository matchRepository;
    private final MatchClient matchClient;

    @Retryable(
            maxAttemptsExpression = "${retry.match.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${retry.match.delay:1000}", multiplier = 2),
            retryFor = ApiServerException.class
    )
    @Transactional
    public void saveMatches(Long league, Integer season, Long teamId) {
        List<MatchApiResponse.Response> responses = matchClient.getMatches(league, season, teamId);
        List<Match> matches = MatchMapper.toEntities(responses);
        Set<Long> existingMatchIds = matchRepository.findAllByMatchIdIn(
                        matches.stream()
                                .map(Match::getMatchId)
                                .collect(Collectors.toSet())
                )
                .stream()
                .map(Match::getMatchId)
                .collect(Collectors.toSet());

        List<Match> newMatches = matches.stream()
                .filter(match -> !existingMatchIds.contains(match.getMatchId()))
                .toList();

        if (newMatches.isEmpty()) {
            log.info("[Match] Matches already exist. league={}, season={}, teamId={}", league, season, teamId);
            return;
        }

        matchRepository.saveAll(newMatches);
        log.info("[Match] Matches saved. league={}, season={}, teamId={}, count={}",
                league, season, teamId, newMatches.size());
    }

    @Recover
    public void recover(ApiServerException e, Long league, Integer season, Long teamId) {
        log.warn(">>> MatchInitializeService --> API-Football fixtures retry failed. league={}, season={}, teamId={}, code={}",
                league, season, teamId, e.getErrorCode().getCode(), e);
    }
}
