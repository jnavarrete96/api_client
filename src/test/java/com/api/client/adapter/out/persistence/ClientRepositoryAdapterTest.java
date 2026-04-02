package com.api.client.adapter.out.persistence;

import com.api.client.domain.model.Client;
import com.api.client.domain.model.ClientSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ActiveProfiles("test")
class ClientRepositoryAdapterTest {

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private ClientRepository clientRepository;

    private ClientRepositoryAdapter adapter;

    private ClientEntity baseEntity;

    @BeforeEach
    void setUp() {
        adapter = new ClientRepositoryAdapter(databaseClient, clientRepository);

        baseEntity = ClientEntity.builder()
                .sharedKey("ana_garcia_a1b2c3")
                .name("Ana Garcia")
                .email("ana.garcia@mail.com")
                .phone("3001234567")
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2025, 1, 15))
                .dataAdded(LocalDate.now())
                .build();

        clientRepository.deleteAll()
                .then(clientRepository.save(baseEntity))
                .block();
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return all clients")
        void shouldReturnAllClients() {
            StepVerifier.create(adapter.findAll())
                    .assertNext(client -> {
                        assertThat(client.getEmail()).isEqualTo("ana.garcia@mail.com");
                        assertThat(client.getName()).isEqualTo("Ana Garcia");
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("findBySharedKey")
    class FindBySharedKey {

        @Test
        @DisplayName("should return client when sharedKey exists")
        void shouldReturnClientWhenFound() {
            StepVerifier.create(adapter.findBySharedKey("ana_garcia_a1b2c3"))
                    .assertNext(client -> {
                        assertThat(client.getSharedKey()).isEqualTo("ana_garcia_a1b2c3");
                        assertThat(client.getEmail()).isEqualTo("ana.garcia@mail.com");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return empty when sharedKey not found")
        void shouldReturnEmptyWhenNotFound() {
            StepVerifier.create(adapter.findBySharedKey("nonexistent"))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("should return client when email exists")
        void shouldReturnClientWhenEmailFound() {
            StepVerifier.create(adapter.findByEmail("ana.garcia@mail.com"))
                    .assertNext(client ->
                            assertThat(client.getEmail()).isEqualTo("ana.garcia@mail.com"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return empty when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            StepVerifier.create(adapter.findByEmail("notfound@mail.com"))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should save and return client with id")
        void shouldSaveClientSuccessfully() {
            Client newClient = Client.builder()
                    .sharedKey("carlos_lopez_d4e5f6")
                    .name("Carlos Lopez")
                    .email("carlos.lopez@mail.com")
                    .phone("3107654321")
                    .startDate(LocalDate.of(2024, 3, 10))
                    .endDate(null)
                    .dataAdded(LocalDate.now())
                    .build();

            StepVerifier.create(adapter.save(newClient))
                    .assertNext(saved -> {
                        assertThat(saved.getId()).isNotNull();
                        assertThat(saved.getEmail()).isEqualTo("carlos.lopez@mail.com");
                        assertThat(saved.getSharedKey()).isEqualTo("carlos_lopez_d4e5f6");
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("advancedSearch")
    class AdvancedSearch {

        @BeforeEach
        void seedExtraData() {
            ClientEntity extra = ClientEntity.builder()
                    .sharedKey("carlos_lopez_d4e5f6")
                    .name("Carlos Lopez")
                    .email("carlos.lopez@mail.com")
                    .phone("3107654321")
                    .startDate(LocalDate.of(2024, 3, 10))
                    .endDate(null)
                    .dataAdded(LocalDate.now())
                    .build();

            clientRepository.save(extra).block();
        }

        @Test
        @DisplayName("should filter by name")
        void shouldFilterByName() {
            ClientSearchCriteria criteria = ClientSearchCriteria.builder()
                    .name("Ana")
                    .page(0).size(10)
                    .build();

            StepVerifier.create(adapter.advancedSearch(criteria))
                    .assertNext(client ->
                            assertThat(client.getName()).isEqualTo("Ana Garcia"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should filter by email")
        void shouldFilterByEmail() {
            ClientSearchCriteria criteria = ClientSearchCriteria.builder()
                    .email("carlos")
                    .page(0).size(10)
                    .build();

            StepVerifier.create(adapter.advancedSearch(criteria))
                    .assertNext(client ->
                            assertThat(client.getEmail()).isEqualTo("carlos.lopez@mail.com"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should filter by date range")
        void shouldFilterByDateRange() {
            ClientSearchCriteria criteria = ClientSearchCriteria.builder()
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2025, 12, 31))
                    .page(0).size(10)
                    .build();

            StepVerifier.create(adapter.advancedSearch(criteria))
                    .assertNext(client ->
                            assertThat(client.getEmail()).isEqualTo("ana.garcia@mail.com"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return all when no filters applied")
        void shouldReturnAllWhenNoFilters() {
            ClientSearchCriteria criteria = ClientSearchCriteria.builder()
                    .page(0).size(10)
                    .build();

            StepVerifier.create(adapter.advancedSearch(criteria))
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should respect pagination")
        void shouldRespectPagination() {
            ClientSearchCriteria criteria = ClientSearchCriteria.builder()
                    .page(0).size(1)
                    .build();

            StepVerifier.create(adapter.advancedSearch(criteria))
                    .expectNextCount(1)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return empty when page is out of range")
        void shouldReturnEmptyWhenPageOutOfRange() {
            ClientSearchCriteria criteria = ClientSearchCriteria.builder()
                    .page(99).size(10)
                    .build();

            StepVerifier.create(adapter.advancedSearch(criteria))
                    .verifyComplete();
        }
    }
}