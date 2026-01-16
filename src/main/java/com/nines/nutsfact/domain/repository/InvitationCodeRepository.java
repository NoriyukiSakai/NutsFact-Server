package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.user.InvitationCode;
import com.nines.nutsfact.infrastructure.mapper.InvitationCodeMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InvitationCodeRepository {

    private final InvitationCodeMapper invitationCodeMapper;

    public List<InvitationCode> findAll() {
        return invitationCodeMapper.findAll();
    }

    public List<InvitationCode> findByBusinessAccountId(Integer businessAccountId) {
        return invitationCodeMapper.findByBusinessAccountId(businessAccountId);
    }

    public Optional<InvitationCode> findById(Integer id) {
        return Optional.ofNullable(invitationCodeMapper.findById(id));
    }

    public Optional<InvitationCode> findByCode(String code) {
        return Optional.ofNullable(invitationCodeMapper.findByCode(code));
    }

    public Optional<InvitationCode> findByCodeAndEmail(String code, String email) {
        return Optional.ofNullable(invitationCodeMapper.findByCodeAndEmail(code, email));
    }

    /**
     * 有効な（未使用かつ期限内の）招待コードをメールアドレスで検索
     */
    public Optional<InvitationCode> findActiveByEmail(String email) {
        return Optional.ofNullable(invitationCodeMapper.findActiveByEmail(email));
    }

    /**
     * ビジネスアカウントに対する有効な（未使用かつ期限内の）招待コード数をカウント
     */
    public int countActiveByBusinessAccountId(Integer businessAccountId) {
        return invitationCodeMapper.countActiveByBusinessAccountId(businessAccountId);
    }

    public void save(InvitationCode invitationCode) {
        if (invitationCode.getId() == null) {
            invitationCodeMapper.insert(invitationCode);
        } else {
            invitationCodeMapper.update(invitationCode);
        }
    }

    public void markAsUsed(Integer id) {
        invitationCodeMapper.markAsUsed(id);
    }

    public void delete(Integer id) {
        invitationCodeMapper.delete(id);
    }
}
