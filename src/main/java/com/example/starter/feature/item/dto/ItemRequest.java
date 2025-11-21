package com.example.starter.feature.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    private long stockQty;
}
