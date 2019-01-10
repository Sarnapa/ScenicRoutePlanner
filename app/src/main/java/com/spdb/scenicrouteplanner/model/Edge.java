package com.spdb.scenicrouteplanner.model;

public class Edge {
    private static long nextID = 0;

    private long id;
    private Way wayInfo;
    private long wayId = -1;
    private long startNodeId = -1;
    private long endNodeId = -1;
    private double length;
    private boolean isTourRoute;

    public Edge() {
    }

    public Edge(long id, long startNodeId, long endNodeId) {
        this.id = id;
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
    }

    public Edge(long id, Way wayInfo, long startNodeId, long endNodeId, double length) {
        this.id = id;
        this.wayInfo = wayInfo;
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.length = length;
    }

    public Edge(long id, Way wayInfo, long startNodeId, long endNodeId, double length, boolean isTourRoute) {
        this.id = id;
        this.wayInfo = wayInfo;
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.length = length;
        this.isTourRoute = isTourRoute;
    }

    public Edge(long id, long startNodeId, long endNodeId, boolean isTourRoute) {
        this.id = id;
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.isTourRoute = isTourRoute;
    }

    public static long getNextId() {
        return nextID++;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public Way getWayInfo() {
        return wayInfo;
    }
    public void setWayInfo(Way wayInfo) {
        this.wayInfo = wayInfo;
    }

    public long getWayId() {
        return wayId;
    }
    public void setWayId(long wayId){
        this.wayId = wayId;
    }

    public long getStartNodeId() {
        return startNodeId;
    }
    public void setStartNodeId(long startNodeId) {
        this.startNodeId = startNodeId;
    }

    public long getEndNodeId() {
        return endNodeId;
    }
    public void setEndNodeId(long endNodeId) {
        this.endNodeId = endNodeId;
    }

    public double getLength() {
        return length;
    }
    public void setLength(double length) {
        this.length = length;
    }

    public boolean isTourRoute() {
        return isTourRoute;
    }
    public void setTourRoute(boolean tourRoute) {
        isTourRoute = tourRoute;
    }
}
