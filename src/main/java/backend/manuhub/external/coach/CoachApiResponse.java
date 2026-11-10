package backend.manuhub.external.coach;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoachApiResponse(List<CoachResponse> response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CoachResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("birth") BirthInfo birth,
            @JsonProperty("nationality") String nationality,
            @JsonProperty("photo") String photo,
            @JsonProperty("team") TeamInfo team,
            @JsonProperty("career") List<CareerInfo> career
    ) {
        public CoachResponse(Long id, String name, TeamInfo team, List<CareerInfo> career) {
            this(id, name, null, null, null, team, career);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BirthInfo(
            @JsonProperty("date") String date,
            @JsonProperty("place") String place
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamInfo(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name
    ) {
        public TeamInfo(Long id) {
            this(id, null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CareerInfo(
            @JsonProperty("team") TeamInfo team,
            @JsonProperty("start") String start,
            @JsonProperty("end") String end
    ) {
        public CareerInfo(TeamInfo team, String start) {
            this(team, start, null);
        }
    }
}
