package org.apikey.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.time.Instant;

import org.apikey.entity.ApiKeyEntity;
import org.apikey.model.ApiKey;
import org.apikey.repository.ApiKeyRepository;
import org.apikey.util.ApiKeyUtils;
import org.apikey.dto.ApiKeyRefreshDto;

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

        ApiKeyEntity existingApiKey = findActiveBySellerId(apikey.getSellerId());
        if (existingApiKey != null) {
            return existingApiKey; // Retornar el token existente en lugar de crear uno nuevo
        }

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

        // Configurar fechas con zona horaria de México
        ZonedDateTime now = ZonedDateTime.now(MEXICO_ZONE);
        ZonedDateTime expiresAt = now.plusHours(24);
        // Convertir a Date para MongoDB (que internamente lo almacena como ISODate)
        apiKeyEntity.createdAt(Date.from(now.toInstant()));
        apiKeyEntity.expiredAt(Date.from(expiresAt.toInstant()));
        //apiKeyEntity.createdAt = LocalDateTime.now().withNano(0);
        //apiKeyEntity.expiredAt = apikey.getExpiredAt();

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

    public ApiKeyEntity findActiveBySellerId(String sellerId) {
        // Buscar ApiKey activa por sellerId que no haya expirado
        // Usar la fecha actual en zona horaria de México
        Date now = Date.from(ZonedDateTime.now(MEXICO_ZONE).toInstant());

        return apiKeyRepository.find("sellerId = ?1 and isActive = true and expiredAt > ?2",
                sellerId, now).firstResult();
    }

    public ApiKey refreshApiKeyBySellerId(String sellerId) {
        // Buscar API key activa para el sellerId
        ApiKey existingKey = findActiveBySellerId(sellerId);

        // Verificar si existe
        if (existingKey == null) {
            return null; // No existe una API key activa para este seller
        }

        // Generar nuevo valor de API key
        String platform = existingKey.getPlatform();
        String newApiKeyValue = ApiKeyUtils.generateApiKeyValue(platform);

        // Actualizar API key con nuevo valor y fechas
        existingKey.setApiKey(newApiKeyValue);
        existingKey.setExpiredAt(ApiKeyUtils.getFutureMexicoDate(24));
        existingKey.setUpdatedAt(ApiKeyUtils.getCurrentMexicoDate()); // Si tienes este campo

        // Guardar y retornar
        return apiKeyRepository.update(existingKey);
    }

    @Override
    public ApiKeyRefreshDto refreshApiKeyBySellerIdAndGetInfo(String sellerId) {
        ApiKey refreshedKey = refreshApiKeyBySellerId(sellerId);

        if (refreshedKey == null) {
            return new ApiKeyRefreshDto("No se encontró una API key activa para el seller ID proporcionado");
        }

        return new ApiKeyRefreshDto(
                refreshedKey.getApiKey(), // Nuevo valor de API key
                refreshedKey.getClientId(),
                refreshedKey.getExpiredAt(),
                "API key regenerada exitosamente"
        );
    }

}
