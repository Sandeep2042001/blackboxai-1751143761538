package com.blingbag.clone.service;

import com.blingbag.clone.model.User;
import com.blingbag.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Create operations
    public User createUser(User user) {
        validateNewUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.addRole("ROLE_USER"); // Default role
        return userRepository.save(user);
    }

    // Read operations
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    // Update operations
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        validateUserUpdate(user, userDetails);
        updateUserFields(user, userDetails);
        return userRepository.save(user);
    }

    // Delete operations
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    // Deactivate user
    public void deactivateUser(Long id) {
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    // Authentication related
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void resetPassword(String email, String newPassword) {
        User user = findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Role management
    public void addRole(Long userId, String role) {
        User user = getUserById(userId);
        user.addRole(role);
        userRepository.save(user);
    }

    public void removeRole(Long userId, String role) {
        User user = getUserById(userId);
        user.removeRole(role);
        userRepository.save(user);
    }

    // Verification
    public void verifyEmail(Long userId) {
        userRepository.updateEmailVerificationStatus(userId, true);
    }

    public void verifyPhone(Long userId) {
        userRepository.updatePhoneVerificationStatus(userId, true);
    }

    // Search operations
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable);
    }

    // Advanced search
    public Page<User> searchUsersWithFilters(
            String role,
            Boolean isActive,
            Boolean emailVerified,
            Boolean phoneVerified,
            String keyword,
            Pageable pageable) {
        return userRepository.searchUsersWithFilters(
                role, isActive, emailVerified, phoneVerified, keyword, pageable);
    }

    // Address management
    public void updateShippingAddress(Long userId, 
            String addressLine1, 
            String addressLine2, 
            String city, 
            String state, 
            String postalCode, 
            String country) {
        User user = getUserById(userId);
        user.setShippingAddressLine1(addressLine1);
        user.setShippingAddressLine2(addressLine2);
        user.setShippingCity(city);
        user.setShippingState(state);
        user.setShippingPostalCode(postalCode);
        user.setShippingCountry(country);
        userRepository.save(user);
    }

    public void copyShippingToBilling(Long userId) {
        User user = getUserById(userId);
        user.copyShippingToBilling();
        userRepository.save(user);
    }

    // Preference management
    public void updateMarketingPreferences(Long userId, boolean marketingEmails, boolean smsNotifications) {
        User user = getUserById(userId);
        user.setMarketingEmails(marketingEmails);
        user.setSmsNotifications(smsNotifications);
        userRepository.save(user);
    }

    // Validation methods
    private void validateNewUser(User user) {
        if (!StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (StringUtils.hasText(user.getPhoneNumber()) && 
            userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered");
        }
    }

    private void validateUserUpdate(User existingUser, User updatedUser) {
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) && 
            userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (!existingUser.getPhoneNumber().equals(updatedUser.getPhoneNumber()) && 
            userRepository.existsByPhoneNumber(updatedUser.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered");
        }
    }

    // Helper methods
    private void updateUserFields(User existingUser, User updatedUser) {
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        // Don't update password here - use separate password update method
        existingUser.setActive(updatedUser.isActive());
        existingUser.setRoles(updatedUser.getRoles());
    }

    // Login tracking
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId);
    }

    // Bulk operations
    public List<User> createUsers(List<User> users) {
        users.forEach(this::validateNewUser);
        users.forEach(user -> user.setPassword(passwordEncoder.encode(user.getPassword())));
        return userRepository.saveAll(users);
    }

    public void deactivateUsers(Set<Long> userIds) {
        userIds.forEach(this::deactivateUser);
    }
}
