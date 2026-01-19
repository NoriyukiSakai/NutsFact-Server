package com.nines.nutsfact.domain.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.nines.nutsfact.domain.model.FoodSemiFinishedProduct;
import com.nines.nutsfact.domain.model.additive.AdditiveSummary;
import com.nines.nutsfact.domain.model.allergy.AllergenSummary;
import com.nines.nutsfact.domain.model.user.BusinessAccount;
import com.nines.nutsfact.domain.repository.FoodSemiFinishedProductRepository;
import com.nines.nutsfact.infrastructure.mapper.BusinessAccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 食品表示ラベルPDF生成サービス
 */
@Slf4j
@Service
public class FoodLabelPdfService {

    private final FoodSemiFinishedProductRepository semiFinishedProductRepository;
    private final IngredientExpansionService ingredientExpansionService;
    private final AdditiveSummaryService additiveSummaryService;
    private final AllergenAggregationService allergenAggregationService;
    private final BusinessAccountMapper businessAccountMapper;

    // フォント（日本語対応）
    private BaseFont baseFont;
    private Font titleFont;
    private Font headerFont;
    private Font normalFont;
    private Font smallFont;

    public FoodLabelPdfService(
            FoodSemiFinishedProductRepository semiFinishedProductRepository,
            IngredientExpansionService ingredientExpansionService,
            AdditiveSummaryService additiveSummaryService,
            AllergenAggregationService allergenAggregationService,
            BusinessAccountMapper businessAccountMapper) {
        this.semiFinishedProductRepository = semiFinishedProductRepository;
        this.ingredientExpansionService = ingredientExpansionService;
        this.additiveSummaryService = additiveSummaryService;
        this.allergenAggregationService = allergenAggregationService;
        this.businessAccountMapper = businessAccountMapper;
        initializeFonts();
    }

    /**
     * フォント初期化（日本語対応）
     */
    private void initializeFonts() {
        try {
            // システムフォントを使用（macOS/Linux: ヒラギノ or IPAフォント）
            String[] fontPaths = {
                "/System/Library/Fonts/ヒラギノ角ゴシック W3.ttc",  // macOS
                "/System/Library/Fonts/Hiragino Sans GB.ttc",       // macOS (Alternative)
                "/usr/share/fonts/truetype/fonts-japanese-gothic.ttf", // Linux
                "/usr/share/fonts/opentype/ipafont-gothic/ipag.ttf",   // Linux IPA Gothic
                "C:\\Windows\\Fonts\\msgothic.ttc"                     // Windows
            };

            for (String fontPath : fontPaths) {
                try {
                    java.io.File fontFile = new java.io.File(fontPath);
                    if (fontFile.exists()) {
                        baseFont = BaseFont.createFont(fontPath + ",0", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                        log.info("Japanese font loaded from: {}", fontPath);
                        break;
                    }
                } catch (Exception e) {
                    log.debug("Font not available at: {}", fontPath);
                }
            }

            // フォントが見つからない場合はデフォルトフォント（日本語非対応）
            if (baseFont == null) {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                log.warn("Japanese font not found, using default font. Japanese characters may not display correctly.");
            }

            // フォントサイズ設定
            titleFont = new Font(baseFont, 14, Font.BOLD);
            headerFont = new Font(baseFont, 10, Font.BOLD);
            normalFont = new Font(baseFont, 9, Font.NORMAL);
            smallFont = new Font(baseFont, 8, Font.NORMAL);

        } catch (Exception e) {
            log.error("Font initialization failed", e);
            // フォールバック
            titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
            smallFont = new Font(Font.HELVETICA, 8, Font.NORMAL);
        }
    }

    /**
     * 食品表示ラベルPDFを生成
     * @param semiId 半完成品ID
     * @return PDFバイト配列
     */
    @Transactional(readOnly = true)
    public byte[] generateLabel(Integer semiId) throws IOException, DocumentException {
        // 半完成品情報を取得
        FoodSemiFinishedProduct product = semiFinishedProductRepository.findById(semiId).orElse(null);
        if (product == null) {
            throw new IllegalArgumentException("半完成品が見つかりません: semiId=" + semiId);
        }

        // 原材料展開
        var expandedResponse = ingredientExpansionService.expand(semiId);
        var aggregatedIngredients = ingredientExpansionService.aggregateByDisplayName(expandedResponse.getIngredients());

        // 添加物情報
        AdditiveSummary additiveSummary = null;
        if (product.getAdditiveSummary() != null && !product.getAdditiveSummary().isEmpty()) {
            additiveSummary = additiveSummaryService.fromJson(product.getAdditiveSummary());
        }
        if (additiveSummary == null) {
            additiveSummary = additiveSummaryService.aggregate(semiId);
        }

        // アレルゲン情報
        AllergenSummary allergenSummary = null;
        if (product.getAllergenSummary() != null && !product.getAllergenSummary().isEmpty()) {
            allergenSummary = allergenAggregationService.fromJson(product.getAllergenSummary());
        }
        if (allergenSummary == null) {
            allergenSummary = allergenAggregationService.aggregate(semiId);
        }

        // ビジネスアカウント情報を取得（販売者・製造者表示用）
        BusinessAccount businessAccount = null;
        if (product.getBusinessAccountId() != null) {
            businessAccount = businessAccountMapper.findById(product.getBusinessAccountId());
        }

        // PDF生成
        return createPdf(product, aggregatedIngredients, additiveSummary, allergenSummary, businessAccount);
    }

    /**
     * PDF作成
     */
    private byte[] createPdf(
            FoodSemiFinishedProduct product,
            List<IngredientExpansionService.AggregatedIngredient> ingredients,
            AdditiveSummary additiveSummary,
            AllergenSummary allergenSummary,
            BusinessAccount businessAccount) throws IOException, DocumentException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // A4サイズで作成
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, baos);
        document.open();

