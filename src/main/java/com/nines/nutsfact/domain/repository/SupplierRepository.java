package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.master.Supplier;
import com.nines.nutsfact.infrastructure.mapper.SupplierMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SupplierRepository {

    private final SupplierMapper supplierMapper;

    public List<Supplier> findAll() {
        return supplierMapper.findAll();
    }

    public List<Supplier> findByBusinessAccountId(Integer businessAccountId) {
        return supplierMapper.findByBusinessAccountId(businessAccountId);
    }

    public Optional<Supplier> findById(Integer supplierId) {
        return Optional.ofNullable(supplierMapper.findById(supplierId));
    }

    public Optional<Supplier> findByIdAndBusinessAccountId(Integer supplierId, Integer businessAccountId) {
        return Optional.ofNullable(supplierMapper.findByIdAndBusinessAccountId(supplierId, businessAccountId));
    }

    public void save(Supplier supplier) {
        if (supplier.getSupplierId() == null) {
            supplierMapper.insert(supplier);
        } else {
            supplierMapper.update(supplier);
        }
    }

    public void delete(Integer supplierId) {
        supplierMapper.delete(supplierId);
    }

    public void deleteByIdAndBusinessAccountId(Integer supplierId, Integer businessAccountId) {
        supplierMapper.deleteByIdAndBusinessAccountId(supplierId, businessAccountId);
    }
}
