package com.nines.nutsfact.domain.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.domain.model.system.SystemParameter;
import com.nines.nutsfact.domain.repository.SystemParameterRepository;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * システムパラメータサービス
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemParameterService {

    private final SystemParameterRepository repository;

    // キャッシュ（頻繁にアクセスするため）
    private final Map<String, SystemParameter> cache = new ConcurrentHashMap<>();

    // パラメータキー定数
    public static final String KEY_ADMIN_INVITATION_EXPIRATION_HOURS = "ADMIN_INVITATION_EXPIRATION_HOURS";
    public static final String KEY_OWNER_INVITATION_EXPIRATION_HOURS = "OWNER_INVITATION_EXPIRATION_HOURS";
    public static final String KEY_LOGIN_FAILURE_LOCKOUT_THRESHOLD = "LOGIN_FAILURE_LOCKOUT_THRESHOLD";
    public static final String KEY_LOCKOUT_DURATION_MINUTES = "LOCKOUT_DURATION_MINUTES";

    // デフォルト値
    private static final int DEFAULT_ADMIN_INVITATION_HOURS = 168;  // 7日
    private static final int DEFAULT_OWNER_INVITATION_HOURS = 168;  // 7日
    private static final int DEFAULT_LOCKOUT_THRESHOLD = 5;
    private static final int DEFAULT_LOCKOUT_DURATION = 60;  // 60分

    public List<SystemParameter> findAll() {
        return repository.findAll();
    }

    public SystemParameter findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemParameter", id));
    }

    public SystemParameter findByKey(String key) {
        // キャッシュから取得を試みる
        SystemParameter cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        // DBから取得してキャッシュに保存
        SystemParameter param = repository.findByKey(key).orElse(null);
        if (param != null) {
            cache.put(key, param);
        }
        return param;
    }

    /**
     * 運営管理者からのビジネスオーナー招待有効期限（時間）
     */
    public Integer getAdminInvitationExpirationHours() {
        return getIntValue(KEY_ADMIN_INVITATION_EXPIRATION_HOURS, DEFAULT_ADMIN_INVITATION_HOURS);
    }

    /**
     * ビジネスオーナーからの利用者招待有効期限（時間）
     */
    public Integer getOwnerInvitationExpirationHours() {
        return getIntValue(KEY_OWNER_INVITATION_EXPIRATION_HOURS, DEFAULT_OWNER_INVITATION_HOURS);
    }

    /**
     * ログイン失敗でロックアウトする回数
     */
    public Integer getLoginFailureLockoutThreshold() {
        return getIntValue(KEY_LOGIN_FAILURE_LOCKOUT_THRESHOLD, DEFAULT_LOCKOUT_THRESHOLD);
    }

    /**
     * ロックアウト時間（分）
     */
    public Integer getLockoutDurationMinutes() {
        return getIntValue(KEY_LOCKOUT_DURATION_MINUTES, DEFAULT_LOCKOUT_DURATION);
    }

    /**
     * パラメータ値を更新
     */
    @Transactional
    public SystemParameter update(Integer id, String value) {
        SystemParameter param = findById(id);
        param.setParameterValue(value);
        repository.save(param);

        // キャッシュをクリア
        cache.remove(param.getParameterKey());
        log.info("SystemParameter updated: key={}, value={}", param.getParameterKey(), value);

        return param;
    }

    /**
     * キャッシュをクリア
     */
    public void clearCache() {
        cache.clear();
        log.info("SystemParameter cache cleared");
    }

    private Integer getIntValue(String key, int defaultValue) {
        SystemParameter param = findByKey(key);
        if (param != null) {
            Integer value = param.getIntValue();
            if (value != null) {
                return value;
            }
        }
        return defaultValue;
    }
}
