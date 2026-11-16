package backend.manuhub.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class MatchSseRegistry {

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public void register(Long matchId, SseEmitter emitter) {
        emitters.compute(matchId, (key, list) -> {
            if (list == null) {
                list = new CopyOnWriteArrayList<>();
            }
            list.add(emitter);
            return list;
        });
    }

    public void unregister(Long matchId, SseEmitter emitter) {
        emitters.computeIfPresent(matchId, (key, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }

    /** 현재 연결 수. 확인과 테스트용. */
    public int getConnectionCount(Long matchId) {
        List<SseEmitter> list = emitters.get(matchId);
        return list == null ? 0 : list.size();
    }
}
