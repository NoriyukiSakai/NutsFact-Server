package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.master.Unit;
import com.nines.nutsfact.infrastructure.mapper.UnitMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UnitRepository {

    private final UnitMapper unitMapper;

    public List<Unit> findAll() {
        return unitMapper.findAll();
    }

    public List<Unit> findByType(Integer unitType) {
        return unitMapper.findByType(unitType);
    }

    public Optional<Unit> findById(Integer unitId) {
        return Optional.ofNullable(unitMapper.findById(unitId));
    }

    public void save(Unit unit) {
        if (unit.getUnitId() == null) {
            unitMapper.insert(unit);
        } else {
            unitMapper.update(unit);
        }
    }

    public void delete(Integer unitId) {
        unitMapper.delete(unitId);
    }
}
