package backend.manuhub.news;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {
    Optional<News> findTopByOrderByPublishedAtDesc();
    boolean existsByOriginalLink(String originalLink);
}
