package com.spdb.scenicrouteplanner.reverseGeocoderService;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NominatimResponseHandler {
    private final JSONArray response;

    public NominatimResponseHandler(JSONArray json) {
        response = json;
    }

    public Address deserializeResponse() {
        Address result = new Address();
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject object = response.getJSONObject(i);
                Log.d("RESPONSE_HANDLER", object.getString("display_name"));
                result.setDisplayName(object.getString("display_name"));
                result.setPlaceID(object.getInt("place_id"));
                result.setOsmType(object.getString("osm_type"));
                result.setOsmID(object.getInt("osm_id"));
                result.setLongitude(object.getDouble("lon"));
                result.setLatitude(object.getDouble("lat"));
                result.setPlaceClass(object.getString("class"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}
