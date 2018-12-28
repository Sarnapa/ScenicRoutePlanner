package com.spdb.scenicrouteplanner.httpService;

import okhttp3.HttpUrl;

public interface HttpService
{
    HttpUrl buildURL(String query);
}
