package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Supplier findById(Integer supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", supplierId));
    }

    @Transactional
    public Supplier create(Supplier supplier) {
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
    public void delete(Integer supplierId) {
        findById(supplierId);
        supplierRepository.delete(supplierId);
    }
}
