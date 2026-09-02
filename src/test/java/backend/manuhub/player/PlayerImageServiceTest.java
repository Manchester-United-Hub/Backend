package backend.manuhub.player;

import backend.manuhub.image.ImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerImageServiceTest {

    @Mock
    private ImageService imageService;

    @Mock
    private PlayerPhotoUpdateService playerPhotoUpdateService;

    @InjectMocks
    private PlayerImageService playerImageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(playerImageService, "publicUrl", "https://r2.dev");
    }

    @Test
    @DisplayName("선수 이미지를 R2에 업로드하고 DB 사진 URL을 갱신한다")
    void uploadsPlayerImageAndUpdatesPhoto() {
        PlayerImageTarget target = new PlayerImageTarget(1485L, "api-photo", "api-photo");
        when(imageService.uploadFromUrl("api-photo", "players/1485.png"))
                .thenReturn("https://r2.dev/players/1485.png");

        playerImageService.uploadAndUpdate(List.of(target));

        verify(imageService).uploadFromUrl("api-photo", "players/1485.png");
        verify(playerPhotoUpdateService).updatePhotos(Map.of(1485L, "https://r2.dev/players/1485.png"));
    }

    @Test
    @DisplayName("이미 R2 URL을 사용하는 선수 이미지는 처리하지 않는다")
    void skipsPlayerAlreadyUsingR2Image() {
        PlayerImageTarget target = new PlayerImageTarget(
                1485L,
                "api-photo",
                "https://r2.dev/players/1485.png"
        );
        playerImageService.uploadAndUpdate(List.of(target));

        verify(imageService, never()).uploadFromUrl(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        );
        verify(playerPhotoUpdateService, never()).updatePhotos(org.mockito.ArgumentMatchers.anyMap());
    }
}
