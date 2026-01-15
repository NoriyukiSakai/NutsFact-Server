package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.user.BusinessAccount;

@Mapper
public interface BusinessAccountMapper {
    List<BusinessAccount> findAll();
    BusinessAccount findById(@Param("id") Integer id);
    BusinessAccount findByCode(@Param("code") String code);
    BusinessAccount findHeadquarters();
    void insert(BusinessAccount businessAccount);
    void update(BusinessAccount businessAccount);
    void updateStatus(@Param("id") Integer id, @Param("registrationStatus") Integer registrationStatus);
    void delete(@Param("id") Integer id);
    String generateNextCode();
}
