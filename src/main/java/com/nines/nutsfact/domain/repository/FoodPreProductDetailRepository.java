package com.nines.nutsfact.domain.repository;

import com.nines.nutsfact.domain.model.FoodPreProductDetailItem;
import com.nines.nutsfact.infrastructure.mapper.FoodPreProductDetailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FoodPreProductDetailRepository {

    private final FoodPreProductDetailMapper mapper;

    public List<FoodPreProductDetailItem> findByPreId(Integer preId) {
        return mapper.findByPreId(preId);
    }

    public Optional<FoodPreProductDetailItem> findById(Integer id) {
        return mapper.findById(id);
    }

    public int insert(FoodPreProductDetailItem entity) {
        return mapper.insert(entity);
    }

    public int update(FoodPreProductDetailItem entity) {
        return mapper.update(entity);
    }

    public int delete(Integer id) {
        return mapper.delete(id);
    }

    public int deleteByPreId(Integer preId) {
        return mapper.deleteByPreId(preId);
    }

    public Integer getLastInsertId() {
        return mapper.getLastInsertId();
    }
}
