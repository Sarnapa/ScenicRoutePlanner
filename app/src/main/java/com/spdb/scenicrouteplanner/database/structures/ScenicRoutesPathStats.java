package com.spdb.scenicrouteplanner.database.structures;

public class ScenicRoutesPathStats
{
    // ==============================
    // Private fields
    // ==============================
    private long startNodeId;
    private long endNodeId;
    private double length;
    private double scenicRoutesLength;
    private double cost;

    // ==============================
    // Getters and Setters
    // ==============================
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

    public double getLengthInKm() {
        return length / 1000;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getScenicRoutesLength() {
        return scenicRoutesLength;
    }

    public double getScenicRoutesLengthInKm() {
        return scenicRoutesLength / 1000;
    }

    public void setScenicRoutesLength(double scenicRoutesLength) {
        this.scenicRoutesLength = scenicRoutesLength;
    }

    public double getCost() {
        return cost;
    }

    public double getCostInMin() {return cost / 60;}

    public void setCost(double cost) {
        this.cost = cost;
    }

    // ==============================
    // Constructors
    // ==============================
    public ScenicRoutesPathStats() {}

    public ScenicRoutesPathStats(long startNodeId, long endNodeId, double length,
                                 double scenicRoutesLength, double cost)
    {
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.length = length;
        this.scenicRoutesLength = scenicRoutesLength;
        this.cost = cost;
    }

}
