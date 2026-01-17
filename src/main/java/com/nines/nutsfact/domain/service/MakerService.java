package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.config.SecurityContextHelper;
import com.nines.nutsfact.domain.model.master.Maker;
import com.nines.nutsfact.domain.repository.MakerRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MakerService {

    private final MakerRepository makerRepository;

    public List<Maker> findAll() {
        return makerRepository.findAll();
    }

    public List<Maker> findAllWithBusinessAccountFilter() {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは製造元を参照できません");
        }
        return makerRepository.findByBusinessAccountId(businessAccountId);
    }

    public Maker findById(Integer makerId) {
        return makerRepository.findById(makerId)
                .orElseThrow(() -> new ResourceNotFoundException("Maker", makerId));
    }

    public Maker findByIdWithBusinessAccountFilter(Integer makerId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        return makerRepository.findByIdAndBusinessAccountId(makerId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Maker", makerId));
    }

    @Transactional
    public Maker create(Maker maker) {
        // businessAccountIdを必須で取得して設定
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        if (businessAccountId == null) {
            throw new IllegalStateException("ビジネスアカウントに所属していないユーザーは製造元を登録できません");
        }
        maker.setBusinessAccountId(businessAccountId);
        makerRepository.save(maker);
        return maker;
    }

    @Transactional
    public Maker update(Integer makerId, Maker maker) {
        findById(makerId);
        maker.setMakerId(makerId);
        makerRepository.save(maker);
        return maker;
    }

    @Transactional
    public Maker updateWithBusinessAccountFilter(Integer makerId, Maker maker) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        makerRepository.findByIdAndBusinessAccountId(makerId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Maker", makerId));
        maker.setMakerId(makerId);
        maker.setBusinessAccountId(businessAccountId);
        makerRepository.save(maker);
        return maker;
    }

    @Transactional
    public void delete(Integer makerId) {
        findById(makerId);
        makerRepository.delete(makerId);
    }

    @Transactional
    public void deleteWithBusinessAccountFilter(Integer makerId) {
        Integer businessAccountId = SecurityContextHelper.getCurrentBusinessAccountId();
        makerRepository.findByIdAndBusinessAccountId(makerId, businessAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Maker", makerId));
        makerRepository.deleteByIdAndBusinessAccountId(makerId, businessAccountId);
    }
}
