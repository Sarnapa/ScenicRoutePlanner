package com.spdb.scenicrouteplanner.reverseGeocoderService;

import android.net.Uri;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.spdb.scenicrouteplanner.activity.MainActivity;
import org.json.JSONArray;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class ReverseGeocoder implements Runnable{

    private static final String BASE_URL = "https://nominatim.openstreetmap.org/";
    private static final int QUEUE_SIZE = 2;
    private Uri url;
    private String query;
    public BlockingQueue<Address> getAddressQueue() {
        return addressQueue;
    }

    private BlockingQueue<Address> addressQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
    public ReverseGeocoder(String q){
        query = q;
    }

    public void prepareAddress() throws Exception{
        if(query.isEmpty()){
            throw new Exception("Invalid query");
        }
        Uri.Builder builder = new Uri.Builder();
        builder.encodedPath(BASE_URL);
        builder.appendQueryParameter("format","json");
        builder.appendQueryParameter("limit","1");
        builder.appendQueryParameter("q", query);
        url = builder.build();
        Log.d("REVERSE_GEOCODER", url.toString());
        Log.d("REVERSE_GEOCODER", "KONIEC");
    }

    @Override
    public void run() {
        JsonArrayRequest jsonRequest = new JsonArrayRequest
                (Request.Method.GET, url.toString(), null, new Response.Listener<JSONArray>(){

                    @Override
                    public void onResponse(JSONArray response){
                        Log.d("REVERSE_GEOCODER", "SUCCESS");
                        Log.d("REVERSE_GEOCODER", response.toString());
                        NominatimResponseHandler responseHandler = new NominatimResponseHandler(response);
                        Address address = responseHandler.deserializeResponse();
                        try {
                            addressQueue.put(address);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.d("REVERSE_GEOCODER", error.getMessage());
                        // TODO: Handle error
                    }
                });
        MainActivity.getInstance().getRequestQueue().add(jsonRequest);
    }

    public void start(){
        Thread t = new Thread (this);
        t.start ();
    }
}
