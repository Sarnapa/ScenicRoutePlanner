package com.spdb.scenicrouteplanner.model;

public class Edge {
    private static long nextID = 0;

    private long id;
    private Way wayInfo;
    private Node startNode;
    private Node endNode;
    private double length;
    private boolean isTourRoute;

    public Edge() {
    }

    public Edge(long id, Way wayInfo, Node startNode, Node endNode, double length) {
        this.id = id;
        this.wayInfo = wayInfo;
        this.startNode = startNode;
        this.endNode = endNode;
        this.length = length;
    }

    public Edge(long id, Way wayInfo, Node startNode, Node endNode, double length, boolean isTourRoute) {
        this.id = id;
        this.wayInfo = wayInfo;
        this.startNode = startNode;
        this.endNode = endNode;
        this.length = length;
        this.isTourRoute = isTourRoute;
    }

    public Edge(long id, Node startNode, Node endNode) {
        this.id = id;
        this.startNode = startNode;
        this.endNode = endNode;
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

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
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
