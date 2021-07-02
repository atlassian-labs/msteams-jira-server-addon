package com.microsoft.teams.utils;

import com.google.api.client.http.HttpRequestFactory;

public interface ImageHelper {
    String replaceImagesInJson(String jsonString, String baseUrl, HttpRequestFactory factory);
}
