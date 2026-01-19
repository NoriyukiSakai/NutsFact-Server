package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.config.SecurityContextHelper;
import com.nines.nutsfact.domain.model.additive.Additive;
import com.nines.nutsfact.domain.model.user.BusinessAccount;
import com.nines.nutsfact.domain.repository.AdditiveRepository;
import com.nines.nutsfact.domain.repository.BusinessAccountRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdditiveService {

    private final AdditiveRepository additiveRepository;
    private final BusinessAccountRepository businessAccountRepository;

    public List<Additive> findAll() {
        return additiveRepository.findAll();
    }

    public List<Additive> findAllWithBusinessAccountFilter() {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは添加物を参照できません");
        }
        return additiveRepository.findByBusinessAccountId(businessAccountId);
    }

    public List<Additive> findActiveWithBusinessAccountFilter() {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは添加物を参照できません");
        }
        return additiveRepository.findActiveByBusinessAccountId(businessAccountId);
    }

    public Additive findById(Integer additiveId) {
        return additiveRepository.findById(additiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Additive", additiveId));
    }

    public Additive findByIdWithBusinessAccountFilter(Integer additiveId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        return additiveRepository.findByIdAndBusinessAccountId(additiveId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Additive", additiveId));
    }

    public List<Additive> search(String keyword, Integer purposeCategory) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは添加物を検索できません");
        }
        return additiveRepository.search(businessAccountId, keyword, purposeCategory);
    }

    @Transactional
    public Additive create(Additive additive) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは添加物を登録できません");
        }
        additive.setBusinessAccountId(businessAccountId);
        if (additive.getIsActive() == null) {
            additive.setIsActive(true);
        }
        additiveRepository.save(additive);
        return additive;
    }

    @Transactional
    public Additive update(Integer additiveId, Additive additive) {
        findById(additiveId);
        additive.setAdditiveId(additiveId);
        additiveRepository.save(additive);
        return additive;
    }

    @Transactional
    public Additive updateWithBusinessAccountFilter(Integer additiveId, Additive additive) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        additiveRepository.findByIdAndBusinessAccountId(additiveId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Additive", additiveId));
        additive.setAdditiveId(additiveId);
        additive.setBusinessAccountId(businessAccountId);
        additiveRepository.save(additive);
        return additive;
    }

    @Transactional
    public void delete(Integer additiveId) {
        findById(additiveId);
        additiveRepository.delete(additiveId);
    }

    @Transactional
    public void deleteWithBusinessAccountFilter(Integer additiveId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        additiveRepository.findByIdAndBusinessAccountId(additiveId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Additive", additiveId));
        additiveRepository.deleteByIdAndBusinessAccountId(additiveId, businessAccountId);
    }

    // ========== 本部マスタ関連 ==========

    /**
     * 本部の添加物マスタを取得
     * @return 本部の有効な添加物リスト
     */
    public List<Additive> findMasterAdditives() {
        BusinessAccount headquarters = businessAccountRepository.findHeadquarters()
                .orElseThrow(() -> new ResourceNotFoundException("本部アカウントが見つかりません"));
        return additiveRepository.findByHeadquarters(headquarters.getId());
    }

    /**
     * コピー可能な添加物の件数を取得（添加物コードが重複しないもののみ）
     * @return コピー可能な件数
     */
    public int countCopyableMasterAdditives() {
        Integer currentBusinessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (currentBusinessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは参照できません");
        }

        BusinessAccount headquarters = businessAccountRepository.findHeadquarters()
                .orElseThrow(() -> new ResourceNotFoundException("本部アカウントが見つかりません"));

        // 本部と同じアカウントの場合は0を返す
        if (headquarters.getId().equals(currentBusinessAccountId)) {
            return 0;
        }

        return additiveRepository.countCopyableFromHeadquarters(headquarters.getId(), currentBusinessAccountId);
    }

    /**
     * 本部の添加物マスタを現在のビジネスアカウントにコピー（添加物コードが重複しないもののみ）
     * @return コピーされた件数
     */
    @Transactional
    public int copyMasterAdditives() {
        Integer currentBusinessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (currentBusinessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーはコピーできません");
        }

        BusinessAccount headquarters = businessAccountRepository.findHeadquarters()
                .orElseThrow(() -> new ResourceNotFoundException("本部アカウントが見つかりません"));

        // 本部と同じアカウントの場合はコピー不可
        if (headquarters.getId().equals(currentBusinessAccountId)) {
            throw new IllegalStateException("本部アカウントは自身からコピーできません");
        }

        return additiveRepository.copyFromHeadquarters(headquarters.getId(), currentBusinessAccountId);
    }
}
