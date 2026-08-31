package backend.manuhub.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerPhotoUpdateServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerPhotoUpdateService playerPhotoUpdateService;

    @Test
    @DisplayName("업로드가 끝난 선수의 사진 URL을 R2 URL로 변경한다")
    void updatesPlayerPhotoToR2Url() {
        Player player = Player.create(1485L, "Bruno Fernandes", "1994-09-08",
                "Portugal", "179 cm", "69 kg", "api-photo");
        when(playerRepository.findAllById(Set.of(1485L))).thenReturn(List.of(player));

        playerPhotoUpdateService.updatePhotos(Map.of(1485L, "https://r2.dev/players/1485.png"));

        assertEquals("https://r2.dev/players/1485.png", player.getPhoto());
    }
}
