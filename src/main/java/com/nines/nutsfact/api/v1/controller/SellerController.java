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

import com.nines.nutsfact.api.v1.request.SellerRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.domain.model.master.Seller;
import com.nines.nutsfact.domain.service.SellerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Seller>>> findAll() {
        List<Seller> sellers = sellerService.findAllWithBusinessAccountFilter();
        return ResponseEntity.ok(ApiResponse.success(sellers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> findById(@PathVariable("id") Integer id) {
        Seller seller = sellerService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(seller));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Seller>> create(@Valid @RequestBody SellerRequest request) {
        Seller seller = convertToEntity(request);
        Seller created = sellerService.create(seller);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody SellerRequest request) {
        Seller seller = convertToEntity(request);
        Seller updated = sellerService.update(id, seller);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    private Seller convertToEntity(SellerRequest request) {
        Seller seller = new Seller();
        seller.setSellerName(request.getSellerName());
        seller.setContactInfo(request.getContactInfo());
        seller.setAddress(request.getAddress());
        seller.setPhoneNumber(request.getPhoneNumber());
        seller.setEmail(request.getEmail());
        seller.setIsActive(request.getIsActive());
        return seller;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Integer id) {
        sellerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
