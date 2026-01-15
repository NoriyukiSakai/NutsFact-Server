package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.allergy.AllergenicControl;

@Mapper
public interface AllergenicControlMapper {
    List<AllergenicControl> findAll();
    List<AllergenicControl> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);
    List<AllergenicControl> findByBusinessAccountIdIsNull();
    AllergenicControl findByFoodId(@Param("foodId") Integer foodId);
    AllergenicControl findByFoodIdAndBusinessAccountId(
            @Param("foodId") Integer foodId,
            @Param("businessAccountId") Integer businessAccountId);
    void insert(AllergenicControl allergenicControl);
    void update(AllergenicControl allergenicControl);
    void upsert(AllergenicControl allergenicControl);
    void delete(@Param("foodId") Integer foodId);
    void deleteByFoodIdAndBusinessAccountId(
            @Param("foodId") Integer foodId,
            @Param("businessAccountId") Integer businessAccountId);
}
