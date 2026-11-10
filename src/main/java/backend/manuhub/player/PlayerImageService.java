package backend.manuhub.player;

import backend.manuhub.image.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerImageService {

    private static final int MAX_CONCURRENT_UPLOADS = 5;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    private final ImageService imageService;
    private final PlayerPhotoUpdateService playerPhotoUpdateService;

    public void uploadAndUpdate(List<PlayerImageTarget> targets) {
        List<PlayerImageTarget> uploadTargets = targets.stream()
                .filter(this::needsUpload)
                .toList();

        if (uploadTargets.isEmpty()) {
            return;
        }

        Map<Long, String> photosByPlayerId;
        try (ExecutorService executor = Executors.newFixedThreadPool(
                MAX_CONCURRENT_UPLOADS,
                Thread.ofVirtual().name("player-image-upload-", 0).factory()
        )) {
            List<CompletableFuture<Optional<UploadedPhoto>>> futures = uploadTargets.stream()
                    .map(target -> CompletableFuture.supplyAsync(() -> upload(target), executor))
                    .toList();

            photosByPlayerId = futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toMap(
                            UploadedPhoto::playerId,
                            UploadedPhoto::photoUrl,
                            (existing, replacement) -> replacement,
                            LinkedHashMap::new
                    ));
        }

        if (!photosByPlayerId.isEmpty()) {
            playerPhotoUpdateService.updatePhotos(photosByPlayerId);
        }

        log.info("[PlayerImage] Upload completed. attempted={}, succeeded={}, failed={}",
                uploadTargets.size(), photosByPlayerId.size(), uploadTargets.size() - photosByPlayerId.size());
    }

    private boolean needsUpload(PlayerImageTarget target) {
        return !isR2Url(target.currentUrl())
                && target.sourceUrl() != null
                && !target.sourceUrl().isBlank();
    }

    private Optional<UploadedPhoto> upload(PlayerImageTarget target) {
        String photoUrl = imageService.uploadFromUrl(
                target.sourceUrl(),
                "players/" + target.playerId() + ".png"
        );

        if (!isR2Url(photoUrl)) {
            return Optional.empty();
        }

        return Optional.of(new UploadedPhoto(target.playerId(), photoUrl));
    }

    private boolean isR2Url(String imageUrl) {
        return imageUrl != null && imageUrl.startsWith(publicUrl + "/");
    }

    private record UploadedPhoto(Long playerId, String photoUrl) {
    }
}
