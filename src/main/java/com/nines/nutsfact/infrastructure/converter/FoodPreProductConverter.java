package com.nines.nutsfact.infrastructure.converter;

import com.nines.nutsfact.api.v1.request.FoodPreProductDetailRequest;
import com.nines.nutsfact.api.v1.request.FoodPreProductRequest;
import com.nines.nutsfact.domain.model.FoodPreProductDetailItem;
import com.nines.nutsfact.domain.model.FoodPreProductItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 仕込品のRequest⇔Entity変換
 */
@Component
public class FoodPreProductConverter {

    public FoodPreProductItem toEntity(FoodPreProductRequest request) {
        if (request == null) {
            return null;
        }

        List<FoodPreProductDetailItem> details = null;
        if (request.getDetails() != null) {
            details = request.getDetails().stream()
                .map(this::toDetailEntity)
                .collect(Collectors.toList());
        }

        return FoodPreProductItem.builder()
            .preId(request.getPreId())
            .preNo(request.getPreNo())
            .preKind(request.getPreKind())
            .preName(request.getPreName())
            .displayName(request.getDisplayName())
            .weightInputMode(request.getWeightInputMode())
            .classCategoryId(request.getClassCategoryId())
            .capacity(request.getCapacity())
            .unit(request.getUnit())
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
            .infStorageMethod(request.getInfStorageMethod())
            .infContamiFlag(request.getInfContamiFlag())
            .infContamination(request.getInfContamination())
            .placeOfOrigin(request.getPlaceOfOrigin())
            .purpose(request.getPurpose())
            .isActive(request.getIsActive())
            .details(details)
            .build();
    }

    public FoodPreProductDetailItem toDetailEntity(FoodPreProductDetailRequest request) {
        if (request == null) {
            return null;
        }

        return FoodPreProductDetailItem.builder()
            .detailId(request.getDetailId())
            .preId(request.getPreId())
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
            .build();
    }
}
