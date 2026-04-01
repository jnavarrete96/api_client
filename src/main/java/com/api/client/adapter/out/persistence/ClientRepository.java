package com.api.client.adapter.out.persistence;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ClientRepository extends R2dbcRepository<ClientEntity, Long> {

    Mono<ClientEntity> findByEmail(String email);

    Mono<ClientEntity> findBySharedKey(String sharedKey);
}
