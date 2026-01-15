package com.nines.nutsfact.api.v1.controller;

import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.domain.model.FoodCompositionDictionary;
import com.nines.nutsfact.domain.service.FoodCompositionDictionaryService;
import com.nines.nutsfact.domain.service.FoodCompositionDictionaryService.UploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 食品成分表辞書（8訂データ）管理API Controller
 * 運営管理者向け機能
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/food-composition-dictionary")
@RequiredArgsConstructor
public class FoodCompositionDictionaryController {

    private final FoodCompositionDictionaryService service;

    /**
     * 成分表辞書一覧取得
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodCompositionDictionary>>> findAll() {
        List<FoodCompositionDictionary> items = service.findAll();
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 成分表辞書詳細取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodCompositionDictionary>> findById(@PathVariable Integer id) {
        return service.findById(id)
            .map(item -> ResponseEntity.ok(ApiResponse.success(item)))
            .orElse(ResponseEntity.ok(ApiResponse.notFound("Food composition not found: " + id)));
    }

    /**
     * 食品群別成分表取得
     */
    @GetMapping("/food-group/{foodGroupId}")
    public ResponseEntity<ApiResponse<List<FoodCompositionDictionary>>> findByFoodGroupId(
            @PathVariable Integer foodGroupId) {
        List<FoodCompositionDictionary> items = service.findByFoodGroupId(foodGroupId);
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 登録件数取得
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> count() {
        int count = service.count();
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    /**
     * CSVファイルアップロード
     * 8訂成分表CSVをFOOD_COMPOSITION_DICTIONARYテーブルに登録
     */
    @PostMapping("/upload/csv")
    public ResponseEntity<ApiResponse<UploadResult>> uploadCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "historyId", defaultValue = "1") Integer historyId,
            @RequestParam(value = "truncate", defaultValue = "false") Boolean truncate) {

        log.info("CSV upload started: filename={}, historyId={}, truncate={}",
                file.getOriginalFilename(), historyId, truncate);

        if (truncate) {
            log.info("Truncating FOOD_COMPOSITION_DICTIONARY table");
            service.truncate();
        }

        UploadResult result = service.uploadCsv(file, historyId);
        log.info("CSV upload completed: success={}, errors={}",
                result.getSuccessCount(), result.getErrorCount());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Excelファイルアップロード
     * 8訂成分表Excel(.xlsx)をFOOD_COMPOSITION_DICTIONARYテーブルに登録
     */
    @PostMapping("/upload/excel")
    public ResponseEntity<ApiResponse<UploadResult>> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "historyId", defaultValue = "1") Integer historyId,
            @RequestParam(value = "truncate", defaultValue = "false") Boolean truncate) {

        log.info("Excel upload started: filename={}, historyId={}, truncate={}",
                file.getOriginalFilename(), historyId, truncate);

        if (truncate) {
            log.info("Truncating FOOD_COMPOSITION_DICTIONARY table");
            service.truncate();
        }

        UploadResult result = service.uploadExcel(file, historyId);
        log.info("Excel upload completed: success={}, errors={}",
                result.getSuccessCount(), result.getErrorCount());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * データ移送（FOOD_COMPOSITION_DICTIONARY → FOOD_RAW_MATERIALS）
     * 8訂成分表データをFOOD_RAW_MATERIALSテーブルに移送/更新
     */
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResult>> transferToRawMaterials(
            @RequestParam(value = "historyId", required = false) Integer historyId) {

        log.info("Data transfer started: historyId={}", historyId);

        TransferResult result = service.transferToRawMaterials(historyId);
        log.info("Data transfer completed: inserted={}, updated={}, errors={}",
                result.insertedCount(), result.updatedCount(), result.errorCount());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * テーブルクリア（開発用）
     */
    @DeleteMapping("/truncate")
    public ResponseEntity<ApiResponse<String>> truncate() {
        log.warn("Truncating FOOD_COMPOSITION_DICTIONARY table");
        service.truncate();
        return ResponseEntity.ok(ApiResponse.success("Table truncated successfully"));
    }

    /**
     * データ移送結果
     */
    public record TransferResult(
        int insertedCount,
        int updatedCount,
        int errorCount,
        List<String> errors
    ) {}
}
