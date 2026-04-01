package com.api.client.config;

import com.api.client.adapter.out.persistence.ClientEntity;
import com.api.client.adapter.out.persistence.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final ClientRepository clientRepository;

    @Override
    public void run(ApplicationArguments args) {
        clientRepository.deleteAll()
                .thenMany(clientRepository.saveAll(buildClients()))
                .doOnNext(c -> log.info("Seeded client: {}", c.getEmail()))
                .doOnComplete(() -> log.info("Data seeding completed"))
                .subscribe();
    }

    private List<ClientEntity> buildClients() {
        return List.of(
                ClientEntity.builder()
                        .sharedKey("ana_garcia_a1b2c3")
                        .name("Ana Garcia")
                        .email("ana.garcia@mail.com")
                        .phone("3001234567")
                        .startDate(LocalDate.of(2024, 1, 15))
                        .endDate(LocalDate.of(2025, 1, 15))
                        .dataAdded(LocalDate.now())
                        .build(),
                ClientEntity.builder()
                        .sharedKey("carlos_lopez_d4e5f6")
                        .name("Carlos Lopez")
                        .email("carlos.lopez@mail.com")
                        .phone("3107654321")
                        .startDate(LocalDate.of(2024, 3, 10))
                        .endDate(null)
                        .dataAdded(LocalDate.now())
                        .build(),
                ClientEntity.builder()
                        .sharedKey("maria_torres_g7h8i9")
                        .name("Maria Torres")
                        .email("maria.torres@mail.com")
                        .phone("3209876543")
                        .startDate(LocalDate.of(2024, 6, 1))
                        .endDate(LocalDate.of(2026, 6, 1))
                        .dataAdded(LocalDate.now())
                        .build(),
                ClientEntity.builder()
                        .sharedKey("juan_perez_j1k2l3")
                        .name("Juan Perez")
                        .email("juan.perez@mail.com")
                        .phone("3154321098")
                        .startDate(LocalDate.of(2023, 11, 20))
                        .endDate(null)
                        .dataAdded(LocalDate.now())
                        .build(),
                ClientEntity.builder()
                        .sharedKey("laura_martinez_m4n5o6")
                        .name("Laura Martinez")
                        .email("laura.martinez@mail.com")
                        .phone("3001112233")
                        .startDate(LocalDate.of(2024, 9, 5))
                        .endDate(LocalDate.of(2025, 9, 5))
                        .dataAdded(LocalDate.now())
                        .build()
        );
    }
}

