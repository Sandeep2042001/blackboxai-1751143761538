package com.blingbag.clone.repository;

import com.blingbag.clone.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Basic finder methods
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    // Find users by role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") String role);
    
    // Find active users
    List<User> findByIsActiveTrue();
    
    // Find users by verification status
    List<User> findByEmailVerifiedTrue();
    
    List<User> findByPhoneVerifiedTrue();
    
    // Find users who opted for marketing emails
    List<User> findByMarketingEmailsTrue();
    
    // Find users who opted for SMS notifications
    List<User> findBySmsNotificationsTrue();
    
    // Find users by creation date range
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Find users by last login date range
    List<User> findByLastLoginBetween(LocalDateTime start, LocalDateTime end);
    
    // Search users by name or email
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    
    // Find users by shipping city
    List<User> findByShippingCity(String city);
    
    // Find users by shipping state
    List<User> findByShippingState(String state);
    
    // Update user's last login
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId);
    
    // Update user's email verification status
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = :status WHERE u.id = :userId")
    void updateEmailVerificationStatus(@Param("userId") Long userId, @Param("status") boolean status);
    
    // Update user's phone verification status
    @Modifying
    @Query("UPDATE User u SET u.phoneVerified = :status WHERE u.id = :userId")
    void updatePhoneVerificationStatus(@Param("userId") Long userId, @Param("status") boolean status);
    
    // Complex user search with filters
    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR :role IN (SELECT r FROM u.roles r)) " +
           "AND (:isActive IS NULL OR u.isActive = :isActive) " +
           "AND (:emailVerified IS NULL OR u.emailVerified = :emailVerified) " +
           "AND (:phoneVerified IS NULL OR u.phoneVerified = :phoneVerified) " +
           "AND (:keyword IS NULL OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchUsersWithFilters(
            @Param("role") String role,
            @Param("isActive") Boolean isActive,
            @Param("emailVerified") Boolean emailVerified,
            @Param("phoneVerified") Boolean phoneVerified,
            @Param("keyword") String keyword,
            Pageable pageable
    );
    
    // Find users who haven't logged in since a specific date
    List<User> findByLastLoginBefore(LocalDateTime date);
    
    // Find users by multiple roles
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r IN :roles")
    List<User> findByRoles(@Param("roles") List<String> roles);
    
    // Count users by role
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
    Long countByRole(@Param("role") String role);
    
    // Count active users
    Long countByIsActiveTrue();
}
