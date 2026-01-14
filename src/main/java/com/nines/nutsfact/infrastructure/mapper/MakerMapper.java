package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.master.Maker;

@Mapper
public interface MakerMapper {
    List<Maker> findAll();
    List<Maker> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);
    Maker findById(@Param("makerId") Integer makerId);
    Maker findByIdAndBusinessAccountId(
            @Param("makerId") Integer makerId,
            @Param("businessAccountId") Integer businessAccountId);
    void insert(Maker maker);
    void update(Maker maker);
    void delete(@Param("makerId") Integer makerId);
    void deleteByIdAndBusinessAccountId(
            @Param("makerId") Integer makerId,
            @Param("businessAccountId") Integer businessAccountId);
}
