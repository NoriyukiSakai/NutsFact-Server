package com.nines.nutsfact.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.user.User;
import com.nines.nutsfact.infrastructure.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final UserMapper userMapper;

    public List<User> findAll() {
        return userMapper.findAll();
    }

    public List<User> findByBusinessAccountId(Integer businessAccountId) {
        return userMapper.findByBusinessAccountId(businessAccountId);
    }

    public Optional<User> findById(Integer userId) {
        return Optional.ofNullable(userMapper.findById(userId));
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userMapper.findByEmail(email));
    }

    public Optional<User> findByAuthUserId(String authUserId) {
        return Optional.ofNullable(userMapper.findByAuthUserId(authUserId));
    }

    public void save(User user) {
        if (user.getUserId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.update(user);
        }
    }

    public void updateLastSignInAt(Integer userId) {
        userMapper.updateLastSignInAt(userId);
    }

    public void delete(Integer userId) {
        userMapper.delete(userId);
    }

    public int countByBusinessAccountId(Integer businessAccountId) {
        return userMapper.countByBusinessAccountId(businessAccountId);
    }

    public void incrementLoginFailureCount(Integer userId) {
        userMapper.incrementLoginFailureCount(userId);
    }

    public void lockUser(Integer userId, LocalDateTime lockedUntil) {
        userMapper.lockUser(userId, lockedUntil);
    }

    public void resetLoginFailureCount(Integer userId) {
        userMapper.resetLoginFailureCount(userId);
    }
}
