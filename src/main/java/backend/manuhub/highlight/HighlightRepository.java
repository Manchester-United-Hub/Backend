package backend.manuhub.highlight;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HighlightRepository extends JpaRepository<Highlight, String> {

    List<Highlight> findAllBySeason(int season);

    Page<Highlight> findAllBySeason(int season, Pageable pageable);
}
