package com.api.client.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Optional;

@Getter
@Builder
public class ClientSearchCriteria {

    private String sharedKey;
    private String name;
    private String email;

    private LocalDate startDate;
    private LocalDate endDate;

    // 🔹 paginación
    private Integer page;
    private Integer size;

    public int getOffset() {
        return Optional.ofNullable(page)
                .filter(p -> size != null)
                .map(p -> p * size)
                .orElse(0);
    }

    public int getLimit() {
        return size != null ? size : 10;
    }
}