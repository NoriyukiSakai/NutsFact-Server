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

import com.nines.nutsfact.api.v1.request.MakerRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.domain.model.master.Maker;
import com.nines.nutsfact.domain.service.MakerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/makers")
@RequiredArgsConstructor
public class MakerController {

    private final MakerService makerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Maker>>> findAll() {
        List<Maker> makers = makerService.findAllWithBusinessAccountFilter();
        return ResponseEntity.ok(ApiResponse.success(makers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Maker>> findById(@PathVariable("id") Integer id) {
        Maker maker = makerService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(maker));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Maker>> create(@Valid @RequestBody MakerRequest request) {
        Maker maker = convertToEntity(request);
        Maker created = makerService.create(maker);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Maker>> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody MakerRequest request) {
        Maker maker = convertToEntity(request);
        Maker updated = makerService.update(id, maker);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    private Maker convertToEntity(MakerRequest request) {
        Maker maker = new Maker();
        maker.setMakerName(request.getMakerName());
        maker.setContactInfo(request.getContactInfo());
        maker.setAddress(request.getAddress());
        maker.setPhoneNumber(request.getPhoneNumber());
        maker.setEmail(request.getEmail());
        maker.setIsActive(request.getIsActive());
        return maker;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Integer id) {
        makerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
