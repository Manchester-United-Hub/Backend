package backend.manuhub.seasonplayer.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record SeasonPlayerListResponse(
        List<SeasonPlayerResponse> players,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static SeasonPlayerListResponse of(List<SeasonPlayerResponse> players, Page<?> playerPage) {
        return new SeasonPlayerListResponse(
                players,
                playerPage.getNumber(),
                playerPage.getSize(),
                playerPage.getTotalElements(),
                playerPage.getTotalPages(),
                playerPage.hasNext()
        );
    }
}
