package com.spdb.scenicrouteplanner.model;

import com.spdb.scenicrouteplanner.lib.GeoCoords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Model {
    private HashMap<Long, Node> nodes = new HashMap<>();
    private List<Edge> edges = new ArrayList<>();
    private List<Way> ways = new ArrayList<>();

    public HashMap<Long, Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdgesList() {
        return edges;
    }

    public List<Way> getWaysList() {
        return ways;
    }

    public void setNodes(HashMap<Long, Node> nodes) { this.nodes = nodes; }

    public void setEdges(List<Edge> edges) { this.edges = edges; }

    public void setWays(List<Way> ways) { this.ways = ways; }

    public void addNode(Node n) {
        nodes.put(n.getId(), n);
    }

    public Node getNodeById(Long id) {
        return nodes.get(id);
    }

    public Edge getEdgeById(Long id) {
        for(Edge e: edges){
            if(e.getId() == id){
                return e;
            }
        }
        return null;
    }

    public void addEdge(Edge e) {
        edges.add(e);
    }

    public void addWay(Way w) {
        ways.add(w);
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
