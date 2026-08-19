package backend.manuhub.rank;

import backend.manuhub.common.util.SeasonProvider;
import backend.manuhub.external.rank.PlayerRankApiResponse;
import backend.manuhub.external.rank.RankClient;
import backend.manuhub.external.rank.TeamRankApiResponse;
import backend.manuhub.image.ImageService;
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
import java.util.function.Supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankService {

    private static final String RANK_CACHE_KEY = "rank:premier-league:%d";
    private static final String TOP_SCORERS_CACHE_KEY = "rank:premier-league:topscorers:%d";
    private static final String TOP_ASSISTS_CACHE_KEY = "rank:premier-league:topassists:%d";

    private static final long CACHE_TTL_HOURS = 25;

    private final RankClient rankClient;
    private final SeasonProvider seasonProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ImageService imageService;


    public TeamRankGetResponse getRank(int season) {
        List<TeamRankResponse> result = getOrFetch(
                String.format(RANK_CACHE_KEY, season),
                new TypeReference<>() {},
                () -> fetchRankBySeason(season)
        );
        return TeamRankGetResponse.from(season, result);
    }

    public PlayerRankGetResponse getTopScorers(int season) {
        List<PlayerRankResponse> result = getOrFetch(
                String.format(TOP_SCORERS_CACHE_KEY, season),
                new TypeReference<>() {},
                () -> toPlayerRankResponses(rankClient.fetchTopScorers(season), true)
        );
        return PlayerRankGetResponse.from(season, result);
    }

    public PlayerRankGetResponse getTopAssists(int season) {
        List<PlayerRankResponse> result = getOrFetch(
                String.format(TOP_ASSISTS_CACHE_KEY, season),
                new TypeReference<>() {},
                () -> toPlayerRankResponses(rankClient.fetchTopAssists(season), false)
        );
        return PlayerRankGetResponse.from(season, result);
    }

    public void updateRank() {
        int currentSeason = seasonProvider.getCurrentSeason();
        saveCache(String.format(RANK_CACHE_KEY, currentSeason), fetchRankBySeason(currentSeason));
        saveCache(String.format(TOP_SCORERS_CACHE_KEY, currentSeason), toPlayerRankResponses(rankClient.fetchTopScorers(currentSeason), true));
        saveCache(String.format(TOP_ASSISTS_CACHE_KEY, currentSeason), toPlayerRankResponses(rankClient.fetchTopAssists(currentSeason), false));
    }

    private <T> List<T> getOrFetch(String cacheKey, TypeReference<List<T>> typeRef, Supplier<List<T>> fetchFn) {
        try {
            List<T> cached = getCache(cacheKey, typeRef);
            if (cached != null) {
                return cached;
            }
            List<T> fetched = fetchFn.get();
            if (!fetched.isEmpty()) {
                saveCache(cacheKey, fetched);
            }
            return fetched;
        } catch (RedisException e) {
            log.error(">>> RankService --> RedisException", e);
            return fetchFn.get();
        }
    }
    private List<TeamRankResponse> fetchRankBySeason(int season) {
        List<TeamRankApiResponse.RankInfo> rankInfos = rankClient.fetchRank(season);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<TeamRankResponse>> futures = rankInfos.stream()
                    .map(r -> CompletableFuture.supplyAsync(() -> TeamRankResponse.create(
                            r.rank(),
                            r.team().id(),
                            r.team().name(),
                            imageService.uploadFromUrl(r.team().logo(), "teams/" + r.team().id() + ".png"),
                            r.points(),
                            r.all().played(),
                            r.all().win(),
                            r.all().draw(),
                            r.all().lose(),
                            r.all().goals().goalsFor(),
                            r.all().goals().goalsAgainst(),
                            r.goalsDiff(),
                            r.form()
                    ), executor))
                    .toList();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        }
    }

    private List<PlayerRankResponse> toPlayerRankResponses(List<PlayerRankApiResponse.PlayerRankInfo> infos, boolean isScorer) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<PlayerRankResponse>> futures = infos.stream()
                    .map(p -> CompletableFuture.supplyAsync(() -> {
                        PlayerRankApiResponse.StatisticsInfo stats = p.statistics().get(0);
                        return PlayerRankResponse.create(
                                0,
                                p.player().id(),
                                p.player().name(),
                                imageService.uploadFromUrl(p.player().photo(), "players/" + p.player().id() + ".png"),
                                stats.team().id(),
                                stats.team().name(),
                                imageService.uploadFromUrl(stats.team().logo(), "teams/" + stats.team().id() + ".png"),
                                stats.goals().total(),
                                stats.goals().assists(),
                                stats.games().appearences(),
                                stats.games().minutes(),
                                stats.shots().total(),
                                stats.shots().on(),
                                stats.passes().key()
                        );
                    }, executor))
                    .toList();

            List<PlayerRankResponse> unranked = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            // 순위 계산
            List<PlayerRankResponse> result = new ArrayList<>();
            int rank = 1;
            for (int i = 0; i < unranked.size(); i++) {
                if (i > 0) {
                    int prevValue = isScorer ? unranked.get(i - 1).goals() : unranked.get(i - 1).assists();
                    int currentValue = isScorer ? unranked.get(i).goals() : unranked.get(i).assists();
                    if (currentValue != prevValue) {
                        rank = i + 1;
                    }
                }
                PlayerRankResponse p = unranked.get(i);
                result.add(PlayerRankResponse.create(rank, p.playerId(), p.playerName(), p.playerPhoto(),
                        p.teamId(), p.teamName(), p.teamLogo(), p.goals(), p.assists(),
                        p.appearences(), p.minutes(), p.shots(), p.shotsOnTarget(), p.keyPasses()));
            }
            return result;
        }
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
