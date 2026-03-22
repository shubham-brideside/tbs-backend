package com.brideside.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {

    private int codeTtlMinutes = 10;
    private int resendCooldownSeconds = 60;
    private int maxVerifyAttempts = 5;

    private final Whatsapp whatsapp = new Whatsapp();
    private final Sms sms = new Sms();

    public int getCodeTtlMinutes() {
        return codeTtlMinutes;
    }

    public void setCodeTtlMinutes(int codeTtlMinutes) {
        this.codeTtlMinutes = codeTtlMinutes;
    }

    public int getResendCooldownSeconds() {
        return resendCooldownSeconds;
    }

    public void setResendCooldownSeconds(int resendCooldownSeconds) {
        this.resendCooldownSeconds = resendCooldownSeconds;
    }

    public int getMaxVerifyAttempts() {
        return maxVerifyAttempts;
    }

    public void setMaxVerifyAttempts(int maxVerifyAttempts) {
        this.maxVerifyAttempts = maxVerifyAttempts;
    }

    public Whatsapp getWhatsapp() {
        return whatsapp;
    }

    public Sms getSms() {
        return sms;
    }

    public static class Whatsapp {
        /** Approved Meta template with exactly one body variable (the OTP). */
        private String templateName = "otp_verification";
        private String languageCode = "en";

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public String getLanguageCode() {
            return languageCode;
        }

        public void setLanguageCode(String languageCode) {
            this.languageCode = languageCode;
        }
    }

    /**
     * MSG91 Flow API — create a flow template in MSG91 with one variable for the OTP code.
     */
    public static class Sms {
        private boolean enabled = false;
        /**
         * When true, SMS OTP is accepted without MSG91: the code is logged at WARN (local/dev only).
         * Never enable in production. Prefer {@code spring.profiles.active=dev} with {@code application-dev.yml}.
         */
        private boolean simulate = false;
        private String authKey = "";
        private String flowTemplateId = "";
        /** Recipient object key that maps to your MSG91 template variable (e.g. VAR1, OTP). */
        private String bodyVarKey = "VAR1";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isSimulate() {
            return simulate;
        }

        public void setSimulate(boolean simulate) {
            this.simulate = simulate;
        }

        public String getAuthKey() {
            return authKey;
        }

        public void setAuthKey(String authKey) {
            this.authKey = authKey;
        }

        public String getFlowTemplateId() {
            return flowTemplateId;
        }

        public void setFlowTemplateId(String flowTemplateId) {
            this.flowTemplateId = flowTemplateId;
        }

        public String getBodyVarKey() {
            return bodyVarKey;
        }

        public void setBodyVarKey(String bodyVarKey) {
            this.bodyVarKey = bodyVarKey;
        }

        public boolean isReady() {
            return enabled && StringUtils.hasText(authKey) && StringUtils.hasText(flowTemplateId);
        }

        /** SMS channel may be used (real MSG91 or simulate mode). */
        public boolean isSmsChannelAvailable() {
            return simulate || isReady();
        }
    }
}
