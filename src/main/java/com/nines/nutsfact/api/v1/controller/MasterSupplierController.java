package com.nines.nutsfact.api.v1.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nines.nutsfact.api.v1.request.SupplierRequest;
import com.nines.nutsfact.domain.model.master.Supplier;
import com.nines.nutsfact.domain.service.SupplierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/apix/MasterSupplier")
@RequiredArgsConstructor
public class MasterSupplierController {

    private final SupplierService supplierService;

    @GetMapping("/getData")
    public ResponseEntity<Map<String, Object>> getData() {
        List<Supplier> suppliers = supplierService.findAllWithBusinessAccountFilter();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", suppliers.size());
        response.put("item", suppliers);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(@RequestParam("id") Integer id) {
        Supplier supplier = supplierService.findByIdWithBusinessAccountFilter(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("supplierId", supplier.getSupplierId());
        response.put("supplierName", supplier.getSupplierName());
        response.put("contactInfo", supplier.getContactInfo());
        response.put("address", supplier.getAddress());
        response.put("phoneNumber", supplier.getPhoneNumber());
        response.put("email", supplier.getEmail());
        response.put("isActive", supplier.getIsActive());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(@Valid @RequestBody SupplierRequest request) {
        Supplier supplier = convertToEntity(request);
        Supplier created = supplierService.create(supplier);
        return ResponseEntity.ok(buildResponse(created));
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody SupplierRequest request) {
        Supplier supplier = convertToEntity(request);
        Supplier updated = supplierService.updateWithBusinessAccountFilter(request.getSupplierId(), supplier);
        return ResponseEntity.ok(buildResponse(updated));
    }

    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestParam("id") Integer id) {
        supplierService.deleteWithBusinessAccountFilter(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getSelect")
    public ResponseEntity<Map<String, Object>> getSelect() {
        List<Supplier> suppliers = supplierService.findAllWithBusinessAccountFilter();
        List<Map<String, Object>> selectItems = suppliers.stream()
            .filter(s -> s.getIsActive() != null && s.getIsActive())
            .map(s -> {
                Map<String, Object> item = new HashMap<>();
                item.put("value", s.getSupplierId());
                item.put("label", s.getSupplierName());
                return item;
            })
            .toList();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", selectItems.size());
        response.put("item", selectItems);
        return ResponseEntity.ok(response);
    }

    private Supplier convertToEntity(SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setSupplierId(request.getSupplierId());
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactInfo(request.getContactInfo());
        supplier.setAddress(request.getAddress());
        supplier.setPhoneNumber(request.getPhoneNumber());
        supplier.setEmail(request.getEmail());
        supplier.setIsActive(request.getIsActive());
        return supplier;
    }

    private Map<String, Object> buildResponse(Supplier supplier) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("supplierId", supplier.getSupplierId());
        response.put("supplierName", supplier.getSupplierName());
        response.put("contactInfo", supplier.getContactInfo());
        response.put("address", supplier.getAddress());
        response.put("phoneNumber", supplier.getPhoneNumber());
        response.put("email", supplier.getEmail());
        response.put("isActive", supplier.getIsActive());
        return response;
    }
}
