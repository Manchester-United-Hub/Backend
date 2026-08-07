package backend.manuhub.external.season;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SeasonApiResponse(List<LeagueResponse> response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LeagueResponse(List<SeasonPeriod> seasons) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SeasonPeriod(
            Integer year,
            LocalDate start,
            LocalDate end,
            Boolean current
    ) {
    }
}
