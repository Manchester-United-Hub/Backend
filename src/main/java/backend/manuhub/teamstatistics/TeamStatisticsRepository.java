package backend.manuhub.teamstatistics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamStatisticsRepository extends JpaRepository<TeamStatistics, Long> {
    public List<TeamStatistics> findAllByOrderBySeasonAsc();
}
