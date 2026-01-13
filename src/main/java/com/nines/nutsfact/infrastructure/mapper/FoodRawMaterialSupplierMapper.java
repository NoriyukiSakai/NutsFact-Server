package com.nines.nutsfact.infrastructure.mapper;

import com.nines.nutsfact.domain.model.FoodRawMaterialSupplier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FoodRawMaterialSupplierMapper {
    List<FoodRawMaterialSupplier> findByFoodId(@Param("foodId") Integer foodId);
    Optional<FoodRawMaterialSupplier> findById(@Param("id") Integer id);
    int insert(@Param("entity") FoodRawMaterialSupplier entity);
    int update(@Param("entity") FoodRawMaterialSupplier entity);
    int delete(@Param("id") Integer id);
    Integer getLastInsertId();
}
