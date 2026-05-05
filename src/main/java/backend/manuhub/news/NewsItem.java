package backend.manuhub.news;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public record NewsItem(
        String title,
        String originalLink,
        String link,
        String description,
        LocalDateTime publishedAt
) {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.RFC_1123_DATE_TIME;

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

    public boolean isValid() {
        return title != null && originalLink != null && link != null && description != null && publishedAt != null;
    }

    public static NewsItem create(String title, String originalLink, String link, String description, String publishedAt) {
        return new NewsItem(
                cleanText(title),
                originalLink,
                link,
                cleanText(description),
                parsePubDate(publishedAt)
        );
    }
}
