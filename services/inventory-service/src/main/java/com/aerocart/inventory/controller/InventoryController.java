package com.aerocart.inventory.controller;

import com.aerocart.inventory.dto.ReserveStockRequest;
import com.aerocart.inventory.model.InventoryItem;
import com.aerocart.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> reserveStock(@RequestBody ReserveStockRequest request) {
        boolean success = inventoryService.reserveStock(request.getProductId(), request.getQuantity());
        if (success) {
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Stock reserved successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("status", "FAILED", "message", "Insufficient stock available"));
        }
    }

    @PostMapping("/release")
    public ResponseEntity<?> releaseStock(@RequestBody ReserveStockRequest request) {
        inventoryService.releaseStock(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Stock released successfully"));
    }

    @PostMapping("/items")
    public ResponseEntity<InventoryItem> createOrUpdate(@RequestParam String productId, @RequestParam int quantity) {
        return ResponseEntity.ok(inventoryService.createOrUpdateInventory(productId, quantity));
    }

    @GetMapping("/items/{productId}")
    public ResponseEntity<InventoryItem> getInventory(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }
}
