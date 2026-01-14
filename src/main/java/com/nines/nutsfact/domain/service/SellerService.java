package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.config.SecurityContextHelper;
import com.nines.nutsfact.domain.model.master.Seller;
import com.nines.nutsfact.domain.repository.SellerRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;

    public List<Seller> findAll() {
        return sellerRepository.findAll();
    }

    public List<Seller> findAllWithBusinessAccountFilter() {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId != null) {
            return sellerRepository.findByBusinessAccountId(businessAccountId);
        }
        return sellerRepository.findAll();
    }

    public Seller findById(Integer sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));
    }

    public Seller findByIdWithBusinessAccountFilter(Integer sellerId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        return sellerRepository.findByIdAndBusinessAccountId(sellerId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));
    }

    @Transactional
    public Seller create(Seller seller) {
        // businessAccountIdを自動設定
        if (seller.getBusinessAccountId() == null) {
            seller.setBusinessAccountId(SecurityContextHelper.getCurrentBusinessAccountId());
        }
        sellerRepository.save(seller);
        return seller;
    }

    @Transactional
    public Seller update(Integer sellerId, Seller seller) {
        findById(sellerId);
        seller.setSellerId(sellerId);
        sellerRepository.save(seller);
        return seller;
    }

    @Transactional
    public Seller updateWithBusinessAccountFilter(Integer sellerId, Seller seller) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        sellerRepository.findByIdAndBusinessAccountId(sellerId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));
        seller.setSellerId(sellerId);
        seller.setBusinessAccountId(businessAccountId);
        sellerRepository.save(seller);
        return seller;
    }

    @Transactional
    public void delete(Integer sellerId) {
        findById(sellerId);
        sellerRepository.delete(sellerId);
    }

    @Transactional
    public void deleteWithBusinessAccountFilter(Integer sellerId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        sellerRepository.findByIdAndBusinessAccountId(sellerId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));
        sellerRepository.deleteByIdAndBusinessAccountId(sellerId, businessAccountId);
    }
}
