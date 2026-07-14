package backend.manuhub.rank;

import backend.manuhub.common.util.SeasonProvider;
import backend.manuhub.external.rank.RankClient;
import backend.manuhub.rank.dto.RankGetResponse;
import backend.manuhub.rank.dto.RankResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankService {

    private static final String RANK_CACHE_KEY = "rank:premier-league";
    private static final long CACHE_TTL_HOURS = 25;

    private final RankClient rankClient;
    private final SeasonProvider seasonProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;


    public RankGetResponse getRank() {

        int currentSeason = seasonProvider.getCurrentSeason();

        try {
            List<RankResponse> result = getCache();
            if (result == null) {
                result = fetchRankBySeason(currentSeason);
                saveCache(result);
            }
            return RankGetResponse.from(currentSeason, result);
        } catch (RedisException e) {
            log.error(">>> RankService --> RedisException", e);
            return RankGetResponse.from(currentSeason, fetchRankBySeason(currentSeason));
        }
    }

    public void updateRank() {
        int currentSeason = seasonProvider.getCurrentSeason();
        saveCache(fetchRankBySeason(currentSeason));
    }

    private List<RankResponse> getCache() {
        try {
            String cached = redisTemplate.opsForValue().get(RANK_CACHE_KEY);
            if (cached == null) return null;
            return objectMapper.readValue(cached, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<RankResponse> fetchRankBySeason(int season) {
        return rankClient.fetchRank(season).stream()
                .map(r -> RankResponse.create(
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

    private void saveCache(List<RankResponse> ranks) {
        try {
            redisTemplate.opsForValue().set(RANK_CACHE_KEY, objectMapper.writeValueAsString(ranks), CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error(">>> RankService --> failed save cache", e);
        }
    }
}
