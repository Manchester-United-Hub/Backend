package backend.manuhub.rank.dto;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record TeamRankGetResponse(
        String season,
        List<TeamRankResponse> ranks
) {
    public static TeamRankGetResponse from(int season, List<TeamRankResponse> ranks) {
        return TeamRankGetResponse.builder()
                .season(season + "-" + String.valueOf(season + 1).substring(2))
                .ranks(ranks)
                .build();
    }
}
