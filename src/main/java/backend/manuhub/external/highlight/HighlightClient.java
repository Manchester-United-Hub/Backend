package backend.manuhub.external.highlight;

import backend.manuhub.exception.ApiClientException;
import backend.manuhub.exception.ApiServerException;
import backend.manuhub.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class HighlightClient {

    @Qualifier("youtubeRestClient")
    private final RestClient restClient;
    private final String apiKey;
    private final String channelId;
    private static final String SEARCH_QUERY = "Men's First Team Highlights";

    public HighlightClient(@Qualifier("youtubeRestClient") RestClient restClient,
                           @Value("${external.youtube.api-key}") String apiKey,
                           @Value("${external.youtube.channel-id}") String channelId) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.channelId = channelId;
    }

    public List<PlaylistItemApiResponse.PlaylistItem> fetchHighlightVideos(int season) {
        PlaylistSearchApiResponse searchResponse = searchHighlightPlaylists();

        return findPlaylistIdBySeason(searchResponse, season)
                .map(this::fetchAllPlaylistItems)
                .orElse(List.of());
    }

    public List<VideoApiResponse.VideoItem> fetchViewCounts(List<String> videoIds) {
        if (videoIds.isEmpty()) {
            return List.of();
        }

        List<VideoApiResponse.VideoItem> allItems = new ArrayList<>();
        for (List<String> chunk : partition(videoIds, 50)) {
            allItems.addAll(fetchViewCountsChunk(chunk));
        }
        return allItems;
    }

    private PlaylistSearchApiResponse searchHighlightPlaylists() { // 키워드가 포함된 특정 재생 목록들 리스트로 받아오기
        return restClient.get()
                .uri("/search?part=snippet&channelId=" + channelId
                        + "&q=" + SEARCH_QUERY
                        + "&type=playlist&key=" + apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ApiClientException(ErrorCode.YOUTUBE_CLIENT_ERROR);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ApiServerException(ErrorCode.YOUTUBE_SERVER_ERROR);
                })
                .body(PlaylistSearchApiResponse.class);
    }

    private Optional<String> findPlaylistIdBySeason(PlaylistSearchApiResponse response, int season) { // 원하는 시즌의 재생목록 찾기
        List<PlaylistSearchApiResponse.SearchItem> items = response.items();
        if (items == null) {
            return Optional.empty();
        }

        String seasonPattern = season + "/" + String.valueOf(season + 1).substring(2);
        return items.stream()
                .filter(item -> item.snippet().title().contains(seasonPattern))
                .map(item -> item.id().playlistId())
                .findFirst();
    }

    private PlaylistItemApiResponse fetchPlaylistItemsPage(String playlistId, String pageToken) { // 재생목록 안에 있는 동영상 리스트
        String uri = "/playlistItems?part=snippet&playlistId=" + playlistId
                + "&maxResults=50&key=" + apiKey
                + (pageToken != null ? "&pageToken=" + pageToken : "");

        return restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ApiClientException(ErrorCode.YOUTUBE_CLIENT_ERROR);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ApiServerException(ErrorCode.YOUTUBE_SERVER_ERROR);
                })
                .body(PlaylistItemApiResponse.class);
    }

    private List<PlaylistItemApiResponse.PlaylistItem> fetchAllPlaylistItems(String playlistId) {
        List<PlaylistItemApiResponse.PlaylistItem> allItems = new ArrayList<>();
        String pageToken = null;

        do {
            PlaylistItemApiResponse response = fetchPlaylistItemsPage(playlistId, pageToken);
            if (response.items() != null) {
                allItems.addAll(response.items());
            }
            pageToken = response.nextPageToken();
        } while (pageToken != null);

        return allItems;
    }

    private List<VideoApiResponse.VideoItem> fetchViewCountsChunk(List<String> videoIds) {
        String ids = String.join(",", videoIds);
        VideoApiResponse response = restClient.get()
                .uri("/videos?part=statistics&id=" + ids + "&key=" + apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ApiClientException(ErrorCode.YOUTUBE_CLIENT_ERROR);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ApiServerException(ErrorCode.YOUTUBE_SERVER_ERROR);
                })
                .body(VideoApiResponse.class);

        List<VideoApiResponse.VideoItem> items = response.items();
        return items == null ? List.of() : items;
    }

    private List<List<String>> partition(List<String> list, int size) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }
}
