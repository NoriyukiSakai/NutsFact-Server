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

import com.nines.nutsfact.api.v1.request.SupplierRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.domain.model.master.Supplier;
import com.nines.nutsfact.domain.service.SupplierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Supplier>>> findAll() {
        List<Supplier> suppliers = supplierService.findAll();
        return ResponseEntity.ok(ApiResponse.success(suppliers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> findById(@PathVariable("id") Integer id) {
        Supplier supplier = supplierService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(supplier));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Supplier>> create(@Valid @RequestBody SupplierRequest request) {
        Supplier supplier = convertToEntity(request);
        Supplier created = supplierService.create(supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody SupplierRequest request) {
        Supplier supplier = convertToEntity(request);
        Supplier updated = supplierService.update(id, supplier);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    private Supplier convertToEntity(SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactInfo(request.getContactInfo());
        supplier.setAddress(request.getAddress());
        supplier.setPhoneNumber(request.getPhoneNumber());
        supplier.setEmail(request.getEmail());
        supplier.setIsActive(request.getIsActive());
        return supplier;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Integer id) {
        supplierService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
