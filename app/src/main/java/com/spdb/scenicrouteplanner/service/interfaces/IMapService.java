package com.spdb.scenicrouteplanner.service.interfaces;

import org.osmdroid.util.BoundingBox;

public interface IMapService
{
    BoundingBox getStartMapExtent(long startNodeId, long endNodeId);
    void putAllEdgesOnMap();
    void clear();
}
