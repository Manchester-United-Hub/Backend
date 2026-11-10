package backend.manuhub.player;

import backend.manuhub.exception.ApiInvalidResponseException;
import backend.manuhub.exception.ErrorCode;
import backend.manuhub.external.player.PlayerProfileApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerProfilePersistenceService {

    private final PlayerRepository playerRepository;

    @Transactional
    public List<PlayerImageTarget> saveMissing(List<PlayerProfileApiResponse.Player> profiles) {
        List<PlayerImageTarget> imageTargets = new ArrayList<>();

        for (PlayerProfileApiResponse.Player profile : profiles) {
            if (profile.id() == null || profile.name() == null) {
                throw new ApiInvalidResponseException(ErrorCode.API_FOOTBALL_PLAYER_INVALID_RESPONSE_ERROR);
            }
            if (playerRepository.existsById(profile.id())) {
                continue;
            }

            Player player = Player.create(
                    profile.id(),
                    profile.name(),
                    profile.birth() == null ? null : profile.birth().date(),
                    profile.nationality(),
                    profile.height(),
                    profile.weight(),
                    profile.photo()
            );
            Player savedPlayer = playerRepository.save(player);
            imageTargets.add(new PlayerImageTarget(
                    profile.id(),
                    profile.photo(),
                    savedPlayer.getPhoto()
            ));
        }

        return imageTargets;
    }
}
