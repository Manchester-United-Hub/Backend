package backend.manuhub.team;

import backend.manuhub.external.team.TeamClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamClient teamClient;


}
