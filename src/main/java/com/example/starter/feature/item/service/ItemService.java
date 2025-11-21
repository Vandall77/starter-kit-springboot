package com.example.starter.feature.item.service;

import com.example.starter.audit.aspect.Auditable;
import com.example.starter.common.exception.ApiException;
import com.example.starter.feature.item.dto.ItemRequest;
import com.example.starter.feature.item.dto.ItemResponse;
import com.example.starter.feature.item.entity.Item;
import com.example.starter.feature.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public List<ItemResponse> findAll() {
        List<Item> entities = itemRepository.findAll();
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemResponse findById(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));
        return toResponse(item);
    }

    @Transactional
    @Auditable(action = "CREATE_ITEM")
    public ItemResponse create(ItemRequest request) {
        if (itemRepository.existsByCode(request.getCode())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Item code already exists");
        }

        Item item = new Item();
        item.setCode(request.getCode());
        item.setName(request.getName());
        item.setDescription(request.getDescription());

        // Entity: Integer, Request: long  → cast ke int dulu
        item.setStockQty((int) request.getStockQty());

        itemRepository.save(item);
        return toResponse(item);
    }

    @Transactional
    @Auditable(action = "UPDATE_ITEM")
    public ItemResponse update(UUID id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));

        if (!item.getCode().equals(request.getCode())
                && itemRepository.existsByCode(request.getCode())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Item code already exists");
        }

        item.setCode(request.getCode());
        item.setName(request.getName());
        item.setDescription(request.getDescription());

        // Sama: bridge long → Integer
        item.setStockQty((int) request.getStockQty());

        itemRepository.save(item);
        return toResponse(item);
    }

    /**
     * SOFT DELETE – pakai @SQLDelete di entity Item (update deleted_at)
     */
    @Transactional
    @Auditable(action = "DELETE_ITEM")
    public void delete(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));
        itemRepository.delete(item);
    }

    /**
     * HARD DELETE – benar-benar DELETE FROM items WHERE id = ?
     */
    @Transactional
    @Auditable(action = "HARD_DELETE_ITEM")
    public void hardDelete(UUID id) {
        if (!itemRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Item not found");
        }
        itemRepository.hardDeleteById(id);
    }

    private ItemResponse toResponse(Item item) {
        // Entity: Integer (bisa null), Response: long → handle null + convert ke long
        Integer stockQtyEntity = item.getStockQty();
        long stockQty = (stockQtyEntity != null) ? stockQtyEntity.longValue() : 0L;

        return ItemResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .name(item.getName())
                .description(item.getDescription())
                .stockQty(stockQty)
                .build();
    }
}
