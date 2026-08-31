package backend.manuhub.player;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlayerPhotoUpdateService {

    private final PlayerRepository playerRepository;

    @Transactional
    public void updatePhotos(Map<Long, String> photosByPlayerId) {
        playerRepository.findAllById(photosByPlayerId.keySet()).forEach(player ->
                player.updatePhoto(photosByPlayerId.get(player.getPlayerId()))
        );
    }
}
