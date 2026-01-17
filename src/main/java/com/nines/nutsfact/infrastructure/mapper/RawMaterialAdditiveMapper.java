package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.additive.RawMaterialAdditive;

@Mapper
public interface RawMaterialAdditiveMapper {
    List<RawMaterialAdditive> findByFoodId(@Param("foodId") Integer foodId);
    List<RawMaterialAdditive> findByFoodIdWithAdditive(@Param("foodId") Integer foodId);
    List<RawMaterialAdditive> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);
    RawMaterialAdditive findById(@Param("id") Integer id);
    RawMaterialAdditive findByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);
    void insert(RawMaterialAdditive rawMaterialAdditive);
    void update(RawMaterialAdditive rawMaterialAdditive);
    void delete(@Param("id") Integer id);
    void deleteByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);
    void deleteByFoodId(@Param("foodId") Integer foodId);
}
