package com.example.cdaxVideo.DTO;

import java.util.ArrayList;
import java.util.List;

public class CartSummaryDTO {
    private List<CartItemDTO> items = new ArrayList<>();
    private Integer itemCount;
    private Double totalPrice;
    private Double discountedPrice;
    private Double discountAmount;
    
    // Constructors
    public CartSummaryDTO() {}
    
    public CartSummaryDTO(List<CartItemDTO> items, Integer itemCount, Double totalPrice, 
                         Double discountedPrice, Double discountAmount) {
        this.items = items;
        this.itemCount = itemCount;
        this.totalPrice = totalPrice;
        this.discountedPrice = discountedPrice;
        this.discountAmount = discountAmount;
    }
    
    // Getters and Setters
    public List<CartItemDTO> getItems() {
        return items;
    }
    
    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }
    
    public Integer getItemCount() {
        return itemCount;
    }
    
    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
        if (this.totalPrice != null) {
            calculateDiscounts();
        }
    }
    
    public Double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
        if (this.itemCount != null) {
            calculateDiscounts();
        }
    }
    
    public Double getDiscountedPrice() {
        return discountedPrice;
    }
    
    public void setDiscountedPrice(Double discountedPrice) {
        this.discountedPrice = discountedPrice;
        if (this.totalPrice != null) {
            this.discountAmount = this.totalPrice - this.discountedPrice;
        }
    }
    
    public Double getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    // Helper method to calculate discounts
    private void calculateDiscounts() {
        if (totalPrice == null) return;
        
        // Apply discount based on item count
        if (itemCount >= 2) {
            // 10% discount for 2+ items
            this.discountedPrice = totalPrice * 0.9;
            this.discountAmount = totalPrice - discountedPrice;
        } else {
            this.discountedPrice = totalPrice;
            this.discountAmount = 0.0;
        }
    }
}