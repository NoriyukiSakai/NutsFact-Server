package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.master.Seller;

@Mapper
public interface SellerMapper {
    List<Seller> findAll();
    List<Seller> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);
    Seller findById(@Param("sellerId") Integer sellerId);
    Seller findByIdAndBusinessAccountId(
            @Param("sellerId") Integer sellerId,
            @Param("businessAccountId") Integer businessAccountId);
    void insert(Seller seller);
    void update(Seller seller);
    void delete(@Param("sellerId") Integer sellerId);
    void deleteByIdAndBusinessAccountId(
            @Param("sellerId") Integer sellerId,
            @Param("businessAccountId") Integer businessAccountId);
}
