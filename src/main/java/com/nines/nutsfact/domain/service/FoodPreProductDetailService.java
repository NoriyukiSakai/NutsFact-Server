package com.nines.nutsfact.domain.service;

import com.nines.nutsfact.config.SecurityContextHelper;
import com.nines.nutsfact.domain.model.FoodPreProductDetailItem;
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
public class FoodPreProductDetailService {

    private final FoodPreProductDetailRepository repository;

    @Transactional(readOnly = true)
    public List<FoodPreProductDetailItem> findByPreId(Integer preId) {
        return repository.findByPreId(preId);
    }

    @Transactional(readOnly = true)
    public List<FoodPreProductDetailItem> findByPreIdWithBusinessAccountFilter(Integer preId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId != null) {
            return repository.findByPreIdAndBusinessAccountId(preId, businessAccountId);
        }
        return repository.findByPreId(preId);
    }

    @Transactional(readOnly = true)
    public FoodPreProductDetailItem findById(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("仕込品明細", id));
    }

    @Transactional(readOnly = true)
    public FoodPreProductDetailItem findByIdWithBusinessAccountFilter(Integer id) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        return repository.findByIdAndBusinessAccountId(id, businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("仕込品明細", id));
    }

    @Transactional
    public FoodPreProductDetailItem create(FoodPreProductDetailItem entity) {
        try {
            // businessAccountIdを自動設定
            if (entity.getBusinessAccountId() == null) {
                entity.setBusinessAccountId(SecurityContextHelper.getCurrentBusinessAccountId());
            }

            repository.insert(entity);
            Integer newId = repository.getLastInsertId();
            entity.setDetailId(newId);

            log.info("仕込品明細を登録しました: ID={}", newId);
            return entity;

        } catch (Exception e) {
            log.error("仕込品明細の登録に失敗: {}", e.getMessage(), e);
            throw new DataAccessFailedException("登録", e);
        }
    }

    @Transactional
    public FoodPreProductDetailItem update(FoodPreProductDetailItem entity) {
        if (entity.getDetailId() == null) {
            throw new IllegalArgumentException("明細IDが指定されていません");
        }

        repository.findById(entity.getDetailId())
            .orElseThrow(() -> new EntityNotFoundException("仕込品明細", entity.getDetailId()));

        try {
            repository.update(entity);
            log.info("仕込品明細を更新しました: ID={}", entity.getDetailId());
            return entity;

        } catch (Exception e) {
            log.error("仕込品明細の更新に失敗: ID={}, {}", entity.getDetailId(), e.getMessage(), e);
            throw new DataAccessFailedException("更新", e);
        }
    }

    @Transactional
    public FoodPreProductDetailItem updateWithBusinessAccountFilter(FoodPreProductDetailItem entity) {
        if (entity.getDetailId() == null) {
            throw new IllegalArgumentException("明細IDが指定されていません");
        }

        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        repository.findByIdAndBusinessAccountId(entity.getDetailId(), businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("仕込品明細", entity.getDetailId()));

        // businessAccountIdを設定
        entity.setBusinessAccountId(businessAccountId);

        try {
            repository.update(entity);
            log.info("仕込品明細を更新しました: ID={}", entity.getDetailId());
            return entity;

        } catch (Exception e) {
            log.error("仕込品明細の更新に失敗: ID={}, {}", entity.getDetailId(), e.getMessage(), e);
            throw new DataAccessFailedException("更新", e);
        }
    }

    @Transactional
    public void delete(Integer id) {
        repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("仕込品明細", id));

        try {
            repository.delete(id);
            log.info("仕込品明細を削除しました: ID={}", id);

        } catch (Exception e) {
            log.error("仕込品明細の削除に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }

    @Transactional
    public void deleteWithBusinessAccountFilter(Integer id) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        repository.findByIdAndBusinessAccountId(id, businessAccountId)
            .orElseThrow(() -> new EntityNotFoundException("仕込品明細", id));

        try {
            repository.deleteByIdAndBusinessAccountId(id, businessAccountId);
            log.info("仕込品明細を削除しました: ID={}", id);

        } catch (Exception e) {
            log.error("仕込品明細の削除に失敗: ID={}, {}", id, e.getMessage(), e);
            throw new DataAccessFailedException("削除", e);
        }
    }
}
