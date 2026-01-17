package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.additive.RawMaterialAdditive;
import com.nines.nutsfact.infrastructure.mapper.RawMaterialAdditiveMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RawMaterialAdditiveRepository {

    private final RawMaterialAdditiveMapper rawMaterialAdditiveMapper;

    public List<RawMaterialAdditive> findByFoodId(Integer foodId) {
        return rawMaterialAdditiveMapper.findByFoodId(foodId);
    }

    public List<RawMaterialAdditive> findByFoodIdWithAdditive(Integer foodId) {
        return rawMaterialAdditiveMapper.findByFoodIdWithAdditive(foodId);
    }

    public List<RawMaterialAdditive> findByBusinessAccountId(Integer businessAccountId) {
        return rawMaterialAdditiveMapper.findByBusinessAccountId(businessAccountId);
    }

    public Optional<RawMaterialAdditive> findById(Integer id) {
        return Optional.ofNullable(rawMaterialAdditiveMapper.findById(id));
    }

    public Optional<RawMaterialAdditive> findByIdAndBusinessAccountId(Integer id, Integer businessAccountId) {
        return Optional.ofNullable(rawMaterialAdditiveMapper.findByIdAndBusinessAccountId(id, businessAccountId));
    }

    public void save(RawMaterialAdditive rawMaterialAdditive) {
        if (rawMaterialAdditive.getId() == null) {
            rawMaterialAdditiveMapper.insert(rawMaterialAdditive);
        } else {
            rawMaterialAdditiveMapper.update(rawMaterialAdditive);
        }
    }

    public void delete(Integer id) {
        rawMaterialAdditiveMapper.delete(id);
    }

    public void deleteByIdAndBusinessAccountId(Integer id, Integer businessAccountId) {
        rawMaterialAdditiveMapper.deleteByIdAndBusinessAccountId(id, businessAccountId);
    }

    public void deleteByFoodId(Integer foodId) {
        rawMaterialAdditiveMapper.deleteByFoodId(foodId);
    }
}
