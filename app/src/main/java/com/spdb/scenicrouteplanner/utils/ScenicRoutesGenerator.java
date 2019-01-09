package com.spdb.scenicrouteplanner.utils;

import android.renderscript.RSIllegalArgumentException;

import com.spdb.scenicrouteplanner.model.Edge;
import com.spdb.scenicrouteplanner.model.Node;
import com.spdb.scenicrouteplanner.model.Way;

import java.util.List;
import java.util.Random;

public class ScenicRoutesGenerator
{
    // ==============================
    // Private fields
    // ==============================
    // Szansa na odcinek widokowy
    private double scenicRoutePercentChange;
    // Maksymalna odleglość (w poziomach) od wybranego odcinka widokowego
    private int neighbourLvlMax;

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
    public ScenicRoutesGenerator()
    {
        this.scenicRoutePercentChange = 0.01f;
        this.neighbourLvlMax = 2;
    }

    public ScenicRoutesGenerator(double scenicRoutePercentChange, int neighbourLvlMax)
    {
        setScenicRoutePercentChange(scenicRoutePercentChange);
        setNeighbourLvlMax(neighbourLvlMax);
    }

    // ==============================
    // Public methods
    // ==============================

    public void generate(List<Edge> edges) throws IllegalArgumentException
    {
        if (edges == null)
            throw new IllegalArgumentException("ScenicRoutesGenerator.generate - null edges argument");

        for (Edge e: edges)
        {
            Way way = e.getWayInfo();
            if (way.getWayType().isCouldBeScenicRoute())
            {
                if (new Random().nextDouble() <= scenicRoutePercentChange)
                {
                    way.setScenicRoute(true);
                    setNeighbours(e.getStartNode(), e,0);
                    setNeighbours(e.getEndNode(), e,0);
                }
            }
        }
    }

    private void setNeighbours(Node n, Edge parentEdge, int currentLvl)
    {
        if (currentLvl <= neighbourLvlMax) {
            for (Edge e : n.getEdges()) {
                if (e != parentEdge) {
                    Way way = e.getWayInfo();
                    if (!way.isScenicRoute() && way.getWayType().isCouldBeScenicRoute()) {
                        way.setScenicRoute(true);
                        if (e.getStartNode() == n)
                            setNeighbours(e.getEndNode(), e, ++currentLvl);
                        else
                            setNeighbours(e.getStartNode(), e, ++currentLvl);
                    }
                }
            }
        }
    }
}
