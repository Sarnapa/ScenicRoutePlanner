package com.spdb.scenicrouteplanner.model;

import android.util.Log;

import com.spdb.scenicrouteplanner.lib.GeoCoords;

import java.util.ArrayList;
import java.util.HashMap;

public class Model {
    private HashMap<Long, Node> nodes = new HashMap<>();
    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<Way> ways = new ArrayList<>();

    public HashMap<Long, Node> getNodes() {
        return nodes;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public ArrayList<Way> getWays() {
        return ways;
    }

    public void printAll() {
        printNodes();
        printEdges();
        printWays();
    }

    public void printNodes() {
        for (HashMap.Entry<Long, Node> entry : nodes.entrySet()) {
            Node tmp = entry.getValue();
            GeoCoords tmpGeoCoords = tmp.getGeoCoords();
            //Log.d("MODEL_TEST", tmp.getId() + " lat:" + tmpGeoCoords.getLatitude() + " lon:" + tmpGeoCoords.getLongitude());
            System.out.println("MODEL_TEST: NODE:" + tmp.getId() + " lat:" + tmpGeoCoords.getLatitude() + " lon:" + tmpGeoCoords.getLongitude());
        }
    }

    public void printEdges() {
        for (Edge tmp : edges) {
            Long nodeId1 = tmp.getStartNode().getId();
            Long nodeId2 = tmp.getEndNode().getId();
            //Log.d("MODEL_TEST", tmp.getId() + " way:" + tmp.getWayId() + " start:" + nodeId1 + " end:" + nodeId2);
            System.out.println("MODEL_TEST: EDGE:" + tmp.getId() + " way:" + tmp.getWayInfo().getId() + " start:" + nodeId1 + " end:" + nodeId2);
        }
    }

    public void printWays() {
        for (Way tmp : ways) {
            //Log.d("MODEL_TEST", "MODEL_TEST: WAY:" + tmp.getId() + " isScenicRoute:" + tmp.isScenicRoute()+ " wayType:" + tmp.getWayType());
            System.out.println("MODEL_TEST: WAY:" + tmp.getId() + " isScenicRoute:" + tmp.isScenicRoute() + " wayType:" + tmp.getWayType());
        }
    }

}
