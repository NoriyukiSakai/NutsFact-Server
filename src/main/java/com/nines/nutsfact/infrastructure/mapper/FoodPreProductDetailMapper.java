package com.nines.nutsfact.infrastructure.mapper;

import com.nines.nutsfact.domain.model.FoodPreProductDetailItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FoodPreProductDetailMapper {

    List<FoodPreProductDetailItem> findByPreId(@Param("preId") Integer preId);

    List<FoodPreProductDetailItem> findByPreIdAndBusinessAccountId(
            @Param("preId") Integer preId,
            @Param("businessAccountId") Integer businessAccountId);

    Optional<FoodPreProductDetailItem> findById(@Param("id") Integer id);

    Optional<FoodPreProductDetailItem> findByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);

    int insert(@Param("entity") FoodPreProductDetailItem entity);

    int update(@Param("entity") FoodPreProductDetailItem entity);

    int delete(@Param("id") Integer id);

    int deleteByPreId(@Param("preId") Integer preId);

    int deleteByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);

    int deleteByPreIdAndBusinessAccountId(
            @Param("preId") Integer preId,
            @Param("businessAccountId") Integer businessAccountId);

    Integer getLastInsertId();

    /**
     * 原材料IDによる参照件数を取得
     */
    int countByDetailFoodId(@Param("foodId") Integer foodId);
}
