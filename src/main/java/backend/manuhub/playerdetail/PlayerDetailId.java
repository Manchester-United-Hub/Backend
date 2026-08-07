package backend.manuhub.playerdetail;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDetailId implements Serializable {

    private Long playerId;
    private Integer season;
    private Long leagueId;
}
