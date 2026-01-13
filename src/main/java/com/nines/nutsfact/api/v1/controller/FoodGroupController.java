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

import com.nines.nutsfact.api.v1.request.FoodGroupRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.domain.model.master.FoodGroup;
import com.nines.nutsfact.domain.service.FoodGroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/food-groups")
@RequiredArgsConstructor
public class FoodGroupController {

    private final FoodGroupService foodGroupService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodGroup>>> findAll() {
        List<FoodGroup> foodGroups = foodGroupService.findAll();
        return ResponseEntity.ok(ApiResponse.success(foodGroups));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodGroup>> findById(@PathVariable("id") Integer id) {
        FoodGroup foodGroup = foodGroupService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(foodGroup));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FoodGroup>> create(@Valid @RequestBody FoodGroupRequest request) {
        FoodGroup foodGroup = new FoodGroup();
        foodGroup.setFoodGroupId(request.getFoodGroupId());
        foodGroup.setFoodGroupName(request.getFoodGroupName());
        foodGroup.setDescription(request.getDescription());

        FoodGroup created = foodGroupService.create(foodGroup);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodGroup>> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody FoodGroupRequest request) {
        FoodGroup foodGroup = new FoodGroup();
        foodGroup.setFoodGroupName(request.getFoodGroupName());
        foodGroup.setDescription(request.getDescription());

        FoodGroup updated = foodGroupService.update(id, foodGroup);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Integer id) {
        foodGroupService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
