package com.nines.nutsfact.domain.service;

import com.nines.nutsfact.config.SecurityContextHelper;
import com.nines.nutsfact.domain.model.FoodRawMaterial;
import com.nines.nutsfact.domain.model.SelectItem;
import com.nines.nutsfact.domain.repository.AllergenicControlRepository;
import com.nines.nutsfact.domain.repository.CompositeRawMaterialIngredientRepository;
import com.nines.nutsfact.domain.repository.FoodRawMaterialRepository;
import com.nines.nutsfact.domain.repository.FoodRawMaterialSupplierRepository;
import com.nines.nutsfact.exception.DataAccessFailedException;
import com.nines.nutsfact.exception.EntityNotFoundException;
import com.nines.nutsfact.exception.ForeignKeyConstraintException;
import com.nines.nutsfact.infrastructure.mapper.FoodPreProductDetailMapper;
import com.nines.nutsfact.infrastructure.mapper.FoodSemiFinishedProductDetailMapper;
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
    private final FoodRawMaterialSupplierRepository supplierRepository;
    private final AllergenicControlRepository allergenicControlRepository;
    private final CompositeRawMaterialIngredientRepository compositeIngredientRepository;
    private final FoodPreProductDetailMapper preProductDetailMapper;
    private final FoodSemiFinishedProductDetailMapper semiFinishedProductDetailMapper;

    @Transactional(readOnly = true)
    public List<FoodRawMaterial> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FoodRawMaterial> findByCategory(Integer categoryId) {
        return repository.findByCategory(categoryId);
    }

    /**
     * カテゴリ別原材料一覧取得（businessAccountIdフィルタリング）
     * categoryId=1（8訂）, 2（拡張）の場合はbusinessAccountIdでフィルタリングしない
     * categoryId=3（ユーザ定義）の場合: 該当business_account_idのデータのみ
     */
    @Transactional(readOnly = true)
    public List<FoodRawMaterial> findByCategoryWithBusinessAccountFilter(Integer categoryId) {
        // patternId=1（8訂）or 2（拡張）はbusiness_account_idでフィルタリングしない
        if (categoryId != null && (categoryId == 1 || categoryId == 2)) {
            return repository.findByCategory(categoryId);
        }

        // patternId=3（ユーザ定義）はbusiness_account_idでフィルタリング
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは原材料を参照できません");
        }
        return repository.findByCategoryAndBusinessAccountId(categoryId, businessAccountId);
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

    /**
     * ID指定で原材料取得（businessAccountIdフィルタリング）
     * categoryId=1（8訂）, 2（拡張）の場合はbusinessAccountIdでフィルタリングしない
     */
    @Transactional(readOnly = true)
    public FoodRawMaterial findByIdWithBusinessAccountFilter(Integer id) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        return repository.findByIdAndBusinessAccountId(id, businessAccountId)
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

            // businessAccountIdを必須で設定
            if (entity.getBusinessAccountId() == null) {
                Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
                if (businessAccountId == null) {
                    throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは原材料を登録できません");
                }
                entity.setBusinessAccountId(businessAccountId);
            }

            // revision_of_food_noを自動設定（同じfood_noかつ同じbusiness_account_idの最大値+1）
            if (entity.getFoodNo() != null && entity.getBusinessAccountId() != null) {
                Integer maxRevision = repository.getMaxRevisionByFoodNoAndBusinessAccountId(
                        entity.getFoodNo(), entity.getBusinessAccountId());
                int newRevision = (maxRevision != null ? maxRevision : -1) + 1;
                entity.setRevisionOfFoodNo(newRevision);
                log.info("revision_of_food_no を {} に設定: food_no={}, businessAccountId={}",
                        newRevision, entity.getFoodNo(), entity.getBusinessAccountId());
            } else {
                entity.setRevisionOfFoodNo(0);
            }

            repository.insert(entity);
            Integer newId = repository.getLastInsertId();
            entity.setFoodId(newId);

            log.info("原材料を登録しました: ID={}, businessAccountId={}, revision={}",
                newId, entity.getBusinessAccountId(), entity.getRevisionOfFoodNo());
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

    /**
     * 原材料更新（businessAccountIdでのフィルタリング付き）
     */
    @Transactional
    public FoodRawMaterial updateWithBusinessAccountFilter(FoodRawMaterial entity) {
        if (entity.getFoodId() == null) {
            throw new IllegalArgumentException("食品IDが指定されていません");
        }

        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        repository.findByIdAndBusinessAccountId(entity.getFoodId(), businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("原材料", entity.getFoodId()));

        // businessAccountIdを確実に設定
        entity.setBusinessAccountId(businessAccountId);

        try {
            repository.update(entity);
            log.info("原材料を更新しました: ID={}, businessAccountId={}", entity.getFoodId(), businessAccountId);
            return entity;

        } catch (Exception e) {
            log.error("原材料の更新に失敗: ID={}, {}", entity.getFoodId(), e.getMessage(), e);
            throw new DataAccessFailedException("更新", e);
        }
    }

    /**
     * 原材料が仕込品・半完成品で使用されているか確認
     * 使用されている場合はForeignKeyConstraintExceptionをスロー
     */
    private void checkRawMaterialNotInUse(Integer foodId) {
        int preProductCount = preProductDetailMapper.countByDetailFoodId(foodId);
        if (preProductCount > 0) {
            throw new ForeignKeyConstraintException(
                "この原材料は仕込品で使用されているため削除できません（" + preProductCount + "件）");
        }

        int semiFinishedCount = semiFinishedProductDetailMapper.countByDetailFoodId(foodId);
        if (semiFinishedCount > 0) {
            throw new ForeignKeyConstraintException(
                "この原材料は半完成品で使用されているため削除できません（" + semiFinishedCount + "件）");
        }
    }

    /**
     * 原材料削除
     * 関連する仕入元情報、アレルゲン情報、複合材料情報も削除する
     */
    @Transactional
    public void delete(Integer id) {
        // 存在確認
        repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("原材料", id));

        // 仕込品・半完成品での使用チェック
        checkRawMaterialNotInUse(id);

        try {
            // 関連データを先に削除（外部キー制約対応）
            supplierRepository.deleteByFoodId(id);
            log.info("原材料の仕入元情報を削除しました: foodId={}", id);

            allergenicControlRepository.delete(id);
            log.info("原材料のアレルゲン情報を削除しました: foodId={}", id);

            compositeIngredientRepository.deleteByFoodId(id);
            log.info("原材料の複合材料情報を削除しました: foodId={}", id);

            // メインの原材料データを削除
            repository.delete(id);
            log.info("原材料を削除しました: ID={}", id);

        } catch (Exception e) {
            log.error("原材料の削除に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }

    /**
     * 原材料削除（businessAccountIdでのフィルタリング付き）
     * 関連する仕入元情報、アレルゲン情報、複合材料情報も削除する
     */
    @Transactional
    public void deleteWithBusinessAccountFilter(Integer id) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();

        repository.findByIdAndBusinessAccountId(id, businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("原材料", id));

        // 仕込品・半完成品での使用チェック
        checkRawMaterialNotInUse(id);

        try {
            // 関連データを先に削除（外部キー制約対応）
            supplierRepository.deleteByFoodIdAndBusinessAccountId(id, businessAccountId);
            log.info("原材料の仕入元情報を削除しました: foodId={}", id);

            allergenicControlRepository.deleteByFoodIdAndBusinessAccountId(id, businessAccountId);
            log.info("原材料のアレルゲン情報を削除しました: foodId={}", id);

            compositeIngredientRepository.deleteByFoodIdAndBusinessAccountId(id, businessAccountId);
            log.info("原材料の複合材料情報を削除しました: foodId={}", id);

            // メインの原材料データを削除
            repository.deleteByIdAndBusinessAccountId(id, businessAccountId);
            log.info("原材料を削除しました: ID={}, businessAccountId={}", id, businessAccountId);

        } catch (Exception e) {
            log.error("原材料の削除に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }
}
