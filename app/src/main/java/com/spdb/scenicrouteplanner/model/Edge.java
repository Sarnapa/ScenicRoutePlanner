package com.spdb.scenicrouteplanner.model;

public class Edge
{
    private int id;
    private Way wayInfo;
    private Node startNode;
    private Node endNode;
    private double length;

    public Edge(int id, Way wayInfo, Node startNode, Node endNode, double length)
    {
        this.id = id;
        this.wayInfo = wayInfo;
        this.startNode = startNode;
        this.endNode = endNode;
        this.length = length;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Way getWayInfo()
    {
        return wayInfo;
    }

    public void setWayInfo(Way wayInfo)
    {
        this.wayInfo = wayInfo;
    }

    public Node getStartNode()
    {
        return startNode;
    }

    public void setStartNode(Node startNode)
    {
        this.startNode = startNode;
    }

    public Node getEndNode()
    {
        return endNode;
    }

    public void setEndNode(Node endNode)
    {
        this.endNode = endNode;
    }

    public double getLength()
    {
        return length;
    }

    public void setLength(double length)
    {
        this.length = length;
    }
}
