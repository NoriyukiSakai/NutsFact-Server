package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.master.Supplier;

@Mapper
public interface SupplierMapper {
    List<Supplier> findAll();
    Supplier findById(@Param("supplierId") Integer supplierId);
    void insert(Supplier supplier);
    void update(Supplier supplier);
    void delete(@Param("supplierId") Integer supplierId);
}
