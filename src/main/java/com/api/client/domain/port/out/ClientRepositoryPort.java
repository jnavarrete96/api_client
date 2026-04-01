package com.api.client.domain.port.out;

import com.api.client.domain.model.Client;
import com.api.client.domain.model.ClientSearchCriteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClientRepositoryPort {

    Flux<Client> findAll();

    Mono<Client> findBySharedKey(String sharedKey);

    Mono<Client> findByEmail(String email);

    Mono<Client> save(Client client);

    Flux<Client> advancedSearch(ClientSearchCriteria criteria);
}
