package backend.manuhub.highlight;

import backend.manuhub.external.highlight.HighlightClient;
import backend.manuhub.external.highlight.PlaylistItemApiResponse;
import backend.manuhub.external.highlight.VideoApiResponse;
import backend.manuhub.highlight.dto.HighlightListGetResponse;
import backend.manuhub.image.ImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("HighlightService 테스트")
@ExtendWith(MockitoExtension.class)
public class HighlightServiceTest {

    @InjectMocks
    private HighlightService highlightService;

    @Mock
    private HighlightRepository highlightRepository;

    @Mock
    private HighlightClient highlightClient;

    @Mock
    private ImageService imageService;

    @Test
    @DisplayName("재생목록이 비어있으면 아무 작업도 하지 않는다")
    void syncHighlightsWhenVideosEmpty() {
        given(highlightClient.fetchHighlightVideos(2026)).willReturn(List.of());

        highlightService.syncHighlights(2026);

        then(highlightRepository).shouldHaveNoInteractions();
        then(imageService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("신규 영상은 썸네일을 업로드하고 저장한다")
    void syncHighlightsSavesNewVideo() {
        PlaylistItemApiResponse.PlaylistItem video = mockPlaylistItem("video1", "Title1", "https://thumb.png", "2026-10-13T21:45:27Z");
        given(highlightClient.fetchHighlightVideos(2026)).willReturn(List.of(video));
        given(highlightClient.fetchViewCounts(List.of("video1")))
                .willReturn(List.of(mockVideoItem("video1", 1000L)));
        given(highlightRepository.findAllBySeason(2026)).willReturn(List.of());
        given(imageService.uploadFromUrl("https://thumb.png", "highlights/video1.png"))
                .willReturn("https://r2.dev/video1.png");

        highlightService.syncHighlights(2026);

        ArgumentCaptor<List<Highlight>> captor = ArgumentCaptor.forClass(List.class);
        then(highlightRepository).should().saveAll(captor.capture());

        List<Highlight> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getVideoId()).isEqualTo("video1");
        assertThat(saved.get(0).getThumbnailUrl()).isEqualTo("https://r2.dev/video1.png");
        assertThat(saved.get(0).getViewCount()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("기존 영상은 조회수만 업데이트하고 새로 저장하지 않는다")
    void syncHighlightsUpdatesExistingViewCount() {
        PlaylistItemApiResponse.PlaylistItem video = mockPlaylistItem("video1", "Title1", "https://thumb.png", "2026-10-13T21:45:27Z");
        Highlight existing = Highlight.create("video1", "Title1", "https://r2.dev/video1.png",
                LocalDateTime.of(2026, 10, 13, 21, 45, 27), 500L, 2026);

        given(highlightClient.fetchHighlightVideos(2026)).willReturn(List.of(video));
        given(highlightClient.fetchViewCounts(List.of("video1")))
                .willReturn(List.of(mockVideoItem("video1", 2000L)));
        given(highlightRepository.findAllBySeason(2026)).willReturn(List.of(existing));

        highlightService.syncHighlights(2026);

        assertThat(existing.getViewCount()).isEqualTo(2000L);
        then(imageService).shouldHaveNoInteractions();
        then(highlightRepository).should().saveAll(List.of());
    }

    @Test
    @DisplayName("조회수 정보가 없는 영상은 0으로 처리된다")
    void syncHighlightsHandlesMissingViewCount() {
        PlaylistItemApiResponse.PlaylistItem video = mockPlaylistItem("video1", "Title1", "https://thumb.png", "2026-10-13T21:45:27Z");
        given(highlightClient.fetchHighlightVideos(2026)).willReturn(List.of(video));
        given(highlightClient.fetchViewCounts(List.of("video1"))).willReturn(List.of());
        given(highlightRepository.findAllBySeason(2026)).willReturn(List.of());
        given(imageService.uploadFromUrl(any(), any())).willReturn("https://r2.dev/video1.png");

        highlightService.syncHighlights(2026);

        ArgumentCaptor<List<Highlight>> captor = ArgumentCaptor.forClass(List.class);
        then(highlightRepository).should().saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getViewCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("season으로 하이라이트 목록을 최신순으로 조회한다")
    void getHighlightsSortedByLatest() {
        Highlight highlight = Highlight.create("video1", "Title1", "https://r2.dev/video1.png",
                LocalDateTime.of(2026, 10, 13, 21, 45, 27), 1000L, 2026);
        Page<Highlight> page = new PageImpl<>(List.of(highlight), PageRequest.of(0, 20), 1);

        given(highlightRepository.findAllBySeason(eq(2026), any(Pageable.class))).willReturn(page);

        HighlightListGetResponse result = highlightService.getHighlights(2026, HighlightSort.LATEST, 0, 20);

        assertThat(result.highlights()).hasSize(1);
        assertThat(result.highlights().get(0).videoId()).isEqualTo("video1");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    private PlaylistItemApiResponse.PlaylistItem mockPlaylistItem(String videoId, String title, String thumbnailUrl, String publishedAt) {
        PlaylistItemApiResponse.ResourceIdInfo resourceId = new PlaylistItemApiResponse.ResourceIdInfo(videoId);
        PlaylistItemApiResponse.ThumbnailInfo thumbnail = new PlaylistItemApiResponse.ThumbnailInfo(thumbnailUrl);
        PlaylistItemApiResponse.ThumbnailsInfo thumbnails = new PlaylistItemApiResponse.ThumbnailsInfo(thumbnail);
        PlaylistItemApiResponse.SnippetInfo snippet = new PlaylistItemApiResponse.SnippetInfo(title, publishedAt, thumbnails, resourceId);
        return new PlaylistItemApiResponse.PlaylistItem(snippet);
    }

    private VideoApiResponse.VideoItem mockVideoItem(String videoId, Long viewCount) {
        VideoApiResponse.StatisticsInfo statistics = new VideoApiResponse.StatisticsInfo(viewCount);
        return new VideoApiResponse.VideoItem(videoId, statistics);
    }
}
