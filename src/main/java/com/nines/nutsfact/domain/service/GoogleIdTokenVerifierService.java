package com.nines.nutsfact.domain.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Google ID Tokenの検証サービス
 */
@Service
@Slf4j
public class GoogleIdTokenVerifierService {

    @Value("${google.client-id.ios:}")
    private String iosClientId;

    @Value("${google.client-id.macos:}")
    private String macosClientId;

    private GoogleIdTokenVerifier verifier;

    /**
     * Google認証で取得したユーザー情報
     */
    public record GoogleUserInfo(
            String sub,         // Google User ID（一意識別子）
            String email,       // メールアドレス
            String name,        // 表示名
            String pictureUrl   // プロフィール画像URL
    ) {}

    /**
     * Google ID Tokenを検証し、ユーザー情報を取得
     *
     * @param idToken Google Sign-Inで取得したIDトークン
     * @return 検証済みのユーザー情報
     * @throws IllegalArgumentException トークンが無効な場合
     */
    public GoogleUserInfo verify(String idToken) {
        try {
            if (verifier == null) {
                initializeVerifier();
            }

            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                log.warn("Google ID Token verification failed: token is null");
                throw new IllegalArgumentException("無効なGoogle IDトークンです");
            }

            GoogleIdToken.Payload payload = token.getPayload();

            // メール検証済みかチェック
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                log.warn("Google ID Token verification failed: email not verified");
                throw new IllegalArgumentException("Googleアカウントのメールアドレスが検証されていません");
            }

            GoogleUserInfo userInfo = new GoogleUserInfo(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("name"),
                    (String) payload.get("picture")
            );

            log.info("Google ID Token verified successfully: sub={}, email={}", userInfo.sub(), userInfo.email());
            return userInfo;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google ID Token verification error", e);
            throw new IllegalArgumentException("Google IDトークンの検証に失敗しました: " + e.getMessage());
        }
    }

    private void initializeVerifier() {
        List<String> clientIds = buildClientIdList();
        if (clientIds.isEmpty()) {
            log.warn("No Google Client IDs configured. Using empty list for development.");
            clientIds = Collections.emptyList();
        }

        verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(clientIds)
                .build();

        log.info("GoogleIdTokenVerifier initialized with {} client IDs", clientIds.size());
    }

    private List<String> buildClientIdList() {
        return Arrays.asList(iosClientId, macosClientId)
                .stream()
                .filter(id -> id != null && !id.isBlank())
                .toList();
    }
}
