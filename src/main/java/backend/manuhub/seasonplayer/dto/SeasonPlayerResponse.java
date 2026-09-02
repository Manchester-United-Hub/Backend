package backend.manuhub.seasonplayer.dto;

import backend.manuhub.player.Player;
import backend.manuhub.seasonplayer.SeasonPlayer;
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
        Integer number,
        String position,
        String photo,
        List<Integer> seasons
) {
    public static SeasonPlayerResponse from(Player player, List<Integer> seasons) {
        return from(player, null, null, seasons);
    }

    public static SeasonPlayerResponse from(Player player, SeasonPlayer seasonPlayer, List<Integer> seasons) {
        if (seasonPlayer == null) {
            return from(player, seasons);
        }

        return from(player, seasonPlayer.getNumber(), seasonPlayer.getPosition(), seasons);
    }

    public static SeasonPlayerResponse from(SeasonPlayer seasonPlayer, List<Integer> seasons) {
        return from(
                seasonPlayer.getPlayer(),
                seasonPlayer.getNumber(),
                seasonPlayer.getPosition(),
                seasons
        );
    }

    private static SeasonPlayerResponse from(Player player, Integer number, String position,
                                             List<Integer> seasons) {
        return SeasonPlayerResponse.builder()
                .id(player.getPlayerId())
                .name(player.getName())
                .birthDate(player.getBirthDate())
                .nationality(player.getNationality())
                .height(player.getHeight())
                .weight(player.getWeight())
                .number(number)
                .position(position)
                .photo(player.getPhoto())
                .seasons(seasons)
                .build();
    }
}
