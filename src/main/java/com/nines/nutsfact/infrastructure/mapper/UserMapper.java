package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.user.User;

@Mapper
public interface UserMapper {
    List<User> findAll();
    List<User> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);
    User findById(@Param("userId") Integer userId);
    User findByEmail(@Param("email") String email);
    User findByAuthUserId(@Param("authUserId") String authUserId);
    void insert(User user);
    void update(User user);
    void updateLastSignInAt(@Param("userId") Integer userId);
    void delete(@Param("userId") Integer userId);
    int countByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);
}
