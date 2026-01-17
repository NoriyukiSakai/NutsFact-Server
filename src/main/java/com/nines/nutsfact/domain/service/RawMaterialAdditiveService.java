package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.config.SecurityContextHelper;
import com.nines.nutsfact.domain.model.additive.RawMaterialAdditive;
import com.nines.nutsfact.domain.repository.RawMaterialAdditiveRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RawMaterialAdditiveService {

    private final RawMaterialAdditiveRepository rawMaterialAdditiveRepository;

    public List<RawMaterialAdditive> findByFoodId(Integer foodId) {
        return rawMaterialAdditiveRepository.findByFoodId(foodId);
    }

    public List<RawMaterialAdditive> findByFoodIdWithAdditive(Integer foodId) {
        return rawMaterialAdditiveRepository.findByFoodIdWithAdditive(foodId);
    }

    public RawMaterialAdditive findById(Integer id) {
        return rawMaterialAdditiveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RawMaterialAdditive", id));
    }

    public RawMaterialAdditive findByIdWithBusinessAccountFilter(Integer id) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        return rawMaterialAdditiveRepository.findByIdAndBusinessAccountId(id, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("RawMaterialAdditive", id));
    }

    @Transactional
    public RawMaterialAdditive create(RawMaterialAdditive rawMaterialAdditive) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは添加物割当を登録できません");
        }
        rawMaterialAdditive.setBusinessAccountId(businessAccountId);
        if (rawMaterialAdditive.getIsActive() == null) {
            rawMaterialAdditive.setIsActive(true);
        }
        if (rawMaterialAdditive.getDisplayOrder() == null) {
            rawMaterialAdditive.setDisplayOrder(0);
        }
        if (rawMaterialAdditive.getExemptionType() == null) {
            rawMaterialAdditive.setExemptionType(0);
        }
        rawMaterialAdditiveRepository.save(rawMaterialAdditive);
        return rawMaterialAdditive;
    }

    @Transactional
    public RawMaterialAdditive update(Integer id, RawMaterialAdditive rawMaterialAdditive) {
        findById(id);
        rawMaterialAdditive.setId(id);
        rawMaterialAdditiveRepository.save(rawMaterialAdditive);
        return rawMaterialAdditive;
    }

    @Transactional
    public RawMaterialAdditive updateWithBusinessAccountFilter(Integer id, RawMaterialAdditive rawMaterialAdditive) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        rawMaterialAdditiveRepository.findByIdAndBusinessAccountId(id, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("RawMaterialAdditive", id));
        rawMaterialAdditive.setId(id);
        rawMaterialAdditive.setBusinessAccountId(businessAccountId);
        rawMaterialAdditiveRepository.save(rawMaterialAdditive);
        return rawMaterialAdditive;
    }

    @Transactional
    public void delete(Integer id) {
        findById(id);
        rawMaterialAdditiveRepository.delete(id);
    }

    @Transactional
    public void deleteWithBusinessAccountFilter(Integer id) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        rawMaterialAdditiveRepository.findByIdAndBusinessAccountId(id, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("RawMaterialAdditive", id));
        rawMaterialAdditiveRepository.deleteByIdAndBusinessAccountId(id, businessAccountId);
    }

    @Transactional
    public void deleteByFoodId(Integer foodId) {
        rawMaterialAdditiveRepository.deleteByFoodId(foodId);
    }
}
