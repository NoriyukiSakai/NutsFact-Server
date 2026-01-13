package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.domain.model.user.User;
import com.nines.nutsfact.domain.repository.UserRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findByBusinessAccountId(Integer businessAccountId) {
        return userRepository.findByBusinessAccountId(businessAccountId);
    }

    public User findById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));
    }

    public User findByAuthUserId(String authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User with authUserId " + authUserId + " not found"));
    }

    @Transactional
    public User create(User user) {
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        userRepository.save(user);
        return user;
    }

    @Transactional
    public User update(Integer userId, User user) {
        findById(userId);
        user.setUserId(userId);
        userRepository.save(user);
        return user;
    }

    @Transactional
    public void updateLastSignInAt(Integer userId) {
        userRepository.updateLastSignInAt(userId);
    }

    @Transactional
    public void delete(Integer userId) {
        findById(userId);
        userRepository.delete(userId);
    }

    @Transactional
    public void deactivate(Integer userId) {
        User user = findById(userId);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activate(Integer userId) {
        User user = findById(userId);
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Transactional
    public User changeRole(Integer userId, Integer role) {
        User user = findById(userId);
        user.setRole(role);
        userRepository.save(user);
        return user;
    }

    public int countByBusinessAccountId(Integer businessAccountId) {
        return userRepository.countByBusinessAccountId(businessAccountId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
