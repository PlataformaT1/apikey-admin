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
    private static final ZoneId MEXICO_ZONE = ZoneId.of("America/Mexico_City");

    @Inject
    ApiKeyRepository apiKeyRepository;

    public List<ApiKeyEntity> getAllApiKey() {
        return apiKeyRepository.findAllApiKey();
    }

    public List<ApiKeyEntity> getApiKeyClient(final String clientId, final String plataform) {
        return apiKeyRepository.findApiKeyClientId(clientId, plataform);
    }

    public ApiKeyEntity createApiKey(final ApiKey apikey) {

        // Configurar fechas con zona horaria de México
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(24);

        // Crear ZonedDateTime en zona de México
        ZonedDateTime nowZoned = ZonedDateTime.now(MEXICO_ZONE);
        ZonedDateTime expiresZoned = nowZoned.plusHours(24);

        // Convertir a Date para MongoDB manteniendo la zona horaria
        Date nowDzoned = Date.from(nowZoned.toInstant());
        Date expiresAtDzoned = Date.from(expiresZoned.toInstant());

        ApiKeyEntity existingApiKey = findActiveBySellerId(apikey.getClientId(),nowDzoned);
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
        apiKeyEntity.isActive = true;2025-04-10T18:21:05.042Z
        apiKeyEntity.requestCount = 0;


        // Corregido: Usar LocalDateTime en lugar de Date si eso es lo que espera la entidad
        apiKeyEntity.createdAt = now;
        apiKeyEntity.expiredAt = expiresAt;
        apiKeyEntity.updatedAt = now;
        // agregamos fechas Localizadas
        apiKeyEntity.createdAtLocalized = nowDzoned;
        apiKeyEntity.expiredAtLocalized = expiresAtDzoned;
        apiKeyEntity.updatedAtLocalized = nowDzoned;

        return apiKeyRepository.saveApiKey(apiKeyEntity);
    }

    public ApiKeyEntity updateApiKey(final ApiKey apikey) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(24);

        // Crear ZonedDateTime en zona de México
        ZonedDateTime nowZoned = ZonedDateTime.now(MEXICO_ZONE);
        ZonedDateTime expiresZoned = nowZoned.plusHours(24);

        // Convertir a Date para MongoDB manteniendo la zona horaria
        Date nowDzoned = Date.from(nowZoned.toInstant());
        Date expiresAtDzoned = Date.from(expiresZoned.toInstant());

        var apiKeyEntity = new ApiKeyEntity();

        apiKeyEntity.apiKey = apikey.getApiKey();
        apiKeyEntity.clientId = apikey.getClientId();
        apiKeyEntity.name = apikey.getName();
        apiKeyEntity.platform = apikey.getPlatform();
        apiKeyEntity.usageLimits = apikey.getUsageLimits();
        apiKeyEntity.platformData = apikey.getPlatformData();
        apiKeyEntity.isActive = apikey.getIsActive();

        apiKeyEntity.expiredAt = expiresAt;
        apiKeyEntity.updatedAt = now;

        apiKeyEntity.expiredAtLocalized = expiresAtDzoned;
        apiKeyEntity.updatedAtLocalized = nowDzoned;

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
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime nowZoned = ZonedDateTime.now(MEXICO_ZONE);
        Date nowDzoned = Date.from(nowZoned.toInstant());
        updateFields.put("updatedAt", now);
        updateFields.put("updatedAtLocalized" , nowZoned);
        return apiKeyRepository.patchApiKey(id, updateFields);

    }

    public void deleteApiKey(final String id) {
        apiKeyRepository.deleteApiKey(id);
    }

    public ApiKeyEntity getApiKeyById(final String id) {
        return apiKeyRepository.findApiKeyById(id);
    }

    public ApiKeyEntity findActiveBySellerId(String sellerId,Date now) {
        // Buscar ApiKey activa por sellerId que no haya expirado

        // Corregido: Usar el método correcto de búsqueda del repositorio
        return apiKeyRepository.findActiveApiKeyBySellerId(sellerId, now);
    }

    public ApiKeyEntity refreshApiKeyBySellerId(String sellerId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(24);

        // Crear ZonedDateTime en zona de México
        ZonedDateTime nowZoned = ZonedDateTime.now(MEXICO_ZONE);
        ZonedDateTime expiresZoned = nowZoned.plusHours(24);

        // Convertir a Date para MongoDB manteniendo la zona horaria
        Date nowDzoned = Date.from(nowZoned.toInstant());
        Date expiresAtDzoned = Date.from(expiresZoned.toInstant());

        // Buscar API key activa para el sellerId
        ApiKeyEntity existingKey = findActiveBySellerId(sellerId,nowDzoned);

        // Verificar si existe
        if (existingKey == null) {
            return null; // No existe una API key activa para este seller
        }

        // Generar nuevo valor de API key
        StringBuilder apiKeyBuilder = new StringBuilder();
        apiKeyBuilder.append("APP-");
        apiKeyBuilder.append(existingKey.platform.toUpperCase());
        apiKeyBuilder.append("-");
        apiKeyBuilder.append(UUID.randomUUID());

        // Actualizar API key con nuevo valor y fechas
        existingKey.apiKey = apiKeyBuilder.toString();

        // Configurar fechas con zona horaria de México

        existingKey.expiredAt = expiresAt;
        existingKey.updatedAt = now;

        existingKey.expiredAtLocalized = expiresAtDzoned;
        existingKey.updatedAtLocalized = nowDzoned;

        // Guardar y retornar
        return apiKeyRepository.updateApiKey(existingKey);
    }

    public ApiKeyRefreshDto refreshApiKeyBySellerIdAndGetInfo(String sellerId) {
        ApiKeyEntity refreshedKey = refreshApiKeyBySellerId(sellerId);

        if (refreshedKey == null) {
            return new ApiKeyRefreshDto("No se encontró una API key activa para el seller ID proporcionado");
        }

        // Convertir LocalDateTime a Date para el DTO si es necesario
        Date expiredAtDate = Date.from(refreshedKey.expiredAt.atZone(MEXICO_ZONE).toInstant());

        return new ApiKeyRefreshDto(
                refreshedKey.apiKey,
                refreshedKey.clientId,
                expiredAtDate,
                "API key regenerada exitosamente"
        );
    }

}
