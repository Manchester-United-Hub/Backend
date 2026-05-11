package backend.manuhub.news;

import backend.manuhub.exception.ErrorCode;
import backend.manuhub.exception.InvalidRequestException;
import backend.manuhub.external.naver.NaverNewsClient;
import backend.manuhub.news.dto.NewsItem;
import backend.manuhub.news.dto.NewsListGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;
    private final NaverNewsClient naverNewsClient;

    @Transactional
    public void syncRecentNews(){
        List<NewsItem> newsItems = fetchNewsItems();
        List<NewsItem> newsItemsToSave = filterRecentNews(newsItems);
        saveAll(newsItemsToSave);
    }

    public NewsListGetResponse getNewsList(LocalDateTime cursorAt, Long cursorId, int size) {
        validateCursor(cursorAt, cursorId);
        List<News> findNewsList = newsRepository.findNewsByCursor(cursorAt, cursorId, size);
        return NewsListGetResponse.from(findNewsList);
    }

    private List<NewsItem> fetchNewsItems(){
        return naverNewsClient.fetchNews().stream()
                .map(n -> NewsItem.create(n.title(), n.originalLink(), n.link(), n.description(), n.publishedAt()))
                .filter(NewsItem::isValid)
                .toList();
    }

    private List<NewsItem> filterRecentNews(List<NewsItem> newsItems){
        LocalDateTime lastPublishedAt = newsRepository.findTopByOrderByPublishedAtDesc()
                .map(News::getPublishedAt)
                .orElse(LocalDateTime.MIN);

        return newsItems.stream()
                .filter(n -> !n.publishedAt().isBefore(lastPublishedAt))
                .toList();
    }

    private void saveAll(List<NewsItem> newsItemsToSave){
        List<News> newsList = newsItemsToSave.stream()
                .filter(n -> !newsRepository.existsByOriginalLink(n.originalLink()))
                .map(n -> News.create(n.title(), n.originalLink(), n.link(), n.description(), n.publishedAt()))
                .toList();

        newsRepository.saveAll(newsList);
    }

    private void validateCursor(LocalDateTime cursorAt, Long cursorId) {
        if ((cursorAt == null) != (cursorId == null)) {
            throw new InvalidRequestException(ErrorCode.INVALID_REQUEST_ERROR);
        }
    }
}
