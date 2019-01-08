package com.spdb.scenicrouteplanner.utils;

import android.util.Log;

import com.spdb.scenicrouteplanner.database.RoutesDbProvider;
import com.spdb.scenicrouteplanner.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AStar {

    private RoutesDbProvider dbProvider;

    public AStar(RoutesDbProvider dbProvider) {
        this.dbProvider = dbProvider;
    }

    public List<Edge> aStar(Node start, Node dest) {
        //int size = model.getNodes().size();
        List<Node> openSet = new ArrayList<>();
        List<Node> closedSet = new ArrayList<>();
        Map<Node, List<Edge>> cameFrom = new HashMap<>();
        //Current best path to node distance
        Map<Node, Double> gScore = new HashMap<>();
        //Estimated distance from start to destination through node
        Map<Node, Double> fScore = new HashMap<>();

        openSet.add(start);
        gScore.put(start, 0.0);
        fScore.put(start, estimatedDistToDest(start, dest));
        cameFrom.put(start, null);

        while (!openSet.isEmpty()) {
            Node current = getMinFScoreNode(openSet, fScore);
            if (current == dest) {
                return reconstructPath(cameFrom, current);
            }

            openSet.remove(current);
            closedSet.add(current);

            for (Edge e : current.getOutgoingEdges()) {
                Double tmpDist = 0.0;
                List<Edge> tmpRoute = new ArrayList<>();
                Edge tmpE = e;
                while (tmpE.getEndNode().getOutgoingEdges().size() == 1 && (tmpE.getEndNode() != dest)) {
                    tmpRoute.add(tmpE);
                    tmpDist += tmpE.getLength();
                    tmpE = tmpE.getEndNode().getOutgoingEdges().get(0);
                }
                tmpDist += tmpE.getLength();
                tmpRoute.add(tmpE);
                Node neighbour = tmpE.getEndNode();
                /*
                if(neighbour.getOutgoingEdges().size() == 0){    //End of route

                }else { // Intersection ( >= 2 edges)

                }*/
                if (closedSet.contains(neighbour))
                    continue;

                Double tentativeGScore = gScore.get(current) + tmpDist;
                if (!openSet.contains(neighbour)) {
                    openSet.add(neighbour);
                } else if (tentativeGScore >= gScore.get(neighbour)) {
                    continue;
                }
                //Better path
                cameFrom.put(neighbour, tmpRoute);
                gScore.put(neighbour, tentativeGScore);
                fScore.put(neighbour, (tentativeGScore + estimatedDistToDest(neighbour, dest)));

            }

        }

        //TODO:find nearest if not destination?
        Log.d("ASTAR", "OPEN SET EMPTY, DESTINATION NOT FOUND");
        return null;
    }


    private Node getMinFScoreNode(List<Node> openSet, Map<Node, Double> fScore) {
        if (openSet.isEmpty())
            return null;

        Node resNode = openSet.get(0);
        Double minVal = fScore.get(resNode);
        for (Node n : openSet) {
            Double currVal = fScore.get(n);
            if (currVal < minVal) {
                minVal = currVal;
                resNode = n;
            }
        }
        return resNode;
    }

    //Heuristic
    private double estimatedDistToDest(Node current, Node dest) {
        return dbProvider.getDistance(current, dest);
    }

    private List<Edge> reconstructPath(Map<Node, List<Edge>> cameFrom, Node current) {
        List<Edge> finalPath = new ArrayList<>();
        List<Edge> tmp = cameFrom.get(current);
        while ((tmp) != null) {
            current = tmp.get(0).getStartNode();
            Collections.reverse(tmp);
            finalPath.addAll(tmp);
            tmp = cameFrom.get(current);
        }
        Collections.reverse(finalPath);
        for (Edge e : finalPath) {
            e.setTourRoute(true);
            //Log.d("ASTAR", "EDGE:"+e.getId()+" " +e.getStartNode().getId() +" "+e.getEndNode().getId());
        }
        return finalPath;
    }


}
