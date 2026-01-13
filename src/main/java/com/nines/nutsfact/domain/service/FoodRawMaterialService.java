package com.nines.nutsfact.domain.service;

import com.nines.nutsfact.domain.model.FoodRawMaterial;
import com.nines.nutsfact.domain.model.SelectItem;
import com.nines.nutsfact.domain.repository.FoodRawMaterialRepository;
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
public class FoodRawMaterialService {

    private final FoodRawMaterialRepository repository;

    @Transactional(readOnly = true)
    public List<FoodRawMaterial> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FoodRawMaterial> findByCategory(Integer categoryId) {
        return repository.findByCategory(categoryId);
    }

    @Transactional(readOnly = true)
    public List<SelectItem> findSelectItems(Integer categoryId) {
        return repository.findSelectItems(categoryId);
    }

    @Transactional(readOnly = true)
    public FoodRawMaterial findById(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("原材料", id));
    }

    @Transactional(readOnly = true)
    public FoodRawMaterial findByFoodNo(String foodNo) {
        return repository.findByFoodNo(foodNo)
            .orElseThrow(() -> new EntityNotFoundException("原材料 (食品番号: " + foodNo + ")"));
    }

    @Transactional
    public FoodRawMaterial create(FoodRawMaterial entity) {
        try {
            // デフォルト値設定
            if (entity.getStatus() == null) {
                entity.setStatus(1);  // 仮登録
            }
            if (entity.getIsActive() == null) {
                entity.setIsActive(true);
            }

            repository.insert(entity);
            Integer newId = repository.getLastInsertId();
            entity.setFoodId(newId);

            log.info("原材料を登録しました: ID={}", newId);
            return entity;

        } catch (Exception e) {
            log.error("原材料の登録に失敗: {}", e.getMessage(), e);
            throw new DataAccessFailedException("登録", e);
        }
    }

    @Transactional
    public FoodRawMaterial update(FoodRawMaterial entity) {
        // 存在確認
        if (entity.getFoodId() == null) {
            throw new IllegalArgumentException("食品IDが指定されていません");
        }

        repository.findById(entity.getFoodId())
            .orElseThrow(() -> new EntityNotFoundException("原材料", entity.getFoodId()));

        try {
            repository.update(entity);
            log.info("原材料を更新しました: ID={}", entity.getFoodId());
            return entity;

        } catch (Exception e) {
            log.error("原材料の更新に失敗: ID={}, {}", entity.getFoodId(), e.getMessage(), e);
            throw new DataAccessFailedException("更新", e);
        }
    }

    @Transactional
    public void delete(Integer id) {
        // 存在確認
        repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("原材料", id));

        try {
            repository.delete(id);
            log.info("原材料を削除しました: ID={}", id);

        } catch (Exception e) {
            log.error("原材料の削除に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }
}
