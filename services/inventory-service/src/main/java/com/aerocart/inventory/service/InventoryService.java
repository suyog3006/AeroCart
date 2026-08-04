package com.aerocart.inventory.service;

import com.aerocart.inventory.model.InventoryItem;
import com.aerocart.inventory.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final DistributedLockService lockService;

    public InventoryService(InventoryRepository inventoryRepository, DistributedLockService lockService) {
        this.inventoryRepository = inventoryRepository;
        this.lockService = lockService;
    }

    /**
     * Reserves stock guarded by Redis Distributed Lock to prevent race conditions during high-volume checkouts.
     */
    public boolean reserveStock(String productId, int quantity) {
        String lockKey = "inventory:" + productId;
        return lockService.executeWithLock(lockKey, 3000, 5000, () -> {
            InventoryItem item = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

            if (item.getAvailableQuantity() < quantity) {
                log.warn("Insufficient stock for product {}. Available: {}, Requested: {}",
                        productId, item.getAvailableQuantity(), quantity);
                return false;
            }

            item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
            item.setReservedQuantity(item.getReservedQuantity() + quantity);
            inventoryRepository.save(item);

            // Sync cache
            lockService.setStockInCache(productId, item.getAvailableQuantity());
            log.info("Successfully reserved {} units for product {}. Remaining: {}", quantity, productId, item.getAvailableQuantity());
            return true;
        });
    }

    /**
     * Compensating transaction to release reserved stock if Saga fails
     */
    public void releaseStock(String productId, int quantity) {
        String lockKey = "inventory:" + productId;
        lockService.executeWithLock(lockKey, 3000, 5000, () -> {
            InventoryItem item = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

            item.setAvailableQuantity(item.getAvailableQuantity() + quantity);
            item.setReservedQuantity(Math.max(0, item.getReservedQuantity() - quantity));
            inventoryRepository.save(item);

            // Sync cache
            lockService.setStockInCache(productId, item.getAvailableQuantity());
            log.info("Released {} units for product {}. Current Available: {}", quantity, productId, item.getAvailableQuantity());
            return null;
        });
    }

    @Transactional
    public InventoryItem createOrUpdateInventory(String productId, int quantity) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElse(new InventoryItem(productId, 0));
        item.setAvailableQuantity(quantity);
        InventoryItem saved = inventoryRepository.save(item);
        lockService.setStockInCache(productId, quantity);
        return saved;
    }

    public InventoryItem getInventory(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
    }
}
