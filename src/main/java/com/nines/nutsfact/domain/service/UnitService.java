package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.domain.model.master.Unit;
import com.nines.nutsfact.domain.repository.UnitRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;

    public List<Unit> findAll() {
        return unitRepository.findAll();
    }

    public List<Unit> findByType(Integer unitType) {
        return unitRepository.findByType(unitType);
    }

    public Unit findById(Integer unitId) {
        return unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", unitId));
    }

    @Transactional
    public Unit create(Unit unit) {
        unitRepository.save(unit);
        return unit;
    }

    @Transactional
    public Unit update(Integer unitId, Unit unit) {
        findById(unitId);
        unit.setUnitId(unitId);
        unitRepository.save(unit);
        return unit;
    }

    @Transactional
    public void delete(Integer unitId) {
        findById(unitId);
        unitRepository.delete(unitId);
    }
}
