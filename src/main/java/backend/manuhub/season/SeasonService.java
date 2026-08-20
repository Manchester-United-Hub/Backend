package backend.manuhub.season;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.InvalidRequestException;
import backend.manuhub.season.dto.SeasonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeasonService {

    private final SeasonRepository seasonRepository;
    private final Clock clock;

    public SeasonResponse getCurrentSeason() {
        LocalDate today = LocalDate.now(clock);
        Season season = seasonRepository
                .findFirstByEndDateGreaterThanEqualOrderByStartDateAsc(today)
                .orElseThrow(() -> new InvalidRequestException(ErrorCode.NOT_FOUND_ERROR));

        boolean started = !today.isBefore(season.getStartDate());
        return new SeasonResponse(season.getYear(), started);
    }
}
