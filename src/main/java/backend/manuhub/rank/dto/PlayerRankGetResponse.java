package backend.manuhub.rank.dto;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record PlayerRankGetResponse(
        String season,
        List<PlayerRankResponse> ranks
) {
    public static PlayerRankGetResponse from(int season, List<PlayerRankResponse> ranks) {
        return PlayerRankGetResponse.builder()
                .season(season + "-" + String.valueOf(season + 1).substring(2))
                .ranks(ranks)
                .build();
    }
}
