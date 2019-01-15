package com.spdb.scenicrouteplanner.database.structures;

public class PathResult
{
    // ==============================
    // Private fields
    // ==============================
    private String algorithm;
    private Long arcRowid;
    private long nodeFrom;
    private long nodeTo;
    private double cost;
    private String geometry;
    private String name;

    // ==============================
    // Private fields
    // ==============================
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Long getArcRowid() {
        return arcRowid;
    }

    public void setArcRowid(Long arcRowid) {
        this.arcRowid = arcRowid;
    }

    public long getNodeFrom() {
        return nodeFrom;
    }

    public void setNodeFrom(Long nodeFrom) {
        this.nodeFrom = nodeFrom;
    }

    public long getNodeTo() {
        return nodeTo;
    }

    public void setNodeTo(Long nodeTo) {
        this.nodeTo = nodeTo;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
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

    // ==============================
    // Constructors
    // ==============================
    public PathResult() {}

    public PathResult(String algorithm, Long arcRowid, Long nodeFrom, Long nodeTo, double cost,
                      String geometry, String name)
    {
        this.algorithm = algorithm;
        this.arcRowid = arcRowid;
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
        this.cost = cost;
        this.geometry = geometry;
        this.name = name;
    }
}
