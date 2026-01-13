package com.nines.nutsfact.infrastructure.mapper;

import com.nines.nutsfact.domain.model.FoodRawMaterial;
import com.nines.nutsfact.domain.model.SelectItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FoodRawMaterialMapper {

    List<FoodRawMaterial> findAll();

    List<FoodRawMaterial> findByCategory(@Param("categoryId") Integer categoryId);

    List<SelectItem> findSelectItems(@Param("categoryId") Integer categoryId);

    Optional<FoodRawMaterial> findById(@Param("id") Integer id);

    Optional<FoodRawMaterial> findByFoodNo(@Param("foodNo") String foodNo);

    int insert(@Param("entity") FoodRawMaterial entity);

    int update(@Param("entity") FoodRawMaterial entity);

    int delete(@Param("id") Integer id);

    Integer getLastInsertId();
}
