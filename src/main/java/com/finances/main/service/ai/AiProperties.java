package com.finances.main.service.ai;

import com.finances.main.model.CategoryType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración de IA y conexiones externas.
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private String systemPrompt;
    private final Rag rag = new Rag();
    private final Ext ext = new Ext();
    private final Recommendations recommendations = new Recommendations();

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public Rag getRag() {
        return rag;
    }

    public Ext getExt() {
        return ext;
    }

    public Recommendations getRecommendations() {
        return recommendations;
    }

    public static class Ext {
        private String baseUrl;
        private String apiKey;
        private int timeoutSeconds = 8;
        private boolean fallbackEnabled = false;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public boolean isFallbackEnabled() {
            return fallbackEnabled;
        }

        public void setFallbackEnabled(boolean fallbackEnabled) {
            this.fallbackEnabled = fallbackEnabled;
        }
    }

    /**
     * Configuración del servicio RAG externo.
     */
    public static class Rag {
        private String baseUrl;
        private String apiKey;
        private int timeoutSeconds = 8;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }

    /**
     * Configuración específica para recomendaciones automáticas.
     */
    public static class Recommendations {
        private boolean enabled = true;
        private long intervalMs = 1_800_000;
        private int lookbackDays = 30;
        private CategoryType categoryType = CategoryType.GASTO;
        private String prompt;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getIntervalMs() {
            return intervalMs;
        }

        public void setIntervalMs(long intervalMs) {
            this.intervalMs = intervalMs;
        }

        public int getLookbackDays() {
            return lookbackDays;
        }

        public void setLookbackDays(int lookbackDays) {
            this.lookbackDays = lookbackDays;
        }

        public CategoryType getCategoryType() {
            return categoryType;
        }

        public void setCategoryType(CategoryType categoryType) {
            this.categoryType = categoryType;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }
}
