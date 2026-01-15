package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.system.SystemParameter;

@Mapper
public interface SystemParameterMapper {
    List<SystemParameter> findAll();
    SystemParameter findById(@Param("id") Integer id);
    SystemParameter findByKey(@Param("parameterKey") String parameterKey);
    void insert(SystemParameter param);
    void update(SystemParameter param);
}
