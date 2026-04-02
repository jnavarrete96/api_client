package com.api.client.domain.service;

import com.api.client.domain.exception.DuplicateClientException;
import com.api.client.domain.exception.InvalidDateRangeException;
import com.api.client.domain.model.Client;
import com.api.client.domain.model.ClientSearchCriteria;
import com.api.client.domain.port.out.ClientRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepositoryPort repository;

    @InjectMocks
    private ClientService clientService;

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
    @DisplayName("getAllClients")
    class GetAllClients {

        @Test
        @DisplayName("should return all clients successfully")
        void shouldReturnAllClients() {
            when(repository.findAll()).thenReturn(Flux.just(baseClient));

            StepVerifier.create(clientService.getAllClients())
                    .expectNext(baseClient)
                    .verifyComplete();

            verify(repository).findAll();
        }

        @Test
        @DisplayName("should return empty flux when no clients exist")
        void shouldReturnEmptyWhenNoClients() {
            when(repository.findAll()).thenReturn(Flux.empty());

            StepVerifier.create(clientService.getAllClients())
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("getClientBySharedKey")
    class GetClientBySharedKey {

        @Test
        @DisplayName("should return client when sharedKey exists")
        void shouldReturnClientWhenFound() {
            when(repository.findBySharedKey("ana_garcia_a1b2c3")).thenReturn(Mono.just(baseClient));

            StepVerifier.create(clientService.getClientBySharedKey("ana_garcia_a1b2c3"))
                    .expectNext(baseClient)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return empty when sharedKey does not exist")
        void shouldReturnEmptyWhenNotFound() {
            when(repository.findBySharedKey("nonexistent")).thenReturn(Mono.empty());

            StepVerifier.create(clientService.getClientBySharedKey("nonexistent"))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("createClient")
    class CreateClient {

        private Client newClient;

        @BeforeEach
        void setUp() {
            newClient = Client.builder()
                    .name("Carlos Lopez")
                    .email("carlos.lopez@mail.com")
                    .phone("3107654321")
                    .startDate(LocalDate.of(2024, 3, 10))
                    .endDate(null)
                    .build();
        }

        @Test
        @DisplayName("should create client successfully when email is not duplicate")
        void shouldCreateClientSuccessfully() {
            Client savedClient = Client.builder()
                    .id(2L)
                    .sharedKey("carlos_lopez_d4e5f6")
                    .name(newClient.getName())
                    .email(newClient.getEmail())
                    .phone(newClient.getPhone())
                    .startDate(newClient.getStartDate())
                    .endDate(newClient.getEndDate())
                    .dataAdded(LocalDate.now())
                    .build();

            when(repository.findByEmail(newClient.getEmail())).thenReturn(Mono.empty());
            when(repository.save(any(Client.class))).thenReturn(Mono.just(savedClient));

            StepVerifier.create(clientService.createClient(newClient))
                    .assertNext(result -> {
                        assertThat(result.getEmail()).isEqualTo(newClient.getEmail());
                        assertThat(result.getName()).isEqualTo(newClient.getName());
                        assertThat(result.getSharedKey()).isNotBlank();
                        assertThat(result.getDataAdded()).isNotNull();
                    })
                    .verifyComplete();

            verify(repository).findByEmail(newClient.getEmail());
            verify(repository).save(any(Client.class));
        }

        @Test
        @DisplayName("should throw DuplicateClientException when email already exists")
        void shouldThrowDuplicateExceptionWhenEmailExists() {
            when(repository.findByEmail(baseClient.getEmail())).thenReturn(Mono.just(baseClient));

            Client duplicateClient = Client.builder()
                    .name("Ana Otra")
                    .email(baseClient.getEmail())
                    .startDate(LocalDate.of(2024, 1, 1))
                    .build();

            StepVerifier.create(clientService.createClient(duplicateClient))
                    .expectError(DuplicateClientException.class)
                    .verify();

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidDateRangeException when endDate is before startDate")
        void shouldThrowInvalidDateRangeWhenEndBeforeStart() {
            Client invalidClient = Client.builder()
                    .name("Test User")
                    .email("test@mail.com")
                    .startDate(LocalDate.of(2024, 6, 1))
                    .endDate(LocalDate.of(2024, 1, 1))
                    .build();

            StepVerifier.create(clientService.createClient(invalidClient))
                    .expectError(InvalidDateRangeException.class)
                    .verify();

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("should create client successfully when endDate is null")
        void shouldCreateClientWhenEndDateIsNull() {
            Client savedClient = newClient.toBuilder().id(3L).sharedKey("carlos_lopez_abc123").dataAdded(LocalDate.now()).build();

            when(repository.findByEmail(newClient.getEmail())).thenReturn(Mono.empty());
            when(repository.save(any(Client.class))).thenReturn(Mono.just(savedClient));

            StepVerifier.create(clientService.createClient(newClient))
                    .assertNext(result -> assertThat(result.getEndDate()).isNull())
                    .verifyComplete();
        }

        @Test
        @DisplayName("should create client successfully when endDate equals startDate")
        void shouldCreateClientWhenEndDateEqualsStartDate() {
            LocalDate sameDate = LocalDate.of(2024, 6, 1);
            Client sameDateClient = Client.builder()
                    .name("Test User")
                    .email("test.same@mail.com")
                    .startDate(sameDate)
                    .endDate(sameDate)
                    .build();

            Client savedClient = sameDateClient.toBuilder().id(4L).sharedKey("test_user_xyz").dataAdded(LocalDate.now()).build();

            when(repository.findByEmail(sameDateClient.getEmail())).thenReturn(Mono.empty());
            when(repository.save(any(Client.class))).thenReturn(Mono.just(savedClient));

            StepVerifier.create(clientService.createClient(sameDateClient))
                    .assertNext(result -> {
                        assertThat(result.getStartDate()).isEqualTo(result.getEndDate());
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("advancedSearch")
    class AdvancedSearch {

        @Test
        @DisplayName("should return matching clients for given criteria")
        void shouldReturnMatchingClients() {
            ClientSearchCriteria criteria = ClientSearchCriteria.builder()
                    .name("Ana")
                    .page(0)
                    .size(10)
                    .build();

            when(repository.advancedSearch(criteria)).thenReturn(Flux.just(baseClient));

            StepVerifier.create(clientService.advancedSearch(criteria))
                    .expectNext(baseClient)
                    .verifyComplete();

            verify(repository).advancedSearch(criteria);
        }

        @Test
        @DisplayName("should return empty when no clients match criteria")
        void shouldReturnEmptyWhenNoMatch() {
            ClientSearchCriteria criteria = ClientSearchCriteria.builder()
                    .name("Nonexistent")
                    .page(0)
                    .size(10)
                    .build();

            when(repository.advancedSearch(criteria)).thenReturn(Flux.empty());

            StepVerifier.create(clientService.advancedSearch(criteria))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("exportClientsToCsv")
    class ExportClientsToCsv {

        @Test
        @DisplayName("should export clients to CSV with header")
        void shouldExportWithHeader() {
            when(repository.findAll()).thenReturn(Flux.just(baseClient));

            StepVerifier.create(clientService.exportClientsToCsv())
                    .assertNext(csv -> {
                        assertThat(csv).startsWith("SharedKey,BusinessId,Email,Phone,StartDate,EndDate,DateAdded");
                        assertThat(csv).contains(baseClient.getSharedKey());
                        assertThat(csv).contains(baseClient.getEmail());
                        assertThat(csv).contains(baseClient.getStartDate().toString());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should export only header when no clients exist")
        void shouldExportOnlyHeaderWhenEmpty() {
            when(repository.findAll()).thenReturn(Flux.empty());

            StepVerifier.create(clientService.exportClientsToCsv())
                    .assertNext(csv -> {
                        assertThat(csv).isEqualTo("SharedKey,BusinessId,Email,Phone,StartDate,EndDate,DateAdded");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should handle null phone and endDate in CSV export")
        void shouldHandleNullFieldsInCsv() {
            Client clientWithNulls = Client.builder()
                    .sharedKey("juan_perez_j1k2l3")
                    .name("Juan Perez")
                    .email("juan.perez@mail.com")
                    .phone(null)
                    .startDate(LocalDate.of(2023, 11, 20))
                    .endDate(null)
                    .dataAdded(LocalDate.now())
                    .build();

            when(repository.findAll()).thenReturn(Flux.just(clientWithNulls));

            StepVerifier.create(clientService.exportClientsToCsv())
                    .assertNext(csv -> {
                        assertThat(csv).contains("juan.perez@mail.com");
                        assertThat(csv).doesNotContain("null");
                    })
                    .verifyComplete();
        }
    }
}
