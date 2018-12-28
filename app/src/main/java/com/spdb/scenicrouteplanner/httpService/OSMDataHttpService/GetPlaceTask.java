package com.spdb.scenicrouteplanner.httpService.OSMDataHttpService;

import android.os.AsyncTask;
import android.util.Log;

import com.spdb.scenicrouteplanner.httpService.HttpService;
import com.spdb.scenicrouteplanner.lib.OSM.Place;

import org.json.JSONArray;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetPlaceTask extends AsyncTask<String, Void, Place> implements HttpService {
    // ==============================
    // Private fields
    // ==============================
    private static final Integer CONNECTION_TIMEOUT = 10000;
    private static final Integer READ_TIMEOUT = 10000;
    private static final Integer WRITE_TIMEOUT = 10000;

    private static final String BASE_URL = "https://nominatim.openstreetmap.org/";

    // ==============================
    // Override AsyncTask
    // ==============================
    @Override
    protected Place doInBackground(String... params) {
        String query = params[0];

        if (query.isEmpty()) {
            this.cancel(true);
        }
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
            String jsonData = response.body().string();
            JSONArray jsonArray = new JSONArray(jsonData);
            Log.d("GET_PLACE_TASK", jsonArray.toString());
            NominatimResponseHandler responseHandler = new NominatimResponseHandler(jsonArray);
            Log.d("GET_PLACE_TASK", "KONIEC");
            return responseHandler.deserializeResponse();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Place result) {
        super.onPostExecute(result);
    }

    // ==============================
    // Override HttpService
    // ==============================
    @Override
    public HttpUrl buildURL(String query) {
        HttpUrl httpUrl = HttpUrl.parse(BASE_URL).newBuilder()
                .addQueryParameter("format", "json")
                .addQueryParameter("limit", "1")
                .addQueryParameter("q", query)
                .build();
        return httpUrl;
    }

}
