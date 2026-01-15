package com.nines.nutsfact.api.v1.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nines.nutsfact.api.v1.request.SystemParameterUpdateRequest;
import com.nines.nutsfact.domain.model.system.SystemParameter;
import com.nines.nutsfact.domain.service.SystemParameterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * システムパラメータコントローラー（運営管理者用）
 */
@RestController
@RequestMapping("/apix/SystemParameter")
@RequiredArgsConstructor
public class SystemParameterController {

    private final SystemParameterService systemParameterService;

    /**
     * システムパラメータ一覧取得
     */
    @GetMapping("/findAll")
    public ResponseEntity<Map<String, Object>> findAll() {
        var parameters = systemParameterService.findAll();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "Success");
        response.put("records", parameters.size());
        response.put("items", parameters.stream().map(this::toMap).toList());
        return ResponseEntity.ok(response);
    }

    /**
     * システムパラメータ詳細取得
     */
    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(@RequestParam("id") Integer id) {
        SystemParameter parameter = systemParameterService.findById(id);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(parameter));
        return ResponseEntity.ok(response);
    }

    /**
     * システムパラメータ更新
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody SystemParameterUpdateRequest request) {
        SystemParameter updated = systemParameterService.update(request.getId(), request.getParameterValue());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "Success");
        response.put("item", toMap(updated));
        return ResponseEntity.ok(response);
    }

    /**
     * キャッシュクリア
     */
    @PostMapping("/clearCache")
    public ResponseEntity<Map<String, Object>> clearCache() {
        systemParameterService.clearCache();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "Success");
        response.put("message", "キャッシュをクリアしました");
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMap(SystemParameter param) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", param.getId());
        map.put("parameter_key", param.getParameterKey());
        map.put("parameter_value", param.getParameterValue());
        map.put("parameter_type", param.getParameterType());
        map.put("description", param.getDescription());
        map.put("create_date", param.getCreateDate() != null ? param.getCreateDate().toString() : null);
        map.put("last_update_date", param.getLastUpdateDate() != null ? param.getLastUpdateDate().toString() : null);
        return map;
    }
}
