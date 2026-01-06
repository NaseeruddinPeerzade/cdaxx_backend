package com.example.cdaxVideo.DTO;

import java.util.ArrayList;
import java.util.List;

public class CheckoutResponseDTO {
    private boolean success;
    private String orderId;
    private Double totalAmount;
    private Double amountPaid;
    private String paymentStatus;
    private List<String> purchasedCourses = new ArrayList<>();
    private String message;
    
    // Constructors
    public CheckoutResponseDTO() {}
    
    public CheckoutResponseDTO(boolean success, String orderId, Double totalAmount, 
                              Double amountPaid, String paymentStatus, 
                              List<String> purchasedCourses, String message) {
        this.success = success;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.paymentStatus = paymentStatus;
        this.purchasedCourses = purchasedCourses;
        this.message = message;
    }
    
    // Success response constructor
    public static CheckoutResponseDTO success(String orderId, Double totalAmount, 
                                             List<String> purchasedCourses) {
        return new CheckoutResponseDTO(true, orderId, totalAmount, totalAmount, 
                                      "COMPLETED", purchasedCourses, "Payment successful");
    }
    
    // Failure response constructor
    public static CheckoutResponseDTO failure(String message) {
        return new CheckoutResponseDTO(false, null, 0.0, 0.0, 
                                      "FAILED", new ArrayList<>(), message);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Double getAmountPaid() {
        return amountPaid;
    }
    
    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public List<String> getPurchasedCourses() {
        return purchasedCourses;
    }
    
    public void setPurchasedCourses(List<String> purchasedCourses) {
        this.purchasedCourses = purchasedCourses;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}