package com.spdb.scenicrouteplanner.service.interfaces;

import com.spdb.scenicrouteplanner.model.Edge;

import java.util.List;

public interface IMapService
{
    void addEdge(Edge edge);
    void addEdges(List<Edge> edges);
    void removeAllEdges();
    void putAllEdgesOnMap();
}
