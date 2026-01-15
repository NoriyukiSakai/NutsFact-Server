package com.nines.nutsfact.infrastructure.mapper;

import com.nines.nutsfact.domain.model.CompositeRawMaterialIngredient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CompositeRawMaterialIngredientMapper {
    List<CompositeRawMaterialIngredient> findByFoodId(@Param("foodId") Integer foodId);

    List<CompositeRawMaterialIngredient> findByFoodIdAndBusinessAccountId(
            @Param("foodId") Integer foodId,
            @Param("businessAccountId") Integer businessAccountId);

    Optional<CompositeRawMaterialIngredient> findById(@Param("id") Integer id);

    Optional<CompositeRawMaterialIngredient> findByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);

    int insert(@Param("entity") CompositeRawMaterialIngredient entity);

    int update(@Param("entity") CompositeRawMaterialIngredient entity);

    int delete(@Param("id") Integer id);

    int deleteByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);

    int deleteByFoodId(@Param("foodId") Integer foodId);

    int deleteByFoodIdAndBusinessAccountId(
            @Param("foodId") Integer foodId,
            @Param("businessAccountId") Integer businessAccountId);

    Integer getLastInsertId();
}
