package backend.manuhub.external.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerProfileApiResponse(
        List<Response> response
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            Player player
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Birth(
            String date
    ) {
    }
}
