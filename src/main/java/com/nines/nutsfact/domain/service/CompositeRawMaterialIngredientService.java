package com.nines.nutsfact.domain.service;

import com.nines.nutsfact.config.SecurityContextHelper;
import com.nines.nutsfact.domain.model.CompositeRawMaterialIngredient;
import com.nines.nutsfact.domain.repository.CompositeRawMaterialIngredientRepository;
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
public class CompositeRawMaterialIngredientService {

    private final CompositeRawMaterialIngredientRepository repository;

    @Transactional(readOnly = true)
    public List<CompositeRawMaterialIngredient> findByFoodId(Integer foodId) {
        return repository.findByFoodId(foodId);
    }

    @Transactional(readOnly = true)
    public List<CompositeRawMaterialIngredient> findByFoodIdWithBusinessAccountFilter(Integer foodId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId != null) {
            return repository.findByFoodIdAndBusinessAccountId(foodId, businessAccountId);
        }
        return repository.findByFoodId(foodId);
    }

    @Transactional(readOnly = true)
    public CompositeRawMaterialIngredient findById(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("複合原材料使用原材料", id));
    }

    @Transactional(readOnly = true)
    public CompositeRawMaterialIngredient findByIdWithBusinessAccountFilter(Integer id) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        return repository.findByIdAndBusinessAccountId(id, businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("複合原材料使用原材料", id));
    }

    @Transactional
    public CompositeRawMaterialIngredient create(CompositeRawMaterialIngredient entity) {
        try {
            if (entity.getIsActive() == null) {
                entity.setIsActive(true);
            }

            // businessAccountIdを自動設定
            if (entity.getBusinessAccountId() == null) {
                entity.setBusinessAccountId(SecurityContextHelper.getCurrentBusinessAccountId());
            }

            repository.insert(entity);
            // useGeneratedKeys により entity.id に自動設定される

            log.info("複合原材料使用原材料を登録しました: ID={}, foodId={}, businessAccountId={}",
                entity.getId(), entity.getFoodId(), entity.getBusinessAccountId());
            return entity;

        } catch (Exception e) {
            log.error("複合原材料使用原材料の登録に失敗: {}", e.getMessage(), e);
            throw new DataAccessFailedException("登録", e);
        }
    }

    @Transactional
    public CompositeRawMaterialIngredient update(CompositeRawMaterialIngredient entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("IDが指定されていません");
        }

        repository.findById(entity.getId())
            .orElseThrow(() -> new EntityNotFoundException("複合原材料使用原材料", entity.getId()));

        try {
            repository.update(entity);
            log.info("複合原材料使用原材料を更新しました: ID={}", entity.getId());
            return entity;

        } catch (Exception e) {
            log.error("複合原材料使用原材料の更新に失敗: ID={}, {}", entity.getId(), e.getMessage(), e);
            throw new DataAccessFailedException("更新", e);
        }
    }

    @Transactional
    public CompositeRawMaterialIngredient updateWithBusinessAccountFilter(CompositeRawMaterialIngredient entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("IDが指定されていません");
        }

        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        repository.findByIdAndBusinessAccountId(entity.getId(), businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("複合原材料使用原材料", entity.getId()));

        entity.setBusinessAccountId(businessAccountId);

        try {
            repository.update(entity);
            log.info("複合原材料使用原材料を更新しました: ID={}, businessAccountId={}", entity.getId(), businessAccountId);
            return entity;

        } catch (Exception e) {
            log.error("複合原材料使用原材料の更新に失敗: ID={}, {}", entity.getId(), e.getMessage(), e);
            throw new DataAccessFailedException("更新", e);
        }
    }

    @Transactional
    public void delete(Integer id) {
        repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("複合原材料使用原材料", id));

        try {
            repository.delete(id);
            log.info("複合原材料使用原材料を削除しました: ID={}", id);

        } catch (Exception e) {
            log.error("複合原材料使用原材料の削除に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }

    @Transactional
    public void deleteWithBusinessAccountFilter(Integer id) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();

        repository.findByIdAndBusinessAccountId(id, businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("複合原材料使用原材料", id));

        try {
            repository.deleteByIdAndBusinessAccountId(id, businessAccountId);
            log.info("複合原材料使用原材料を削除しました: ID={}, businessAccountId={}", id, businessAccountId);

        } catch (Exception e) {
            log.error("複合原材料使用原材料の削除に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }

    @Transactional
    public void deleteByFoodIdWithBusinessAccountFilter(Integer foodId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();

        try {
            if (businessAccountId != null) {
                repository.deleteByFoodIdAndBusinessAccountId(foodId, businessAccountId);
            } else {
                repository.deleteByFoodId(foodId);
            }
            log.info("複合原材料使用原材料をfoodIdで一括削除しました: foodId={}, businessAccountId={}", foodId, businessAccountId);

        } catch (Exception e) {
            log.error("複合原材料使用原材料の一括削除に失敗: foodId={}, {}", foodId, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }

    @Transactional
    public void saveAll(Integer foodId, List<CompositeRawMaterialIngredient> ingredients) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();

        // 既存データを削除
        if (businessAccountId != null) {
            repository.deleteByFoodIdAndBusinessAccountId(foodId, businessAccountId);
        } else {
            repository.deleteByFoodId(foodId);
        }

        // 新規データを登録
        for (int i = 0; i < ingredients.size(); i++) {
            CompositeRawMaterialIngredient ingredient = ingredients.get(i);
            ingredient.setFoodId(foodId);
            ingredient.setBusinessAccountId(businessAccountId);
            ingredient.setDisplayOrder(i + 1);
            if (ingredient.getIsActive() == null) {
                ingredient.setIsActive(true);
            }
            repository.insert(ingredient);
        }

        log.info("複合原材料使用原材料を一括保存しました: foodId={}, count={}, businessAccountId={}",
            foodId, ingredients.size(), businessAccountId);
    }
}
