package backend.manuhub.news;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {
    Optional<News> findTopByOrderByPublishedAtDesc();
    boolean existsByOriginalLink(String originalLink);

    @Query("""
        SELECT n FROM News n
        WHERE (:cursorAt IS NULL OR n.publishedAt < :cursorAt
            OR (n.publishedAt = :cursorAt AND n.id < :cursorId))
        ORDER BY n.publishedAt DESC, n.id DESC
        LIMIT :size
        """)
    List<News> findNewsByCursor(
            @Param("cursorAt") LocalDateTime cursorAt,
            @Param("cursorId") Long cursorId,
            @Param("size") int size
    );
}
