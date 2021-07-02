package com.microsoft.teams.utils;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class ExceptionHelpers {

    public static final String GENERIC_ERROR_MESSAGE = "Something went wrong in Jira Server application.";
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHelpers.class);

    public static String getResponseErrorMessage(String exceptionContent) {
        if (exceptionContent == null || StringUtils.isBlank(exceptionContent)) {
            return GENERIC_ERROR_MESSAGE;
        }
        // Format: {"errorMessages":["Error message"],"errors":{"summary":"Summary must be less than 255 characters."}}
        try (JsonReader reader = new JsonReader(new StringReader(exceptionContent))) {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("errorMessages")) {                    
                    reader.beginArray();
                    while (reader.hasNext()) {
                        return reader.nextString();
                    }
                    reader.endArray();
                } else if (name.equals("errors")) {
                    reader.beginObject();
                    JsonToken errorToken = reader.peek();
                    if (errorToken != JsonToken.END_OBJECT)
                    {
                        return String.format("%s error: %s", StringUtils.capitalize(reader.nextName()), reader.nextString());
                    }
                    return StringUtils.EMPTY;
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            return GENERIC_ERROR_MESSAGE;
        }
        return GENERIC_ERROR_MESSAGE;
    }

    public static void exceptionLogExtender(String signature, Level level, Exception e) {
        LOG.debug(signature + " Active threads count: " + Thread.activeCount());

        String lineSeparator =  System.getProperty("line.separator");
        String extendedStackTrace = signature + e.getClass().getCanonicalName() + ": "
                + e.getMessage() + lineSeparator + "ROOT CAUSE: " + ExceptionUtils.getRootCauseMessage(e)
                + lineSeparator + "EXCEPTION STACK TRACE: " + ExceptionUtils.getStackTrace(e);

        if (LOG != null && level != null) {
            switch (level) {
                case INFO: LOG.info(extendedStackTrace); break;
                case WARN: LOG.warn(extendedStackTrace); break;
                case DEBUG: LOG.debug(extendedStackTrace); break;
                case ERROR: LOG.error(extendedStackTrace); break;
                case TRACE: LOG.trace(extendedStackTrace); break;
            }
        }
    }
}
