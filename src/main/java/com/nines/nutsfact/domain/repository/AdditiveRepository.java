package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.additive.Additive;
import com.nines.nutsfact.infrastructure.mapper.AdditiveMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdditiveRepository {

    private final AdditiveMapper additiveMapper;

    public List<Additive> findAll() {
        return additiveMapper.findAll();
    }

    public List<Additive> findByBusinessAccountId(Integer businessAccountId) {
        return additiveMapper.findByBusinessAccountId(businessAccountId);
    }

    public List<Additive> findActiveByBusinessAccountId(Integer businessAccountId) {
        return additiveMapper.findActiveByBusinessAccountId(businessAccountId);
    }

    public Optional<Additive> findById(Integer additiveId) {
        return Optional.ofNullable(additiveMapper.findById(additiveId));
    }

    public Optional<Additive> findByIdAndBusinessAccountId(Integer additiveId, Integer businessAccountId) {
        return Optional.ofNullable(additiveMapper.findByIdAndBusinessAccountId(additiveId, businessAccountId));
    }

    public List<Additive> search(Integer businessAccountId, String keyword, Integer purposeCategory) {
        return additiveMapper.search(businessAccountId, keyword, purposeCategory);
    }

    public void save(Additive additive) {
        if (additive.getAdditiveId() == null) {
            additiveMapper.insert(additive);
        } else {
            additiveMapper.update(additive);
        }
    }

    public void delete(Integer additiveId) {
        additiveMapper.delete(additiveId);
    }

    public void deleteByIdAndBusinessAccountId(Integer additiveId, Integer businessAccountId) {
        additiveMapper.deleteByIdAndBusinessAccountId(additiveId, businessAccountId);
    }

    /**
     * 本部の添加物マスタを取得
     * @param headquartersBusinessAccountId 本部のビジネスアカウントID
     * @return 本部の有効な添加物リスト
     */
    public List<Additive> findByHeadquarters(Integer headquartersBusinessAccountId) {
        return additiveMapper.findByHeadquarters(headquartersBusinessAccountId);
    }

    /**
     * 本部の添加物マスタを指定のビジネスアカウントにコピー（添加物コードが重複しないもののみ）
     * @param sourceBusinessAccountId コピー元（本部）のビジネスアカウントID
     * @param targetBusinessAccountId コピー先のビジネスアカウントID
     * @return コピーされた件数
     */
    public int copyFromHeadquarters(Integer sourceBusinessAccountId, Integer targetBusinessAccountId) {
        return additiveMapper.copyFromHeadquarters(sourceBusinessAccountId, targetBusinessAccountId);
    }

    /**
     * コピー可能な添加物の件数を取得（添加物コードが重複しないもののみ）
     * @param sourceBusinessAccountId コピー元（本部）のビジネスアカウントID
     * @param targetBusinessAccountId コピー先のビジネスアカウントID
     * @return コピー可能な件数
     */
    public int countCopyableFromHeadquarters(Integer sourceBusinessAccountId, Integer targetBusinessAccountId) {
        return additiveMapper.countCopyableFromHeadquarters(sourceBusinessAccountId, targetBusinessAccountId);
    }
}
