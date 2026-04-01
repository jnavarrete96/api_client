package com.api.client.domain.port.out;

import com.api.client.domain.model.Client;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface ClientRepositoryPort {

    Flux<Client> findAll();

    Mono<Client> findBySharedKey(String sharedKey);

    Mono<Client> findByEmail(String email);

    Mono<Client> save(Client client);

    Flux<Client> advancedSearch(
            String sharedKey,
            String name,
            String email,
            LocalDate startDate,
            LocalDate endDate
    );
}
