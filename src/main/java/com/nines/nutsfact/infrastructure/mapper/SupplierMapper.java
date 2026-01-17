package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.master.Supplier;

@Mapper
public interface SupplierMapper {
    List<Supplier> findAll();
    List<Supplier> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);
    Supplier findById(@Param("supplierId") Integer supplierId);
    Supplier findByIdAndBusinessAccountId(
            @Param("supplierId") Integer supplierId,
            @Param("businessAccountId") Integer businessAccountId);
    void insert(Supplier supplier);
    void update(Supplier supplier);
    void delete(@Param("supplierId") Integer supplierId);
    void deleteByIdAndBusinessAccountId(
            @Param("supplierId") Integer supplierId,
            @Param("businessAccountId") Integer businessAccountId);
}
