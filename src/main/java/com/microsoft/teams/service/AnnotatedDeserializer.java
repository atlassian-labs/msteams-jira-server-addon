package com.microsoft.teams.service;

import com.google.gson.*;
import com.microsoft.teams.anotations.JsonRequired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class AnnotatedDeserializer<T> implements JsonDeserializer {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotatedDeserializer.class);

    @Override
    public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        T pojo = new Gson().fromJson(je, type);

        Field[] fields = pojo.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.getAnnotation(JsonRequired.class) != null) {
                try {
                    f.setAccessible(true);
                    if (f.get(pojo) == null) {
                        throw new JsonParseException("Missing field in JSON: " + f.getName());
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    LOG.warn(ex.getMessage());
                }
            }
        }
        return pojo;
    }
}
