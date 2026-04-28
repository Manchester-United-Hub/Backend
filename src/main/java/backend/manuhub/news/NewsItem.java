package backend.manuhub.news;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@RequiredArgsConstructor
public class NewsItem {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.RFC_1123_DATE_TIME;

    private final String title;
    private final String originalLink;
    private final String link;
    private final String description;
    private final LocalDateTime publishedAt;

    public static NewsItem create(String title, String originalLink, String link, String description, String publishedAt){
        return new NewsItem(
                cleanText(title),
                originalLink,
                link,
                cleanText(description),
                parsePubDate(publishedAt)
        );
    }

    public boolean isValid() {
        return title != null && originalLink != null && link != null && description != null && publishedAt != null;
    }

    private static String cleanText(String text) {
        if (text == null) return null;

        return text
                .replaceAll("<[^>]*>", "")
                .replaceAll("&quot;", "\"");
    }

    private static LocalDateTime parsePubDate(String pubDate) {
        try {
            return OffsetDateTime.parse(pubDate, DATE_TIME_FORMATTER)
                    .toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }
}
