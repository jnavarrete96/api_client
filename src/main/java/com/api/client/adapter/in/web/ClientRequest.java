package com.api.client.adapter.in.web;

import com.api.client.domain.model.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClientRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(
            regexp = "^[\\p{L}\\s]+$",
            message = "Name must contain only letters and spaces"
    )
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String phone;

    private LocalDate startDate;
    private LocalDate endDate;

    public static Client toDomain(ClientRequest request) {
        return Client.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }
}
