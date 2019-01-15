package com.spdb.scenicrouteplanner.utils;

import com.spdb.scenicrouteplanner.database.modelDatabase.RoutesDbProvider;
import com.spdb.scenicrouteplanner.model.Edge;
import com.spdb.scenicrouteplanner.model.Node;
import com.spdb.scenicrouteplanner.model.Way;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ScenicRoutesGenerator
{
    // ==============================
    // Private fields
    // ==============================
    private static final int MAX_WAYS = 1000;
    // Szansa na odcinek widokowy
    private double scenicRoutePercentChange;
    // Maksymalna odleglość (w poziomach) od wybranego odcinka widokowego
    private int neighbourLvlMax;
    // Waye, które zostały uznane za odcinki widokowe
    private HashMap<Long, Way> scenicWaysDict = new HashMap<>();

    private RoutesDbProvider dbProvider;

    // ==============================
    // Getters and setters
    // ==============================
    public void setScenicRoutePercentChange(double scenicRoutePercentChange)
    {
        scenicRoutePercentChange = scenicRoutePercentChange < 0.0f ? 0.0f : scenicRoutePercentChange;
        scenicRoutePercentChange = scenicRoutePercentChange > 1.0f ? 1.0f : scenicRoutePercentChange;
        this.scenicRoutePercentChange = scenicRoutePercentChange;
    }

    public void setNeighbourLvlMax(int neighbourLvlMax)
    {
        this.neighbourLvlMax = neighbourLvlMax;
    }

    // ==============================
    // Constructors
    // ==============================
    public ScenicRoutesGenerator(RoutesDbProvider dbProvider)
    {
        this.scenicRoutePercentChange = 0.01f;
        this.neighbourLvlMax = 2;
        this.dbProvider = dbProvider;
    }

    public ScenicRoutesGenerator(RoutesDbProvider dbProvider, double scenicRoutePercentChange, int neighbourLvlMax)
    {
        setScenicRoutePercentChange(scenicRoutePercentChange);
        setNeighbourLvlMax(neighbourLvlMax);
        this.dbProvider = dbProvider;
    }

    // ==============================
    // Public methods
    // ==============================
    public void generate()
    {
        long edgesCount = dbProvider.getEdgesCount();

        for (long i = 0; i < edgesCount; ++i)
        {
            if (new Random().nextDouble() <= scenicRoutePercentChange)
            {
                Edge e = getEdge(i);
                if (!scenicWaysDict.containsKey(e.getWayId()))
                {
                    Way w = e.getWayInfo();
                    if (w.getWayType().isScenicRoute())
                    {
                        setScenicWay(w);
                        setNeighbours(getNode(e.getStartNodeId()), e, 0);
                        setNeighbours(getNode(e.getEndNodeId()), e, 0);
                    }
                }
            }
        }

        if (scenicWaysDict.size() > 0)
        {
            dbProvider.setScenicRoute(new ArrayList(scenicWaysDict.values()));
        }
    }

    // ==============================
    // Private methods
    // ==============================
    private void setNeighbours(Node n, Edge parentEdge, int currentLvl)
    {
        if (currentLvl <= neighbourLvlMax) {
            for (Edge e : n.getEdges()) {
                if (e != parentEdge && !scenicWaysDict.containsKey(e.getWayId())) {
                    Way w = e.getWayInfo();
                    if (!w.isScenicRoute() && w.getWayType().isScenicRoute()) {
                        setScenicWay(w);

                        if (e.getStartNodeId() == n.getId())
                            setNeighbours(getNode(e.getEndNodeId()), e, ++currentLvl);
                        else
                            setNeighbours(getNode(e.getStartNodeId()), e, ++currentLvl);
                    }
                }
            }
        }
    }

    private Node getNode(long id)
    {
        Node node = dbProvider.getNodeById(id);
        if (node == null)
            node = new Node();

        return node;
    }

    private Edge getEdge(long id)
    {
        Edge edge = dbProvider.getEdgeById(id);

        if (edge == null)
            edge = new Edge();

        return edge;
    }

    private void setScenicWay(Way w)
    {
        w.setScenicRoute(true);
        scenicWaysDict.put(w.getId(), w);

        if(scenicWaysDict.size() >= MAX_WAYS)
        {
            dbProvider.setScenicRoute(new ArrayList(scenicWaysDict.values()));
        }
    }
}
