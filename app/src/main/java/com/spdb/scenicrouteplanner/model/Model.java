package com.spdb.scenicrouteplanner.model;

import android.util.Log;

import com.spdb.scenicrouteplanner.lib.GeoCoords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Model {
    private HashMap<Long, Node> nodes = new HashMap<>();
    private HashMap<Long, Edge> edges = new HashMap<>();
    private HashMap<Long, Way> ways = new HashMap<>();

    public HashMap<Long, Node> getNodes() {
        return nodes;
    }

    public HashMap<Long, Edge> getEdges() {
        return edges;
    }

    public List<Edge> getEdgesList() {
        return new ArrayList<>(edges.values());
    }

    public HashMap<Long, Way> getWays() {
        return ways;
    }

    public List<Way> getWaysList() {
        return new ArrayList<>(ways.values());
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
        for (HashMap.Entry<Long, Edge> entry : edges.entrySet()) {
            Edge tmp = entry.getValue();
            Long nodeId1 = tmp.getStartNode().getId();
            Long nodeId2 = tmp.getEndNode().getId();
            //Log.d("MODEL_TEST", tmp.getId() + " way:" + tmp.getWayId() + " start:" + nodeId1 + " end:" + nodeId2);
            System.out.println("MODEL_TEST: EDGE:" + tmp.getId() + " way:" + tmp.getWayInfo().getId() + " start:" + nodeId1 + " end:" + nodeId2);
        }
    }

    public void printWays() {
        for (HashMap.Entry<Long, Way> entry : ways.entrySet()) {
            Way tmp = entry.getValue();
            //Log.d("MODEL_TEST", "MODEL_TEST: WAY:" + tmp.getId() + " isScenicRoute:" + tmp.isScenicRoute()+ " wayType:" + tmp.getWayType());
            System.out.println("MODEL_TEST: WAY:" + tmp.getId() + " isScenicRoute:" + tmp.isScenicRoute() + " wayType:" + tmp.getWayType());
        }
    }

}
