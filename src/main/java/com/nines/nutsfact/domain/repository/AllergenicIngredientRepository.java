package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.allergy.AllergenicIngredient;
import com.nines.nutsfact.infrastructure.mapper.AllergenicIngredientMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AllergenicIngredientRepository {

    private final AllergenicIngredientMapper allergenicIngredientMapper;

    public List<AllergenicIngredient> findAll() {
        return allergenicIngredientMapper.findAll();
    }

    public Optional<AllergenicIngredient> findById(Integer allergenicIngredientId) {
        return Optional.ofNullable(allergenicIngredientMapper.findById(allergenicIngredientId));
    }

    public void save(AllergenicIngredient allergenicIngredient) {
        if (allergenicIngredient.getAllergenicIngredientId() == null) {
            allergenicIngredientMapper.insert(allergenicIngredient);
        } else {
            allergenicIngredientMapper.update(allergenicIngredient);
        }
    }

    public void delete(Integer allergenicIngredientId) {
        allergenicIngredientMapper.delete(allergenicIngredientId);
    }
}
