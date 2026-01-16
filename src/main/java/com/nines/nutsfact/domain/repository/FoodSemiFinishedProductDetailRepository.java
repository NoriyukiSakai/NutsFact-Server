package com.nines.nutsfact.domain.repository;

import com.nines.nutsfact.domain.model.FoodSemiFinishedProductDetail;
import com.nines.nutsfact.infrastructure.mapper.FoodSemiFinishedProductDetailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FoodSemiFinishedProductDetailRepository {

    private final FoodSemiFinishedProductDetailMapper mapper;

    public List<FoodSemiFinishedProductDetail> findBySemiId(Integer semiId) {
        return mapper.findBySemiId(semiId);
    }

    public List<FoodSemiFinishedProductDetail> findBySemiIdAndBusinessAccountId(Integer semiId, Integer businessAccountId) {
        return mapper.findBySemiIdAndBusinessAccountId(semiId, businessAccountId);
    }

    public Optional<FoodSemiFinishedProductDetail> findById(Integer id) {
        return mapper.findById(id);
    }

    public Optional<FoodSemiFinishedProductDetail> findByIdAndBusinessAccountId(Integer id, Integer businessAccountId) {
        return mapper.findByIdAndBusinessAccountId(id, businessAccountId);
    }

    public int insert(FoodSemiFinishedProductDetail entity) {
        return mapper.insert(entity);
    }

    public int update(FoodSemiFinishedProductDetail entity) {
        return mapper.update(entity);
    }

    public int delete(Integer id) {
        return mapper.delete(id);
    }

    public int deleteBySemiId(Integer semiId) {
        return mapper.deleteBySemiId(semiId);
    }

    public int deleteByIdAndBusinessAccountId(Integer id, Integer businessAccountId) {
        return mapper.deleteByIdAndBusinessAccountId(id, businessAccountId);
    }

    public int deleteBySemiIdAndBusinessAccountId(Integer semiId, Integer businessAccountId) {
        return mapper.deleteBySemiIdAndBusinessAccountId(semiId, businessAccountId);
    }

    public Integer getLastInsertId() {
        return mapper.getLastInsertId();
    }
}
