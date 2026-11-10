package backend.manuhub.coach;

import backend.manuhub.coach.dto.CoachGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coaches")
public class CoachController implements CoachAPI {

    private final CoachService coachService;

    @GetMapping("/{teamId}")
    public ResponseEntity<CoachGetResponse> getCoach(@PathVariable Long teamId) {
        return ResponseEntity.ok(coachService.getCoach(teamId));
    }
}
