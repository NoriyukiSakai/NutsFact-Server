package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.user.BusinessAccount;
import com.nines.nutsfact.infrastructure.mapper.BusinessAccountMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BusinessAccountRepository {

    private final BusinessAccountMapper businessAccountMapper;

    public List<BusinessAccount> findAll() {
        return businessAccountMapper.findAll();
    }

    public Optional<BusinessAccount> findById(Integer id) {
        return Optional.ofNullable(businessAccountMapper.findById(id));
    }

    public Optional<BusinessAccount> findByCode(String code) {
        return Optional.ofNullable(businessAccountMapper.findByCode(code));
    }

    public void save(BusinessAccount businessAccount) {
        if (businessAccount.getId() == null) {
            businessAccountMapper.insert(businessAccount);
        } else {
            businessAccountMapper.update(businessAccount);
        }
    }

    public void updateStatus(Integer id, Integer registrationStatus) {
        businessAccountMapper.updateStatus(id, registrationStatus);
    }

    public void delete(Integer id) {
        businessAccountMapper.delete(id);
    }

    public String generateNextCode() {
        return businessAccountMapper.generateNextCode();
    }
}
