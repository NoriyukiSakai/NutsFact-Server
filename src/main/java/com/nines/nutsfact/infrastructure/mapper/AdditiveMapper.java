package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.additive.Additive;

@Mapper
public interface AdditiveMapper {
    List<Additive> findAll();
    List<Additive> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);
    List<Additive> findActiveByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);
    Additive findById(@Param("additiveId") Integer additiveId);
    Additive findByIdAndBusinessAccountId(
            @Param("additiveId") Integer additiveId,
            @Param("businessAccountId") Integer businessAccountId);
    List<Additive> search(
            @Param("businessAccountId") Integer businessAccountId,
            @Param("keyword") String keyword,
            @Param("purposeCategory") Integer purposeCategory);
    void insert(Additive additive);
    void update(Additive additive);
    void delete(@Param("additiveId") Integer additiveId);
    void deleteByIdAndBusinessAccountId(
            @Param("additiveId") Integer additiveId,
            @Param("businessAccountId") Integer businessAccountId);

    /**
     * 本部の添加物マスタを取得
     * @param headquartersBusinessAccountId 本部のビジネスアカウントID
     * @return 本部の有効な添加物リスト
     */
    List<Additive> findByHeadquarters(@Param("headquartersBusinessAccountId") Integer headquartersBusinessAccountId);

    /**
     * 本部の添加物マスタを指定のビジネスアカウントにコピー（添加物コードが重複しないもののみ）
     * @param sourceBusinessAccountId コピー元（本部）のビジネスアカウントID
     * @param targetBusinessAccountId コピー先のビジネスアカウントID
     * @return コピーされた件数
     */
    int copyFromHeadquarters(
            @Param("sourceBusinessAccountId") Integer sourceBusinessAccountId,
            @Param("targetBusinessAccountId") Integer targetBusinessAccountId);

    /**
     * コピー可能な添加物の件数を取得（添加物コードが重複しないもののみ）
     * @param sourceBusinessAccountId コピー元（本部）のビジネスアカウントID
     * @param targetBusinessAccountId コピー先のビジネスアカウントID
     * @return コピー可能な件数
     */
    int countCopyableFromHeadquarters(
            @Param("sourceBusinessAccountId") Integer sourceBusinessAccountId,
            @Param("targetBusinessAccountId") Integer targetBusinessAccountId);
}
