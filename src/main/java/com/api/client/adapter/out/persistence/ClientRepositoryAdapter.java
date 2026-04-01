package com.api.client.adapter.out.persistence;

import com.api.client.domain.model.Client;
import com.api.client.domain.model.ClientSearchCriteria;
import com.api.client.domain.port.out.ClientRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientRepositoryAdapter implements ClientRepositoryPort {

    private final DatabaseClient databaseClient;
    private final ClientRepository clientRepository;

    @Override
    public Flux<Client> findAll() {
        log.debug("Fetching all clients from database");
        return clientRepository.findAll()
                .map(this::toDomain);
    }

    @Override
    public Mono<Client> findBySharedKey(String sharedKey) {
        log.debug("Searching client by sharedKey: {}", sharedKey);
        return clientRepository.findBySharedKey(sharedKey)
                .map(this::toDomain);
    }

    @Override
    public Mono<Client> findByEmail(String email) {
        log.debug("Searching client by email: {}", email);
        return clientRepository.findByEmail(email)
                .map(this::toDomain);
    }

    @Override
    public Mono<Client> save(Client client) {
        log.debug("Saving client with email: {}", client.getEmail());
        return clientRepository.save(toEntity(client))
                .map(this::toDomain);
    }

    @Override
    public Flux<Client> advancedSearch(ClientSearchCriteria criteria) {
        log.debug("Executing advanced search with criteria: {}", criteria);

        DynamicQuery dq = new DynamicQuery("SELECT * FROM client WHERE 1=1");
        dq.addLikeFilter("shared_key", "sharedKey", criteria.getSharedKey());
        dq.addLikeFilter("name",       "name",      criteria.getName());
        dq.addLikeFilter("email",      "email",     criteria.getEmail());
        dq.addRangeFilter("start_date", "startDate", criteria.getStartDate(), false);
        dq.addRangeFilter("end_date",   "endDate",   criteria.getEndDate(),   true);

        return dq.apply(databaseClient, criteria.getLimit(), criteria.getOffset())
                .map((row, metadata) -> ClientEntity.builder()
                        .id(row.get("id", Long.class))
                        .sharedKey(row.get("shared_key", String.class))
                        .name(row.get("name", String.class))
                        .email(row.get("email", String.class))
                        .phone(row.get("phone", String.class))
                        .startDate(row.get("start_date", java.time.LocalDate.class))
                        .endDate(row.get("end_date", java.time.LocalDate.class))
                        .dataAdded(row.get("data_added", java.time.LocalDate.class))
                        .build())
                .all()
                .map(this::toDomain);
    }

    private Client toDomain(ClientEntity entity) {
        return Client.builder()
                .id(entity.getId())
                .sharedKey(entity.getSharedKey())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .dataAdded(entity.getDataAdded())
                .build();
    }

    private ClientEntity toEntity(Client client) {
        return ClientEntity.builder()
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
