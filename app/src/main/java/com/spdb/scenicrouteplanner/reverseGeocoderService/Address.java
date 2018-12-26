package com.spdb.scenicrouteplanner.reverseGeocoderService;

 /*
 [{
 "place_id":"79702876",
 "licence":"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright",
 "osm_type":"way",
 "osm_id":"23485444",
 "boundingbox":["52.2185991","52.2192531","21.0106801","21.0129243"],
 "lat":"52.2189474",
 "lon":"21.012146755762",
 "display_name":"Wydział Elektroniki i Technik Informacyjnych, 15/19, Nowowiejska, VIII, Śródmieście, Warszawa, województwo mazowieckie, 00-665, Polska",
 "class":"building",
 "type":"yes",
 "importance":0.6010000000000001
 }]
 */

public class Address {
    private String displayName;
    private int placeID;
    private String osmType;
    private int osmID;
    private double longitude;
    private double latitude;
    private String placeClass;

    public int getPlaceID() {
        return placeID;
    }

    public void setPlaceID(int placeID) {
        this.placeID = placeID;
    }

    public String getOsmType() {
        return osmType;
    }

    public void setOsmType(String osmType) {
        this.osmType = osmType;
    }

    public int getOsmID() {
        return osmID;
    }

    public void setOsmID(int osmID) {
        this.osmID = osmID;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getPlaceClass() {
        return placeClass;
    }

    public void setPlaceClass(String placeClass) {
        this.placeClass = placeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName){
        this.displayName = displayName;
    }

}
