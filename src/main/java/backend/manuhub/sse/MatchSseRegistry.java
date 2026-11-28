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

    public List<SseEmitter> getEmitters(Long matchId) {
        // 전송 도중 연결이 바뀌어도 순회할 구독자 목록을 유지한다.
        List<SseEmitter> list = emitters.get(matchId);
        return list == null ? List.of() : List.copyOf(list);
    }

    public List<Long> getSubscribedMatchIds() {
        // 구독자가 있는 경기만 heartbeat 대상으로 삼는다.
        return List.copyOf(emitters.keySet());
    }
}
