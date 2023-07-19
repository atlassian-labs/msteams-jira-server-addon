package com.microsoft.teams.service;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component
public class HttpClientService {
    private final CookieStore httpCookieStore = new BasicCookieStore();
    private final HttpClient client = HttpClientBuilder
            .create()
            .setDefaultCookieStore(httpCookieStore)
            .disableRedirectHandling()
            .build();

    public HttpClient getClient() {
        return client;
    }

    void clearCookie() {
        httpCookieStore.clear();
    }
}
