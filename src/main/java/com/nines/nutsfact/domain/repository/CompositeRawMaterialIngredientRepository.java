package com.nines.nutsfact.domain.repository;

import com.nines.nutsfact.domain.model.CompositeRawMaterialIngredient;
import com.nines.nutsfact.infrastructure.mapper.CompositeRawMaterialIngredientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CompositeRawMaterialIngredientRepository {

    private final CompositeRawMaterialIngredientMapper mapper;

    public List<CompositeRawMaterialIngredient> findByFoodId(Integer foodId) {
        return mapper.findByFoodId(foodId);
    }

    public List<CompositeRawMaterialIngredient> findByFoodIdAndBusinessAccountId(Integer foodId, Integer businessAccountId) {
        return mapper.findByFoodIdAndBusinessAccountId(foodId, businessAccountId);
    }

    public Optional<CompositeRawMaterialIngredient> findById(Integer id) {
        return mapper.findById(id);
    }

    public Optional<CompositeRawMaterialIngredient> findByIdAndBusinessAccountId(Integer id, Integer businessAccountId) {
        return mapper.findByIdAndBusinessAccountId(id, businessAccountId);
    }

    public int insert(CompositeRawMaterialIngredient entity) {
        return mapper.insert(entity);
    }

    public int update(CompositeRawMaterialIngredient entity) {
        return mapper.update(entity);
    }

    public int delete(Integer id) {
        return mapper.delete(id);
    }

    public int deleteByIdAndBusinessAccountId(Integer id, Integer businessAccountId) {
        return mapper.deleteByIdAndBusinessAccountId(id, businessAccountId);
    }

    public int deleteByFoodId(Integer foodId) {
        return mapper.deleteByFoodId(foodId);
    }

    public int deleteByFoodIdAndBusinessAccountId(Integer foodId, Integer businessAccountId) {
        return mapper.deleteByFoodIdAndBusinessAccountId(foodId, businessAccountId);
    }

    public Integer getLastInsertId() {
        return mapper.getLastInsertId();
    }
}