        // タイトル
        Paragraph title = new Paragraph("食品表示ラベル", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // ラベル枠
        PdfPTable labelTable = new PdfPTable(1);
        labelTable.setWidthPercentage(100);

        PdfPCell labelCell = new PdfPCell();
        labelCell.setPadding(15);
        labelCell.setBorderWidth(2);

        // ラベル内容
        labelCell.addElement(createLabelContent(product, ingredients, additiveSummary, allergenSummary, businessAccount));
        labelTable.addCell(labelCell);

        document.add(labelTable);
        document.close();

        return baos.toByteArray();
    }

    /**
     * ラベル内容作成
     */
    private Paragraph createLabelContent(
            FoodSemiFinishedProduct product,
            List<IngredientExpansionService.AggregatedIngredient> ingredients,
            AdditiveSummary additiveSummary,
            AllergenSummary allergenSummary,
            BusinessAccount businessAccount) {

        Paragraph content = new Paragraph();
        content.setLeading(14);

        // 名称
        String displayName = product.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = product.getSemiName();
        }
        content.add(createLabelRow("名称", displayName));

        // 原材料名
        String ingredientsText = buildIngredientsText(ingredients, additiveSummary, allergenSummary,
                product.getAllergenDisplayMode());
        content.add(createLabelRow("原材料名", ingredientsText));

        // 内容量
        String capacityText = buildCapacityText(product);
        content.add(createLabelRow("内容量", capacityText));

        // 期限表示
        String expirationText = buildExpirationText(product);
        if (expirationText != null && !expirationText.isEmpty()) {
            String expirationLabel = product.getInfLmtKind() != null && product.getInfLmtKind() == 1
                    ? "消費期限" : "賞味期限";
            content.add(createLabelRow(expirationLabel, expirationText));
        }

        // 保存方法
        if (product.getInfStorageMethod() != null && !product.getInfStorageMethod().isEmpty()) {
            content.add(createLabelRow("保存方法", product.getInfStorageMethod()));
        }

        // 栄養成分表示
        content.add(Chunk.NEWLINE);
        content.add(createNutritionTable(product));

