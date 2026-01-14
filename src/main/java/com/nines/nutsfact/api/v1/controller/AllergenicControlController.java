package com.nines.nutsfact.api.v1.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nines.nutsfact.api.v1.request.AllergenicControlRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.domain.model.allergy.AllergenicControl;
import com.nines.nutsfact.domain.service.AllergenicControlService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/allergenic-controls")
@RequiredArgsConstructor
public class AllergenicControlController {

    private final AllergenicControlService allergenicControlService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AllergenicControl>>> findAll() {
        List<AllergenicControl> controls = allergenicControlService.findAllWithBusinessAccountFilter();
        return ResponseEntity.ok(ApiResponse.success(controls));
    }

    @GetMapping("/food/{foodId}")
    public ResponseEntity<ApiResponse<AllergenicControl>> findByFoodId(@PathVariable("foodId") Integer foodId) {
        AllergenicControl control = allergenicControlService.findByFoodIdWithBusinessAccountFilter(foodId);
        return ResponseEntity.ok(ApiResponse.success(control));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AllergenicControl>> save(@Valid @RequestBody AllergenicControlRequest request) {
        AllergenicControl control = convertToEntity(request);
        AllergenicControl saved = allergenicControlService.save(control);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/food/{foodId}")
    public ResponseEntity<ApiResponse<AllergenicControl>> update(
            @PathVariable("foodId") Integer foodId,
            @Valid @RequestBody AllergenicControlRequest request) {
        request.setFoodId(foodId);
        AllergenicControl control = convertToEntity(request);
        AllergenicControl saved = allergenicControlService.save(control);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @DeleteMapping("/food/{foodId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("foodId") Integer foodId) {
        allergenicControlService.deleteWithBusinessAccountFilter(foodId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private AllergenicControl convertToEntity(AllergenicControlRequest request) {
        AllergenicControl control = new AllergenicControl();
        control.setFoodId(request.getFoodId());
        control.setItem1Val(request.getItem1Val());
        control.setItem2Val(request.getItem2Val());
        control.setItem3Val(request.getItem3Val());
        control.setItem4Val(request.getItem4Val());
        control.setItem5Val(request.getItem5Val());
        control.setItem6Val(request.getItem6Val());
        control.setItem7Val(request.getItem7Val());
        control.setItem8Val(request.getItem8Val());
        control.setItem9Val(request.getItem9Val());
        control.setItem10Val(request.getItem10Val());
        control.setItem11Val(request.getItem11Val());
        control.setItem12Val(request.getItem12Val());
        control.setItem13Val(request.getItem13Val());
        control.setItem14Val(request.getItem14Val());
        control.setItem15Val(request.getItem15Val());
        control.setItem16Val(request.getItem16Val());
        control.setItem17Val(request.getItem17Val());
        control.setItem18Val(request.getItem18Val());
        control.setItem19Val(request.getItem19Val());
        control.setItem20Val(request.getItem20Val());
        control.setItem21Val(request.getItem21Val());
        control.setItem22Val(request.getItem22Val());
        control.setItem23Val(request.getItem23Val());
        control.setItem24Val(request.getItem24Val());
        control.setItem25Val(request.getItem25Val());
        control.setItem26Val(request.getItem26Val());
        control.setItem27Val(request.getItem27Val());
        control.setItem28Val(request.getItem28Val());
        control.setItem29Val(request.getItem29Val());
        control.setItem30Val(request.getItem30Val());
        return control;
    }
}
