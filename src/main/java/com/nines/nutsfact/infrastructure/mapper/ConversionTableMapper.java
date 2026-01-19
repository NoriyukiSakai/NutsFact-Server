package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.ConversionTable;

@Mapper
public interface ConversionTableMapper {

    List<ConversionTable> findAll();

    List<ConversionTable> findByKubun(@Param("kubun") Integer kubun);

    ConversionTable findById(@Param("id") Integer id);
}
