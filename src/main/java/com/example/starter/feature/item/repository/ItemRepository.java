package com.example.starter.feature.item.repository;

import com.example.starter.feature.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    boolean existsByCode(String code);

    Optional<Item> findByCode(String code);

    @Modifying
    @Query(
            value = "DELETE FROM items WHERE id = :id",
            nativeQuery = true
    )
    void hardDeleteById(@Param("id") UUID id);
}
