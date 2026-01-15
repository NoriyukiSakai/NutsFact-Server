package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.user.UserBusinessAccountMembership;
import com.nines.nutsfact.infrastructure.mapper.UserBusinessAccountMembershipMapper;

import lombok.RequiredArgsConstructor;

/**
 * ユーザービジネスアカウント所属リポジトリ
 */
@Repository
@RequiredArgsConstructor
public class UserBusinessAccountMembershipRepository {

    private final UserBusinessAccountMembershipMapper membershipMapper;

    public List<UserBusinessAccountMembership> findByUserId(Integer userId) {
        return membershipMapper.findByUserId(userId);
    }

    public List<UserBusinessAccountMembership> findByBusinessAccountId(Integer businessAccountId) {
        return membershipMapper.findByBusinessAccountId(businessAccountId);
    }

    public Optional<UserBusinessAccountMembership> findByUserIdAndBusinessAccountId(Integer userId, Integer businessAccountId) {
        return Optional.ofNullable(membershipMapper.findByUserIdAndBusinessAccountId(userId, businessAccountId));
    }

    public Optional<UserBusinessAccountMembership> findDefaultByUserId(Integer userId) {
        return Optional.ofNullable(membershipMapper.findDefaultByUserId(userId));
    }

    public void save(UserBusinessAccountMembership membership) {
        if (membership.getId() == null) {
            membershipMapper.insert(membership);
        } else {
            membershipMapper.update(membership);
        }
    }

    public void delete(Integer id) {
        membershipMapper.delete(id);
    }

    public void deleteByUserId(Integer userId) {
        membershipMapper.deleteByUserId(userId);
    }

    /**
     * デフォルトアカウントを設定
     * @param userId ユーザーID
     * @param businessAccountId ビジネスアカウントID
     */
    public void setDefaultAccount(Integer userId, Integer businessAccountId) {
        membershipMapper.clearDefaultByUserId(userId);
        membershipMapper.setDefaultByUserIdAndBusinessAccountId(userId, businessAccountId);
    }

    public int countByUserId(Integer userId) {
        return membershipMapper.countByUserId(userId);
    }
}
