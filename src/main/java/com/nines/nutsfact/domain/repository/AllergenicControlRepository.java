package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.allergy.AllergenicControl;
import com.nines.nutsfact.infrastructure.mapper.AllergenicControlMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AllergenicControlRepository {

    private final AllergenicControlMapper allergenicControlMapper;

    public List<AllergenicControl> findAll() {
        return allergenicControlMapper.findAll();
    }

    public List<AllergenicControl> findByBusinessAccountId(Integer businessAccountId) {
        return allergenicControlMapper.findByBusinessAccountId(businessAccountId);
    }

    public List<AllergenicControl> findByBusinessAccountIdIsNull() {
        return allergenicControlMapper.findByBusinessAccountIdIsNull();
    }

    public Optional<AllergenicControl> findByFoodId(Integer foodId) {
        return Optional.ofNullable(allergenicControlMapper.findByFoodId(foodId));
    }

    public Optional<AllergenicControl> findByFoodIdAndBusinessAccountId(Integer foodId, Integer businessAccountId) {
        return Optional.ofNullable(allergenicControlMapper.findByFoodIdAndBusinessAccountId(foodId, businessAccountId));
    }

    public void save(AllergenicControl allergenicControl) {
        allergenicControlMapper.upsert(allergenicControl);
    }

    public void delete(Integer foodId) {
        allergenicControlMapper.delete(foodId);
    }

    public void deleteByFoodIdAndBusinessAccountId(Integer foodId, Integer businessAccountId) {
        allergenicControlMapper.deleteByFoodIdAndBusinessAccountId(foodId, businessAccountId);
    }
}
