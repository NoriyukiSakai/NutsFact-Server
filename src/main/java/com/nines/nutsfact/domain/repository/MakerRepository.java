package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.master.Maker;
import com.nines.nutsfact.infrastructure.mapper.MakerMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MakerRepository {

    private final MakerMapper makerMapper;

    public List<Maker> findAll() {
        return makerMapper.findAll();
    }

    public List<Maker> findByBusinessAccountId(Integer businessAccountId) {
        return makerMapper.findByBusinessAccountId(businessAccountId);
    }

    public Optional<Maker> findById(Integer makerId) {
        return Optional.ofNullable(makerMapper.findById(makerId));
    }

    public Optional<Maker> findByIdAndBusinessAccountId(Integer makerId, Integer businessAccountId) {
        return Optional.ofNullable(makerMapper.findByIdAndBusinessAccountId(makerId, businessAccountId));
    }

    public void save(Maker maker) {
        if (maker.getMakerId() == null) {
            makerMapper.insert(maker);
        } else {
            makerMapper.update(maker);
        }
    }

    public void delete(Integer makerId) {
        makerMapper.delete(makerId);
    }

    public void deleteByIdAndBusinessAccountId(Integer makerId, Integer businessAccountId) {
        makerMapper.deleteByIdAndBusinessAccountId(makerId, businessAccountId);
    }
}
