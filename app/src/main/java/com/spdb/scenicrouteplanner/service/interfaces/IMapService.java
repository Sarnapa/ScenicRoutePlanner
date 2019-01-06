package com.spdb.scenicrouteplanner.service.interfaces;

import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.model.Edge;
import com.spdb.scenicrouteplanner.model.Node;

import org.osmdroid.util.BoundingBox;

import java.util.List;

public interface IMapService
{
    void setStartMapExtent(GeoCoords v1, GeoCoords v2);

    void setStartNode(Node n);

    void setEndNode(Node n);

    BoundingBox getStartMapExtent();

    void addEdge(Edge edge);

    void addEdges(List<Edge> edges);

    void removeStartEndNode();

    void removeAllEdges();

    void putStartEndNodeOnMap();

    void putAllEdgesOnMap();
}
