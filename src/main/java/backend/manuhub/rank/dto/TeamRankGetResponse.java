package backend.manuhub.rank.dto;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record RankGetResponse(
        String season,
        List<RankResponse> ranks
) {
    public static RankGetResponse from(int season, List<RankResponse> ranks) {
        return RankGetResponse.builder()
                .season(season + "-" + String.valueOf(season + 1).substring(2))
                .ranks(ranks)
                .build();
    }
}
