package com.nines.nutsfact.domain.repository;

import com.nines.nutsfact.domain.model.FoodRawMaterialSupplier;
import com.nines.nutsfact.infrastructure.mapper.FoodRawMaterialSupplierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FoodRawMaterialSupplierRepository {

    private final FoodRawMaterialSupplierMapper mapper;

    public List<FoodRawMaterialSupplier> findByFoodId(Integer foodId) {
        return mapper.findByFoodId(foodId);
    }

    public Optional<FoodRawMaterialSupplier> findById(Integer id) {
        return mapper.findById(id);
    }

    public int insert(FoodRawMaterialSupplier entity) {
        return mapper.insert(entity);
    }

    public int update(FoodRawMaterialSupplier entity) {
        return mapper.update(entity);
    }

    public int delete(Integer id) {
        return mapper.delete(id);
    }

    public Integer getLastInsertId() {
        return mapper.getLastInsertId();
    }
}
