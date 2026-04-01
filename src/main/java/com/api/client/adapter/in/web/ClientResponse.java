package com.api.client.adapter.in.web;

import com.api.client.domain.model.Client;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ClientResponse {
    private Long id;
    private String sharedKey;
    private String name;
    private String email;
    private String phone;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate dataAdded;

    public static ClientResponse fromDomain(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .sharedKey(client.getSharedKey())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .startDate(client.getStartDate())
                .endDate(client.getEndDate())
                .dataAdded(client.getDataAdded())
                .build();
    }
}
