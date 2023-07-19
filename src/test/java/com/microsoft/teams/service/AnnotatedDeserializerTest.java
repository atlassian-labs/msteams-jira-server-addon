package com.microsoft.teams.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;
import org.junit.Test;
import org.mockito.internal.util.reflection.GenericMetadataSupport;

public class AnnotatedDeserializerTest {
    @Test
    public void testDeserialize() throws JsonParseException {
        AnnotatedDeserializer<Object> annotatedDeserializer = (AnnotatedDeserializer<Object>) mock(
                AnnotatedDeserializer.class);
        when(annotatedDeserializer.deserialize(any(), any(),
                any())).thenReturn("42");
        JsonArray je = new JsonArray();
        annotatedDeserializer.deserialize(je, new GenericMetadataSupport.TypeVarBoundedType(null),
                mock(JsonDeserializationContext.class));
        verify(annotatedDeserializer).deserialize(any(), any(),
                any());
    }
}

