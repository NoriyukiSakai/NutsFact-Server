package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.master.FoodGroup;

@Mapper
public interface FoodGroupMapper {
    List<FoodGroup> findAll();
    FoodGroup findById(@Param("foodGroupId") Integer foodGroupId);
    void insert(FoodGroup foodGroup);
    void update(FoodGroup foodGroup);
    void delete(@Param("foodGroupId") Integer foodGroupId);
}
