package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.allergy.AllergenicIngredient;

@Mapper
public interface AllergenicIngredientMapper {
    List<AllergenicIngredient> findAll();
    AllergenicIngredient findById(@Param("allergenicIngredientId") Integer allergenicIngredientId);
    void insert(AllergenicIngredient allergenicIngredient);
    void update(AllergenicIngredient allergenicIngredient);
    void delete(@Param("allergenicIngredientId") Integer allergenicIngredientId);
}
