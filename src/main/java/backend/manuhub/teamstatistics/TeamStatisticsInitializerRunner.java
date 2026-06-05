package backend.manuhub.teamstatistics;

import backend.manuhub.exception.ApiInvalidResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class TeamStatisticsInitializerRunner implements CommandLineRunner {

    private final TeamStatisticsInitializeService teamStatisticsInitializeService;

    @Override
    public void run(String... args){
        int lastSeason = getLastSeason();

        for(int i = lastSeason - 2; i <= lastSeason; i++){
            try{
                teamStatisticsInitializeService.saveTeamStatistics(i);

            } catch (ApiInvalidResponseException e){
                log.error(">>> TeamStatisticsInitializerRunner --> ApiInvalidResponseException, season : {}", i, e);
            }
        }
    }
 
    public int getLastSeason() {
        LocalDate now = LocalDate.now();
        LocalDate deadline = LocalDate.of(now.getYear(), 6, 1);

        if (now.isAfter(deadline)) {
            return now.getYear() - 1;
        }
        return now.getYear() - 2;
    }
}
