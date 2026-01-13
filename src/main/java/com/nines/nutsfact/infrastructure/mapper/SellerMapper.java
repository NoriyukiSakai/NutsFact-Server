package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.master.Seller;

@Mapper
public interface SellerMapper {
    List<Seller> findAll();
    Seller findById(@Param("sellerId") Integer sellerId);
    void insert(Seller seller);
    void update(Seller seller);
    void delete(@Param("sellerId") Integer sellerId);
}
