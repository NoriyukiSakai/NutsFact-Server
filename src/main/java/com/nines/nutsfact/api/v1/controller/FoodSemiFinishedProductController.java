package com.nines.nutsfact.api.v1.controller;

import com.nines.nutsfact.api.v1.request.FoodSemiFinishedProductRequest;
import com.nines.nutsfact.api.v1.response.ApiResponse;
import com.nines.nutsfact.api.v1.response.DeleteResponse;
import com.nines.nutsfact.domain.model.FoodSemiFinishedProduct;
import com.nines.nutsfact.domain.model.SelectItem;
import com.nines.nutsfact.domain.model.additive.AdditiveSummary;
import com.nines.nutsfact.domain.model.allergy.AllergenSummary;
import com.nines.nutsfact.domain.service.AdditiveSummaryService;
import com.nines.nutsfact.domain.service.AllergenAggregationService;
import com.nines.nutsfact.domain.service.FoodLabelPdfService;
import com.nines.nutsfact.domain.service.FoodSemiFinishedProductService;
import com.nines.nutsfact.domain.service.IngredientExpansionService;
import com.nines.nutsfact.infrastructure.converter.FoodSemiFinishedProductConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 半完成品API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/food-semi-finished-products")
@RequiredArgsConstructor
public class FoodSemiFinishedProductController {

    private final FoodSemiFinishedProductService service;
    private final FoodSemiFinishedProductConverter converter;
    private final AllergenAggregationService allergenAggregationService;
    private final AdditiveSummaryService additiveSummaryService;
    private final IngredientExpansionService ingredientExpansionService;
    private final FoodLabelPdfService foodLabelPdfService;

    /**
     * 半完成品一覧取得（businessAccountIdでフィルタリング）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodSemiFinishedProduct>>> findAll() {
        List<FoodSemiFinishedProduct> items = service.findAllWithBusinessAccountFilter();
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 選択リスト用半完成品一覧取得
     */
    @GetMapping("/select-items")
    public ResponseEntity<ApiResponse<List<SelectItem>>> findSelectItems() {
        List<SelectItem> items = service.findSelectItems();
        return ResponseEntity.ok(ApiResponse.success(items, items.size()));
    }

    /**
     * 半完成品詳細取得（明細含む、businessAccountIdでフィルタリング）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodSemiFinishedProduct>> findById(@PathVariable Integer id) {
        FoodSemiFinishedProduct item = service.findByIdWithBusinessAccountFilter(id);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    /**
     * 半完成品新規作成（businessAccountIdを自動設定）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FoodSemiFinishedProduct>> create(
            @Valid @RequestBody FoodSemiFinishedProductRequest request) {
        FoodSemiFinishedProduct entity = converter.toEntity(request);
        FoodSemiFinishedProduct created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(created));
    }

    /**
     * 半完成品更新（businessAccountIdでフィルタリング）
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodSemiFinishedProduct>> update(
            @PathVariable Integer id,
            @Valid @RequestBody FoodSemiFinishedProductRequest request) {
        request.setSemiId(id);
        FoodSemiFinishedProduct entity = converter.toEntity(request);
        FoodSemiFinishedProduct updated = service.updateWithBusinessAccountFilter(entity);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 半完成品削除（businessAccountIdでフィルタリング）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> delete(@PathVariable Integer id) {
        service.deleteWithBusinessAccountFilter(id);
        return ResponseEntity.ok(DeleteResponse.success(id));
    }

    /**
     * アレルゲン情報再計算
     */
    @PostMapping("/{id}/recalculate-allergens")
    public ResponseEntity<ApiResponse<AllergenSummary>> recalculateAllergens(@PathVariable Integer id) {
        AllergenSummary summary = allergenAggregationService.recalculateAndSave(id);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * 添加物情報再計算
     */
    @PostMapping("/{id}/recalculate-additives")
    public ResponseEntity<ApiResponse<AdditiveSummary>> recalculateAdditives(@PathVariable Integer id) {
        AdditiveSummary summary = additiveSummaryService.recalculateAndSave(id);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * 展開済み原材料一覧取得
     * 仕込品を再帰的に展開し、ラベル表示用の原材料リストを返す
     */
    @GetMapping("/{id}/expanded-ingredients")
    public ResponseEntity<ApiResponse<IngredientExpansionService.ExpandedIngredientsResponse>> getExpandedIngredients(
            @PathVariable Integer id) {
        IngredientExpansionService.ExpandedIngredientsResponse response = ingredientExpansionService.expand(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 展開済み原材料を合算して取得
     * 表示名で合算し、重量順にソートした結果を返す
     */
    @GetMapping("/{id}/aggregated-ingredients")
    public ResponseEntity<ApiResponse<List<IngredientExpansionService.AggregatedIngredient>>> getAggregatedIngredients(
            @PathVariable Integer id) {
        IngredientExpansionService.ExpandedIngredientsResponse response = ingredientExpansionService.expand(id);
        List<IngredientExpansionService.AggregatedIngredient> aggregated =
            ingredientExpansionService.aggregateByDisplayName(response.getIngredients());
        return ResponseEntity.ok(ApiResponse.success(aggregated, aggregated.size()));
    }

    /**
     * 食品表示ラベルPDF出力
     * @param id 半完成品ID
     * @return PDFファイル
     */
    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Integer id) {
        try {
            byte[] pdfBytes = foodLabelPdfService.generateLabel(id);

            // 半完成品名を取得してファイル名に使用
            FoodSemiFinishedProduct product = service.findByIdWithBusinessAccountFilter(id);
            String fileName = product != null && product.getSemiName() != null
                    ? product.getSemiName() + "_食品表示ラベル.pdf"
                    : "食品表示ラベル.pdf";

            // ファイル名をURLエンコード（日本語対応）
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfBytes.length);
            headers.setContentDispositionFormData("attachment", encodedFileName);
            // RFC 5987 形式でも設定（ブラウザ互換性向上）
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.warn("PDF生成エラー: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("PDF生成中にエラーが発生しました: semiId={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
