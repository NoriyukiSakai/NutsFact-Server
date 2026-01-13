package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.domain.model.allergy.AllergenicIngredient;
import com.nines.nutsfact.domain.repository.AllergenicIngredientRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AllergenicIngredientService {

    private final AllergenicIngredientRepository allergenicIngredientRepository;

    public List<AllergenicIngredient> findAll() {
        return allergenicIngredientRepository.findAll();
    }

    public AllergenicIngredient findById(Integer allergenicIngredientId) {
        return allergenicIngredientRepository.findById(allergenicIngredientId)
                .orElseThrow(() -> new ResourceNotFoundException("AllergenicIngredient", allergenicIngredientId));
    }

    @Transactional
    public AllergenicIngredient create(AllergenicIngredient allergenicIngredient) {
        allergenicIngredientRepository.save(allergenicIngredient);
        return allergenicIngredient;
    }

    @Transactional
    public AllergenicIngredient update(Integer allergenicIngredientId, AllergenicIngredient allergenicIngredient) {
        findById(allergenicIngredientId);
        allergenicIngredient.setAllergenicIngredientId(allergenicIngredientId);
        allergenicIngredientRepository.save(allergenicIngredient);
        return allergenicIngredient;
    }

    @Transactional
    public void delete(Integer allergenicIngredientId) {
        findById(allergenicIngredientId);
        allergenicIngredientRepository.delete(allergenicIngredientId);
    }
}
