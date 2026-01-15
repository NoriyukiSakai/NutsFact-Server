package com.nines.nutsfact.domain.service;

import com.nines.nutsfact.api.v1.controller.FoodCompositionDictionaryController.TransferResult;
import com.nines.nutsfact.domain.model.FoodCompositionDictionary;
import com.nines.nutsfact.domain.model.FoodRawMaterial;
import com.nines.nutsfact.domain.model.nutrition.NutritionBasic;
import com.nines.nutsfact.domain.model.nutrition.NutritionMinerals;
import com.nines.nutsfact.domain.model.nutrition.NutritionVitamins;
import com.nines.nutsfact.domain.repository.FoodCompositionDictionaryRepository;
import com.nines.nutsfact.domain.repository.FoodRawMaterialRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodCompositionDictionaryService {
    private final FoodCompositionDictionaryRepository repository;
    private final FoodRawMaterialRepository rawMaterialRepository;

    @Transactional(readOnly = true)
    public List<FoodCompositionDictionary> findAll() {
        return repository.findAll();
    }

    public Optional<FoodCompositionDictionary> findById(Integer id) {
        return repository.findById(id);
    }

    public List<FoodCompositionDictionary> findByFoodGroupId(Integer foodGroupId) {
        return repository.findByFoodGroupId(foodGroupId);
    }

    @Transactional
    public void insert(FoodCompositionDictionary entity) {
        repository.insert(entity);
    }

    @Transactional
    public void update(FoodCompositionDictionary entity) {
        repository.update(entity);
    }

    @Transactional
    public void delete(Integer id) {
        repository.delete(id);
    }

    @Transactional
    public void truncate() {
        repository.truncate();
    }

    public int count() {
        return repository.count();
    }

    /**
     * データ移送（FOOD_COMPOSITION_DICTIONARY → FOOD_RAW_MATERIALS）
     * 8訂成分表データをFOOD_RAW_MATERIALSテーブルに移送/更新
     * フラグ変換: 0:実測値→2, 1:推定値→1, 2,3:Tr→4, 4,5:-→5
     */
    @Transactional
    public TransferResult transferToRawMaterials(Integer historyId) {
        int insertedCount = 0;
        int updatedCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        List<FoodCompositionDictionary> compositionList = repository.findAll();

        for (FoodCompositionDictionary comp : compositionList) {
            try {
                // historyIdが指定されている場合はフィルタリング
                if (historyId != null && !historyId.equals(comp.getHistoryId())) {
                    continue;
                }

                FoodRawMaterial rawMaterial = convertToRawMaterial(comp);

                // 既存レコードを検索（food_noでマッチング）
                Optional<FoodRawMaterial> existing = rawMaterialRepository.findByFoodNo(
                    String.valueOf(comp.getFoodNo()));

                if (existing.isPresent()) {
                    // 更新
                    rawMaterial.setFoodId(existing.get().getFoodId());
                    int updated = rawMaterialRepository.updateNutritionByFoodNo(rawMaterial);
                    if (updated > 0) {
                        updatedCount++;
                    }
                } else {
                    // 新規挿入
                    rawMaterialRepository.insert(rawMaterial);
                    insertedCount++;
                }
            } catch (Exception e) {
                log.warn("データ移送エラー: foodNo={}, error={}", comp.getFoodNo(), e.getMessage());
                errors.add("foodNo " + comp.getFoodNo() + ": " + e.getMessage());
                errorCount++;
            }
        }

        log.info("データ移送完了: inserted={}, updated={}, errors={}",
                insertedCount, updatedCount, errorCount);
        return new TransferResult(insertedCount, updatedCount, errorCount, errors);
    }

    /**
     * FoodCompositionDictionaryをFoodRawMaterialに変換
     */
    private FoodRawMaterial convertToRawMaterial(FoodCompositionDictionary comp) {
        return FoodRawMaterial.builder()
            .foodGroupId(comp.getFoodGroupId())
            .foodNo(String.valueOf(comp.getFoodNo()))
            .indexNo(comp.getIndexNo())
            .foodName(comp.getFoodName())
            .originalFoodId(comp.getFoodId())
            .originalFoodGroupId(comp.getFoodGroupId())
            .originalFoodNo(String.valueOf(comp.getFoodNo()))
            .originalIndexNo(comp.getIndexNo())
            .originalFoodName(comp.getFoodName())
            .foodFukuBunrui(comp.getFoodFukuBunrui())
            .foodRuiKubun(comp.getFoodRuiKubun())
            .foodDaiBunrui(comp.getFoodDaiBunrui())
            .foodCyuBunrui(comp.getFoodCyuBunrui())
            .foodSyoBunrui(comp.getFoodSyoBunrui())
            .foodSaibun(comp.getFoodSaibun())
            .categoryId(1)  // 8訂成分データ
            .description(comp.getDescription())
            .isActive(comp.getIsActive())
            .basicNutrition(buildBasicNutrition(comp))
            .minerals(buildMinerals(comp))
            .vitamins(buildVitamins(comp))
            .build();
    }

    /**
     * 基本栄養成分を構築
     */
    private NutritionBasic buildBasicNutrition(FoodCompositionDictionary comp) {
        return NutritionBasic.builder()
            .refuse(comp.getRefuse())
            .refuseFlag(0)  // 廃棄率はflagなし
            .enerc(comp.getEnerc())
            .enercFlag(0)
            .enercKcal(comp.getEnercKcal())
            .enercKcalFlag(0)
            .water(comp.getWater())
            .waterFlag(convertFlag(comp.getWaterFlag()))
            .protcaa(comp.getProtcaa())
            .protcaaFlag(convertFlag(comp.getProtcaaFlag()))
            .prot(comp.getProt())
            .protFlag(convertFlag(comp.getProtFlag()))
            .fatnlea(comp.getFatnlea())
            .fatnleaFlag(convertFlag(comp.getFatnleaFlag()))
            .chole(comp.getChole())
            .choleFlag(convertFlag(comp.getCholeFlag()))
            .fat(comp.getFat())
            .fatFlag(convertFlag(comp.getFatFlag()))
            .choavlm(comp.getChoavlm())
            .choavlmFlag(convertFlag(comp.getChoavlmFlag()))
            .choavlmMark(comp.getChoavlmMark())
            .choavl(comp.getChoavl())
            .choavlFlag(convertFlag(comp.getChoavlFlag()))
            .choavldf(comp.getChoavldf())
            .choavldfFlag(convertFlag(comp.getChoavldfFlag()))
            .choavldfMark(comp.getChoavldfMark())
            .fib(comp.getFib())
            .fibFlag(convertFlag(comp.getFibFlag()))
            .polyl(comp.getPolyl())
            .polylFlag(convertFlag(comp.getPolylFlag()))
            .chocdf(comp.getChocdf())
            .chocdfFlag(convertFlag(comp.getChocdfFlag()))
            .oa(comp.getOa())
            .oaFlag(convertFlag(comp.getOaFlag()))
            .ash(comp.getAsh())
            .ashFlag(convertFlag(comp.getAshFlag()))
            .alc(comp.getAlc())
            .alcFlag(convertFlag(comp.getAlcFlag()))
            .naclEq(comp.getNaclEq())
            .naclEqFlag(convertFlag(comp.getNaclEqFlag()))
            .build();
    }

    /**
     * ミネラル成分を構築
     */
    private NutritionMinerals buildMinerals(FoodCompositionDictionary comp) {
        return NutritionMinerals.builder()
            .na(comp.getNa())
            .naFlag(convertFlag(comp.getNaFlag()))
            .k(comp.getK())
            .kFlag(convertFlag(comp.getKFlag()))
            .ca(comp.getCa())
            .caFlag(convertFlag(comp.getCaFlag()))
            .mg(comp.getMg())
            .mgFlag(convertFlag(comp.getMgFlag()))
            .p(comp.getP())
            .pFlag(convertFlag(comp.getPFlag()))
            .fe(comp.getFe())
            .feFlag(convertFlag(comp.getFeFlag()))
            .zn(comp.getZn())
            .znFlag(convertFlag(comp.getZnFlag()))
            .cu(comp.getCu())
            .cuFlag(convertFlag(comp.getCuFlag()))
            .mn(comp.getMn())
            .mnFlag(convertFlag(comp.getMnFlag()))
            .idd(comp.getId())
            .iddFlag(convertFlag(comp.getIdFlag()))
            .se(comp.getSe())
            .seFlag(convertFlag(comp.getSeFlag()))
            .cr(comp.getCr())
            .crFlag(convertFlag(comp.getCrFlag()))
            .mo(comp.getMo())
            .moFlag(convertFlag(comp.getMoFlag()))
            .build();
    }

    /**
     * ビタミン成分を構築
     */
    private NutritionVitamins buildVitamins(FoodCompositionDictionary comp) {
        return NutritionVitamins.builder()
            .ret(comp.getRet())
            .retFlag(convertFlag(comp.getRetFlag()))
            .carta(comp.getCarta())
            .cartaFlag(convertFlag(comp.getCartaFlag()))
            .cartb(comp.getCartb())
            .cartbFlag(convertFlag(comp.getCartbFlag()))
            .crypxb(comp.getCrypxb())
            .crypxbFlag(convertFlag(comp.getCrypxbFlag()))
            .cartbeq(comp.getCartbeq())
            .cartbeqFlag(convertFlag(comp.getCartbeqFlag()))
            .vitaRae(comp.getVitaRae())
            .vitaRaeFlag(convertFlag(comp.getVitaRaeFlag()))
            .vitd(comp.getVitd())
            .vitdFlag(convertFlag(comp.getVitdFlag()))
            .tocpha(comp.getTocpha())
            .tocphaFlag(convertFlag(comp.getTocphaFlag()))
            .tocphb(comp.getTocphb())
            .tocphbFlag(convertFlag(comp.getTocphbFlag()))
            .tocphg(comp.getTocphg())
            .tocphgFlag(convertFlag(comp.getTocphgFlag()))
            .tocphd(comp.getTocphd())
            .tocphdFlag(convertFlag(comp.getTocphdFlag()))
            .vitk(comp.getVitk())
            .vitkFlag(convertFlag(comp.getVitkFlag()))
            .thia(comp.getThia())
            .thiaFlag(convertFlag(comp.getThiaFlag()))
            .ribf(comp.getRibf())
            .ribfFlag(convertFlag(comp.getRibfFlag()))
            .nia(comp.getNia())
            .niaFlag(convertFlag(comp.getNiaFlag()))
            .niac(comp.getNiac())
            .niacFlag(convertFlag(comp.getNiacFlag()))
            .vitb6a(comp.getVitb6a())
            .vitb6aFlag(convertFlag(comp.getVitb6aFlag()))
            .vitb12(comp.getVitb12())
            .vitb12Flag(convertFlag(comp.getVitb12Flag()))
            .fol(comp.getFol())
            .folFlag(convertFlag(comp.getFolFlag()))
            .pantac(comp.getPantac())
            .pantacFlag(convertFlag(comp.getPantacFlag()))
            .biot(comp.getBiot())
            .biotFlag(convertFlag(comp.getBiotFlag()))
            .vitc(comp.getVitc())
            .vitcFlag(convertFlag(comp.getVitcFlag()))
            .build();
    }

    /**
     * フラグ変換
     * FOOD_COMPOSITION_DICTIONARY: 0:実測値, 1:推定値, 2:実測Tr, 3:推定Tr, 4:実測-, 5:推定-
     * FOOD_RAW_MATERIALS: 0:初期値, 1:参照値, 2:実測値, 3:変更値, 4:微量(Tr), 5:含まれない(-)
     */
    private Integer convertFlag(Integer sourceFlag) {
        if (sourceFlag == null) {
            return 0; // 初期値
        }
        return switch (sourceFlag) {
            case 0 -> 2;  // 実測値 → 実測値
            case 1 -> 1;  // 推定値 → 参照値
            case 2, 3 -> 4;  // Tr → 微量
            case 4, 5 -> 5;  // - → 含まれない
            default -> 0;  // その他 → 初期値
        };
    }

    /**
     * CSVファイルをアップロードしてFOOD_COMPOSITION_DICTIONARYに登録
     */
    @Transactional
    public UploadResult uploadCsv(MultipartFile file, Integer historyId) {
        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {

            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                try {
                    // BOMを除去
                    if (line.startsWith("\uFEFF")) {
                        line = line.substring(1);
                    }

                    // ヘッダー行をスキップ
                    if (lineNumber == 1 && line.contains("食品群") || line.contains("食品番号")) {
                        continue;
                    }

                    FoodCompositionDictionary entity = parseCsvLine(line);
                    entity.setHistoryId(historyId);
                    repository.insert(entity);
                    successCount++;
                } catch (Exception e) {
                    log.warn("CSV行{}のパースエラー: {}", lineNumber, e.getMessage());
                    errors.add("行" + lineNumber + ": " + e.getMessage());
                    errorCount++;
                }
            }
        } catch (Exception e) {
            log.error("CSVファイル読み込みエラー", e);
            errors.add("ファイル読み込みエラー: " + e.getMessage());
        }

        return new UploadResult(successCount, errorCount, errors);
    }

    /**
     * Excelファイル(.xlsx)をアップロードしてFOOD_COMPOSITION_DICTIONARYに登録
     */
    @Transactional
    public UploadResult uploadExcel(MultipartFile file, Integer historyId) {
        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();

            for (int i = 1; i < rowCount; i++) { // 1行目はヘッダーなのでスキップ
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    FoodCompositionDictionary entity = parseExcelRow(row);
                    if (entity != null && entity.getFoodNo() != null) {
                        entity.setHistoryId(historyId);
                        repository.insert(entity);
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("Excel行{}のパースエラー: {}", i + 1, e.getMessage());
                    errors.add("行" + (i + 1) + ": " + e.getMessage());
                    errorCount++;
                }
            }
        } catch (Exception e) {
            log.error("Excelファイル読み込みエラー", e);
            errors.add("ファイル読み込みエラー: " + e.getMessage());
        }

        return new UploadResult(successCount, errorCount, errors);
    }

    /**
     * CSV行をFoodCompositionDictionaryに変換
     */
    private FoodCompositionDictionary parseCsvLine(String line) throws Exception {
        String[] values = line.split(",", 62);
        FoodBunrui fd = new FoodBunrui();
        fd.makeBunruiArr(values[3]);

        return FoodCompositionDictionary.builder()
            .foodGroupId(Integer.parseInt(values[0]))
            .foodNo(Integer.parseInt(values[1]))
            .indexNo(values[2])
            .foodName(values[3])
            .foodFukuBunrui(fd.getFukubunrui())
            .foodRuiKubun(fd.getRuikubun())
            .foodDaiBunrui(fd.getDaibunrui())
            .foodCyuBunrui(fd.getCyubunrui())
            .foodSyoBunrui(fd.getSyobunrui())
            .foodSaibun(fd.getSaibun())
            .refuse(Float.parseFloat(values[4]))
            .enerc(Integer.parseInt(values[5]))
            .enercKcal(Integer.parseInt(values[6]))
            .water(getFloatValue(values[7]))
            .waterFlag(getValueCondition(values[7]))
            .protcaa(getFloatValue(values[8]))
            .protcaaFlag(getValueCondition(values[8]))
            .prot(getFloatValue(values[9]))
            .protFlag(getValueCondition(values[9]))
            .fatnlea(getFloatValue(values[10]))
            .fatnleaFlag(getValueCondition(values[10]))
            .chole(getFloatValue(values[11]))
            .choleFlag(getValueCondition(values[11]))
            .fat(getFloatValue(values[12]))
            .fatFlag(getValueCondition(values[12]))
            .choavlm(getFloatValue(values[13]))
            .choavlmFlag(getValueCondition(values[13]))
            .choavlmMark(checkAsterisk(values[14]))
            .choavl(getFloatValue(values[15]))
            .choavlFlag(getValueCondition(values[15]))
            .choavldf(getFloatValue(values[16]))
            .choavldfFlag(getValueCondition(values[16]))
            .choavldfMark(checkAsterisk(values[17]))
            .fib(getFloatValue(values[18]))
            .fibFlag(getValueCondition(values[18]))
            .polyl(getFloatValue(values[19]))
            .polylFlag(getValueCondition(values[19]))
            .chocdf(getFloatValue(values[20]))
            .chocdfFlag(getValueCondition(values[20]))
            .oa(getFloatValue(values[21]))
            .oaFlag(getValueCondition(values[21]))
            .ash(getFloatValue(values[22]))
            .ashFlag(getValueCondition(values[22]))
            .na(getFloatValue(values[23]))
            .naFlag(getValueCondition(values[23]))
            .k(getFloatValue(values[24]))
            .kFlag(getValueCondition(values[24]))
            .ca(getFloatValue(values[25]))
            .caFlag(getValueCondition(values[25]))
            .mg(getFloatValue(values[26]))
            .mgFlag(getValueCondition(values[26]))
            .p(getFloatValue(values[27]))
            .pFlag(getValueCondition(values[27]))
            .fe(getFloatValue(values[28]))
            .feFlag(getValueCondition(values[28]))
            .zn(getFloatValue(values[29]))
            .znFlag(getValueCondition(values[29]))
            .cu(getFloatValue(values[30]))
            .cuFlag(getValueCondition(values[30]))
            .mn(getFloatValue(values[31]))
            .mnFlag(getValueCondition(values[31]))
            .id(getFloatValue(values[33]))
            .idFlag(getValueCondition(values[33]))
            .se(getFloatValue(values[34]))
            .seFlag(getValueCondition(values[34]))
            .cr(getFloatValue(values[35]))
            .crFlag(getValueCondition(values[35]))
            .mo(getFloatValue(values[36]))
            .moFlag(getValueCondition(values[36]))
            .ret(getFloatValue(values[37]))
            .retFlag(getValueCondition(values[37]))
            .carta(getFloatValue(values[38]))
            .cartaFlag(getValueCondition(values[38]))
            .cartb(getFloatValue(values[39]))
            .cartbFlag(getValueCondition(values[39]))
            .crypxb(getFloatValue(values[40]))
            .crypxbFlag(getValueCondition(values[40]))
            .cartbeq(getFloatValue(values[41]))
            .cartbeqFlag(getValueCondition(values[41]))
            .vitaRae(getFloatValue(values[42]))
            .vitaRaeFlag(getValueCondition(values[42]))
            .vitd(getFloatValue(values[43]))
            .vitdFlag(getValueCondition(values[43]))
            .tocpha(getFloatValue(values[44]))
            .tocphaFlag(getValueCondition(values[44]))
            .tocphb(getFloatValue(values[45]))
            .tocphbFlag(getValueCondition(values[45]))
            .tocphg(getFloatValue(values[46]))
            .tocphgFlag(getValueCondition(values[46]))
            .tocphd(getFloatValue(values[47]))
            .tocphdFlag(getValueCondition(values[47]))
            .vitk(getFloatValue(values[48]))
            .vitkFlag(getValueCondition(values[48]))
            .thia(getFloatValue(values[49]))
            .thiaFlag(getValueCondition(values[49]))
            .ribf(getFloatValue(values[50]))
            .ribfFlag(getValueCondition(values[50]))
            .nia(getFloatValue(values[51]))
            .niaFlag(getValueCondition(values[51]))
            .niac(getFloatValue(values[52]))
            .niacFlag(getValueCondition(values[52]))
            .vitb6a(getFloatValue(values[53]))
            .vitb6aFlag(getValueCondition(values[53]))
            .vitb12(getFloatValue(values[54]))
            .vitb12Flag(getValueCondition(values[54]))
            .fol(getFloatValue(values[55]))
            .folFlag(getValueCondition(values[55]))
            .pantac(getFloatValue(values[56]))
            .pantacFlag(getValueCondition(values[56]))
            .biot(getFloatValue(values[57]))
            .biotFlag(getValueCondition(values[57]))
            .vitc(getFloatValue(values[58]))
            .vitcFlag(getValueCondition(values[58]))
            .alc(getFloatValue(values[59]))
            .alcFlag(getValueCondition(values[59]))
            .naclEq(getFloatValue(values[60]))
            .naclEqFlag(getValueCondition(values[60]))
            .description(values.length > 61 && values[61] != null && !values[61].isEmpty() ? values[61] : null)
            .isActive(true)
            .build();
    }

    /**
     * Excel行をFoodCompositionDictionaryに変換
     */
    private FoodCompositionDictionary parseExcelRow(Row row) throws Exception {
        String foodName = getCellStringValue(row.getCell(3));
        if (foodName == null || foodName.isEmpty()) {
            return null;
        }

        FoodBunrui fd = new FoodBunrui();
        fd.makeBunruiArr(foodName);

        return FoodCompositionDictionary.builder()
            .foodGroupId(getCellIntValue(row.getCell(0)))
            .foodNo(getCellIntValue(row.getCell(1)))
            .indexNo(getCellStringValue(row.getCell(2)))
            .foodName(foodName)
            .foodFukuBunrui(fd.getFukubunrui())
            .foodRuiKubun(fd.getRuikubun())
            .foodDaiBunrui(fd.getDaibunrui())
            .foodCyuBunrui(fd.getCyubunrui())
            .foodSyoBunrui(fd.getSyobunrui())
            .foodSaibun(fd.getSaibun())
            .refuse(getCellFloatValue(row.getCell(4)))
            .enerc(getCellIntValue(row.getCell(5)))
            .enercKcal(getCellIntValue(row.getCell(6)))
            .water(getExcelFloatValue(row.getCell(7)))
            .waterFlag(getExcelValueCondition(row.getCell(7)))
            .protcaa(getExcelFloatValue(row.getCell(8)))
            .protcaaFlag(getExcelValueCondition(row.getCell(8)))
            .prot(getExcelFloatValue(row.getCell(9)))
            .protFlag(getExcelValueCondition(row.getCell(9)))
            .fatnlea(getExcelFloatValue(row.getCell(10)))
            .fatnleaFlag(getExcelValueCondition(row.getCell(10)))
            .chole(getExcelFloatValue(row.getCell(11)))
            .choleFlag(getExcelValueCondition(row.getCell(11)))
            .fat(getExcelFloatValue(row.getCell(12)))
            .fatFlag(getExcelValueCondition(row.getCell(12)))
            .choavlm(getExcelFloatValue(row.getCell(13)))
            .choavlmFlag(getExcelValueCondition(row.getCell(13)))
            .choavlmMark(checkExcelAsterisk(row.getCell(14)))
            .choavl(getExcelFloatValue(row.getCell(15)))
            .choavlFlag(getExcelValueCondition(row.getCell(15)))
            .choavldf(getExcelFloatValue(row.getCell(16)))
            .choavldfFlag(getExcelValueCondition(row.getCell(16)))
            .choavldfMark(checkExcelAsterisk(row.getCell(17)))
            .fib(getExcelFloatValue(row.getCell(18)))
            .fibFlag(getExcelValueCondition(row.getCell(18)))
            .polyl(getExcelFloatValue(row.getCell(19)))
            .polylFlag(getExcelValueCondition(row.getCell(19)))
            .chocdf(getExcelFloatValue(row.getCell(20)))
            .chocdfFlag(getExcelValueCondition(row.getCell(20)))
            .oa(getExcelFloatValue(row.getCell(21)))
            .oaFlag(getExcelValueCondition(row.getCell(21)))
            .ash(getExcelFloatValue(row.getCell(22)))
            .ashFlag(getExcelValueCondition(row.getCell(22)))
            .na(getExcelFloatValue(row.getCell(23)))
            .naFlag(getExcelValueCondition(row.getCell(23)))
            .k(getExcelFloatValue(row.getCell(24)))
            .kFlag(getExcelValueCondition(row.getCell(24)))
            .ca(getExcelFloatValue(row.getCell(25)))
            .caFlag(getExcelValueCondition(row.getCell(25)))
            .mg(getExcelFloatValue(row.getCell(26)))
            .mgFlag(getExcelValueCondition(row.getCell(26)))
            .p(getExcelFloatValue(row.getCell(27)))
            .pFlag(getExcelValueCondition(row.getCell(27)))
            .fe(getExcelFloatValue(row.getCell(28)))
            .feFlag(getExcelValueCondition(row.getCell(28)))
            .zn(getExcelFloatValue(row.getCell(29)))
            .znFlag(getExcelValueCondition(row.getCell(29)))
            .cu(getExcelFloatValue(row.getCell(30)))
            .cuFlag(getExcelValueCondition(row.getCell(30)))
            .mn(getExcelFloatValue(row.getCell(31)))
            .mnFlag(getExcelValueCondition(row.getCell(31)))
            .id(getExcelFloatValue(row.getCell(33)))
            .idFlag(getExcelValueCondition(row.getCell(33)))
            .se(getExcelFloatValue(row.getCell(34)))
            .seFlag(getExcelValueCondition(row.getCell(34)))
            .cr(getExcelFloatValue(row.getCell(35)))
            .crFlag(getExcelValueCondition(row.getCell(35)))
            .mo(getExcelFloatValue(row.getCell(36)))
            .moFlag(getExcelValueCondition(row.getCell(36)))
            .ret(getExcelFloatValue(row.getCell(37)))
            .retFlag(getExcelValueCondition(row.getCell(37)))
            .carta(getExcelFloatValue(row.getCell(38)))
            .cartaFlag(getExcelValueCondition(row.getCell(38)))
            .cartb(getExcelFloatValue(row.getCell(39)))
            .cartbFlag(getExcelValueCondition(row.getCell(39)))
            .crypxb(getExcelFloatValue(row.getCell(40)))
            .crypxbFlag(getExcelValueCondition(row.getCell(40)))
            .cartbeq(getExcelFloatValue(row.getCell(41)))
            .cartbeqFlag(getExcelValueCondition(row.getCell(41)))
            .vitaRae(getExcelFloatValue(row.getCell(42)))
            .vitaRaeFlag(getExcelValueCondition(row.getCell(42)))
            .vitd(getExcelFloatValue(row.getCell(43)))
            .vitdFlag(getExcelValueCondition(row.getCell(43)))
            .tocpha(getExcelFloatValue(row.getCell(44)))
            .tocphaFlag(getExcelValueCondition(row.getCell(44)))
            .tocphb(getExcelFloatValue(row.getCell(45)))
            .tocphbFlag(getExcelValueCondition(row.getCell(45)))
            .tocphg(getExcelFloatValue(row.getCell(46)))
            .tocphgFlag(getExcelValueCondition(row.getCell(46)))
            .tocphd(getExcelFloatValue(row.getCell(47)))
            .tocphdFlag(getExcelValueCondition(row.getCell(47)))
            .vitk(getExcelFloatValue(row.getCell(48)))
            .vitkFlag(getExcelValueCondition(row.getCell(48)))
            .thia(getExcelFloatValue(row.getCell(49)))
            .thiaFlag(getExcelValueCondition(row.getCell(49)))
            .ribf(getExcelFloatValue(row.getCell(50)))
            .ribfFlag(getExcelValueCondition(row.getCell(50)))
            .nia(getExcelFloatValue(row.getCell(51)))
            .niaFlag(getExcelValueCondition(row.getCell(51)))
            .niac(getExcelFloatValue(row.getCell(52)))
            .niacFlag(getExcelValueCondition(row.getCell(52)))
            .vitb6a(getExcelFloatValue(row.getCell(53)))
            .vitb6aFlag(getExcelValueCondition(row.getCell(53)))
            .vitb12(getExcelFloatValue(row.getCell(54)))
            .vitb12Flag(getExcelValueCondition(row.getCell(54)))
            .fol(getExcelFloatValue(row.getCell(55)))
            .folFlag(getExcelValueCondition(row.getCell(55)))
            .pantac(getExcelFloatValue(row.getCell(56)))
            .pantacFlag(getExcelValueCondition(row.getCell(56)))
            .biot(getExcelFloatValue(row.getCell(57)))
            .biotFlag(getExcelValueCondition(row.getCell(57)))
            .vitc(getExcelFloatValue(row.getCell(58)))
            .vitcFlag(getExcelValueCondition(row.getCell(58)))
            .alc(getExcelFloatValue(row.getCell(59)))
            .alcFlag(getExcelValueCondition(row.getCell(59)))
            .naclEq(getExcelFloatValue(row.getCell(60)))
            .naclEqFlag(getExcelValueCondition(row.getCell(60)))
            .description(getCellStringValue(row.getCell(61)))
            .isActive(true)
            .build();
    }

    // Excel Cell値取得ヘルパー
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        String value = cell.getStringCellValue();
        return value != null && !value.isEmpty() ? value.trim() : null;
    }

    private Integer getCellIntValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else {
                String value = cell.getStringCellValue();
                return value != null && !value.isEmpty() ? Integer.parseInt(value.trim()) : null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Float getCellFloatValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (float) cell.getNumericCellValue();
            } else {
                String value = cell.getStringCellValue();
                return value != null && !value.isEmpty() ? Float.parseFloat(value.trim()) : null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Float getExcelFloatValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (float) cell.getNumericCellValue();
            } else {
                String value = cell.getStringCellValue();
                return getFloatValue(value);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getExcelValueCondition(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return 0; // 数値は実測値
            } else {
                String value = cell.getStringCellValue();
                return getValueCondition(value);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean checkExcelAsterisk(Cell cell) {
        if (cell == null) return false;
        try {
            String value = getCellStringValue(cell);
            return value != null && value.contains("*");
        } catch (Exception e) {
            return false;
        }
    }

    // CSV値変換ヘルパー（リファクタリング前のロジック）
    private Boolean checkAsterisk(String value) {
        return value != null && value.contains("*");
    }

    private Integer getValueCondition(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        } else if (value.equals("*")) {
            return 6;
        } else if (value.equals("Tr")) {
            return 2;
        } else if (value.equals("-")) {
            return 4;
        } else if (value.equals("(Tr)")) {
            return 3;
        } else if (value.equals("(-)")) {
            return 5;
        } else if (value.matches("\\(.+?\\)")) {
            return 1;
        } else {
            return 0;
        }
    }

    private Float getFloatValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        } else if (value.equals("*")) {
            return 0f;
        } else if (value.equals("Tr")) {
            return -1f;
        } else if (value.equals("-")) {
            return 0f;
        } else if (value.equals("(Tr)")) {
            return -1f;
        } else if (value.equals("(-)")) {
            return 0f;
        } else {
            String v = value.replace("†", "");
            if (v.matches("\\(.+?\\)")) {
                String x = v.substring(v.indexOf("(") + 1, v.indexOf(")"));
                return Float.valueOf(x);
            } else {
                return Float.valueOf(v);
            }
        }
    }

    // 食品分類パーサー
    private static class FoodBunrui {
        @Getter private String fukubunrui = "";
        @Getter private String ruikubun = "";
        @Getter private String daibunrui = "";
        @Getter private String cyubunrui = "";
        @Getter private String syobunrui = "";
        @Getter private String saibun = "";

        public void makeBunruiArr(String value) {
            if (value == null) return;
            String[] arr = value.split("　");
            int idx = 0;
            for (String data : arr) {
                if (data.isEmpty()) continue;
                String firstChar = data.substring(0, 1);
                switch (firstChar) {
                    case "＜":
                        if (idx == 0 && data.contains("＞")) {
                            fukubunrui = data.substring(data.indexOf("＜") + 1, data.indexOf("＞"));
                        }
                        break;
                    case "（":
                        if (idx == 0 && data.contains("）")) {
                            ruikubun = data.substring(data.indexOf("（") + 1, data.indexOf("）"));
                        }
                        break;
                    case "［":
                        if (data.contains("］")) {
                            cyubunrui = data.substring(data.indexOf("［") + 1, data.indexOf("］"));
                        }
                        break;
                    default:
                        if (idx >= 3) {
                            saibun = saibun + "　" + data;
                        } else if (idx == 2) {
                            saibun = data;
                        } else if (idx == 1) {
                            syobunrui = data;
                        } else if (idx == 0) {
                            daibunrui = data;
                        }
                        idx++;
                }
            }
        }
    }

    // アップロード結果
    @Getter
    public static class UploadResult {
        private final int successCount;
        private final int errorCount;
        private final List<String> errors;

        public UploadResult(int successCount, int errorCount, List<String> errors) {
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.errors = errors;
        }
    }
}
