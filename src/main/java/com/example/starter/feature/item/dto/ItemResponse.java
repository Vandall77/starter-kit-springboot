package com.example.starter.feature.item.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ItemResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private long stockQty;
}
