package com.nines.nutsfact.infrastructure.mapper;

import com.nines.nutsfact.domain.model.FoodRawMaterial;
import com.nines.nutsfact.domain.model.SelectItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FoodRawMaterialMapper {

    List<FoodRawMaterial> findAll();

    List<FoodRawMaterial> findByCategory(@Param("categoryId") Integer categoryId);

    List<FoodRawMaterial> findByCategoryAndBusinessAccountId(
            @Param("categoryId") Integer categoryId,
            @Param("businessAccountId") Integer businessAccountId);

    List<FoodRawMaterial> findByCategoryAndBusinessAccountIdIsNull(@Param("categoryId") Integer categoryId);

    List<FoodRawMaterial> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);

    List<SelectItem> findSelectItems(@Param("categoryId") Integer categoryId);

    Optional<FoodRawMaterial> findById(@Param("id") Integer id);

    Optional<FoodRawMaterial> findByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);

    Optional<FoodRawMaterial> findByFoodNo(@Param("foodNo") String foodNo);

    int insert(@Param("entity") FoodRawMaterial entity);

    int update(@Param("entity") FoodRawMaterial entity);

    int delete(@Param("id") Integer id);

    int deleteByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);

    Integer getLastInsertId();

    /**
     * 栄養成分データを更新（原材料にすでに存在する場合）
     * カテゴリ1（8訂成分データ）のみ対象
     */
    int updateNutritionByFoodNo(@Param("entity") FoodRawMaterial entity);

    /**
     * 指定されたfood_noとbusiness_account_idの最大revision_of_food_noを取得
     * @param foodNo 食品番号
     * @param businessAccountId ビジネスアカウントID
     * @return 最大リビジョン番号（存在しない場合はnull）
     */
    Integer getMaxRevisionByFoodNoAndBusinessAccountId(
            @Param("foodNo") String foodNo,
            @Param("businessAccountId") Integer businessAccountId);
}
