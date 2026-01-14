package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.master.ClassCategory;
import com.nines.nutsfact.infrastructure.mapper.ClassCategoryMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ClassCategoryRepository {

    private final ClassCategoryMapper classCategoryMapper;

    public List<ClassCategory> findAll() {
        return classCategoryMapper.findAll();
    }

    public List<ClassCategory> findByType(Integer categoryType) {
        return classCategoryMapper.findByType(categoryType);
    }

    public List<ClassCategory> findByBusinessAccountId(Integer businessAccountId) {
        return classCategoryMapper.findByBusinessAccountId(businessAccountId);
    }

    public List<ClassCategory> findByTypeAndBusinessAccountId(Integer categoryType, Integer businessAccountId) {
        return classCategoryMapper.findByTypeAndBusinessAccountId(categoryType, businessAccountId);
    }

    public Optional<ClassCategory> findById(Integer categoryId) {
        return Optional.ofNullable(classCategoryMapper.findById(categoryId));
    }

    public Optional<ClassCategory> findByIdAndBusinessAccountId(Integer categoryId, Integer businessAccountId) {
        return Optional.ofNullable(classCategoryMapper.findByIdAndBusinessAccountId(categoryId, businessAccountId));
    }

    public void save(ClassCategory classCategory) {
        if (classCategory.getCategoryId() == null || classCategory.getCategoryId() == 0) {
            classCategoryMapper.insert(classCategory);
        } else {
            classCategoryMapper.update(classCategory);
        }
    }

    public void delete(Integer categoryId) {
        classCategoryMapper.delete(categoryId);
    }

    public void deleteByIdAndBusinessAccountId(Integer categoryId, Integer businessAccountId) {
        classCategoryMapper.deleteByIdAndBusinessAccountId(categoryId, businessAccountId);
    }
}
