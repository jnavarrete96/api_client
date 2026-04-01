package com.api.client.config;

import com.api.client.domain.port.in.ClientUseCase;
import com.api.client.domain.port.out.ClientRepositoryPort;
import com.api.client.domain.service.ClientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Bean
    public ClientUseCase clientUseCase(ClientRepositoryPort repositoryPort) {
        return new ClientService(repositoryPort);
    }
}
