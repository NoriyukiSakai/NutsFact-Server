package com.nines.nutsfact.infrastructure.mapper;

import com.nines.nutsfact.domain.model.FoodSemiFinishedProduct;
import com.nines.nutsfact.domain.model.SelectItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FoodSemiFinishedProductMapper {

    List<FoodSemiFinishedProduct> findAll();

    List<FoodSemiFinishedProduct> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);

    List<FoodSemiFinishedProduct> findByBusinessAccountIdIsNull();

    List<SelectItem> findSelectItems(@Param("businessAccountId") Integer businessAccountId);

    Optional<FoodSemiFinishedProduct> findById(@Param("id") Integer id);

    Optional<FoodSemiFinishedProduct> findByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);

    int insert(@Param("entity") FoodSemiFinishedProduct entity);

    int update(@Param("entity") FoodSemiFinishedProduct entity);

    int updateAllergenSummary(
            @Param("id") Integer id,
            @Param("allergenSummary") String allergenSummary);

    int delete(@Param("id") Integer id);

    int deleteByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);

    Integer getLastInsertId();
}
