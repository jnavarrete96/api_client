package com.api.client.domain.port.in;

import com.api.client.domain.model.Client;
import com.api.client.domain.model.ClientSearchCriteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClientUseCase {

    Flux<Client> getAllClients();

    Mono<Client> getClientBySharedKey(String sharedKey);

    Mono<Client> createClient(Client client);

    Flux<Client> advancedSearch(ClientSearchCriteria criteria);

    Mono<String> exportClientsToCsv();
}
