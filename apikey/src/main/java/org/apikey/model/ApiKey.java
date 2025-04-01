package org.apikey.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.bson.types.ObjectId;

import lombok.Data;

@Data
public class ApiKey {

    private ObjectId id;
    private String name;
    private String clientId;
    private String apiKey;
    private String platform;

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    private Boolean isActive;

    private Integer requestCount;

    private Map<String, Object> platformData;

    private UsageLimits usageLimits;

}
