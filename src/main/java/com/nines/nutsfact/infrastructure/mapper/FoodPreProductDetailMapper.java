package com.nines.nutsfact.infrastructure.mapper;

import com.nines.nutsfact.domain.model.FoodPreProductDetailItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FoodPreProductDetailMapper {

    List<FoodPreProductDetailItem> findByPreId(@Param("preId") Integer preId);

    Optional<FoodPreProductDetailItem> findById(@Param("id") Integer id);

    int insert(@Param("entity") FoodPreProductDetailItem entity);

    int update(@Param("entity") FoodPreProductDetailItem entity);

    int delete(@Param("id") Integer id);

    int deleteByPreId(@Param("preId") Integer preId);

    Integer getLastInsertId();
}
