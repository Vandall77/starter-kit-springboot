package com.example.starter.feature.item.controller;

import com.example.starter.common.web.ApiResponse;
import com.example.starter.feature.item.dto.ItemRequest;
import com.example.starter.feature.item.dto.ItemResponse;
import com.example.starter.feature.item.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    @PreAuthorize("hasAuthority('ITEM_READ')")
    public ApiResponse<List<ItemResponse>> getAll() {
        return ApiResponse.ok(itemService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ITEM_READ')")
    public ApiResponse<ItemResponse> getById(@PathVariable("id") UUID id) {
        return ApiResponse.ok(itemService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ITEM_CREATE')")
    public ApiResponse<ItemResponse> create(@RequestBody @Valid ItemRequest request) {
        return ApiResponse.ok(itemService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ITEM_UPDATE')")
    public ApiResponse<ItemResponse> update(
            @PathVariable("id") UUID id,
            @RequestBody @Valid ItemRequest request
    ) {
        return ApiResponse.ok(itemService.update(id, request));
    }

    /**
     * SOFT DELETE – pakai @SQLDelete (update deleted_at)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ITEM_DELETE')")
    public ApiResponse<Void> delete(@PathVariable("id") UUID id) {
        itemService.delete(id);
        return ApiResponse.ok(null);
    }

    /**
     * HARD DELETE – endpoint khusus, sebaiknya permission super ketat.
     */
    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasAuthority('ITEM_HARD_DELETE')")
    public ApiResponse<Void> hardDelete(@PathVariable("id") UUID id) {
        itemService.hardDelete(id);
        return ApiResponse.ok(null);
    }
}
