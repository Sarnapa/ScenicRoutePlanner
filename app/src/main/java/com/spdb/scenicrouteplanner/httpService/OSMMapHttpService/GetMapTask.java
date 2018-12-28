package com.spdb.scenicrouteplanner.httpService.OSMMapHttpService;

import android.os.AsyncTask;
import android.util.Log;

import com.spdb.scenicrouteplanner.httpService.HttpService;
import com.spdb.scenicrouteplanner.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class GetMapTask extends AsyncTask<String, Integer, String> implements HttpService {
    // ==============================
    // Private fields
    // ==============================
    private static final Integer CONNECTION_TIMEOUT = 10000;
    private static final Integer READ_TIMEOUT = 10000;
    private static final Integer WRITE_TIMEOUT = 10000;

    private static final String BASE_URL = "http://api.openstreetmap.org/api/0.6/map";
    private static final String DIRECTORY = "/SRP/maps";

    // ==============================
    // Override AsyncTask
    // ==============================
    @Override
    protected String doInBackground(String... params) {
        String query = params[0];
        if (query.isEmpty()) {
            this.cancel(true);
        }

        File destFile = FileUtils.buildPath(DIRECTORY,"osm");
        Log.d("GET_MAP_TASK", destFile.toString());
        Log.d("GET_MAP_TASK", buildURL(query).toString());
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(buildURL(query))
                    .build();
            Response response = client.newCall(request).execute();
            BufferedSink sink = Okio.buffer(Okio.sink(destFile));
            sink.writeAll(response.body().source());
            sink.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        Log.d("GET_MAP_TASK", "KONIEC");
        return null;
    }

    // ==============================
    // Override HttpService
    // ==============================
    @Override
    public HttpUrl buildURL(String query) {
        HttpUrl httpUrl = HttpUrl.parse(BASE_URL).newBuilder()
                .addEncodedQueryParameter("bbox", query)
                .build();
        return httpUrl;
    }
}
