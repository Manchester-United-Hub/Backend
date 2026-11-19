package backend.manuhub.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RestClientConfigTest {

    @Test
    @DisplayName("공통 RestClient 요청 팩토리에 연결 5초와 응답 30초 타임아웃을 적용한다")
    void appliesCommonTimeouts() {
        RestClientConfig config = new RestClientConfig();
        ReflectionTestUtils.setField(config, "connectTimeout", Duration.ofSeconds(5));
        ReflectionTestUtils.setField(config, "readTimeout", Duration.ofSeconds(30));

        SimpleClientHttpRequestFactory requestFactory =
                ReflectionTestUtils.invokeMethod(config, "requestFactory");

        assertThat(requestFactory).isNotNull();
        assertThat(ReflectionTestUtils.getField(requestFactory, "connectTimeout"))
                .isEqualTo(5_000);
        assertThat(ReflectionTestUtils.getField(requestFactory, "readTimeout"))
                .isEqualTo(30_000);
    }
}
