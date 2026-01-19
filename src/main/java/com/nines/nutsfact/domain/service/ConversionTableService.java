package com.nines.nutsfact.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nines.nutsfact.domain.model.ConversionTable;
import com.nines.nutsfact.exception.ResourceNotFoundException;
import com.nines.nutsfact.infrastructure.mapper.ConversionTableMapper;

import lombok.RequiredArgsConstructor;

/**
 * 重量変換テーブルサービス
 * システム共通のシードデータを提供（business_accountに依存しない）
 */
@Service
@RequiredArgsConstructor
public class ConversionTableService {

    private final ConversionTableMapper conversionTableMapper;

    /**
     * 全ての変換テーブルを取得
     */
    public List<ConversionTable> findAll() {
        return conversionTableMapper.findAll();
    }

    /**
     * 区分で絞り込んで変換テーブルを取得
     * @param kubun 1:液状 2:粉類（調味料） 3:粉類（飲料） 4:固形・半固形
     */
    public List<ConversionTable> findByKubun(Integer kubun) {
        return conversionTableMapper.findByKubun(kubun);
    }

    /**
     * IDで変換テーブルを取得
     */
    public ConversionTable findById(Integer id) {
        ConversionTable table = conversionTableMapper.findById(id);
        if (table == null) {
            throw new ResourceNotFoundException("ConversionTable", id);
        }
        return table;
    }
}
