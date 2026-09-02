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
import java.util.Map;
import java.util.function.Function;
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
        int insertedCount = upsertMatches(responses);

        log.info("[Match] Matches upserted. league={}, season={}, teamId={}, totalCount={}, insertedCount={}",
                league, season, teamId, responses.size(), insertedCount);
    }

    @Retryable(
            maxAttemptsExpression = "${retry.match.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${retry.match.delay:1000}", multiplier = 2),
            retryFor = ApiServerException.class
    )
    @Transactional
    public void updateMatch(Long matchId) {
        MatchApiResponse.Response response = matchClient.getMatch(matchId);
        upsertMatches(List.of(response));

        log.info("[Match] Match updated. matchId={}", matchId);
    }

    private int upsertMatches(List<MatchApiResponse.Response> responses) {
        List<Match> matches = MatchMapper.toEntities(responses);
        Map<Long, Match> existingMatchesByMatchId = matchRepository.findAllByMatchIdIn(
                        matches.stream()
                                .map(Match::getMatchId)
                                .collect(Collectors.toSet())
                )
                .stream()
                .collect(Collectors.toMap(Match::getMatchId, Function.identity()));

        List<Match> newMatches = matches.stream()
                .filter(match -> {
                    Match existingMatch = existingMatchesByMatchId.get(match.getMatchId());
                    if (existingMatch == null) {
                        return true;
                    }

                    existingMatch.updateFrom(match);
                    return false;
                })
                .toList();

        if (!newMatches.isEmpty()) {
            matchRepository.saveAll(newMatches);
        }

        return newMatches.size();
    }

    @Recover
    public void recover(ApiServerException e, Long league, Integer season, Long teamId) {
        log.warn(">>> MatchInitializeService --> API-Football fixtures retry failed. league={}, season={}, teamId={}, code={}",
                league, season, teamId, e.getErrorCode().getCode(), e);
    }

    @Recover
    public void recover(ApiServerException e, Long matchId) {
        log.warn(">>> MatchInitializeService --> API-Football fixture retry failed. matchId={}, code={}",
                matchId, e.getErrorCode().getCode(), e);
    }
}
