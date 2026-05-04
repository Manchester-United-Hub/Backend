package backend.manuhub.teamstatistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamStatisticsInitializer implements CommandLineRunner {

    private final TeamStatisticsInitializeService teamStatisticsInitializeService;

    @Override
    public void run(String... args) throws Exception {
        int lastSeason = calculateLastSeason();

        for (int i = 2; i >= 0; i--) {
            int selectSeason = lastSeason - i;
            teamStatisticsInitializeService.initializeTeamStatistics(selectSeason);
        }
    }

    // 6월 1일 기준으로 마지막 시즌 계산
    private int calculateLastSeason() {
        LocalDate now = LocalDate.now();

        LocalDate leagueLastDay = LocalDate.of(now.getYear(), 6, 1);

        if (now.isBefore(leagueLastDay)) {
            return now.getYear() - 2; // 재작년
        }

        return now.getYear() - 1; // 작년
    }
}
