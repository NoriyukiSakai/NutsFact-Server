package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.master.ClassCategory;

@Mapper
public interface ClassCategoryMapper {
    List<ClassCategory> findAll();
    List<ClassCategory> findByType(@Param("categoryType") Integer categoryType);
    ClassCategory findById(@Param("categoryId") Integer categoryId);
    void insert(ClassCategory classCategory);
    void update(ClassCategory classCategory);
    void delete(@Param("categoryId") Integer categoryId);
}
