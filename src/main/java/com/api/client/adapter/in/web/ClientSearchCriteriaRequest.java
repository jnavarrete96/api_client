package com.api.client.adapter.in.web;

import com.api.client.domain.model.ClientSearchCriteria;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClientSearchCriteriaRequest {

    private String sharedKey;
    private String name;
    private String email;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page;
    private Integer size;

    public ClientSearchCriteria toDomain() {
        return ClientSearchCriteria.builder()
                .sharedKey(this.sharedKey)
                .name(this.name)
                .email(this.email)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .page(this.page)
                .size(this.size)
                .build();
    }
}