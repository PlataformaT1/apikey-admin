package org.apikey.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apikey.entity.ApiKeyEntity;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ApiKeyRepository {

    @Inject
    MongoClient mongoClient;

    private MongoCollection<ApiKeyEntity> getCollection() {
        return mongoClient
                .getDatabase("api_key_db")
                .getCollection("api_key", ApiKeyEntity.class);
    }

    public List<ApiKeyEntity> findAllApiKey() {
        return getCollection().find().into(new ArrayList<>());
    }

    public List<ApiKeyEntity> findApiKeyClientId(final String clientId, final String platform) {
        Bson filter = Filters.and(
                Filters.eq("clientId", clientId),
                Filters.eq("platform", platform));
        return getCollection().find(filter).into(new ArrayList<>());
    }

    public ApiKeyEntity saveApiKey(ApiKeyEntity apiKeyEntity) {
        getCollection().insertOne(apiKeyEntity);
        return apiKeyEntity;
    }

    public ApiKeyEntity updateApiKey(ApiKeyEntity apiKeyEntity) {
        Bson filter = Filters.and(
                Filters.eq("_id", apiKeyEntity.id));

        getCollection().replaceOne(filter, apiKeyEntity);
        return apiKeyEntity;
    }

    public ApiKeyEntity patchApiKey(String id, Map<String, Object> updateFields) {
        Bson filter = Filters.eq("_id", new ObjectId(id));

        if (!updateFields.isEmpty()) {
            Bson updateOperation = new Document("$set", updateFields);
            UpdateResult result = getCollection().updateOne(filter, updateOperation);

            if (result.getMatchedCount() == 0) {
                throw new NotFoundException("API key with id " + id + " not found.");
            }
        }

        return getCollection().find(filter).first();
    }

    public void deleteApiKey(String id) {
        Bson filter = Filters.eq("_id", new ObjectId(id));
        var result = getCollection().deleteOne(filter);

        if (result.getDeletedCount() == 0) {
            throw new NotFoundException("API key with id " + id + " not found.");
        }
    }

    public ApiKeyEntity findApiKeyById(String id) {
        Bson filter = Filters.eq("_id", new ObjectId(id));
        ApiKeyEntity apiKeyEntity = getCollection().find(filter).first();
        if (apiKeyEntity == null) {
            throw new NotFoundException("API key with id " + id + " not found.");
        }
        return apiKeyEntity;
    }
}
