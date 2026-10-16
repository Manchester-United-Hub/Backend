package backend.manuhub.highlight.dto;

import backend.manuhub.highlight.Highlight;
import org.springframework.data.domain.Page;

import java.util.List;

public record HighlightListGetResponse(
        int season,
        List<HighlightGetResponse> highlights,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static HighlightListGetResponse from(int season, Page<Highlight> highlightPage) {
        List<HighlightGetResponse> responses = highlightPage.getContent().stream()
                .map(HighlightGetResponse::from)
                .toList();

        return new HighlightListGetResponse(
                season,
                responses,
                highlightPage.getNumber(),
                highlightPage.getSize(),
                highlightPage.getTotalElements(),
                highlightPage.getTotalPages(),
                highlightPage.hasNext()
        );
    }
}
