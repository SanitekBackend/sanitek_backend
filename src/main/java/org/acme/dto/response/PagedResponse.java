package org.acme.dto.response;

import java.util.List;

public record PagedResponse<T>(
        List<T> data,
        int page,
        int pageSize,
        long total
) {
    public static <T> PagedResponse<T> of(List<T> data, int page, int pageSize, long total) {
        return new PagedResponse<>(data, page, pageSize, total);
    }
}
