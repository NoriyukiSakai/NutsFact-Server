package com.nines.nutsfact.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nines.nutsfact.domain.model.system.SystemParameter;
import com.nines.nutsfact.infrastructure.mapper.SystemParameterMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SystemParameterRepository {

    private final SystemParameterMapper systemParameterMapper;

    public List<SystemParameter> findAll() {
        return systemParameterMapper.findAll();
    }

    public Optional<SystemParameter> findById(Integer id) {
        return Optional.ofNullable(systemParameterMapper.findById(id));
    }

    public Optional<SystemParameter> findByKey(String parameterKey) {
        return Optional.ofNullable(systemParameterMapper.findByKey(parameterKey));
    }

    public void save(SystemParameter param) {
        if (param.getId() == null) {
            systemParameterMapper.insert(param);
        } else {
            systemParameterMapper.update(param);
        }
    }
}
