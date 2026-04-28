package backend.manuhub.news;

import backend.manuhub.external.naver.NaverNewsClient;
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

    private List<NewsItem> fetchNewsItems(){
        return naverNewsClient.fetchNews().stream()
                .map(n -> NewsItem.create(n.getTitle(), n.getOriginalLink(), n.getLink(), n.getDescription(), n.getPublishedAt()))
                .filter(NewsItem::isValid)
                .toList();
    }

    private List<NewsItem> filterRecentNews(List<NewsItem> newsItems){
        LocalDateTime lastPublishedAt = newsRepository.findTopByOrderByPublishedAtDesc()
                .map(News::getPublishedAt)
                .orElse(LocalDateTime.MIN);

        return newsItems.stream()
                .filter(n -> !n.getPublishedAt().isBefore(lastPublishedAt))
                .toList();
    }



    private void saveAll(List<NewsItem> newsItemsToSave){
        List<News> newsList = newsItemsToSave.stream()
                .filter(n -> !newsRepository.existsByOriginalLink(n.getOriginalLink()))
                .map(n -> News.create(n.getTitle(), n.getOriginalLink(), n.getLink(), n.getDescription(), n.getPublishedAt()))
                .toList();

        newsRepository.saveAll(newsList);
    }
}
