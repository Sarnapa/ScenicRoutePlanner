package com.spdb.scenicrouteplanner.utils;

import android.util.Log;

import com.spdb.scenicrouteplanner.database.modelDatabase.RoutesDbProvider;
import com.spdb.scenicrouteplanner.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class AStar
{
    // ==============================
    // Private fields
    // ==============================
    private RoutesDbProvider dbProvider;

    // ==============================
    // Constructors
    // ==============================
    public AStar(RoutesDbProvider dbProvider) {
        this.dbProvider = dbProvider;
    }

    // ==============================
    // Public methods
    // ==============================
    public List<Edge> aStar(Node start, Node dest) {
        //Current best path to node distance
        Map<Node, Double> gScore = new HashMap<>();
        //Estimated distance from start to destination through node
        final Map<Node, Double> fScore = new HashMap<>();

        Comparator<Node> comparator = new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if(fScore.get(o1) > fScore.get(o2))
                    return 1;
                if(fScore.get(o1) < fScore.get(o2))
                    return -1;
                return 0;
            }
        };

        List<Node> openSet = new ArrayList<>();
        //PriorityQueue<Node> openSet = new PriorityQueue<>(100, comparator);
        List<Node> closedSet = new ArrayList<>();
        Map<Node, List<Edge>> cameFrom = new HashMap<>();

        openSet.add(start);
        gScore.put(start, 0.0);
        fScore.put(start, estimatedDistToDest(start, dest));
        cameFrom.put(start, null);

        Node current;

        while (!openSet.isEmpty()) {
            current = getMinFScoreNode(openSet, fScore);
            //current = openSet.poll();
            if (current == dest) {
                return reconstructPath(cameFrom, current);
            }

            openSet.remove(current);
            closedSet.add(current);

            for (Edge e : current.getEdges()) {
                Double tmpDist = 0.0;
                List<Edge> tmpRoute = new ArrayList<>();
                Edge tmpE = e;
                Node endNode = getNode(tmpE.getEndNodeId());
                while (endNode.getEdges().size() == 1 && (endNode != dest))
                {
                    tmpRoute.add(tmpE);
                    tmpDist += tmpE.getLength();
                    tmpE = endNode.getEdges().get(0);
                    endNode = getNode(tmpE.getEndNodeId());
                }
                tmpDist += tmpE.getLength();
                tmpRoute.add(tmpE);
                Node neighbour = endNode;
                /*
                if(neighbour.getOutgoingEdges().size() == 0){    //End of route

                }else { // Intersection ( >= 2 edges)

                }*/
                if (closedSet.contains(neighbour))
                    continue;

                Double tentativeGScore = gScore.get(current) + tmpDist;
                if (!openSet.contains(neighbour)) {
                    //fScore.put(neighbour, Double.MAX_VALUE);
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

    // ==============================
    // Private methods
    // ==============================
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

    private List<Edge> reconstructPath(Map<Node, List<Edge>> cameFrom, Node current)
    {
        List<Edge> finalPath = new ArrayList<>();
        List<Edge> tmp = cameFrom.get(current);
        while ((tmp) != null) {
            current = getNode(tmp.get(0).getStartNodeId());
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

    private Node getNode(long id)
    {
        Node endNode = dbProvider.getNodeById(id);
        if (endNode == null)
            endNode = new Node();

        return endNode;
    }
}
