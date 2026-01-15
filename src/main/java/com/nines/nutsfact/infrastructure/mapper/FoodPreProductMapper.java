package com.nines.nutsfact.infrastructure.mapper;

import com.nines.nutsfact.domain.model.FoodPreProductItem;
import com.nines.nutsfact.domain.model.SelectItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FoodPreProductMapper {

    List<FoodPreProductItem> findAll();

    List<FoodPreProductItem> findByKind(@Param("preKind") Integer preKind);

    List<FoodPreProductItem> findByKindAndBusinessAccountId(
            @Param("preKind") Integer preKind,
            @Param("businessAccountId") Integer businessAccountId);

    List<FoodPreProductItem> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);

    List<FoodPreProductItem> findByBusinessAccountIdIsNull();

    List<FoodPreProductItem> findByKindAndBusinessAccountIdIsNull(@Param("preKind") Integer preKind);

    List<SelectItem> findSelectItems(@Param("preKind") Integer preKind);

    Optional<FoodPreProductItem> findById(@Param("id") Integer id);

    Optional<FoodPreProductItem> findByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);

    int insert(@Param("entity") FoodPreProductItem entity);

    int update(@Param("entity") FoodPreProductItem entity);

    int delete(@Param("id") Integer id);

    int deleteByIdAndBusinessAccountId(
            @Param("id") Integer id,
            @Param("businessAccountId") Integer businessAccountId);

    Integer getLastInsertId();
}
