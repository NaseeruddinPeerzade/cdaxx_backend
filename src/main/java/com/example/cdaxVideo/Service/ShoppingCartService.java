package com.example.cdaxVideo.Service;

import org.springframework.stereotype.Service;
import com.example.cdaxVideo.Entity.Course;
import com.example.cdaxVideo.Entity.ShoppingCartItem;
import com.example.cdaxVideo.Entity.User;
import com.example.cdaxVideo.Entity.UserCoursePurchase;
import com.example.cdaxVideo.Repository.ShoppingCartRepository;
import com.example.cdaxVideo.Repository.CourseRepository;
import com.example.cdaxVideo.Repository.UserRepository;
import com.example.cdaxVideo.Repository.UserCoursePurchaseRepository;
import com.example.cdaxVideo.DTO.*;


import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShoppingCartService {
    private final ShoppingCartRepository cartRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserCoursePurchaseRepository purchaseRepository;
    
    public ShoppingCartService(ShoppingCartRepository cartRepository,
                              CourseRepository courseRepository,
                              UserRepository userRepository,
                              UserCoursePurchaseRepository purchaseRepository) {
        this.cartRepository = cartRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
    }
    
    public CartItemDTO addToCart(Long userId, Long courseId) {
        // Check if already in cart
        if (cartRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new RuntimeException("Course already in cart");
        }
        
        // Check if user already purchased this course
        boolean alreadyPurchased = purchaseRepository.existsByUserIdAndCourseId(userId, courseId);
        if (alreadyPurchased) {
            throw new RuntimeException("You already own this course");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        ShoppingCartItem cartItem = new ShoppingCartItem();
        cartItem.setUser(user);
        cartItem.setCourse(course);
        cartItem.setaddedAt(LocalDateTime.now());
        
        ShoppingCartItem saved = cartRepository.save(cartItem);
        return mapToDTO(saved);
    }
    
    public void removeFromCart(Long userId, Long courseId) {
        cartRepository.deleteByUserIdAndCourseId(userId, courseId);
    }
    
    public void clearCart(Long userId) {
        cartRepository.deleteAllByUserId(userId);
    }
    
    public boolean isCourseInCart(Long userId, Long courseId) {
        return cartRepository.existsByUserIdAndCourseId(userId, courseId);
    }
    
    public List<CartItemDTO> getCartItems(Long userId) {
        return cartRepository.findByUserId(userId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    public CartSummaryDTO getCartSummary(Long userId) {
        List<CartItemDTO> items = getCartItems(userId);
        double total = items.stream()
            .mapToDouble(CartItemDTO::getPrice)
            .sum();
        
        CartSummaryDTO summary = new CartSummaryDTO();
        summary.setItems(items);
        summary.setItemCount(items.size());
        summary.setTotalPrice(total);
        summary.setDiscountedPrice(calculateDiscountedPrice(items));
        
        return summary;
    }
    
    public CheckoutResponseDTO checkout(Long userId, CheckoutRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<CartItemDTO> cartItems = getCartItems(userId);
        List<String> purchasedCourseTitles = new ArrayList<>();
        double totalAmount = 0.0;
        
        // Validate cart items
        if (cartItems.isEmpty()) {
            return CheckoutResponseDTO.failure("Your cart is empty");
        }
        
        // Process each course in cart
        for (CartItemDTO cartItem : cartItems) {
            // Check if course still exists
            Course course = courseRepository.findById(cartItem.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found: " + cartItem.getCourseId()));
            
            // Check if already purchased (in case of race condition)
            boolean alreadyPurchased = purchaseRepository.existsByUserIdAndCourseId(userId, cartItem.getCourseId());
            if (alreadyPurchased) {
                // Remove from cart but don't purchase again
                cartRepository.deleteByUserIdAndCourseId(userId, cartItem.getCourseId());
                continue;
            }
            
            // Create purchase record
            UserCoursePurchase purchase = new UserCoursePurchase();
            purchase.setUser(user);
            purchase.setCourse(course);
            purchase.setPurchasedOn(java.sql.Date.valueOf(LocalDateTime.now().toLocalDate()));
            
            purchaseRepository.save(purchase);
            purchasedCourseTitles.add(course.getTitle());
            totalAmount += cartItem.getPrice();
            
            // Remove from cart after purchase
            cartRepository.deleteByUserIdAndCourseId(userId, cartItem.getCourseId());
        }
        
        if (purchasedCourseTitles.isEmpty()) {
            return CheckoutResponseDTO.failure("No courses were purchased");
        }
        
        // Generate order ID
        String orderId = "ORD-" + System.currentTimeMillis() + "-" + userId;
        
        // Apply coupon discount if provided
        double finalAmount = applyCoupon(totalAmount, request.getCouponCode());
        
        // Here you would integrate with payment gateway
        // For now, we'll assume payment is successful
        boolean paymentSuccess = processPayment(userId, finalAmount, request.getPaymentMethod());
        
        if (paymentSuccess) {
            return CheckoutResponseDTO.success(orderId, finalAmount, purchasedCourseTitles);
        } else {
            return CheckoutResponseDTO.failure("Payment failed. Please try again.");
        }
    }
    
    private double calculateDiscountedPrice(List<CartItemDTO> items) {
        if (items.isEmpty()) {
            return 0.0;
        }
        
        double total = items.stream().mapToDouble(CartItemDTO::getPrice).sum();
        
        // Apply discount logic here
        // Example: 10% discount for 2+ courses
        if (items.size() >= 2) {
            return total * 0.9; // 10% discount
        }
        
        return total;
    }
    
    private double applyCoupon(double amount, String couponCode) {
        if (couponCode == null || couponCode.isEmpty()) {
            return amount;
        }
        
        // Apply coupon logic here
        // Example: 20% off with coupon "SAVE20"
        if ("SAVE20".equalsIgnoreCase(couponCode)) {
            return amount * 0.8;
        }
        
        return amount;
    }
    
    private boolean processPayment(Long userId, double amount, String paymentMethod) {
        // TODO: Integrate with actual payment gateway
        // For now, simulate successful payment
        System.out.println("Processing payment for user " + userId + 
                          ": $" + amount + " via " + paymentMethod);
        
        // Simulate payment processing
        try {
            Thread.sleep(1000); // Simulate API call delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Return true for successful payment simulation
        return true;
    }
    
    private CartItemDTO mapToDTO(ShoppingCartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setCourseId(item.getCourse().getId());
        dto.setCourseTitle(item.getCourse().getTitle());
        dto.setThumbnailUrl(item.getCourse().getThumbnailUrl());
        dto.setPrice(item.getCourse().getPrice());
        dto.setDuration(item.getCourse().getTotalDuration());
        dto.setAddedAt(item.getaddedAt());
        return dto;
    }
}