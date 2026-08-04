package com.aerocart.order.dto;

import java.math.BigDecimal;

public class CreateOrderRequest {
    private Long userId;
    private String productId;
    private int quantity;
    private BigDecimal amount;

    public CreateOrderRequest() {}

    public CreateOrderRequest(Long userId, String productId, int quantity, BigDecimal amount) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.amount = amount;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
