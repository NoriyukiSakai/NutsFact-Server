package com.nines.nutsfact.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.config.SecurityContextHelper;
import com.nines.nutsfact.domain.model.allergy.AllergenicControl;
import com.nines.nutsfact.domain.repository.AllergenicControlRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AllergenicControlService {

    private final AllergenicControlRepository allergenicControlRepository;

    public List<AllergenicControl> findAll() {
        return allergenicControlRepository.findAll();
    }

    public List<AllergenicControl> findAllWithBusinessAccountFilter() {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId != null) {
            return allergenicControlRepository.findByBusinessAccountId(businessAccountId);
        }
        // 運営管理者（businessAccountId=null）はbusiness_account_idがnullのデータのみ
        return allergenicControlRepository.findByBusinessAccountIdIsNull();
    }

    public AllergenicControl findByFoodId(Integer foodId) {
        return allergenicControlRepository.findByFoodId(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("AllergenicControl", foodId));
    }

    public AllergenicControl findByFoodIdWithBusinessAccountFilter(Integer foodId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        return allergenicControlRepository.findByFoodIdAndBusinessAccountId(foodId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("AllergenicControl", foodId));
    }

    public Optional<AllergenicControl> findByFoodIdOptional(Integer foodId) {
        return allergenicControlRepository.findByFoodId(foodId);
    }

    public Optional<AllergenicControl> findByFoodIdOptionalWithBusinessAccountFilter(Integer foodId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        return allergenicControlRepository.findByFoodIdAndBusinessAccountId(foodId, businessAccountId);
    }

    @Transactional
    public AllergenicControl save(AllergenicControl allergenicControl) {
        // businessAccountIdを自動設定
        if (allergenicControl.getBusinessAccountId() == null) {
            allergenicControl.setBusinessAccountId(SecurityContextHelper.getCurrentBusinessAccountId());
        }
        allergenicControlRepository.save(allergenicControl);
        return allergenicControl;
    }

    @Transactional
    public void delete(Integer foodId) {
        allergenicControlRepository.delete(foodId);
    }

    @Transactional
    public void deleteWithBusinessAccountFilter(Integer foodId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        allergenicControlRepository.findByFoodIdAndBusinessAccountId(foodId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("AllergenicControl", foodId));
        allergenicControlRepository.deleteByFoodIdAndBusinessAccountId(foodId, businessAccountId);
    }
}
