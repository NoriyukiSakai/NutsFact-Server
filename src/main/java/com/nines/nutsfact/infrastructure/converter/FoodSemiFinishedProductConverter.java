package com.nines.nutsfact.infrastructure.converter;

import com.nines.nutsfact.api.v1.request.FoodSemiFinishedProductDetailRequest;
import com.nines.nutsfact.api.v1.request.FoodSemiFinishedProductRequest;
import com.nines.nutsfact.domain.model.FoodSemiFinishedProduct;
import com.nines.nutsfact.domain.model.FoodSemiFinishedProductDetail;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 半完成品のRequest⇔Entity変換
 */
@Component
public class FoodSemiFinishedProductConverter {

    public FoodSemiFinishedProduct toEntity(FoodSemiFinishedProductRequest request) {
        if (request == null) {
            return null;
        }

        List<FoodSemiFinishedProductDetail> details = null;
        if (request.getDetails() != null) {
            details = request.getDetails().stream()
                .map(this::toDetailEntity)
                .collect(Collectors.toList());
        }

        return FoodSemiFinishedProduct.builder()
            .semiId(request.getSemiId())
            .semiNo(request.getSemiNo())
            .semiName(request.getSemiName())
            .displayName(request.getDisplayName())
            .classCategoryId(request.getClassCategoryId())
            .capacity(request.getCapacity())
            .unit(request.getUnit())
            .weightInputMode(request.getWeightInputMode())
            .infUnit(request.getInfUnit())
            .infVolume(request.getInfVolume())
            .infDisplay(request.getInfDisplay())
            .infEnergy(request.getInfEnergy())
            .infProtein(request.getInfProtein())
            .infFat(request.getInfFat())
            .infCarbo(request.getInfCarbo())
            .infSugar(request.getInfSugar())
            .infSodium(request.getInfSodium())
            .infLmtKind(request.getInfLmtKind())
            .infLmtDateFlag(request.getInfLmtDateFlag())
            .infLmtDate(request.getInfLmtDate())
            .infLmtDays(request.getInfLmtDays())
            .infStorageMethod(request.getInfStorageMethod())
            .infContamiFlag(request.getInfContamiFlag())
            .infContamination(request.getInfContamination())
            .placeOfOrigin(request.getPlaceOfOrigin())
            .purpose(request.getPurpose())
            .isActive(request.getIsActive())
            .details(details)
            .build();
    }

    public FoodSemiFinishedProductDetail toDetailEntity(FoodSemiFinishedProductDetailRequest request) {
        if (request == null) {
            return null;
        }

        return FoodSemiFinishedProductDetail.builder()
            .detailId(request.getDetailId())
            .semiId(request.getSemiId())
            .componentKb(request.getComponentKb())
            .detailFoodId(request.getDetailFoodId())
            .detailPreId(request.getDetailPreId())
            .compositeRawMaterialsKb(request.getCompositeRawMaterialsKb())
            .detailFoodName(request.getDetailFoodName())
            .detailPreName(request.getDetailPreName())
            .mixingRatio(request.getMixingRatio())
            .weight(request.getWeight())
            .costPrice(request.getCostPrice())
            .energy(request.getEnergy())
            .protein(request.getProtein())
            .fat(request.getFat())
            .carbo(request.getCarbo())
            .sugar(request.getSugar())
            .sodium(request.getSodium())
            .isActive(request.getIsActive())
            .build();
    }
}
