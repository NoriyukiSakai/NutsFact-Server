package com.nines.nutsfact.api.v1.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nines.nutsfact.api.v1.request.ClassCategoryRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.domain.model.master.ClassCategory;
import com.nines.nutsfact.domain.service.ClassCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/class-categories")
@RequiredArgsConstructor
public class ClassCategoryController {

    private final ClassCategoryService classCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassCategory>>> findAll() {
        List<ClassCategory> categories = classCategoryService.findAll();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<ClassCategory>>> findByType(@PathVariable("type") Integer type) {
        List<ClassCategory> categories = classCategoryService.findByType(type);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassCategory>> findById(@PathVariable("id") Integer id) {
        ClassCategory category = classCategoryService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClassCategory>> create(@Valid @RequestBody ClassCategoryRequest request) {
        ClassCategory category = new ClassCategory();
        category.setCategoryType(request.getCategoryType());
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());

        ClassCategory created = classCategoryService.create(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassCategory>> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ClassCategoryRequest request) {
        ClassCategory category = new ClassCategory();
        category.setCategoryType(request.getCategoryType());
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());

        ClassCategory updated = classCategoryService.update(id, category);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Integer id) {
        classCategoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
