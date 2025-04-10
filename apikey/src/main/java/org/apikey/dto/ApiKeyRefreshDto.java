package org.apikey.dto;

import java.util.Date;

public class ApiKeyRefreshDto {
    private String apiKey;
    private String clientId;
    private Date expiredAt;
    private Boolean success;
    private String message;

    // Constructor vacío
    public ApiKeyRefreshDto() {
    }

    // Constructor para casos exitosos con mensaje personalizable
    public ApiKeyRefreshDto(String apiKey, String clientId, Date expiredAt, String message) {
        this.apiKey = apiKey;
        this.clientId = clientId;
        this.expiredAt = expiredAt;
        this.success = true;
        this.message = message;
    }

    // Constructor para casos exitosos (mensaje predeterminado)
    public ApiKeyRefreshDto(String apiKey, String clientId, Date expiredAt) {
        this(apiKey, clientId, expiredAt, "API key refrescada exitosamente");
    }

    // Constructor para casos de error
    public ApiKeyRefreshDto(String message) {
        this.success = false;
        this.message = message;
    }

    // Getters y setters...
}