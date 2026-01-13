package com.nines.nutsfact.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.domain.model.allergy.AllergenicControl;
import com.nines.nutsfact.domain.repository.AllergenicControlRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AllergenicControlService {

    private final AllergenicControlRepository allergenicControlRepository;

    public List<AllergenicControl> findAll() {
        return allergenicControlRepository.findAll();
    }

    public AllergenicControl findByFoodId(Integer foodId) {
        return allergenicControlRepository.findByFoodId(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("AllergenicControl", foodId));
    }

    public Optional<AllergenicControl> findByFoodIdOptional(Integer foodId) {
        return allergenicControlRepository.findByFoodId(foodId);
    }

    @Transactional
    public AllergenicControl save(AllergenicControl allergenicControl) {
        allergenicControlRepository.save(allergenicControl);
        return allergenicControl;
    }

    @Transactional
    public void delete(Integer foodId) {
        allergenicControlRepository.delete(foodId);
    }
}
