package backend.manuhub.common.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SeasonProvider {

    public int getCurrentSeason() {
        LocalDate now = LocalDate.now();
        return now.getMonthValue() >= 8 ? now.getYear() : now.getYear() - 1;
    }
}
