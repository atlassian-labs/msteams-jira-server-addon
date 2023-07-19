package com.microsoft.teams.utils;

import com.google.api.client.http.HttpRequestFactory;

public interface ImageEncoder {
    String encodeImageToBase64(String imageUrl, HttpRequestFactory factory);
}
