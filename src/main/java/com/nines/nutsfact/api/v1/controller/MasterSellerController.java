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

import com.nines.nutsfact.api.v1.request.SellerRequest;
import com.nines.nutsfact.domain.model.master.Seller;
import com.nines.nutsfact.domain.service.SellerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/apix/MasterSaller")
@RequiredArgsConstructor
public class MasterSellerController {

    private final SellerService sellerService;

    @GetMapping("/getData")
    public ResponseEntity<Map<String, Object>> getData() {
        List<Seller> sellers = sellerService.findAllWithBusinessAccountFilter();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", sellers.size());
        response.put("item", sellers);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(@RequestParam("id") Integer id) {
        Seller seller = sellerService.findByIdWithBusinessAccountFilter(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("sellerId", seller.getSellerId());
        response.put("sellerName", seller.getSellerName());
        response.put("contactInfo", seller.getContactInfo());
        response.put("address", seller.getAddress());
        response.put("phoneNumber", seller.getPhoneNumber());
        response.put("email", seller.getEmail());
        response.put("isActive", seller.getIsActive());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(@Valid @RequestBody SellerRequest request) {
        Seller seller = convertToEntity(request);
        Seller created = sellerService.create(seller);
        return ResponseEntity.ok(buildResponse(created));
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody SellerRequest request) {
        Seller seller = convertToEntity(request);
        Seller updated = sellerService.updateWithBusinessAccountFilter(request.getSellerId(), seller);
        return ResponseEntity.ok(buildResponse(updated));
    }

    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestParam("id") Integer id) {
        sellerService.deleteWithBusinessAccountFilter(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getSelect")
    public ResponseEntity<Map<String, Object>> getSelect() {
        List<Seller> sellers = sellerService.findAllWithBusinessAccountFilter();
        List<Map<String, Object>> selectItems = sellers.stream()
            .filter(s -> s.getIsActive() != null && s.getIsActive())
            .map(s -> {
                Map<String, Object> item = new HashMap<>();
                item.put("value", s.getSellerId());
                item.put("label", s.getSellerName());
                return item;
            })
            .toList();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", selectItems.size());
        response.put("item", selectItems);
        return ResponseEntity.ok(response);
    }

    private Seller convertToEntity(SellerRequest request) {
        Seller seller = new Seller();
        seller.setSellerId(request.getSellerId());
        seller.setSellerName(request.getSellerName());
        seller.setContactInfo(request.getContactInfo());
        seller.setAddress(request.getAddress());
        seller.setPhoneNumber(request.getPhoneNumber());
        seller.setEmail(request.getEmail());
        seller.setIsActive(request.getIsActive());
        return seller;
    }

    private Map<String, Object> buildResponse(Seller seller) {
        Map<String, Object> item = new HashMap<>();
        item.put("sellerId", seller.getSellerId());
        item.put("sellerName", seller.getSellerName());
        item.put("contactInfo", seller.getContactInfo());
        item.put("address", seller.getAddress());
        item.put("phoneNumber", seller.getPhoneNumber());
        item.put("email", seller.getEmail());
        item.put("isActive", seller.getIsActive());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("item", item);
        return response;
    }
}
