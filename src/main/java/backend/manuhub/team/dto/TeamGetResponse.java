package backend.manuhub.team.dto;

import backend.manuhub.team.Team;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record TeamGetResponse(
        Long id,
        String name,
        String photo,
        Integer founded,
        String location,
        String stadium,
        String coachName
) {

    public static TeamGetResponse from(Team t, String coachName) {
        return TeamGetResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .photo(t.getLogoUrl())
                .founded(t.getFounded())
                .location(t.getCity() + ", " + t.getCountry())
                .stadium(t.getStadium())
                .coachName(coachName)
                .build();
    }
}
