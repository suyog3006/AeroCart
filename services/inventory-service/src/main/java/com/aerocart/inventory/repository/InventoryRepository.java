package com.aerocart.inventory.repository;

import com.aerocart.inventory.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    
    Optional<InventoryItem> findByProductId(String productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryItem> findWithLockByProductId(String productId);
}
