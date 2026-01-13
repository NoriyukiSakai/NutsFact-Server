package com.nines.nutsfact.domain.repository;

import com.nines.nutsfact.domain.model.FoodPreProductItem;
import com.nines.nutsfact.domain.model.SelectItem;
import com.nines.nutsfact.infrastructure.mapper.FoodPreProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FoodPreProductRepository {

    private final FoodPreProductMapper mapper;

    public List<FoodPreProductItem> findAll() {
        return mapper.findAll();
    }

    public List<FoodPreProductItem> findByKind(Integer preKind) {
        return mapper.findByKind(preKind);
    }

    public List<SelectItem> findSelectItems(Integer preKind) {
        return mapper.findSelectItems(preKind);
    }

    public Optional<FoodPreProductItem> findById(Integer id) {
        return mapper.findById(id);
    }

    public int insert(FoodPreProductItem entity) {
        return mapper.insert(entity);
    }

    public int update(FoodPreProductItem entity) {
        return mapper.update(entity);
    }

    public int delete(Integer id) {
        return mapper.delete(id);
    }

    public Integer getLastInsertId() {
        return mapper.getLastInsertId();
    }
}
