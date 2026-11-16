package backend.manuhub.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class MatchSseRegistryTest {

    private MatchSseRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new MatchSseRegistry();
    }

    @Test
    @DisplayName("연결을 등록하면 해당 경기의 연결 수가 늘어난다")
    void register() {
        registry.register(1L, new SseEmitter());

        assertThat(registry.getConnectionCount(1L)).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 경기에 여러 연결을 등록할 수 있다")
    void register_multiple() {
        registry.register(1L, new SseEmitter());
        registry.register(1L, new SseEmitter());

        assertThat(registry.getConnectionCount(1L)).isEqualTo(2);
    }

    @Test
    @DisplayName("경기별로 연결이 분리된다")
    void register_separatedByMatch() {
        registry.register(1L, new SseEmitter());
        registry.register(2L, new SseEmitter());

        assertThat(registry.getConnectionCount(1L)).isEqualTo(1);
        assertThat(registry.getConnectionCount(2L)).isEqualTo(1);
    }

    @Test
    @DisplayName("해제하면 그 연결만 빠진다")
    void unregister_onlyTarget() {
        SseEmitter first = new SseEmitter();
        SseEmitter second = new SseEmitter();
        registry.register(1L, first);
        registry.register(1L, second);

        registry.unregister(1L, first);

        assertThat(registry.getConnectionCount(1L)).isEqualTo(1);
    }

    @Test
    @DisplayName("마지막 연결을 해제하면 연결 수가 0이 된다")
    void unregister_last() {
        SseEmitter emitter = new SseEmitter();
        registry.register(1L, emitter);

        registry.unregister(1L, emitter);

        assertThat(registry.getConnectionCount(1L)).isZero();
    }

    @Test
    @DisplayName("등록되지 않은 연결을 해제해도 예외가 없다")
    void unregister_notRegistered() {
        assertThatCode(() -> registry.unregister(1L, new SseEmitter()))
                .doesNotThrowAnyException();
    }
}
