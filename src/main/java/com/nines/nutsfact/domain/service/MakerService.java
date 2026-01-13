package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Maker findById(Integer makerId) {
        return makerRepository.findById(makerId)
                .orElseThrow(() -> new ResourceNotFoundException("Maker", makerId));
    }

    @Transactional
    public Maker create(Maker maker) {
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
    public void delete(Integer makerId) {
        findById(makerId);
        makerRepository.delete(makerId);
    }
}
