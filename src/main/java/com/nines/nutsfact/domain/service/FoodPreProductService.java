package com.nines.nutsfact.domain.service;

import com.nines.nutsfact.config.SecurityContextHelper;
import com.nines.nutsfact.domain.model.FoodPreProductItem;
import com.nines.nutsfact.domain.model.FoodPreProductDetailItem;
import com.nines.nutsfact.domain.model.SelectItem;
import com.nines.nutsfact.domain.repository.FoodPreProductRepository;
import com.nines.nutsfact.domain.repository.FoodPreProductDetailRepository;
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
public class FoodPreProductService {

    private final FoodPreProductRepository repository;
    private final FoodPreProductDetailRepository detailRepository;

    @Transactional(readOnly = true)
    public List<FoodPreProductItem> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FoodPreProductItem> findAllWithBusinessAccountFilter() {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        log.info("仕込品一覧取得: businessAccountId={}", businessAccountId);
        if (businessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは仕込品を参照できません");
        }
        List<FoodPreProductItem> items = repository.findByBusinessAccountId(businessAccountId);
        log.info("仕込品一覧取得結果: {} 件", items.size());
        return items;
    }

    @Transactional(readOnly = true)
    public List<FoodPreProductItem> findByKind(Integer preKind) {
        return repository.findByKind(preKind);
    }

    @Transactional(readOnly = true)
    public List<FoodPreProductItem> findByKindWithBusinessAccountFilter(Integer preKind) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは仕込品を参照できません");
        }
        return repository.findByKindAndBusinessAccountId(preKind, businessAccountId);
    }

    @Transactional(readOnly = true)
    public List<SelectItem> findSelectItems(Integer preKind) {
        return repository.findSelectItems(preKind);
    }

    @Transactional(readOnly = true)
    public FoodPreProductItem findById(Integer id) {
        FoodPreProductItem item = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("仕込品", id));

        // 明細も取得
        List<FoodPreProductDetailItem> details = detailRepository.findByPreId(id);
        item.setDetails(details);

        return item;
    }

    @Transactional(readOnly = true)
    public FoodPreProductItem findByIdWithBusinessAccountFilter(Integer id) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        FoodPreProductItem item = repository.findByIdAndBusinessAccountId(id, businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("仕込品", id));

        // 明細も取得
        List<FoodPreProductDetailItem> details = detailRepository.findByPreId(id);
        item.setDetails(details);

        return item;
    }

    @Transactional
    public FoodPreProductItem create(FoodPreProductItem entity) {
        try {
            if (entity.getIsActive() == null) {
                entity.setIsActive(true);
            }

            // businessAccountIdを必須で設定
            if (entity.getBusinessAccountId() == null) {
                Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
                if (businessAccountId == null) {
                    throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは仕込品を登録できません");
                }
                entity.setBusinessAccountId(businessAccountId);
            }

            repository.insert(entity);
            Integer newId = repository.getLastInsertId();
            entity.setPreId(newId);

            // 明細も登録
            if (entity.getDetails() != null && !entity.getDetails().isEmpty()) {
                for (FoodPreProductDetailItem detail : entity.getDetails()) {
                    detail.setPreId(newId);
                    // 明細にもbusinessAccountIdを設定
                    if (detail.getBusinessAccountId() == null) {
                        detail.setBusinessAccountId(entity.getBusinessAccountId());
                    }
                    detailRepository.insert(detail);
                }
            }

            log.info("仕込品を登録しました: ID={}", newId);
            return entity;

        } catch (Exception e) {
            log.error("仕込品の登録に失敗: {}", e.getMessage(), e);
            throw new DataAccessFailedException("登録", e);
        }
    }

    @Transactional
    public FoodPreProductItem update(FoodPreProductItem entity) {
        if (entity.getPreId() == null) {
            throw new IllegalArgumentException("仕込品IDが指定されていません");
        }

        repository.findById(entity.getPreId())
            .orElseThrow(() -> new EntityNotFoundException("仕込品", entity.getPreId()));

        try {
            repository.update(entity);

            // 集計値の更新
            updateSummary(entity);

            log.info("仕込品を更新しました: ID={}", entity.getPreId());
            return entity;

        } catch (Exception e) {
            log.error("仕込品の更新に失敗: ID={}, {}", entity.getPreId(), e.getMessage(), e);
            throw new DataAccessFailedException("更新", e);
        }
    }

    @Transactional
    public FoodPreProductItem updateWithBusinessAccountFilter(FoodPreProductItem entity) {
        if (entity.getPreId() == null) {
            throw new IllegalArgumentException("仕込品IDが指定されていません");
        }

        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        repository.findByIdAndBusinessAccountId(entity.getPreId(), businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("仕込品", entity.getPreId()));

        // businessAccountIdを設定
        entity.setBusinessAccountId(businessAccountId);

        try {
            repository.update(entity);

            // 集計値の更新
            updateSummary(entity);

            log.info("仕込品を更新しました: ID={}", entity.getPreId());
            return entity;

        } catch (Exception e) {
            log.error("仕込品の更新に失敗: ID={}, {}", entity.getPreId(), e.getMessage(), e);
            throw new DataAccessFailedException("更新", e);
        }
    }

    @Transactional
    public void delete(Integer id) {
        repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("仕込品", id));

        try {
            // 明細を先に削除
            detailRepository.deleteByPreId(id);
            repository.delete(id);
            log.info("仕込品を削除しました: ID={}", id);

        } catch (Exception e) {
            log.error("仕込品の削除に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }

    @Transactional
    public void deleteWithBusinessAccountFilter(Integer id) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        repository.findByIdAndBusinessAccountId(id, businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("仕込品", id));

        try {
            // 明細を先に削除
            detailRepository.deleteByPreIdAndBusinessAccountId(id, businessAccountId);
            repository.deleteByIdAndBusinessAccountId(id, businessAccountId);
            log.info("仕込品を削除しました: ID={}", id);

        } catch (Exception e) {
            log.error("仕込品の削除に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }

    /**
     * 仕込品の集計値を更新
     */
    private void updateSummary(FoodPreProductItem entity) {
        List<FoodPreProductDetailItem> details = detailRepository.findByPreId(entity.getPreId());

        float weightSum = 0f;
        float costPriceSum = 0f;

        for (FoodPreProductDetailItem detail : details) {
            if (detail.getWeight() != null) {
                weightSum += detail.getWeight();
            }
            if (detail.getCostPrice() != null) {
                costPriceSum += detail.getCostPrice();
            }
        }

        entity.setWeightSum(weightSum);
        entity.setCostPriceSum(costPriceSum);
        entity.setDetailCount(details.size());
    }
}
