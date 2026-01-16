package com.nines.nutsfact.domain.service;

import com.nines.nutsfact.config.SecurityContextHelper;
import com.nines.nutsfact.domain.model.FoodSemiFinishedProduct;
import com.nines.nutsfact.domain.model.FoodSemiFinishedProductDetail;
import com.nines.nutsfact.domain.model.SelectItem;
import com.nines.nutsfact.domain.repository.FoodSemiFinishedProductRepository;
import com.nines.nutsfact.domain.repository.FoodSemiFinishedProductDetailRepository;
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
public class FoodSemiFinishedProductService {

    private final FoodSemiFinishedProductRepository repository;
    private final FoodSemiFinishedProductDetailRepository detailRepository;

    @Transactional(readOnly = true)
    public List<FoodSemiFinishedProduct> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FoodSemiFinishedProduct> findAllWithBusinessAccountFilter() {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId != null) {
            return repository.findByBusinessAccountId(businessAccountId);
        }
        return repository.findByBusinessAccountIdIsNull();
    }

    @Transactional(readOnly = true)
    public List<SelectItem> findSelectItems() {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        return repository.findSelectItems(businessAccountId);
    }

    @Transactional(readOnly = true)
    public FoodSemiFinishedProduct findById(Integer id) {
        FoodSemiFinishedProduct item = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("半完成品", id));

        List<FoodSemiFinishedProductDetail> details = detailRepository.findBySemiId(id);
        item.setDetails(details);

        return item;
    }

    @Transactional(readOnly = true)
    public FoodSemiFinishedProduct findByIdWithBusinessAccountFilter(Integer id) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        FoodSemiFinishedProduct item = repository.findByIdAndBusinessAccountId(id, businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("半完成品", id));

        List<FoodSemiFinishedProductDetail> details = detailRepository.findBySemiId(id);
        item.setDetails(details);

        return item;
    }

    @Transactional
    public FoodSemiFinishedProduct create(FoodSemiFinishedProduct entity) {
        try {
            if (entity.getIsActive() == null) {
                entity.setIsActive(true);
            }

            if (entity.getBusinessAccountId() == null) {
                entity.setBusinessAccountId(SecurityContextHelper.getCurrentBusinessAccountId());
            }

            repository.insert(entity);
            Integer newId = repository.getLastInsertId();
            entity.setSemiId(newId);

            if (entity.getDetails() != null && !entity.getDetails().isEmpty()) {
                for (FoodSemiFinishedProductDetail detail : entity.getDetails()) {
                    detail.setSemiId(newId);
                    if (detail.getBusinessAccountId() == null) {
                        detail.setBusinessAccountId(entity.getBusinessAccountId());
                    }
                    detailRepository.insert(detail);
                }
            }

            updateSummary(entity);
            repository.update(entity);

            log.info("半完成品を登録しました: ID={}", newId);
            return entity;

        } catch (Exception e) {
            log.error("半完成品の登録に失敗: {}", e.getMessage(), e);
            throw new DataAccessFailedException("登録", e);
        }
    }

    @Transactional
    public FoodSemiFinishedProduct update(FoodSemiFinishedProduct entity) {
        if (entity.getSemiId() == null) {
            throw new IllegalArgumentException("半完成品IDが指定されていません");
        }

        repository.findById(entity.getSemiId())
            .orElseThrow(() -> new EntityNotFoundException("半完成品", entity.getSemiId()));

        try {
            updateSummary(entity);
            repository.update(entity);

            log.info("半完成品を更新しました: ID={}", entity.getSemiId());
            return entity;

        } catch (Exception e) {
            log.error("半完成品の更新に失敗: ID={}, {}", entity.getSemiId(), e.getMessage(), e);
            throw new DataAccessFailedException("更新", e);
        }
    }

    @Transactional
    public FoodSemiFinishedProduct updateWithBusinessAccountFilter(FoodSemiFinishedProduct entity) {
        if (entity.getSemiId() == null) {
            throw new IllegalArgumentException("半完成品IDが指定されていません");
        }

        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        repository.findByIdAndBusinessAccountId(entity.getSemiId(), businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("半完成品", entity.getSemiId()));

        entity.setBusinessAccountId(businessAccountId);

        try {
            updateSummary(entity);
            repository.update(entity);

            log.info("半完成品を更新しました: ID={}", entity.getSemiId());
            return entity;

        } catch (Exception e) {
            log.error("半完成品の更新に失敗: ID={}, {}", entity.getSemiId(), e.getMessage(), e);
            throw new DataAccessFailedException("更新", e);
        }
    }

    @Transactional
    public void delete(Integer id) {
        repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("半完成品", id));

        try {
            detailRepository.deleteBySemiId(id);
            repository.delete(id);
            log.info("半完成品を削除しました: ID={}", id);

        } catch (Exception e) {
            log.error("半完成品の削除に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }

    @Transactional
    public void deleteWithBusinessAccountFilter(Integer id) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        repository.findByIdAndBusinessAccountId(id, businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("半完成品", id));

        try {
            detailRepository.deleteBySemiIdAndBusinessAccountId(id, businessAccountId);
            repository.deleteByIdAndBusinessAccountId(id, businessAccountId);
            log.info("半完成品を削除しました: ID={}", id);

        } catch (Exception e) {
            log.error("半完成品の削除に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }

    @Transactional
    public void updateAllergenSummary(Integer id, String allergenSummary) {
        repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("半完成品", id));

        try {
            repository.updateAllergenSummary(id, allergenSummary);
            log.info("半完成品のアレルゲン情報を更新しました: ID={}", id);

        } catch (Exception e) {
            log.error("半完成品のアレルゲン情報更新に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("アレルゲン情報更新", e);
        }
    }

    private void updateSummary(FoodSemiFinishedProduct entity) {
        List<FoodSemiFinishedProductDetail> details = detailRepository.findBySemiId(entity.getSemiId());

        float weightSum = 0f;
        float costPriceSum = 0f;

        for (FoodSemiFinishedProductDetail detail : details) {
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
