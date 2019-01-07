package com.spdb.scenicrouteplanner.utils;

import android.util.Log;

import com.spdb.scenicrouteplanner.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AStar {

    public List<Edge> aStar(Model model, Node start, Node dest) {
        int size = model.getNodes().size();
        List<Node> openSet = new ArrayList<>(size);
        List<Node> closedSet = new ArrayList<>(size);
        //Map<Node, List<Long>> cameFrom = new HashMap<>(size);
        Map<Node, Node> cameFrom = new HashMap<>(size);
        //Current best path to node distance
        Map<Node, Double> gScore = new HashMap<>(size);
        //Estimated distance from start to destination through node
        Map<Node, Double> fScore = new HashMap<>(size);

        openSet.add(start);
        gScore.put(start, 0.0);
        fScore.put(start, estimatedDistToDest(start, dest));
        cameFrom.put(start, null);

        while (!openSet.isEmpty()) {
            Node current = getMinFScoreNode(openSet, fScore);
            if (current == dest) {
                return reconstructPath(model, cameFrom, current);
            }

            openSet.remove(current);
            closedSet.add(current);

            for (Edge e : current.getOutgoingEdges()) {
                Double tmpDist = 0.0;
                //List<Long> tmpRoute = new ArrayList<>();
                Edge tmpE = e;
                while (tmpE.getEndNode().getOutgoingEdges().size() == 1 && (tmpE.getEndNode() != dest)) {
                    //tmpRoute.add(tmpE.getId());
                    tmpDist += tmpE.getLength();
                    tmpE = tmpE.getEndNode().getOutgoingEdges().get(0);
                }
                tmpDist += tmpE.getLength();
                //tmpRoute.add(tmpE.getId());
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
                cameFrom.put(neighbour, current);
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
        return Double.MAX_VALUE;
    }

    private List<Edge> reconstructPath(Model model, Map<Node, Node> cameFrom, Node current) {
        Log.d("ASTAR", "BEGIN RECONSTRUCT PATH");
        List<Edge> finalPath = new ArrayList<>();
        Node tmp;
        while ((tmp = cameFrom.get(current)) != null) {
            finalPath.add(new Edge(Edge.getNextId(), tmp, current, true));
        }
        Collections.reverse(finalPath);
        return finalPath;
    }
    /*
    private List<Edge> reconstructPath(Model model, Map<Node, List<Long>> cameFrom, Node current) {
        List<Edge> finalPath = new ArrayList<>();
        List<Long> tmp;
        while ((tmp = cameFrom.get(current)) != null) {
            current = model.getEdgeById(tmp.get(0)).getStartNode();
            Collections.reverse(tmp);
            for(long id:tmp){
                finalPath.add(model.getEdgeById(id));
            }
        }
        Collections.reverse(finalPath);
        return finalPath;
    }
    */
}
