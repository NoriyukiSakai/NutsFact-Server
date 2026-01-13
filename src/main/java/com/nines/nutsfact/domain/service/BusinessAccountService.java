package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.domain.model.user.BusinessAccount;
import com.nines.nutsfact.domain.repository.BusinessAccountRepository;
import com.nines.nutsfact.domain.repository.UserRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessAccountService {

    private final BusinessAccountRepository businessAccountRepository;
    private final UserRepository userRepository;

    public List<BusinessAccount> findAll() {
        return businessAccountRepository.findAll();
    }

    public BusinessAccount findById(Integer id) {
        return businessAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", id));
    }

    public BusinessAccount findByCode(String code) {
        return businessAccountRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount with code " + code + " not found"));
    }

    @Transactional
    public BusinessAccount create(BusinessAccount businessAccount) {
        String nextCode = businessAccountRepository.generateNextCode();
        businessAccount.setCode(nextCode);
        if (businessAccount.getRegistrationStatus() == null) {
            businessAccount.setRegistrationStatus(0);
        }
        if (businessAccount.getMaxUserCount() == null) {
            businessAccount.setMaxUserCount(3);
        }
        if (businessAccount.getCurrentUserCount() == null) {
            businessAccount.setCurrentUserCount(0);
        }
        if (businessAccount.getIsActive() == null) {
            businessAccount.setIsActive(true);
        }
        businessAccountRepository.save(businessAccount);
        return businessAccount;
    }

    @Transactional
    public BusinessAccount update(Integer id, BusinessAccount businessAccount) {
        BusinessAccount existing = findById(id);
        businessAccount.setId(id);
        businessAccount.setCode(existing.getCode());
        businessAccountRepository.save(businessAccount);
        return businessAccount;
    }

    @Transactional
    public void requestDeletion(Integer id) {
        findById(id);
        businessAccountRepository.updateStatus(id, 3);
    }

    @Transactional
    public void suspend(Integer id) {
        findById(id);
        businessAccountRepository.updateStatus(id, 2);
    }

    @Transactional
    public void reactivate(Integer id) {
        findById(id);
        businessAccountRepository.updateStatus(id, 1);
    }

    @Transactional
    public void delete(Integer id) {
        findById(id);
        businessAccountRepository.delete(id);
    }

    @Transactional
    public void updateCurrentUserCount(Integer id) {
        BusinessAccount account = findById(id);
        int count = userRepository.countByBusinessAccountId(id);
        account.setCurrentUserCount(count);
        businessAccountRepository.save(account);
    }
}
