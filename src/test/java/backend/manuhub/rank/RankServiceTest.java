package backend.manuhub.rank;

import backend.manuhub.common.util.SeasonProvider;
import backend.manuhub.external.rank.RankApiResponse;
import backend.manuhub.external.rank.RankClient;
import backend.manuhub.rank.dto.RankGetResponse;
import backend.manuhub.rank.dto.RankResponse;
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

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(seasonProvider.getCurrentSeason()).willReturn(2025);
    }

    @Test
    @DisplayName("캐시가 있으면 API 호출 없이 캐시를 반환한다")
    void getRankFromCache() throws JsonProcessingException {
        List<RankResponse> cached = List.of(RankResponse.create(1, 42L, "Arsenal", "https://logo.png", 85, 38, 26, 7, 5, 71, 27, 44, "WWWWW"));
        given(valueOperations.get(anyString())).willReturn("cachedJson");
        given(objectMapper.readValue(eq("cachedJson"), any(TypeReference.class))).willReturn(cached);

        RankGetResponse result = rankService.getRank();

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

        RankGetResponse result = rankService.getRank();

        assertThat(result.ranks()).hasSize(1);
        then(valueOperations).should().set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("Redis 오류 시 API를 직접 호출해서 반환한다")
    void getRankWhenRedisError() {
        given(redisTemplate.opsForValue()).willThrow(new RedisException("Redis 오류"));
        given(rankClient.fetchRank(2025)).willReturn(List.of(mockRankInfo()));

        RankGetResponse result = rankService.getRank();

        assertThat(result.ranks()).hasSize(1);
        then(rankClient).should().fetchRank(2025);
    }

    @Test
    @DisplayName("updateRank 시 API를 호출하고 캐시에 저장한다")
    void updateRank() throws JsonProcessingException {
        given(rankClient.fetchRank(2025)).willReturn(List.of(mockRankInfo()));
        given(objectMapper.writeValueAsString(any())).willReturn("json");

        rankService.updateRank();

        then(rankClient).should().fetchRank(2025);
        then(valueOperations).should().set(anyString(), anyString(), anyLong(), any());
    }

    private RankApiResponse.RankInfo mockRankInfo() {
        RankApiResponse.TeamInfo teamInfo = new RankApiResponse.TeamInfo(42L, "Arsenal", "https://logo.png");
        RankApiResponse.GoalsInfo goalsInfo = new RankApiResponse.GoalsInfo(71, 27);
        RankApiResponse.AllInfo allInfo = new RankApiResponse.AllInfo(38, 26, 7, 5, goalsInfo);
        return new RankApiResponse.RankInfo(1, teamInfo, 85, 44, "WWWWW", allInfo);
    }
}
