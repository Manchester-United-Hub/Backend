package backend.manuhub.coach;

import backend.manuhub.image.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoachImageService {

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    private final ImageService imageService;
    private final CoachPhotoUpdateService coachPhotoUpdateService;

    public void uploadAndUpdate(CoachImageTarget target) {
        if (!needsUpload(target)) {
            return;
        }

        String photoUrl = imageService.uploadFromUrl(
                target.sourceUrl(),
                "coaches/" + target.coachId() + ".png"
        );

        if (!isR2Url(photoUrl)) {
            log.warn("[CoachImage] R2 upload failed. coachId={}", target.coachId());
            return;
        }

        coachPhotoUpdateService.updatePhoto(target.coachId(), photoUrl);
    }

    private boolean needsUpload(CoachImageTarget target) {
        return !isR2Url(target.currentUrl())
                && target.sourceUrl() != null
                && !target.sourceUrl().isBlank();
    }

    private boolean isR2Url(String imageUrl) {
        return imageUrl != null && imageUrl.startsWith(publicUrl + "/");
    }
}
