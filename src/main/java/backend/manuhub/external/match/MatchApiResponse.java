package backend.manuhub.external.match;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public record MatchApiResponse(
        List<Response> response
) {

    public record Response(
            Fixture fixture,
            League league,
            Teams teams,
            Goals goals
    ) {
    }

    public record Fixture(
            Long id,
            OffsetDateTime date,
            Venue venue,
            Status status
    ) {
    }

    /**
     * API-Football이 제공하는 경기 진행 상태 정보입니다.
     * 경기 시작 여부와 현재 진행 단계, 경기 시간을 판단할 때 사용합니다.
     */
    public record Status(
            // 사람이 읽을 수 있는 전체 상태명입니다. 예: "Not Started", "First Half", "Match Finished"
            @JsonProperty("long") String longStatus,
            // TBD: 경기 시간 미정, NS: 경기 시작 전, 1H: 전반전, HT: 하프타임,
            // 2H: 후반전, ET: 연장전, BT: 연장전 휴식, P: 승부차기 진행 중,
            // SUSP: 일시 중단, INT: 중단, FT: 정규시간 종료, AET: 연장전 종료,
            // PEN: 승부차기 종료, PST: 연기, CANC: 취소, ABD: 포기·중단,
            // AWD: 행정 판정 완료, WO: 부전승, LIVE: 경기 진행 중
            @JsonProperty("short") String shortCode,
            // 정규 경기 기준 현재 진행된 분입니다. 경기 시작 전에는 null일 수 있습니다.
            Integer elapsed,
            // 전반·후반 추가시간 또는 연장 관련 추가 진행 시간입니다. 없으면 null일 수 있습니다.
            Integer extra
    ) {
    }

    public record Venue(
            Long id,
            String name,
            String city
    ) {
    }

    public record League(
            Long id,
            String name,
            String country,
            Integer season,
            String round
    ) {
    }

    public record Teams(
            Team home,
            Team away
    ) {
    }

    public record Team(
            Long id,
            String name,
            String logo,
            Boolean winner
    ) {
    }

    public record Goals(
            Integer home,
            Integer away
    ) {
    }
}
