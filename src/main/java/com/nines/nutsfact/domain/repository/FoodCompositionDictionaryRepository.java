package com.nines.nutsfact.domain.repository;

import com.nines.nutsfact.domain.model.FoodCompositionDictionary;
import com.nines.nutsfact.infrastructure.mapper.FoodCompositionDictionaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FoodCompositionDictionaryRepository {
    private final FoodCompositionDictionaryMapper mapper;

    public List<FoodCompositionDictionary> findAll() {
        return mapper.findAll();
    }

    public Optional<FoodCompositionDictionary> findById(Integer id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    public List<FoodCompositionDictionary> findByFoodGroupId(Integer foodGroupId) {
        return mapper.findByFoodGroupId(foodGroupId);
    }

    public int insert(FoodCompositionDictionary entity) {
        return mapper.insert(entity);
    }

    public int update(FoodCompositionDictionary entity) {
        return mapper.update(entity);
    }

    public int delete(Integer id) {
        return mapper.delete(id);
    }

    public void truncate() {
        mapper.truncate();
    }

    public int count() {
        return mapper.count();
    }
}
