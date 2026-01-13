package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Seller findById(Integer sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));
    }

    @Transactional
    public Seller create(Seller seller) {
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
    public void delete(Integer sellerId) {
        findById(sellerId);
        sellerRepository.delete(sellerId);
    }
}
