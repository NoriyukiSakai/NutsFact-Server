package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.master.Seller;
import com.nines.nutsfact.infrastructure.mapper.SellerMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SellerRepository {

    private final SellerMapper sellerMapper;

    public List<Seller> findAll() {
        return sellerMapper.findAll();
    }

    public List<Seller> findByBusinessAccountId(Integer businessAccountId) {
        return sellerMapper.findByBusinessAccountId(businessAccountId);
    }

    public Optional<Seller> findById(Integer sellerId) {
        return Optional.ofNullable(sellerMapper.findById(sellerId));
    }

    public Optional<Seller> findByIdAndBusinessAccountId(Integer sellerId, Integer businessAccountId) {
        return Optional.ofNullable(sellerMapper.findByIdAndBusinessAccountId(sellerId, businessAccountId));
    }

    public void save(Seller seller) {
        if (seller.getSellerId() == null) {
            sellerMapper.insert(seller);
        } else {
            sellerMapper.update(seller);
        }
    }

    public void delete(Integer sellerId) {
        sellerMapper.delete(sellerId);
    }

    public void deleteByIdAndBusinessAccountId(Integer sellerId, Integer businessAccountId) {
        sellerMapper.deleteByIdAndBusinessAccountId(sellerId, businessAccountId);
    }
}
