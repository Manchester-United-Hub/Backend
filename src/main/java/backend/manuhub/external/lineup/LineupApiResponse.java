package backend.manuhub.external.lineup;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LineupApiResponse(
        List<Response> response
) {

    public record Response(
            Team team,
            String formation,
            Coach coach,
            @JsonProperty("startXI") List<LineupPlayer> startPlayers,
            List<LineupPlayer> substitutes
    ) {
    }

    public record Team(
            Long id,
            String name,
            String logo
    ) {
    }

    public record Coach(
            Long id,
            String name,
            String photo
    ) {
    }

    public record LineupPlayer(
            Player player
    ) {
    }

    public record Player(
            Long id,
            String name,
            Integer number,
            String pos,
            String grid
    ) {
    }
}
