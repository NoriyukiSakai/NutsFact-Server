package com.nines.nutsfact.api.v1.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nines.nutsfact.domain.model.ConversionTable;
import com.nines.nutsfact.domain.service.ConversionTableService;

import lombok.RequiredArgsConstructor;

/**
 * 重量変換テーブルAPIコントローラー
 * システム共通のシードデータを提供
 */
@RestController
@RequestMapping("/api/v1/conversion-tables")
@RequiredArgsConstructor
public class ConversionTableController {

    private final ConversionTableService conversionTableService;

    /**
     * 全ての変換テーブルを取得
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll(
            @RequestParam(value = "kubun", required = false) Integer kubun) {

        List<ConversionTable> tables;
        if (kubun != null) {
            tables = conversionTableService.findByKubun(kubun);
        } else {
            tables = conversionTableService.findAll();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", tables.size());
        response.put("item", tables);
        return ResponseEntity.ok(response);
    }
}
