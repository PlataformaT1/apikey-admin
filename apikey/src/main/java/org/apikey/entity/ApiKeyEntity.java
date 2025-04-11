package org.apikey.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import org.apikey.model.UsageLimits;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@MongoEntity(collection = "api_key")
public class ApiKeyEntity {

    @BsonId
    public ObjectId id;
    public String name;
    public String clientId;
    public String apiKey;
    public String platform;
    public LocalDateTime createdAt;
    public LocalDateTime expiredAt;
    public LocalDateTime updatedAt;
    public Date createdAtLocalized;
    public Date expiredAtLocalized;
    public Date updatedAtLocalized;
    public Boolean isActive;
    public Integer requestCount;
    public Map<String, Object> platformData;
    public UsageLimits usageLimits;

}
