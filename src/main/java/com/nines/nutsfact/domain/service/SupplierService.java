package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.config.SecurityContextHelper;
import com.nines.nutsfact.domain.model.master.Supplier;
import com.nines.nutsfact.domain.repository.SupplierRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public List<Supplier> findAllWithBusinessAccountFilter() {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは仕入元を参照できません");
        }
        return supplierRepository.findByBusinessAccountId(businessAccountId);
    }

    public Supplier findById(Integer supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", supplierId));
    }

    public Supplier findByIdWithBusinessAccountFilter(Integer supplierId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        return supplierRepository.findByIdAndBusinessAccountId(supplierId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", supplierId));
    }

    @Transactional
    public Supplier create(Supplier supplier) {
        // businessAccountIdを必須で取得して設定
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは仕入元を登録できません");
        }
        supplier.setBusinessAccountId(businessAccountId);
        supplierRepository.save(supplier);
        return supplier;
    }

    @Transactional
    public Supplier update(Integer supplierId, Supplier supplier) {
        findById(supplierId);
        supplier.setSupplierId(supplierId);
        supplierRepository.save(supplier);
        return supplier;
    }

    @Transactional
    public Supplier updateWithBusinessAccountFilter(Integer supplierId, Supplier supplier) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        supplierRepository.findByIdAndBusinessAccountId(supplierId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", supplierId));
        supplier.setSupplierId(supplierId);
        supplier.setBusinessAccountId(businessAccountId);
        supplierRepository.save(supplier);
        return supplier;
    }

    @Transactional
    public void delete(Integer supplierId) {
        findById(supplierId);
        supplierRepository.delete(supplierId);
    }

    @Transactional
    public void deleteWithBusinessAccountFilter(Integer supplierId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        supplierRepository.findByIdAndBusinessAccountId(supplierId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", supplierId));
        supplierRepository.deleteByIdAndBusinessAccountId(supplierId, businessAccountId);
    }
}
