package com.spdb.scenicrouteplanner.service.interfaces;

import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.model.Edge;

import org.osmdroid.util.BoundingBox;

import java.util.List;

public interface IMapService
{
    void setStartMapExtent(GeoCoords v1, GeoCoords v2);

    BoundingBox getStartMapExtent();

    void addEdge(Edge edge);

    void addEdges(List<Edge> edges);

    void removeAllEdges();

    void putAllEdgesOnMap();
}
