package com.api.client.adapter.in.web;

import com.api.client.domain.exception.DuplicateClientException;
import com.api.client.domain.exception.InvalidDateRangeException;
import com.api.client.domain.model.Client;
import com.api.client.domain.model.ClientSearchCriteria;
import com.api.client.domain.port.in.ClientUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(ClientController.class)
@Import(GlobalExceptionHandler.class)
class ClientControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ClientUseCase clientUseCase;

    private Client baseClient;

    @BeforeEach
    void setUp() {
        baseClient = Client.builder()
                .id(1L)
                .sharedKey("ana_garcia_a1b2c3")
                .name("Ana Garcia")
                .email("ana.garcia@mail.com")
                .phone("3001234567")
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2025, 1, 15))
                .dataAdded(LocalDate.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/clients")
    class GetAllClients {

        @Test
        @DisplayName("should return 200 with list of clients")
        void shouldReturn200WithClients() {
            when(clientUseCase.getAllClients())
                    .thenReturn(reactor.core.publisher.Flux.just(baseClient));

            webTestClient.get()
                    .uri("/api/clients")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(200)
                    .jsonPath("$.count").isEqualTo(1)
                    .jsonPath("$.data[0].email").isEqualTo("ana.garcia@mail.com")
                    .jsonPath("$.data[0].sharedKey").isEqualTo("ana_garcia_a1b2c3");
        }

        @Test
        @DisplayName("should return 200 with empty list when no clients")
        void shouldReturn200WithEmptyList() {
            when(clientUseCase.getAllClients())
                    .thenReturn(reactor.core.publisher.Flux.empty());

            webTestClient.get()
                    .uri("/api/clients")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(200)
                    .jsonPath("$.count").isEqualTo(0)
                    .jsonPath("$.data").isArray();
        }
    }

    @Nested
    @DisplayName("GET /api/clients/{sharedKey}")
    class GetBySharedKey {

        @Test
        @DisplayName("should return 200 when client found")
        void shouldReturn200WhenFound() {
            when(clientUseCase.getClientBySharedKey("ana_garcia_a1b2c3"))
                    .thenReturn(reactor.core.publisher.Mono.just(baseClient));

            webTestClient.get()
                    .uri("/api/clients/ana_garcia_a1b2c3")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(200)
                    .jsonPath("$.data[0].email").isEqualTo("ana.garcia@mail.com");
        }

        @Test
        @DisplayName("should return 200 with empty when client not found")
        void shouldReturn200WhenNotFound() {
            when(clientUseCase.getClientBySharedKey("nonexistent"))
                    .thenReturn(reactor.core.publisher.Mono.empty());

            webTestClient.get()
                    .uri("/api/clients/nonexistent")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.data").doesNotExist();
        }
    }

    @Nested
    @DisplayName("POST /api/clients")
    class CreateClient {

        private final String validRequestBody = """
                {
                    "name": "Carlos Lopez",
                    "email": "carlos.lopez@mail.com",
                    "phone": "3107654321",
                    "startDate": "2024-03-10",
                    "endDate": "2025-03-10"
                }
                """;

        @Test
        @DisplayName("should return 201 when client created successfully")
        void shouldReturn201WhenCreated() {
            when(clientUseCase.createClient(any(Client.class)))
                    .thenReturn(reactor.core.publisher.Mono.just(baseClient));

            webTestClient.post()
                    .uri("/api/clients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(validRequestBody)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(200)
                    .jsonPath("$.message").isEqualTo("Client created successfully")
                    .jsonPath("$.data[0].email").isEqualTo("ana.garcia@mail.com");
        }

        @Test
        @DisplayName("should return 409 when email already exists")
        void shouldReturn409WhenDuplicateEmail() {
            when(clientUseCase.createClient(any(Client.class)))
                    .thenReturn(reactor.core.publisher.Mono.error(
                            new DuplicateClientException("carlos.lopez@mail.com")));

            webTestClient.post()
                    .uri("/api/clients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(validRequestBody)
                    .exchange()
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(409)
                    .jsonPath("$.message").isEqualTo("Client with email already exists: carlos.lopez@mail.com");
        }

        @Test
        @DisplayName("should return 400 when endDate is before startDate")
        void shouldReturn400WhenInvalidDateRange() {
            when(clientUseCase.createClient(any(Client.class)))
                    .thenReturn(reactor.core.publisher.Mono.error(
                            new InvalidDateRangeException()));

            webTestClient.post()
                    .uri("/api/clients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(validRequestBody)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(400)
                    .jsonPath("$.message").isEqualTo("End date cannot be before start date");
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() {
            String invalidBody = """
                    {
                        "name": "",
                        "email": "test@mail.com",
                        "startDate": "2024-01-01"
                    }
                    """;

            webTestClient.post()
                    .uri("/api/clients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(invalidBody)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(400);
        }

        @Test
        @DisplayName("should return 400 when email format is invalid")
        void shouldReturn400WhenEmailInvalid() {
            String invalidBody = """
                    {
                        "name": "Test User",
                        "email": "not-an-email",
                        "startDate": "2024-01-01"
                    }
                    """;

            webTestClient.post()
                    .uri("/api/clients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(invalidBody)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("POST /api/clients/search")
    class AdvancedSearch {

        @Test
        @DisplayName("should return 200 with matching clients")
        void shouldReturn200WithResults() {
            when(clientUseCase.advancedSearch(any(ClientSearchCriteria.class)))
                    .thenReturn(reactor.core.publisher.Flux.just(baseClient));

            webTestClient.post()
                    .uri("/api/clients/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "name": "Ana",
                                "page": 0,
                                "size": 10
                            }
                            """)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo(200)
                    .jsonPath("$.count").isEqualTo(1)
                    .jsonPath("$.data[0].name").isEqualTo("Ana Garcia");
        }

        @Test
        @DisplayName("should return 200 with empty list when no matches")
        void shouldReturn200WithEmptyResults() {
            when(clientUseCase.advancedSearch(any(ClientSearchCriteria.class)))
                    .thenReturn(reactor.core.publisher.Flux.empty());

            webTestClient.post()
                    .uri("/api/clients/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "name": "Nonexistent",
                                "page": 0,
                                "size": 10
                            }
                            """)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.count").isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("GET /api/clients/export")
    class ExportCsv {

        @Test
        @DisplayName("should return 200 with CSV content")
        void shouldReturn200WithCsv() {
            String csv = "SharedKey,BusinessId,Email,Phone,StartDate,EndDate,DateAdded\n" +
                    "ana_garcia_a1b2c3,Ana Garcia,ana.garcia@mail.com,3001234567,2024-01-15,2025-01-15,2026-04-01";

            when(clientUseCase.exportClientsToCsv())
                    .thenReturn(reactor.core.publisher.Mono.just(csv));

            webTestClient.get()
                    .uri("/api/clients/export")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                    .expectBody(String.class)
                    .isEqualTo(csv);
        }
    }
}