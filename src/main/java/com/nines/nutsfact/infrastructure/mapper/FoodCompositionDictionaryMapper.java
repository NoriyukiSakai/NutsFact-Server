package com.nines.nutsfact.infrastructure.mapper;

import com.nines.nutsfact.domain.model.FoodCompositionDictionary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FoodCompositionDictionaryMapper {
    List<FoodCompositionDictionary> findAll();
    FoodCompositionDictionary findById(@Param("id") Integer id);
    List<FoodCompositionDictionary> findByFoodGroupId(@Param("foodGroupId") Integer foodGroupId);
    int insert(@Param("entity") FoodCompositionDictionary entity);
    int update(@Param("entity") FoodCompositionDictionary entity);
    int delete(@Param("id") Integer id);
    void truncate();
    int count();
}
