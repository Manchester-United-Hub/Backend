package backend.manuhub.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class ImageService {

    @Value("${cloudflare.r2.bucket}")
    private String bucket;
    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;


    @Qualifier("defaultRestClient")
    private final RestClient restClient;
    private final S3Client s3Client;

    public ImageService(@Qualifier("defaultRestClient") RestClient restClient, S3Client s3Client) {
        this.restClient = restClient;
        this.s3Client = s3Client;
    }

    public String uploadFromUrl(String imageUrl, String key) {
        try {
            byte[] imageBytes = restClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .body(byte[].class);

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType("image/png")
                            .build(),
                    RequestBody.fromBytes(imageBytes)
            );
            return publicUrl + "/" + key;
        } catch (Exception e) {
            log.error(">>> ImageService --> 이미지 업로드 실패: {}", imageUrl, e);
            return imageUrl;
        }
    }
}
