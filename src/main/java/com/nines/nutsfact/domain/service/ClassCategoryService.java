package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.domain.model.master.ClassCategory;
import com.nines.nutsfact.domain.repository.ClassCategoryRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassCategoryService {

    private final ClassCategoryRepository classCategoryRepository;

    public List<ClassCategory> findAll() {
        return classCategoryRepository.findAll();
    }

    public List<ClassCategory> findByType(Integer categoryType) {
        return classCategoryRepository.findByType(categoryType);
    }

    public ClassCategory findById(Integer categoryId) {
        return classCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassCategory", categoryId));
    }

    @Transactional
    public ClassCategory create(ClassCategory classCategory) {
        classCategoryRepository.save(classCategory);
        return classCategory;
    }

    @Transactional
    public ClassCategory update(Integer categoryId, ClassCategory classCategory) {
        findById(categoryId);
        classCategory.setCategoryId(categoryId);
        classCategoryRepository.save(classCategory);
        return classCategory;
    }

    @Transactional
    public void delete(Integer categoryId) {
        findById(categoryId);
        classCategoryRepository.delete(categoryId);
    }
}
