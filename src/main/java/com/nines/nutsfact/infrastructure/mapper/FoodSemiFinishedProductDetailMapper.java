package com.nines.nutsfact.infrastructure.mapper;

import com.nines.nutsfact.domain.model.FoodSemiFinishedProductDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FoodSemiFinishedProductDetailMapper {

    List<FoodSemiFinishedProductDetail> findBySemiId(@Param("semiId") Integer semiId);

    Optional<FoodSemiFinishedProductDetail> findById(@Param("id") Integer id);

    List<FoodSemiFinishedProductDetail> findBySemiIdAndBusinessAccountId(
            @Param("semiId") Integer semiId,
            @Param("businessAccountId") Integer businessAccountId);

    Optional<FoodSemiFinishedProductDetail> findByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);

    int insert(@Param("entity") FoodSemiFinishedProductDetail entity);

    int update(@Param("entity") FoodSemiFinishedProductDetail entity);

    int delete(@Param("id") Integer id);

    int deleteBySemiId(@Param("semiId") Integer semiId);

    int deleteByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);

    int deleteBySemiIdAndBusinessAccountId(
            @Param("semiId") Integer semiId,
            @Param("businessAccountId") Integer businessAccountId);

    Integer getLastInsertId();

    /**
     * 原材料IDによる参照件数を取得
     */
    int countByDetailFoodId(@Param("foodId") Integer foodId);
}
