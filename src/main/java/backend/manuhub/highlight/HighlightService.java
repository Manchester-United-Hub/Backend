package backend.manuhub.highlight;

import backend.manuhub.external.highlight.HighlightClient;
import backend.manuhub.external.highlight.PlaylistItemApiResponse;
import backend.manuhub.external.highlight.VideoApiResponse;
import backend.manuhub.highlight.dto.HighlightListGetResponse;
import backend.manuhub.highlight.dto.HighlightResponse;
import backend.manuhub.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HighlightService {

    private final HighlightRepository highlightRepository;
    private final HighlightClient highlightClient;
    private final ImageService imageService;

    @Transactional
    public void syncHighlights(int season) {
        List<PlaylistItemApiResponse.PlaylistItem> videos = highlightClient.fetchHighlightVideos(season);
        if (videos.isEmpty()) return;
        Map<String, Long> viewCounts = getViewCounts(videos);
        saveNewOrUpdateViewCount(season, videos, viewCounts);
    }

    public HighlightListGetResponse getHighlights(int season, HighlightSort sort, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, sort.toSort());
        Page<Highlight> result = highlightRepository.findAllBySeason(season, pageRequest);
        return HighlightListGetResponse.from(season, result);
    }

    private Map<String, Long> getViewCounts(List<PlaylistItemApiResponse.PlaylistItem> videos) {
        List<String> videoIds = videos.stream()
                .map(v -> v.snippet().resourceId().videoId())
                .toList();

        return highlightClient.fetchViewCounts(videoIds).stream()
                .filter(item -> item.statistics() != null && item.statistics().viewCount() != null)
                .collect(Collectors.toMap(VideoApiResponse.VideoItem::id, item -> item.statistics().viewCount()));
    }

    private void saveNewOrUpdateViewCount(int season, List<PlaylistItemApiResponse.PlaylistItem> videos, Map<String, Long> viewCounts) {
        Map<String, Highlight> existMap = highlightRepository.findAllBySeason(season).stream()
                .collect(Collectors.toMap(Highlight::getVideoId, h -> h));

        List<HighlightResponse> newHighlights = new ArrayList<>();
        for (PlaylistItemApiResponse.PlaylistItem v : videos) {
            String vId = v.snippet().resourceId().videoId();
            Highlight existH = existMap.get(vId);
            Long viewCount = viewCounts.getOrDefault(vId, 0L);
            if (existH!=null) existH.updateViewCount(viewCount);
            else newHighlights.add(toHighlightResponse(v, viewCount));
        }
        saveNewHighlights(newHighlights, season);
    }

    private HighlightResponse toHighlightResponse(PlaylistItemApiResponse.PlaylistItem video, Long viewCount) {
        return HighlightResponse.create(
                video.snippet().resourceId().videoId(),
                video.snippet().title(),
                video.snippet().thumbnails().high().url(),
                LocalDateTime.ofInstant(Instant.parse(video.snippet().publishedAt()), ZoneOffset.UTC),
                viewCount
        );
    }

    private void saveNewHighlights(List<HighlightResponse> newHighlights, int season) {
        List<Highlight> highlights = newHighlights.stream()
                .map(n ->
                        Highlight.create(
                                n.videoId(),
                                n.title(),
                                imageService.uploadFromUrl(n.thumbnailUrl(), "highlights/" + n.videoId() + ".png"),
                                n.publishedAt(),
                                n.viewCount(),
                                season)
                ).toList();
        highlightRepository.saveAll(highlights);
    }
}
