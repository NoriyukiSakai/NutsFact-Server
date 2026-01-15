package com.nines.nutsfact.domain.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nines.nutsfact.domain.model.user.InvitationCode;
import com.nines.nutsfact.domain.model.user.User;
import com.nines.nutsfact.domain.repository.InvitationCodeRepository;
import com.nines.nutsfact.domain.repository.UserRepository;
import com.nines.nutsfact.exception.DuplicateEntityException;
import com.nines.nutsfact.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationCodeService {

    private final InvitationCodeRepository invitationCodeRepository;
    private final SystemParameterService systemParameterService;
    private final UserRepository userRepository;

    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    // ロール定義
    public static final int ROLE_SYSTEM_ADMIN = 0;       // 運営管理者
    public static final int ROLE_BUSINESS_OWNER = 10;    // ビジネスオーナー
    public static final int ROLE_BUSINESS_USER = 21;     // ビジネス利用者（デフォルト）

    public List<InvitationCode> findAll() {
        return invitationCodeRepository.findAll();
    }

    public List<InvitationCode> findByBusinessAccountId(Integer businessAccountId) {
        return invitationCodeRepository.findByBusinessAccountId(businessAccountId);
    }

    public InvitationCode findById(Integer id) {
        return invitationCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InvitationCode", id));
    }

    public InvitationCode findByCode(String code) {
        return invitationCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("InvitationCode with code " + code + " not found"));
    }

    @Transactional
    public InvitationCode create(Integer businessAccountId, String email, Integer role,
                                  Integer expirationDays, Integer createdByUserId) {
        // 同一メールアドレスで有効な（未使用かつ期限内の）招待が既に存在する場合はエラー
        invitationCodeRepository.findActiveByEmail(email).ifPresent(existing -> {
            throw new DuplicateEntityException(
                String.format("メールアドレス %s には既に有効な招待コードが存在します", email)
            );
        });

        InvitationCode invitationCode = new InvitationCode();
        invitationCode.setBusinessAccountId(businessAccountId);
        invitationCode.setCode(generateCode());
        invitationCode.setEmail(email);
        // roleがnullの場合はビジネス利用者（21）をデフォルト設定
        invitationCode.setRole(role != null ? role : ROLE_BUSINESS_USER);

        // 有効期限を計算（招待者のロールに応じて切り替え）
        LocalDateTime expiresAt = calculateExpiresAt(createdByUserId, expirationDays);
        invitationCode.setExpiresAt(expiresAt);

        invitationCode.setIsUsed(false);
        invitationCode.setCreatedByUserId(createdByUserId);
        invitationCodeRepository.save(invitationCode);

        log.info("Created invitation code: email={}, role={}, expiresAt={}", email, role, expiresAt);
        return invitationCode;
    }

    /**
     * 有効期限を計算
     * 招待者のロールに応じて、運営管理者用またはビジネスオーナー用の有効期限を使用
     */
    private LocalDateTime calculateExpiresAt(Integer createdByUserId, Integer expirationDays) {
        // expirationDaysが明示的に指定されている場合はそれを使用（日単位）
        if (expirationDays != null) {
            return LocalDateTime.now().plusDays(expirationDays);
        }

        // 招待者のロールを取得
        User creator = userRepository.findById(createdByUserId).orElse(null);
        int creatorRole = creator != null ? (creator.getRole() != null ? creator.getRole() : ROLE_BUSINESS_USER) : ROLE_BUSINESS_USER;

        // ロールに応じて有効期限（時間単位）を取得
        int expirationHours;
        if (creatorRole == ROLE_SYSTEM_ADMIN) {
            // 運営管理者からの招待
            expirationHours = systemParameterService.getAdminInvitationExpirationHours();
            log.debug("Using admin invitation expiration: {} hours", expirationHours);
        } else {
            // ビジネスオーナー等からの招待
            expirationHours = systemParameterService.getOwnerInvitationExpirationHours();
            log.debug("Using owner invitation expiration: {} hours", expirationHours);
        }

        return LocalDateTime.now().plusHours(expirationHours);
    }

    public InvitationCodeVerifyResult verify(String code, String email) {
        InvitationCode invitationCode = invitationCodeRepository.findByCodeAndEmail(code, email)
                .orElse(null);

        if (invitationCode == null) {
            return InvitationCodeVerifyResult.invalid("CODE_NOT_FOUND", "招待コードが見つかりません");
        }

        if (invitationCode.getIsUsed()) {
            return InvitationCodeVerifyResult.invalid("ALREADY_USED", "この招待コードは既に使用されています");
        }

        if (invitationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            return InvitationCodeVerifyResult.invalid("EXPIRED", "この招待コードは有効期限が切れています");
        }

        return InvitationCodeVerifyResult.valid(invitationCode);
    }

    @Transactional
    public void markAsUsed(Integer id) {
        findById(id);
        invitationCodeRepository.markAsUsed(id);
    }

    @Transactional
    public void revoke(Integer id) {
        findById(id);
        invitationCodeRepository.delete(id);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARACTERS.charAt(RANDOM.nextInt(CODE_CHARACTERS.length())));
        }
        return sb.toString();
    }

    public static class InvitationCodeVerifyResult {
        private final boolean valid;
        private final InvitationCode invitationCode;
        private final String errorCode;
        private final String errorMessage;

        private InvitationCodeVerifyResult(boolean valid, InvitationCode invitationCode,
                                            String errorCode, String errorMessage) {
            this.valid = valid;
            this.invitationCode = invitationCode;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public static InvitationCodeVerifyResult valid(InvitationCode invitationCode) {
            return new InvitationCodeVerifyResult(true, invitationCode, null, null);
        }

        public static InvitationCodeVerifyResult invalid(String errorCode, String errorMessage) {
            return new InvitationCodeVerifyResult(false, null, errorCode, errorMessage);
        }

        public boolean isValid() { return valid; }
        public InvitationCode getInvitationCode() { return invitationCode; }
        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
    }
}
