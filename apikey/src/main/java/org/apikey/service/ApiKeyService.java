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
        ZonedDateTime now = ZonedDateTime.now(MEXICO_ZONE);
        ZonedDateTime expiresAt = now.plusHours(24);

        ApiKeyEntity existingApiKey = findActiveBySellerId(apikey.getClientId(),now);
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


        // Corregido: Usar LocalDateTime en lugar de Date si eso es lo que espera la entidad
        apiKeyEntity.createdAt = LocalDateTime.ofInstant(now.toInstant(), MEXICO_ZONE);
        apiKeyEntity.expiredAt = LocalDateTime.ofInstant(expiresAt.toInstant(), MEXICO_ZONE);

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

        ZonedDateTime now = ZonedDateTime.now(MEXICO_ZONE);
        ZonedDateTime expiresAt = now.plusHours(24);
        // Corregido: Convertir Date a LocalDateTime si es necesario
        apiKeyEntity.expiredAt = LocalDateTime.ofInstant(expiresAt.toInstant(), MEXICO_ZONE);

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
        // Convertir Date a LocalDateTime para la comparación
        LocalDateTime now = LocalDateTime.now(MEXICO_ZONE);

        // Corregido: Usar el método correcto de búsqueda del repositorio
        return apiKeyRepository.findActiveApiKeyBySellerId(sellerId, now);
    }

    public ApiKeyEntity refreshApiKeyBySellerId(String sellerId) {
        // Buscar API key activa para el sellerId
        ApiKeyEntity existingKey = findActiveBySellerId(sellerId);

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
        ZonedDateTime expiresAt = ZonedDateTime.now(MEXICO_ZONE).plusHours(24);
        existingKey.expiredAt = LocalDateTime.ofInstant(expiresAt.toInstant(), MEXICO_ZONE);

        // Si existe un campo updatedAt, también actualizarlo
        // existingKey.updatedAt = LocalDateTime.now(MEXICO_ZONE);

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
