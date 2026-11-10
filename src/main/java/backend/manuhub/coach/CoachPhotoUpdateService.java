package backend.manuhub.coach;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoachPhotoUpdateService {

    private final CoachRepository coachRepository;

    @Transactional
    public void updatePhoto(Long coachId, String photoUrl) {
        coachRepository.findById(coachId)
                .ifPresent(coach -> coach.updatePhoto(photoUrl));
    }
}
