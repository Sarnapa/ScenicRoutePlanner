package com.spdb.scenicrouteplanner.database.structures;

public class ScenicRoutesPathRow
{
    // ==============================
    // Private fields
    // ==============================
    private Long arcRowid;
    private long nodeFrom;
    private long nodeTo;
    private double cost;
    private double length;
    private String geometry;
    private String name;
    private boolean isScenicRoute;

    // ==============================
    // Getters and Setters
    // ==============================

    public Long getArcRowid() {
        return arcRowid;
    }

    public void setArcRowid(Long arcRowid) {
        this.arcRowid = arcRowid;
    }

    public long getNodeFrom() {
        return nodeFrom;
    }

    public void setNodeFrom(long nodeFrom) {
        this.nodeFrom = nodeFrom;
    }

    public long getNodeTo() {
        return nodeTo;
    }

    public void setNodeTo(long nodeTo) {
        this.nodeTo = nodeTo;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isScenicRoute() {
        return isScenicRoute;
    }

    public void setScenicRoute(boolean scenicRoute) {
        isScenicRoute = scenicRoute;
    }

    // ==============================
    // Constructors
    // ==============================
    public ScenicRoutesPathRow() {}

    public ScenicRoutesPathRow(Long arcRowid, long nodeFrom, long nodeTo, double cost, double length,
                               String geometry, String name, boolean isScenicRoute)
    {
        this.arcRowid = arcRowid;
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
        this.cost = cost;
        this.length = length;
        this.geometry = geometry;
        this.name = name;
        this.isScenicRoute = isScenicRoute;
    }
}
