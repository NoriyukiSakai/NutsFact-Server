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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nines.nutsfact.api.v1.request.AdditiveRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.domain.model.additive.Additive;
import com.nines.nutsfact.domain.service.AdditiveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/additives")
@RequiredArgsConstructor
public class AdditiveController {

    private final AdditiveService additiveService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Additive>>> findAll() {
        List<Additive> additives = additiveService.findAllWithBusinessAccountFilter();
        return ResponseEntity.ok(ApiResponse.success(additives, additives.size()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Additive>>> findActive() {
        List<Additive> additives = additiveService.findActiveWithBusinessAccountFilter();
        return ResponseEntity.ok(ApiResponse.success(additives, additives.size()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Additive>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer purposeCategory) {
        List<Additive> additives = additiveService.search(keyword, purposeCategory);
        return ResponseEntity.ok(ApiResponse.success(additives, additives.size()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Additive>> findById(@PathVariable("id") Integer id) {
        Additive additive = additiveService.findByIdWithBusinessAccountFilter(id);
        return ResponseEntity.ok(ApiResponse.success(additive));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Additive>> create(@Valid @RequestBody AdditiveRequest request) {
        Additive additive = convertToEntity(request);
        Additive created = additiveService.create(additive);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Additive>> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody AdditiveRequest request) {
        Additive additive = convertToEntity(request);
        Additive updated = additiveService.updateWithBusinessAccountFilter(id, additive);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Integer id) {
        additiveService.deleteWithBusinessAccountFilter(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 本部の添加物マスタ一覧を取得
     */
    @GetMapping("/master")
    public ResponseEntity<ApiResponse<List<Additive>>> findMasterAdditives() {
        List<Additive> additives = additiveService.findMasterAdditives();
        return ResponseEntity.ok(ApiResponse.success(additives, additives.size()));
    }

    /**
     * 本部の添加物マスタを自分のビジネスアカウントにコピー
     */
    @PostMapping("/copy-master")
    public ResponseEntity<ApiResponse<CopyMasterResponse>> copyMasterAdditives() {
        int copiedCount = additiveService.copyMasterAdditives();
        return ResponseEntity.ok(ApiResponse.success(new CopyMasterResponse(copiedCount)));
    }

    private Additive convertToEntity(AdditiveRequest request) {
        return Additive.builder()
                .additiveCode(request.getAdditiveCode())
                .substanceName(request.getSubstanceName())
                .simplifiedName(request.getSimplifiedName())
                .purposeCategory(request.getPurposeCategory())
                .collectiveName(request.getCollectiveName())
                .requiresPurposeDisplay(request.getRequiresPurposeDisplay())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .build();
    }

    /**
     * 本部マスタコピーのレスポンス
     */
    public record CopyMasterResponse(int copiedCount) {}
}
