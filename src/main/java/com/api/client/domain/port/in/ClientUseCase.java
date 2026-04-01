package com.api.client.domain.port.in;

import com.api.client.domain.model.Client;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface ClientUseCase {

    Flux<Client> getAllClients();

    Mono<Client> getClientBySharedKey(String sharedKey);

    Mono<Client> createClient(Client client);

    Flux<Client> advancedSearch(
            String sharedKey,
            String name,
            String email,
            LocalDate startDate,
            LocalDate endDate
    );

    Mono<String> exportClientsToCsv();
}
