package backend.manuhub.highlight;

import org.springframework.data.domain.Sort;

public enum HighlightSort {

    LATEST("publishedAt"),
    VIEW_COUNT("viewCount");

    private final String property;

    HighlightSort(String property) {
        this.property = property;
    }

    public Sort toSort() {
        return Sort.by(Sort.Direction.DESC, property);
    }
}
