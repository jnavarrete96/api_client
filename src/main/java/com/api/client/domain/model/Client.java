package com.api.client.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class Client {
    private Long id;

    private String sharedKey;
    private String name;
    private String email;
    private String phone;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate dataAdded;
}
