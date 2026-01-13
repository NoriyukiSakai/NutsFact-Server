package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.domain.model.master.FoodGroup;
import com.nines.nutsfact.domain.repository.FoodGroupRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodGroupService {

    private final FoodGroupRepository foodGroupRepository;

    public List<FoodGroup> findAll() {
        return foodGroupRepository.findAll();
    }

    public FoodGroup findById(Integer foodGroupId) {
        return foodGroupRepository.findById(foodGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("FoodGroup", foodGroupId));
    }

    @Transactional
    public FoodGroup create(FoodGroup foodGroup) {
        foodGroupRepository.save(foodGroup);
        return foodGroup;
    }

    @Transactional
    public FoodGroup update(Integer foodGroupId, FoodGroup foodGroup) {
        findById(foodGroupId);
        foodGroup.setFoodGroupId(foodGroupId);
        foodGroupRepository.save(foodGroup);
        return foodGroup;
    }

    @Transactional
    public void delete(Integer foodGroupId) {
        findById(foodGroupId);
        foodGroupRepository.delete(foodGroupId);
    }
}
