package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.user.UserBusinessAccountMembership;

@Mapper
public interface UserBusinessAccountMembershipMapper {
    List<UserBusinessAccountMembership> findByUserId(@Param("userId") Integer userId);
    List<UserBusinessAccountMembership> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);
    UserBusinessAccountMembership findByUserIdAndBusinessAccountId(
        @Param("userId") Integer userId,
        @Param("businessAccountId") Integer businessAccountId
    );
    UserBusinessAccountMembership findDefaultByUserId(@Param("userId") Integer userId);
    void insert(UserBusinessAccountMembership membership);
    void update(UserBusinessAccountMembership membership);
    void delete(@Param("id") Integer id);
    void deleteByUserId(@Param("userId") Integer userId);
    void clearDefaultByUserId(@Param("userId") Integer userId);
    void setDefaultByUserIdAndBusinessAccountId(
        @Param("userId") Integer userId,
        @Param("businessAccountId") Integer businessAccountId
    );
    int countByUserId(@Param("userId") Integer userId);
}
