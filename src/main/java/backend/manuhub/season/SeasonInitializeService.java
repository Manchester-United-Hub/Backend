package backend.manuhub.season;

import backend.manuhub.exception.ApiServerException;
import backend.manuhub.exception.ErrorCode;
import backend.manuhub.external.season.SeasonApiResponse;
import backend.manuhub.external.season.SeasonClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonInitializeService {

    private final SeasonRepository seasonRepository;
    private final SeasonClient seasonClient;
    private final Clock clock;

    @Transactional
    public int saveSeasonsFrom(Integer startSeason) {
        LocalDate today = LocalDate.now(clock);
        List<SeasonApiResponse.SeasonPeriod> seasons = seasonClient.getPremierLeagueSeasons().stream()
                .filter(this::hasValidPeriod)
                .toList();

        SeasonApiResponse.SeasonPeriod currentSeason = seasons.stream()
                .filter(season -> !season.end().isBefore(today))
                .min(Comparator.comparing(SeasonApiResponse.SeasonPeriod::start))
                .orElseThrow(() -> new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR));

        List<SeasonApiResponse.SeasonPeriod> targetSeasons = seasons.stream()
                .filter(season -> season.year() >= startSeason)
                .filter(season -> season.year() <= currentSeason.year())
                .sorted(Comparator.comparing(SeasonApiResponse.SeasonPeriod::year))
                .toList();

        if (targetSeasons.isEmpty()) {
            throw new ApiServerException(ErrorCode.API_FOOTBALL_SERVER_ERROR);
        }

        targetSeasons.forEach(this::saveOrUpdate);

        log.info("[Season] Seasons saved. seasons={}~{}", startSeason, currentSeason.year());
        return currentSeason.year();
    }

    private boolean hasValidPeriod(SeasonApiResponse.SeasonPeriod season) {
        return season != null
                && season.year() != null
                && season.start() != null
                && season.end() != null;
    }

    private void saveOrUpdate(SeasonApiResponse.SeasonPeriod response) {
        Season season = seasonRepository.findByYear(response.year())
                .map(savedSeason -> {
                    savedSeason.updatePeriod(response.start(), response.end());
                    return savedSeason;
                })
                .orElseGet(() -> Season.create(response.year(), response.start(), response.end()));

        seasonRepository.save(season);
    }
}
