package com.nines.nutsfact.domain.service;

import com.nines.nutsfact.domain.model.FoodRawMaterialSupplier;
import com.nines.nutsfact.domain.repository.FoodRawMaterialSupplierRepository;
import com.nines.nutsfact.exception.DataAccessFailedException;
import com.nines.nutsfact.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodRawMaterialSupplierService {

    private final FoodRawMaterialSupplierRepository repository;

    @Transactional(readOnly = true)
    public List<FoodRawMaterialSupplier> findByFoodId(Integer foodId) {
        return repository.findByFoodId(foodId);
    }

    @Transactional(readOnly = true)
    public FoodRawMaterialSupplier findById(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("原材料仕入先情報", id));
    }

    @Transactional
    public FoodRawMaterialSupplier create(FoodRawMaterialSupplier entity) {
        try {
            if (entity.getIsActive() == null) {
                entity.setIsActive(true);
            }

            repository.insert(entity);
            // useGeneratedKeys により entity.id に自動設定される

            log.info("原材料仕入先情報を登録しました: ID={}", entity.getId());
            return entity;

        } catch (Exception e) {
            log.error("原材料仕入先情報の登録に失敗: {}", e.getMessage(), e);
            throw new DataAccessFailedException("登録", e);
        }
    }

    @Transactional
    public FoodRawMaterialSupplier update(FoodRawMaterialSupplier entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("IDが指定されていません");
        }

        repository.findById(entity.getId())
            .orElseThrow(() -> new EntityNotFoundException("原材料仕入先情報", entity.getId()));

        try {
            repository.update(entity);
            log.info("原材料仕入先情報を更新しました: ID={}", entity.getId());
            return entity;

        } catch (Exception e) {
            log.error("原材料仕入先情報の更新に失敗: ID={}, {}", entity.getId(), e.getMessage(), e);
            throw new DataAccessFailedException("更新", e);
        }
    }

    @Transactional
    public void delete(Integer id) {
        repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("原材料仕入先情報", id));

        try {
            repository.delete(id);
            log.info("原材料仕入先情報を削除しました: ID={}", id);

        } catch (Exception e) {
            log.error("原材料仕入先情報の削除に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }
}
