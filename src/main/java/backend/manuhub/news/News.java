package backend.manuhub.news;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "news")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class News {

    @Id
    @Column(name = "news_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    @NotNull
    private String title;

    @Column(name = "original_link", unique = true)
    @NotNull
    private String originalLink;

    @Column(name = "link")
    @NotNull
    private String link;

    @Column(name = "description")
    @NotNull
    private String description;

    @Column(name = "published_at")
    @NotNull
    private LocalDateTime publishedAt;

    public static News create(String title, String originalLink, String link, String description, LocalDateTime publishedAt) {
        return News.builder()
                .title(title)
                .originalLink(originalLink)
                .link(link)
                .description(description)
                .publishedAt(publishedAt)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private News(String title, String originalLink, String link, String description, LocalDateTime publishedAt) {
        this.title = title;
        this.originalLink = originalLink;
        this.link = link;
        this.description = description;
        this.publishedAt = publishedAt;
    }
}
