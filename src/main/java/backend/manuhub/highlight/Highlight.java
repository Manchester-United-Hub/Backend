package backend.manuhub.highlight;

import backend.manuhub.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "highlights")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Highlight extends BaseTimeEntity {

    @Id
    @Column(name = "video_id")
    private String videoId;

    @Column(name = "title")
    private String title;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(nullable = false)
    private Integer season;

    public static Highlight create(String videoId, String title, String thumbnailUrl,
                                   LocalDateTime publishedAt, Long viewCount, Integer season) {
        return Highlight.builder()
                .videoId(videoId)
                .title(title)
                .thumbnailUrl(thumbnailUrl)
                .publishedAt(publishedAt)
                .viewCount(viewCount)
                .season(season)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Highlight(String videoId, String title, String thumbnailUrl,
                      LocalDateTime publishedAt, Long viewCount, Integer season) {
        this.videoId = videoId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.publishedAt = publishedAt;
        this.viewCount = viewCount;
        this.season = season;
    }

    public void updateViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
}