        // 販売者表示
        if (Boolean.TRUE.equals(product.getShowSeller()) && businessAccount != null) {
            String sellerText = buildSellerText(businessAccount);
            if (sellerText != null && !sellerText.isEmpty()) {
                content.add(Chunk.NEWLINE);
                content.add(createLabelRow("販売者", sellerText));
            }
        }

        // 製造者表示
        if (Boolean.TRUE.equals(product.getShowManufacturer()) && businessAccount != null) {
            String manufacturerText = buildManufacturerText(businessAccount);
            if (manufacturerText != null && !manufacturerText.isEmpty()) {
                content.add(Chunk.NEWLINE);
                content.add(createLabelRow("製造者", manufacturerText));
            }
        }

        return content;
    }

    /**
     * 販売者テキスト構築
     */
    private String buildSellerText(BusinessAccount account) {
        if (account == null) return "";

        StringBuilder sb = new StringBuilder();
        if (account.getLabelSellerName() != null && !account.getLabelSellerName().isEmpty()) {
            sb.append(account.getLabelSellerName());
            if (account.getLabelSellerAddress() != null && !account.getLabelSellerAddress().isEmpty()) {
                sb.append(" ").append(account.getLabelSellerAddress());
            }
        }
        return sb.toString();
    }

    /**
     * 製造者テキスト構築
     */
    private String buildManufacturerText(BusinessAccount account) {
        if (account == null) return "";

        StringBuilder sb = new StringBuilder();
        if (account.getLabelManufacturerName() != null && !account.getLabelManufacturerName().isEmpty()) {
            sb.append(account.getLabelManufacturerName());
            if (account.getLabelManufacturerAddress() != null && !account.getLabelManufacturerAddress().isEmpty()) {
                sb.append(" ").append(account.getLabelManufacturerAddress());
            }
        }
        return sb.toString();
    }

    /**
     * ラベル行作成
     */
    private Paragraph createLabelRow(String label, String value) {
        Paragraph row = new Paragraph();
        row.setSpacingAfter(5);

        Chunk labelChunk = new Chunk(label + "：", headerFont);
        row.add(labelChunk);
        row.add(new Chunk(value != null ? value : "", normalFont));

        return row;
    }

    /**
     * 原材料名テキスト構築
     */
    private String buildIngredientsText(
            List<IngredientExpansionService.AggregatedIngredient> ingredients,
            AdditiveSummary additiveSummary,
            AllergenSummary allergenSummary,
            Integer allergenDisplayMode) {

        StringBuilder sb = new StringBuilder();

        // 原材料（重量順）
        for (int i = 0; i < ingredients.size(); i++) {
            if (i > 0) sb.append("、");
            sb.append(ingredients.get(i).getName());
        }

        // 添加物（スラッシュで区切り）
        if (additiveSummary != null) {
            String additiveText = additiveSummary.toDisplayString();
            if (additiveText != null && !additiveText.isEmpty()) {
                sb.append(additiveText);
            }
        }

        // アレルゲン（一括表示の場合）
        if (allergenDisplayMode != null && allergenDisplayMode == 1 && allergenSummary != null) {
            String allergenText = buildAllergenText(allergenSummary);
            if (allergenText != null && !allergenText.isEmpty()) {
                sb.append("、（一部に").append(allergenText).append("を含む）");
            }
        }

        return sb.toString();
    }

    /**
     * アレルゲンテキスト構築
     */
    private String buildAllergenText(AllergenSummary summary) {
        if (summary == null || summary.getAllergens() == null || summary.getAllergens().isEmpty()) {
            return "";
        }

        return summary.getAllergens().stream()
                .map(a -> a.getName())
                .collect(Collectors.joining("・"));
    }

    /**
     * 内容量テキスト構築
     */
    private String buildCapacityText(FoodSemiFinishedProduct product) {
        if (product.getCapacity() == null) {
            return "";
        }

        String unit;
        switch (product.getUnit() != null ? product.getUnit() : 0) {
            case 1 -> unit = "ml";
            case 2 -> unit = "個";
            default -> unit = "g";
        }

        // 整数の場合は小数点以下を省略
        if (product.getCapacity() == Math.floor(product.getCapacity())) {
            return String.format("%.0f%s", product.getCapacity(), unit);
        }
        return String.format("%.1f%s", product.getCapacity(), unit);
    }

    /**
     * 期限表示テキスト構築
     */
    private String buildExpirationText(FoodSemiFinishedProduct product) {
        if (Boolean.TRUE.equals(product.getInfLmtDateFlag()) && product.getInfLmtDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
            return sdf.format(product.getInfLmtDate());
        } else if (product.getInfLmtDays() != null && product.getInfLmtDays() > 0) {
            return "製造日より" + product.getInfLmtDays() + "日";
        }
        return "枠外下部に記載";
    }

    /**
     * 栄養成分表示テーブル作成
     */
    private PdfPTable createNutritionTable(FoodSemiFinishedProduct product) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        try {
            table.setWidths(new float[]{60, 40});
        } catch (DocumentException e) {
            log.error("Table width setting failed", e);
        }

        // ヘッダー
        String unitText = getNutritionUnitText(product);
        PdfPCell headerCell = new PdfPCell(new Phrase("栄養成分表示（" + unitText + "）", headerFont));
        headerCell.setColspan(2);
        headerCell.setBackgroundColor(new java.awt.Color(240, 240, 240));
        headerCell.setPadding(5);
        table.addCell(headerCell);

        // 栄養成分行
        addNutritionRow(table, "熱量", formatEnergy(product.getInfEnergy()));
        addNutritionRow(table, "たんぱく質", formatGrams(product.getInfProtein()));
        addNutritionRow(table, "脂質", formatGrams(product.getInfFat()));
        addNutritionRow(table, "炭水化物", formatGrams(product.getInfCarbo()));
        addNutritionRow(table, "食塩相当量", formatSalt(product.getInfSodium()));

        // 推定値表記
        PdfPCell footerCell = new PdfPCell(new Phrase(getInfDisplayText(product.getInfDisplay()), smallFont));
        footerCell.setColspan(2);
        footerCell.setPadding(5);
        footerCell.setBorderWidthTop(0);
        table.addCell(footerCell);

        return table;
    }

    /**
     * 栄養成分行追加
     */
    private void addNutritionRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, normalFont));
        labelCell.setPadding(3);
        labelCell.setBorderWidthTop(0);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont));
        valueCell.setPadding(3);
        valueCell.setBorderWidthTop(0);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    /**
     * 栄養成分表示単位テキスト取得
     */
    private String getNutritionUnitText(FoodSemiFinishedProduct product) {
        Integer mode = product.getNutritionDisplayMode();
        if (mode == null) mode = 0;

        return switch (mode) {
            case 1 -> {
                Float servingSize = product.getServingSize();
                if (servingSize != null && servingSize > 0) {
                    yield String.format("1食分%.0gあたり", servingSize);
                }
                yield "1食分あたり";
            }
            case 2 -> "1個あたり";
            default -> "100gあたり";
        };
    }

    /**
     * 推定値表記テキスト取得
     */
    private String getInfDisplayText(Integer infDisplay) {
        if (infDisplay == null) infDisplay = 0;
        return switch (infDisplay) {
            case 1 -> "推定値";
            case 2 -> "日本食品標準成分表の計算による推定値";
            case 3 -> "サンプル品分析による推定値";
            default -> "この表示値は目安です。";
        };
    }

    /**
     * 熱量フォーマット（整数、kcal）
     */
    private String formatEnergy(Float value) {
        if (value == null) return "-";
        return String.format("%.0fkcal", value);
    }

    /**
     * グラムフォーマット（整数、g）
     */
    private String formatGrams(Float value) {
        if (value == null) return "-";
        return String.format("%.0fg", value);
    }

    /**
     * 食塩相当量フォーマット（小数第1位、g）
     */
    private String formatSalt(Float value) {
        if (value == null) return "-";
        return String.format("%.1fg", value);
    }
}
