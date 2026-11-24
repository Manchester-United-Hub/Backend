package backend.manuhub.lineup;

/** 저장 트랜잭션이 커밋된 뒤에만 최신 라인업을 전송하도록 갱신 요청을 분리한다. */
public record MatchLineupRefreshEvent(Long matchId) {
}
