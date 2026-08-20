package backend.manuhub.rank;

import backend.manuhub.common.util.SeasonProvider;
import backend.manuhub.external.rank.PlayerRankApiResponse;
import backend.manuhub.external.rank.TeamRankApiResponse;
import backend.manuhub.external.rank.RankClient;
import backend.manuhub.image.ImageService;
import backend.manuhub.rank.dto.PlayerRankGetResponse;
import backend.manuhub.rank.dto.PlayerRankResponse;
import backend.manuhub.rank.dto.TeamRankGetResponse;
import backend.manuhub.rank.dto.TeamRankResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;

@DisplayName("RankService 테스트")
@ExtendWith(MockitoExtension.class)
public class RankServiceTest {

    @InjectMocks
    private RankService rankService;

    @Mock
    private RankClient rankClient;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SeasonProvider seasonProvider;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(imageService.uploadFromUrl(anyString(), anyString())).thenReturn("https://r2.dev/test.png");
    }

    @Test
    @DisplayName("캐시가 있으면 API 호출 없이 캐시를 반환한다")
    void getRankFromCache() throws JsonProcessingException {
        List<TeamRankResponse> cached = List.of(TeamRankResponse.create(1, 42L, "Arsenal", "https://logo.png", 85, 38, 26, 7, 5, 71, 27, 44, "WWWWW"));
        given(valueOperations.get(anyString())).willReturn("cachedJson");
        given(objectMapper.readValue(eq("cachedJson"), any(TypeReference.class))).willReturn(cached);

        TeamRankGetResponse result = rankService.getRank();

        assertThat(result.ranks()).hasSize(1);
        assertThat(result.season()).isEqualTo("2025-26");
        then(rankClient).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("캐시가 없으면 API를 호출하고 캐시에 저장한다")
    void getRankFromApi() throws JsonProcessingException {
        given(valueOperations.get(anyString())).willReturn(null);
        given(rankClient.fetchRank(2025)).willReturn(List.of(mockRankInfo()));
        given(objectMapper.writeValueAsString(any())).willReturn("json");

        TeamRankGetResponse result = rankService.getRank();

        assertThat(result.ranks()).hasSize(1);
        then(valueOperations).should().set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("Redis 오류 시 API를 직접 호출해서 반환한다")
    void getRankWhenRedisError() {
        given(redisTemplate.opsForValue()).willThrow(new RedisException("Redis 오류"));
        given(rankClient.fetchRank(2025)).willReturn(List.of(mockRankInfo()));

        TeamRankGetResponse result = rankService.getRank();

        assertThat(result.ranks()).hasSize(1);
        then(rankClient).should().fetchRank(2025);
    }

    @Test
    @DisplayName("updateRank 시 팀, 득점, 어시스트 순위 모두 캐시에 저장한다")
    void updateRank() throws JsonProcessingException {
        given(seasonProvider.getCurrentSeason()).willReturn(2025);
        given(rankClient.fetchRank(2025)).willReturn(List.of(mockRankInfo()));
        given(rankClient.fetchTopScorers(2025)).willReturn(List.of(mockPlayerRankInfo()));
        given(rankClient.fetchTopAssists(2025)).willReturn(List.of(mockPlayerRankInfo()));
        given(objectMapper.writeValueAsString(any())).willReturn("json");

        rankService.updateRank();

        then(rankClient).should().fetchRank(2025);
        then(rankClient).should().fetchTopScorers(2025);
        then(rankClient).should().fetchTopAssists(2025);
        then(valueOperations).should(times(3)).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("득점 순위 캐시가 있으면 API 호출 없이 반환한다")
    void getTopScorersFromCache() throws JsonProcessingException {
        List<PlayerRankResponse> cached = List.of(mockPlayerRankResponse());
        given(valueOperations.get(anyString())).willReturn("cachedJson");
        given(objectMapper.readValue(eq("cachedJson"), any(TypeReference.class))).willReturn(cached);

        PlayerRankGetResponse result = rankService.getTopScorers();

        assertThat(result.ranks()).hasSize(1);
        assertThat(result.season()).isEqualTo("2025-26");
        then(rankClient).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("득점 순위 캐시가 없으면 API를 호출하고 캐시에 저장한다")
    void getTopScorersFromApi() throws JsonProcessingException {
        given(valueOperations.get(anyString())).willReturn(null);
        given(rankClient.fetchTopScorers(2025)).willReturn(List.of(mockPlayerRankInfo()));
        given(objectMapper.writeValueAsString(any())).willReturn("json");

        PlayerRankGetResponse result = rankService.getTopScorers();

        assertThat(result.ranks()).hasSize(1);
        then(valueOperations).should().set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("득점 순위 Redis 오류 시 API를 직접 호출해서 반환한다")
    void getTopScorersWhenRedisError() {
        given(redisTemplate.opsForValue()).willThrow(new RedisException("Redis 오류"));
        given(rankClient.fetchTopScorers(2025)).willReturn(List.of(mockPlayerRankInfo()));

        PlayerRankGetResponse result = rankService.getTopScorers();

        assertThat(result.ranks()).hasSize(1);
        then(rankClient).should().fetchTopScorers(2025);
    }

    @Test
    @DisplayName("어시스트 순위 캐시가 있으면 API 호출 없이 반환한다")
    void getTopAssistsFromCache() throws JsonProcessingException {
        List<PlayerRankResponse> cached = List.of(mockPlayerRankResponse());
        given(valueOperations.get(anyString())).willReturn("cachedJson");
        given(objectMapper.readValue(eq("cachedJson"), any(TypeReference.class))).willReturn(cached);

        PlayerRankGetResponse result = rankService.getTopAssists();

        assertThat(result.ranks()).hasSize(1);
        assertThat(result.season()).isEqualTo("2025-26");
        then(rankClient).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("어시스트 순위 캐시가 없으면 API를 호출하고 캐시에 저장한다")
    void getTopAssistsFromApi() throws JsonProcessingException {
        given(valueOperations.get(anyString())).willReturn(null);
        given(rankClient.fetchTopAssists(2025)).willReturn(List.of(mockPlayerRankInfo()));
        given(objectMapper.writeValueAsString(any())).willReturn("json");

        PlayerRankGetResponse result = rankService.getTopAssists();

        assertThat(result.ranks()).hasSize(1);
        then(valueOperations).should().set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("어시스트 순위 Redis 오류 시 API를 직접 호출해서 반환한다")
    void getTopAssistsWhenRedisError() {
        given(redisTemplate.opsForValue()).willThrow(new RedisException("Redis 오류"));
        given(rankClient.fetchTopAssists(2025)).willReturn(List.of(mockPlayerRankInfo()));

        PlayerRankGetResponse result = rankService.getTopAssists();

        assertThat(result.ranks()).hasSize(1);
        then(rankClient).should().fetchTopAssists(2025);
    }

    private TeamRankApiResponse.RankInfo mockRankInfo() {
        TeamRankApiResponse.TeamInfo teamInfo = new TeamRankApiResponse.TeamInfo(42L, "Arsenal", "https://logo.png");
        TeamRankApiResponse.GoalsInfo goalsInfo = new TeamRankApiResponse.GoalsInfo(71, 27);
        TeamRankApiResponse.AllInfo allInfo = new TeamRankApiResponse.AllInfo(38, 26, 7, 5, goalsInfo);
        return new TeamRankApiResponse.RankInfo(1, teamInfo, 85, 44, "WWWWW", allInfo);
    }

    private PlayerRankApiResponse.PlayerRankInfo mockPlayerRankInfo() {
        PlayerRankApiResponse.PlayerInfo playerInfo = new PlayerRankApiResponse.PlayerInfo(1100L, "E. Haaland", "https://photo.png");
        PlayerRankApiResponse.TeamInfo teamInfo = new PlayerRankApiResponse.TeamInfo(50L, "Manchester City", "https://logo.png");
        PlayerRankApiResponse.GamesInfo gamesInfo = new PlayerRankApiResponse.GamesInfo(35, 2958);
        PlayerRankApiResponse.ShotsInfo shotsInfo = new PlayerRankApiResponse.ShotsInfo(102, 59);
        PlayerRankApiResponse.GoalsInfo goalsInfo = new PlayerRankApiResponse.GoalsInfo(27, 8);
        PlayerRankApiResponse.PassesInfo passesInfo = new PlayerRankApiResponse.PassesInfo(25);
        PlayerRankApiResponse.StatisticsInfo statsInfo = new PlayerRankApiResponse.StatisticsInfo(teamInfo, gamesInfo, shotsInfo, goalsInfo, passesInfo);
        return new PlayerRankApiResponse.PlayerRankInfo(playerInfo, List.of(statsInfo));
    }

    private PlayerRankResponse mockPlayerRankResponse() {
        return PlayerRankResponse.create(1, 1100L, "E. Haaland", "https://r2.dev/test.png", 50L, "Manchester City", "https://r2.dev/test.png", 27, 8, 35, 2958, 102, 59, 25);
    }
}