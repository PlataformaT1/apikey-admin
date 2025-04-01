package org.apikey.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apikey.entity.ApiKeyEntity;
import org.apikey.model.ApiKey;
import org.apikey.repository.ApiKeyRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ApiKeyService {

    @Inject
    ApiKeyRepository apiKeyRepository;

    public List<ApiKeyEntity> getAllApiKey() {

        return apiKeyRepository.findAllApiKey();
    }

    public List<ApiKeyEntity> getApiKeyClient(final String clientId, final String plataform) {
        return apiKeyRepository.findApiKeyClientId(clientId, plataform);
    }

    public ApiKeyEntity createApiKey(final ApiKey apikey) {

        StringBuilder apiKeyBuilder = new StringBuilder();
        apiKeyBuilder.append("APP-");
        apiKeyBuilder.append(apikey.getPlatform().toUpperCase());
        apiKeyBuilder.append("-");
        apiKeyBuilder.append(UUID.randomUUID());

        var apiKeyEntity = new ApiKeyEntity();

        apiKeyEntity.apiKey = apiKeyBuilder.toString();

        apiKeyEntity.clientId = apikey.getClientId();
        apiKeyEntity.name = apikey.getName();
        apiKeyEntity.platform = apikey.getPlatform();
        apiKeyEntity.usageLimits = apikey.getUsageLimits();
        apiKeyEntity.platformData = apikey.getPlatformData();

        apiKeyEntity.isActive = true;
        apiKeyEntity.requestCount = 0;
        apiKeyEntity.createdAt = LocalDateTime.now().withNano(0);
        apiKeyEntity.expiredAt = apikey.getExpiredAt();

        return apiKeyRepository.saveApiKey(apiKeyEntity);
    }

    public ApiKeyEntity updateApiKey(final ApiKey apikey) {
        var apiKeyEntity = new ApiKeyEntity();

        apiKeyEntity.apiKey = apikey.getApiKey();
        apiKeyEntity.clientId = apikey.getClientId();
        apiKeyEntity.name = apikey.getName();
        apiKeyEntity.platform = apikey.getPlatform();
        apiKeyEntity.usageLimits = apikey.getUsageLimits();
        apiKeyEntity.platformData = apikey.getPlatformData();
        apiKeyEntity.isActive = apikey.getIsActive();
        apiKeyEntity.expiredAt = apikey.getExpiredAt();

        return apiKeyRepository.updateApiKey(apiKeyEntity);
    }

    public ApiKeyEntity patchApiKey(final String id, final ApiKey apikey) {
        Map<String, Object> updateFields = new HashMap<>();

        if (apikey.getName() != null)
            updateFields.put("name", apikey.getName());
        if (apikey.getPlatform() != null)
            updateFields.put("platform", apikey.getPlatform());
        if (apikey.getIsActive() != null)
            updateFields.put("isActive", apikey.getIsActive());
        if (apikey.getPlatformData() != null)
            updateFields.put("platformData", apikey.getPlatformData());
        if (apikey.getUsageLimits() != null)
            updateFields.put("usageLimits", apikey.getUsageLimits());

        return apiKeyRepository.patchApiKey(id, updateFields);

    }

    public void deleteApiKey(final String id) {
        apiKeyRepository.deleteApiKey(id);
    }

    public ApiKeyEntity getApiKeyById(final String id) {
        return apiKeyRepository.findApiKeyById(id);
    }


}
