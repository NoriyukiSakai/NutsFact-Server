package com.nines.nutsfact.domain.repository;

import com.nines.nutsfact.domain.model.FoodSemiFinishedProduct;
import com.nines.nutsfact.domain.model.SelectItem;
import com.nines.nutsfact.infrastructure.mapper.FoodSemiFinishedProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FoodSemiFinishedProductRepository {

    private final FoodSemiFinishedProductMapper mapper;

    public List<FoodSemiFinishedProduct> findAll() {
        return mapper.findAll();
    }

    public List<FoodSemiFinishedProduct> findByBusinessAccountId(Integer businessAccountId) {
        return mapper.findByBusinessAccountId(businessAccountId);
    }

    public List<SelectItem> findSelectItems(Integer businessAccountId) {
        return mapper.findSelectItems(businessAccountId);
    }

    public Optional<FoodSemiFinishedProduct> findById(Integer id) {
        return mapper.findById(id);
    }

    public Optional<FoodSemiFinishedProduct> findByIdAndBusinessAccountId(Integer id, Integer businessAccountId) {
        return mapper.findByIdAndBusinessAccountId(id, businessAccountId);
    }

    public int insert(FoodSemiFinishedProduct entity) {
        return mapper.insert(entity);
    }

    public int update(FoodSemiFinishedProduct entity) {
        return mapper.update(entity);
    }

    public int updateAllergenSummary(Integer id, String allergenSummary) {
        return mapper.updateAllergenSummary(id, allergenSummary);
    }

    public int delete(Integer id) {
        return mapper.delete(id);
    }

    public int deleteByIdAndBusinessAccountId(Integer id, Integer businessAccountId) {
        return mapper.deleteByIdAndBusinessAccountId(id, businessAccountId);
    }

    public Integer getLastInsertId() {
        return mapper.getLastInsertId();
    }
}
