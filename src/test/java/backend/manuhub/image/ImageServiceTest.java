package backend.manuhub.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImageServiceTest {

    private RestClient restClient;
    private S3Client s3Client;
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class, RETURNS_DEEP_STUBS);
        s3Client = mock(S3Client.class);
        imageService = new ImageService(restClient, s3Client);
        ReflectionTestUtils.setField(imageService, "bucket", "manuhub");
        ReflectionTestUtils.setField(imageService, "publicUrl", "https://r2.dev");
    }

    @Test
    @DisplayName("원본 이미지를 내려받아 R2에 업로드하고 공개 URL을 반환한다")
    void uploadsImageAndReturnsPublicUrl() {
        when(restClient.get().uri("api-photo").retrieve().body(byte[].class))
                .thenReturn(new byte[]{1, 2, 3});

        String result = imageService.uploadFromUrl("api-photo", "players/1485.png");

        assertEquals("https://r2.dev/players/1485.png", result);
        verify(s3Client).putObject(
                argThat((PutObjectRequest request) -> request.bucket().equals("manuhub")
                        && request.key().equals("players/1485.png")
                        && request.contentType().equals("image/png")),
                any(RequestBody.class)
        );
    }
}
