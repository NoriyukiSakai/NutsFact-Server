package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.config.SecurityContextHelper;
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

    public List<ClassCategory> findAllWithBusinessAccountFilter() {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId != null) {
            return classCategoryRepository.findByBusinessAccountId(businessAccountId);
        }
        return classCategoryRepository.findAll();
    }

    public List<ClassCategory> findByType(Integer categoryType) {
        return classCategoryRepository.findByType(categoryType);
    }

    public List<ClassCategory> findByTypeWithBusinessAccountFilter(Integer categoryType) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId != null) {
            return classCategoryRepository.findByTypeAndBusinessAccountId(categoryType, businessAccountId);
        }
        return classCategoryRepository.findByType(categoryType);
    }

    public ClassCategory findById(Integer categoryId) {
        return classCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassCategory", categoryId));
    }

    public ClassCategory findByIdWithBusinessAccountFilter(Integer categoryId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        return classCategoryRepository.findByIdAndBusinessAccountId(categoryId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassCategory", categoryId));
    }

    @Transactional
    public ClassCategory create(ClassCategory classCategory) {
        // businessAccountIdを自動設定
        if (classCategory.getBusinessAccountId() == null) {
            classCategory.setBusinessAccountId(SecurityContextHelper.getCurrentBusinessAccountId());
        }
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
    public ClassCategory updateWithBusinessAccountFilter(Integer categoryId, ClassCategory classCategory) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        classCategoryRepository.findByIdAndBusinessAccountId(categoryId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassCategory", categoryId));
        classCategory.setCategoryId(categoryId);
        classCategory.setBusinessAccountId(businessAccountId);
        classCategoryRepository.save(classCategory);
        return classCategory;
    }

    @Transactional
    public void delete(Integer categoryId) {
        findById(categoryId);
        classCategoryRepository.delete(categoryId);
    }

    @Transactional
    public void deleteWithBusinessAccountFilter(Integer categoryId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        classCategoryRepository.findByIdAndBusinessAccountId(categoryId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassCategory", categoryId));
        classCategoryRepository.deleteByIdAndBusinessAccountId(categoryId, businessAccountId);
    }
}
