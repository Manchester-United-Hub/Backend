package backend.manuhub.player;

import backend.manuhub.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlayerImageService {

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    private final ImageService imageService;
    private final PlayerPhotoUpdateService playerPhotoUpdateService;

    public void uploadAndUpdate(List<PlayerImageTarget> targets) {
        Map<Long, String> photosByPlayerId = new LinkedHashMap<>();

        for (PlayerImageTarget target : targets) {
            if (isR2Url(target.currentUrl())
                    || target.sourceUrl() == null
                    || target.sourceUrl().isBlank()) {
                continue;
            }

            String photoUrl = imageService.uploadFromUrl(
                    target.sourceUrl(),
                    "players/" + target.playerId() + ".png"
            );
            photosByPlayerId.put(target.playerId(), photoUrl);
        }

        if (!photosByPlayerId.isEmpty()) {
            playerPhotoUpdateService.updatePhotos(photosByPlayerId);
        }
    }

    private boolean isR2Url(String imageUrl) {
        return imageUrl != null && imageUrl.startsWith(publicUrl + "/");
    }
}
