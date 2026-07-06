package backend.manuhub.team.dto;

public record TeamResponse(Long id,
                           String name,
                           String logoUrl,
                           String city,
                           String country,
                           String stadium,
                           Integer founded) {

    public static TeamResponse create(Long id, String name, String logoUrl, String city, String country,String stadium, Integer founded) {
        return new TeamResponse(id, name, logoUrl, city, country, stadium, founded);
    }
}
