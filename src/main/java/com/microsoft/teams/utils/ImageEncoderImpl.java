package com.microsoft.teams.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ImageEncoderImpl implements ImageEncoder {

    private static final Logger LOG = LoggerFactory.getLogger(ImageEncoderImpl.class);

    private static final int BUFFER_SIZE = 1024;
    private static final int ONE_SECOND = 1000;

    public String encodeImageToBase64(String iconUrlString, HttpRequestFactory factory) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            long start = System.currentTimeMillis();
            HttpResponse response = factory.buildGetRequest(new GenericUrl(StringEscapeUtils.unescapeJavaScript(iconUrlString))).execute();
            String contentType = response.getContentType();
            InputStream inputStream = response.getContent();
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            String iconEncodedString = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            response.disconnect();

            long processTime = System.currentTimeMillis() - start;
            if (processTime > ONE_SECOND) {
                LOG.debug("===>Encoding time longer than one second {}, for url {}", processTime, iconUrlString);
            }
            return String.format("data:%s;base64,%s", contentType, iconEncodedString);
        } catch (Exception e) {
            LOG.error(String.format("Could not process image to encode \"%s\"", iconUrlString));
            return iconUrlString;
        }
    }
}
