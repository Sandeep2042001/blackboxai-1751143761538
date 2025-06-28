package com.blingbag.clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false)
    private String firstName;
    
    @Size(max = 50)
    private String lastName;
    
    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotBlank
    @Size(min = 6, max = 100)
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String phoneNumber;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    @Column(name = "email_verified")
    private boolean emailVerified = false;
    
    @Column(name = "phone_verified")
    private boolean phoneVerified = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Shipping Address
    @Column(name = "shipping_address_line1")
    private String shippingAddressLine1;
    
    @Column(name = "shipping_address_line2")
    private String shippingAddressLine2;
    
    @Column(name = "shipping_city")
    private String shippingCity;
    
    @Column(name = "shipping_state")
    private String shippingState;
    
    @Column(name = "shipping_postal_code")
    private String shippingPostalCode;
    
    @Column(name = "shipping_country")
    private String shippingCountry;
    
    // Billing Address (if different from shipping)
    @Column(name = "billing_address_line1")
    private String billingAddressLine1;
    
    @Column(name = "billing_address_line2")
    private String billingAddressLine2;
    
    @Column(name = "billing_city")
    private String billingCity;
    
    @Column(name = "billing_state")
    private String billingState;
    
    @Column(name = "billing_postal_code")
    private String billingPostalCode;
    
    @Column(name = "billing_country")
    private String billingCountry;
    
    // Preferences
    @Column(name = "marketing_emails")
    private boolean marketingEmails = true;
    
    @Column(name = "sms_notifications")
    private boolean smsNotifications = true;
    
    // Helper methods
    public void addRole(String role) {
        roles.add(role);
    }
    
    public void removeRole(String role) {
        roles.remove(role);
    }
    
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
    
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
    
    // Method to copy shipping address to billing
    public void copyShippingToBilling() {
        this.billingAddressLine1 = this.shippingAddressLine1;
        this.billingAddressLine2 = this.shippingAddressLine2;
        this.billingCity = this.shippingCity;
        this.billingState = this.shippingState;
        this.billingPostalCode = this.shippingPostalCode;
        this.billingCountry = this.shippingCountry;
    }
}
