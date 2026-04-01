package com.api.client.domain.service;

import com.api.client.domain.exception.DuplicateClientException;
import com.api.client.domain.exception.InvalidDateRangeException;
import com.api.client.domain.model.Client;
import com.api.client.domain.model.ClientSearchCriteria;
import com.api.client.domain.port.in.ClientUseCase;
import com.api.client.domain.port.out.ClientRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ClientService implements ClientUseCase {

    private final ClientRepositoryPort repository;

    @Override
    public Flux<Client> getAllClients() {
        log.info("Fetching all clients");
        return repository.findAll();
    }

    @Override
    public Mono<Client> getClientBySharedKey(String sharedKey) {
        log.info("Searching client by sharedKey: {}", sharedKey);
        return repository.findBySharedKey(sharedKey);
    }

    @Override
    public Mono<Client> createClient(Client client) {
        log.info("Creating client with email: {}", client.getEmail());

        if (client.getEndDate() != null && client.getEndDate().isBefore(client.getStartDate())) {
            return Mono.error(new InvalidDateRangeException());
        }

        return repository.findByEmail(client.getEmail())
                .flatMap(existing -> {
                    log.warn("Client already exists with email: {}", client.getEmail());
                    return Mono.<Client>error(new DuplicateClientException(client.getEmail()));
                })
                .switchIfEmpty(
                        Mono.defer(() -> repository.save(buildClient(client)))
                );
    }

    @Override
    public Flux<Client> advancedSearch(ClientSearchCriteria criteria) {
        log.info("Performing advanced search");
        return repository.advancedSearch(criteria);
    }

    @Override
    public Mono<String> exportClientsToCsv() {
        log.info("Exporting clients to CSV");
        return repository.findAll()
                .map(client -> String.join(",",
                        client.getSharedKey(),
                        client.getName(),
                        client.getEmail(),
                        client.getPhone(),
                        client.getDataAdded().toString()
                ))
                .collectList()
                .map(lines -> {
                    String header = "SharedKey,Name,Email,Phone,DateAdded";
                    return header + "\n" + String.join("\n", lines);
                });
    }

    private Client buildClient(Client client) {

        String generatedSharedKey = generateSharedKey(client.getName());

        return Client.builder()
                .id(null)
                .sharedKey(generatedSharedKey)
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .startDate(client.getStartDate())
                .endDate(client.getEndDate())
                .dataAdded(LocalDate.now())
                .build();
    }

    private String generateSharedKey(String name) {
        return name.toLowerCase().replace(" ", "_") + "_" + UUID.randomUUID().toString().substring(0, 6);
    }

}
