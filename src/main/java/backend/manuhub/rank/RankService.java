package backend.manuhub.rank;

import backend.manuhub.common.util.SeasonProvider;
import backend.manuhub.external.rank.PlayerRankApiResponse;
import backend.manuhub.external.rank.RankClient;
import backend.manuhub.rank.dto.PlayerRankGetResponse;
import backend.manuhub.rank.dto.PlayerRankResponse;
import backend.manuhub.rank.dto.TeamRankGetResponse;
import backend.manuhub.rank.dto.TeamRankResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankService {

    private static final String RANK_CACHE_KEY = "rank:premier-league";
    private static final String TOP_SCORERS_CACHE_KEY = "rank:premier-league:topscorers";
    private static final String TOP_ASSISTS_CACHE_KEY = "rank:premier-league:topassists";

    private static final long CACHE_TTL_HOURS = 25;

    private final RankClient rankClient;
    private final SeasonProvider seasonProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;


    public TeamRankGetResponse getRank() {
        int currentSeason = seasonProvider.getCurrentSeason();
        try {
            List<TeamRankResponse> result = getCache(RANK_CACHE_KEY, new TypeReference<>() {});
            if (result == null) {
                result = fetchRankBySeason(currentSeason);
                saveCache(RANK_CACHE_KEY, result);
            }
            return TeamRankGetResponse.from(currentSeason, result);
        } catch (RedisException e) {
            log.error(">>> RankService --> RedisException", e);
            return TeamRankGetResponse.from(currentSeason, fetchRankBySeason(currentSeason));
        }
    }

    public PlayerRankGetResponse getTopScorers() {
        int currentSeason = seasonProvider.getCurrentSeason();
        try {
            List<PlayerRankResponse> result = getCache(TOP_SCORERS_CACHE_KEY, new TypeReference<>() {});
            if (result == null) {
                result = toPlayerRankResponses(rankClient.fetchTopScorers(currentSeason), true);
                saveCache(TOP_SCORERS_CACHE_KEY, result);
            }
            return PlayerRankGetResponse.from(currentSeason, result);
        } catch (RedisException e) {
            log.error(">>> RankService --> RedisException", e);
            return PlayerRankGetResponse.from(currentSeason, toPlayerRankResponses(rankClient.fetchTopScorers(currentSeason), true));
        }
    }

    public PlayerRankGetResponse getTopAssists() {
        int currentSeason = seasonProvider.getCurrentSeason();
        try {
            List<PlayerRankResponse> result = getCache(TOP_ASSISTS_CACHE_KEY, new TypeReference<>() {});
            if (result == null) {
                result = toPlayerRankResponses(rankClient.fetchTopAssists(currentSeason), false);
                saveCache(TOP_ASSISTS_CACHE_KEY, result);
            }
            return PlayerRankGetResponse.from(currentSeason, result);
        } catch (RedisException e) {
            log.error(">>> RankService --> RedisException", e);
            return PlayerRankGetResponse.from(currentSeason, toPlayerRankResponses(rankClient.fetchTopAssists(currentSeason), false));
        }
    }

    public void updateRank() {
        int currentSeason = seasonProvider.getCurrentSeason();
        saveCache(RANK_CACHE_KEY, fetchRankBySeason(currentSeason));
        saveCache(TOP_SCORERS_CACHE_KEY, toPlayerRankResponses(rankClient.fetchTopScorers(currentSeason), true));
        saveCache(TOP_ASSISTS_CACHE_KEY, toPlayerRankResponses(rankClient.fetchTopAssists(currentSeason), false));
    }
    private List<TeamRankResponse> fetchRankBySeason(int season) {
        return rankClient.fetchRank(season).stream()
                .map(r -> TeamRankResponse.create(
                        r.rank(),
                        r.team().id(),
                        r.team().name(),
                        r.team().logo(),
                        r.points(),
                        r.all().played(),
                        r.all().win(),
                        r.all().draw(),
                        r.all().lose(),
                        r.all().goals().goalsFor(),
                        r.all().goals().goalsAgainst(),
                        r.goalsDiff(),
                        r.form()
                ))
                .toList();
    }

    private List<PlayerRankResponse> toPlayerRankResponses(List<PlayerRankApiResponse.PlayerRankInfo> infos, boolean isScorer) {
        List<PlayerRankResponse> result = new ArrayList<>();
        int rank = 1;

        for (int i = 0; i < infos.size(); i++) {
            PlayerRankApiResponse.PlayerRankInfo p = infos.get(i);
            PlayerRankApiResponse.StatisticsInfo stats = p.statistics().get(0);
            int currentValue = isScorer ? stats.goals().total() : stats.goals().assists();

            if (i > 0) {
                PlayerRankApiResponse.StatisticsInfo prevStats = infos.get(i - 1).statistics().get(0);
                int prevValue = isScorer ? prevStats.goals().total() : prevStats.goals().assists();
                if (currentValue != prevValue) {
                    rank = i + 1;
                }
            }

            result.add(PlayerRankResponse.create(
                    rank,
                    p.player().id(),
                    p.player().name(),
                    p.player().photo(),
                    stats.team().id(),
                    stats.team().name(),
                    stats.team().logo(),
                    stats.goals().total(),
                    stats.goals().assists(),
                    stats.games().appearences(),
                    stats.games().minutes(),
                    stats.shots().total(),
                    stats.shots().on(),
                    stats.passes().key()
            ));
        }
        return result;
    }

    private <T> List<T> getCache(String key, TypeReference<List<T>> typeReference) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached == null) return null;
            return objectMapper.readValue(cached, typeReference);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private <T> void saveCache(String key, List<T> data) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(data), CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error(">>> RankService --> failed save cache", e);
        }
    }
}
