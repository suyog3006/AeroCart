package com.aerocart.inventory.dto;

public class ReserveStockRequest {
    private String productId;
    private int quantity;

    public ReserveStockRequest() {}

    public ReserveStockRequest(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
