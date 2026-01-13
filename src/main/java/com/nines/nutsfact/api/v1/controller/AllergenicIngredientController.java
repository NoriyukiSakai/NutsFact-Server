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

import com.nines.nutsfact.api.v1.request.AllergenicIngredientRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.domain.model.allergy.AllergenicIngredient;
import com.nines.nutsfact.domain.service.AllergenicIngredientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/allergenic-ingredients")
@RequiredArgsConstructor
public class AllergenicIngredientController {

    private final AllergenicIngredientService allergenicIngredientService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AllergenicIngredient>>> findAll() {
        List<AllergenicIngredient> ingredients = allergenicIngredientService.findAll();
        return ResponseEntity.ok(ApiResponse.success(ingredients));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AllergenicIngredient>> findById(@PathVariable("id") Integer id) {
        AllergenicIngredient ingredient = allergenicIngredientService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(ingredient));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AllergenicIngredient>> create(
            @Valid @RequestBody AllergenicIngredientRequest request) {
        AllergenicIngredient ingredient = new AllergenicIngredient();
        ingredient.setAllergenicIngredientName(request.getAllergenicIngredientName());
        ingredient.setDescription(request.getDescription());
        ingredient.setCategory(request.getCategory());

        AllergenicIngredient created = allergenicIngredientService.create(ingredient);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AllergenicIngredient>> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody AllergenicIngredientRequest request) {
        AllergenicIngredient ingredient = new AllergenicIngredient();
        ingredient.setAllergenicIngredientName(request.getAllergenicIngredientName());
        ingredient.setDescription(request.getDescription());
        ingredient.setCategory(request.getCategory());

        AllergenicIngredient updated = allergenicIngredientService.update(id, ingredient);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Integer id) {
        allergenicIngredientService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
