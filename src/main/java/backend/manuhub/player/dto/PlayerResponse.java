package backend.manuhub.player.dto;

import backend.manuhub.player.Player;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PROTECTED)
public record PlayerResponse(
        Long id,
        String name,
        String birthDate,
        String nationality,
        String height,
        String weight,
        Integer number,
        String position,
        String photo
) {
    public static PlayerResponse from(Player player) {
        return PlayerResponse.builder()
                .id(player.getPlayerId())
                .name(player.getName())
                .birthDate(player.getBirthDate())
                .nationality(player.getNationality())
                .height(player.getHeight())
                .weight(player.getWeight())
                .number(player.getNumber())
                .position(player.getPosition())
                .photo(player.getPhoto())
                .build();
    }
}
