package com.api.client.adapter.in.web;

import com.api.client.domain.port.in.ClientUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientUseCase clientUseCase;

    @GetMapping
    public Mono<ApiResponse<ClientResponse>> getAllClients() {
        log.info("GET /api/clients");
        return clientUseCase.getAllClients()
                .map(ClientResponse::fromDomain)
                .collectList()
                .map(list -> ApiResponse.ok(list, "Clients retrieved successfully"));
    }

    @GetMapping("/{sharedKey}")
    public Mono<ApiResponse<ClientResponse>> getBySharedKey(@PathVariable String sharedKey) {
        log.info("GET /api/clients/{}", sharedKey);
        return clientUseCase.getClientBySharedKey(sharedKey)
                .map(ClientResponse::fromDomain)
                .map(response -> ApiResponse.ok(List.of(response), "Client found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiResponse<ClientResponse>> createClient(@Valid @RequestBody ClientRequest request) {
        log.info("POST /api/clients");
        return clientUseCase.createClient(ClientRequest.toDomain(request))
                .map(ClientResponse::fromDomain)
                .map(response -> ApiResponse.ok(List.of(response), "Client created successfully"));
    }

    @PostMapping("/search")
    public Mono<ApiResponse<ClientResponse>> advancedSearch(
            @RequestBody ClientSearchCriteriaRequest request) {
        log.info("POST /api/clients/search");
        return clientUseCase.advancedSearch(request.toDomain())
                .map(ClientResponse::fromDomain)
                .collectList()
                .map(list -> ApiResponse.ok(list, "Search completed successfully"));
    }

    @GetMapping(value = "/export", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> exportCsv() {
        log.info("GET /api/clients/export/csv");
        return clientUseCase.exportClientsToCsv();
    }
}
