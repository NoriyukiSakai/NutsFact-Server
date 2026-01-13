package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.master.FoodGroup;
import com.nines.nutsfact.infrastructure.mapper.FoodGroupMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FoodGroupRepository {

    private final FoodGroupMapper foodGroupMapper;

    public List<FoodGroup> findAll() {
        return foodGroupMapper.findAll();
    }

    public Optional<FoodGroup> findById(Integer foodGroupId) {
        return Optional.ofNullable(foodGroupMapper.findById(foodGroupId));
    }

    public void save(FoodGroup foodGroup) {
        if (foodGroup.getFoodGroupId() == null) {
            foodGroupMapper.insert(foodGroup);
        } else {
            foodGroupMapper.update(foodGroup);
        }
    }

    public void delete(Integer foodGroupId) {
        foodGroupMapper.delete(foodGroupId);
    }
}
