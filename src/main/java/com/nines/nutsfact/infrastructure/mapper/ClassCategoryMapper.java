package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.master.ClassCategory;

@Mapper
public interface ClassCategoryMapper {
    List<ClassCategory> findAll();
    List<ClassCategory> findByType(@Param("categoryType") Integer categoryType);
    List<ClassCategory> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);
    List<ClassCategory> findByTypeAndBusinessAccountId(
            @Param("categoryType") Integer categoryType,
            @Param("businessAccountId") Integer businessAccountId);
    ClassCategory findById(@Param("categoryId") Integer categoryId);
    ClassCategory findByIdAndBusinessAccountId(
            @Param("categoryId") Integer categoryId,
            @Param("businessAccountId") Integer businessAccountId);
    void insert(ClassCategory classCategory);
    void update(ClassCategory classCategory);
    void delete(@Param("categoryId") Integer categoryId);
    void deleteByIdAndBusinessAccountId(
            @Param("categoryId") Integer categoryId,
            @Param("businessAccountId") Integer businessAccountId);
}
