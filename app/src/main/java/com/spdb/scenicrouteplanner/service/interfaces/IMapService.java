package com.spdb.scenicrouteplanner.service.interfaces;

import com.spdb.scenicrouteplanner.model.Edge;

import java.util.ArrayList;

public interface IMapService {
    void addEdge(Edge edge);

    void addEdges(ArrayList<Edge> edges);

    void removeAllEdges();

    void putAllEdgesOnMap();
}
