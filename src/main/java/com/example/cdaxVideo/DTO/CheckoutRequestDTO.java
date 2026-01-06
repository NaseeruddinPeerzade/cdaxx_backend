package com.example.cdaxVideo.DTO;

import java.util.ArrayList;
import java.util.List;

public class CheckoutRequestDTO {
    private List<Long> courseIds = new ArrayList<>();
    private String paymentMethod;
    private String couponCode;
    private AddressDTO billingAddress; // Optional
    
    // Constructors
    public CheckoutRequestDTO() {}
    
    public CheckoutRequestDTO(List<Long> courseIds, String paymentMethod, 
                             String couponCode, AddressDTO billingAddress) {
        this.courseIds = courseIds;
        this.paymentMethod = paymentMethod;
        this.couponCode = couponCode;
        this.billingAddress = billingAddress;
    }
    
    // Getters and Setters
    public List<Long> getCourseIds() {
        return courseIds;
    }
    
    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getCouponCode() {
        return couponCode;
    }
    
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
    
    public AddressDTO getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(AddressDTO billingAddress) {
        this.billingAddress = billingAddress;
    }
}