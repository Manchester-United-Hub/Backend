package backend.manuhub.external.player;

import java.util.List;

public record PlayerApiResponse(
        List<Response> response,
        Paging paging
) {
    public record Response(Player player, List<Statistics> statistics) {
    }

    public record Player(
            Long id,
            String name,
            Birth birth,
            String nationality,
            String height,
            String weight,
            String photo
    ) {
    }

    public record Birth(String date) {
    }

    public record Statistics(
            Team team,
            League league,
            Games games,
            Substitutes substitutes,
            Shots shots,
            Goals goals,
            Passes passes,
            Tackles tackles,
            Duels duels,
            Dribbles dribbles,
            Fouls fouls,
            Cards cards,
            Penalty penalty
    ) {
    }

    public record Team(Long id, String name) {
    }

    public record League(Long id, String name, Integer season) {
    }

    public record Games(
            Integer appearences,
            Integer lineups,
            Integer minutes,
            Integer number,
            String position,
            String rating,
            Boolean captain
    ) {
    }

    public record Substitutes(Integer in, Integer out, Integer bench) {
    }

    public record Shots(Integer total, Integer on) {
    }

    public record Goals(Integer total, Integer conceded, Integer assists, Integer saves) {
    }

    public record Passes(Integer total, Integer key, Integer accuracy) {
    }

    public record Tackles(Integer total, Integer blocks, Integer interceptions) {
    }

    public record Duels(Integer total, Integer won) {
    }

    public record Dribbles(Integer attempts, Integer success, Integer past) {
    }

    public record Fouls(Integer drawn, Integer committed) {
    }

    public record Cards(Integer yellow, Integer yellowred, Integer red) {
    }

    public record Penalty(Integer won, Integer scored, Integer missed, Integer saved) {
    }

    public record Paging(Integer current, Integer total) {
    }
}
