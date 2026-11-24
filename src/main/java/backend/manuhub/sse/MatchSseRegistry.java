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
        // 전송 중 연결이 추가·해제되어도 순회할 대상 목록이 바뀌지 않도록 복사본을 반환한다.
        List<SseEmitter> list = emitters.get(matchId);
        return list == null ? List.of() : List.copyOf(list);
    }

    public List<Long> getSubscribedMatchIds() {
        // 접속 중인 경기에만 heartbeat를 보내 불필요한 전체 경기 순회를 피한다.
        return List.copyOf(emitters.keySet());
    }
}
