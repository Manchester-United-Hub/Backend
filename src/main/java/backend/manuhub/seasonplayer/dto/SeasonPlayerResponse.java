package backend.manuhub.seasonplayer.dto;

import backend.manuhub.player.Player;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PROTECTED)
public record SeasonPlayerResponse(
        Long id,
        String name,
        String birthDate,
        String nationality,
        String height,
        String weight,
        String photo,
        List<Integer> seasons
) {
    public static SeasonPlayerResponse from(Player player, List<Integer> seasons) {
        return SeasonPlayerResponse.builder()
                .id(player.getPlayerId())
                .name(player.getName())
                .birthDate(player.getBirthDate())
                .nationality(player.getNationality())
                .height(player.getHeight())
                .weight(player.getWeight())
                .photo(player.getPhoto())
                .seasons(seasons)
                .build();
    }
}
