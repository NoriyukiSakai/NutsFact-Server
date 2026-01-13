package com.nines.nutsfact.domain.repository;

import com.nines.nutsfact.domain.model.FoodRawMaterial;
import com.nines.nutsfact.domain.model.SelectItem;
import com.nines.nutsfact.infrastructure.mapper.FoodRawMaterialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FoodRawMaterialRepository {

    private final FoodRawMaterialMapper mapper;

    public List<FoodRawMaterial> findAll() {
        return mapper.findAll();
    }

    public List<FoodRawMaterial> findByCategory(Integer categoryId) {
        return mapper.findByCategory(categoryId);
    }

    public List<SelectItem> findSelectItems(Integer categoryId) {
        return mapper.findSelectItems(categoryId);
    }

    public Optional<FoodRawMaterial> findById(Integer id) {
        return mapper.findById(id);
    }

    public Optional<FoodRawMaterial> findByFoodNo(String foodNo) {
        return mapper.findByFoodNo(foodNo);
    }

    public int insert(FoodRawMaterial entity) {
        return mapper.insert(entity);
    }

    public int update(FoodRawMaterial entity) {
        return mapper.update(entity);
    }

    public int delete(Integer id) {
        return mapper.delete(id);
    }

    public Integer getLastInsertId() {
        return mapper.getLastInsertId();
    }
}
