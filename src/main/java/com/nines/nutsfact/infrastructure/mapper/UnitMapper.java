package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.master.Unit;

@Mapper
public interface UnitMapper {
    List<Unit> findAll();
    List<Unit> findByType(@Param("unitType") Integer unitType);
    Unit findById(@Param("unitId") Integer unitId);
    void insert(Unit unit);
    void update(Unit unit);
    void delete(@Param("unitId") Integer unitId);
}
