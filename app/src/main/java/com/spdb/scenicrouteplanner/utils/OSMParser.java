package com.spdb.scenicrouteplanner.utils;

import android.util.Log;

import com.spdb.scenicrouteplanner.database.RoutesDbProvider;
import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.lib.OSM.OSMClassLib;
import com.spdb.scenicrouteplanner.model.Edge;
import com.spdb.scenicrouteplanner.model.Model;
import com.spdb.scenicrouteplanner.model.Node;
import com.spdb.scenicrouteplanner.model.Way;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Route;

public class OSMParser {

    private static final String KEY_OSM = "osm";
    private static final String KEY_NODE = "node";
    private static final String KEY_WAY = "way";
    private static final String KEY_REL = "relation";
    private static final String KEY_TAG = "tag";
    private static final String KEY_ND = "nd";

    private final RoutesDbProvider dbProvider;

    private final int NODES_BUFFER_SIZE = 1000;
    private final int EDGES_BUFFER_SIZE = 1000;
    private final int WAYS_BUFFER_SIZE = 1000;

    private List<Node> nodesBuffer = new ArrayList<>(NODES_BUFFER_SIZE);
    private List<Edge> edgesBuffer = new ArrayList<>(EDGES_BUFFER_SIZE);
    private List<Way> waysBuffer = new ArrayList<>(WAYS_BUFFER_SIZE);


    public OSMParser(RoutesDbProvider dbProvider) {
        this.dbProvider = dbProvider;
    }

    public void parseOSMFile(String filePath) throws Exception {
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = new BufferedInputStream(new FileInputStream(filePath));
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            processParsing(parser);
        } catch (XmlPullParserException e) {
            //TODO:Unexpected token (position:TEXT You requested to...
            e.printStackTrace();
            throw new Exception("You requested too many nodes (limit is 50000). Either request a smaller area, or use planet.osm");
        } catch (IOException e) {
            //TODO: FileNotFound
            e.printStackTrace();
        }
    }

    private void processParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
        //Model model = new Model();
        int eventType = parser.nextTag();
        String elemName = parser.getName();
        if (!KEY_OSM.equals(elemName)) {
            Log.e("OSMParser", "Invalid file format.");
            throw new IOException("Invalid file format.");
        } else {
            eventType = parser.next();
        }

        int nodesNumber = 0;
        int edgesNumber = 0;
        int waysNumber = 0;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    elemName = parser.getName();
                    switch (elemName) {
                        case KEY_NODE: {
                            long id = Long.parseLong(parser.getAttributeValue(null, "id"));
                            double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
                            double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
                            //model.addNode(new Node(id, new GeoCoords(lat, lon)));

                            nodesNumber++;
                            if (nodesNumber >= NODES_BUFFER_SIZE) {
                                dbProvider.addNodes(nodesBuffer);
                                nodesBuffer.clear();
                                nodesNumber = 1;
                            }
                            nodesBuffer.add(new Node(id, new GeoCoords(lat, lon)));

                            eventType = parser.next();
                            break;
                        }

                        case KEY_WAY: {
                            long wayId = Long.parseLong(parser.getAttributeValue(null, "id"));
                            //Log.d("PARSER_TEST", "INSIDE WAY:"+ wayId);
                            String highway = "";
                            int maxSpeed = 0;
                            ArrayList<Edge> tmpEdges = new ArrayList<>();
                            long nodeId1 = 0;
                            //Node node1 = null;
                            long nodeId2;
                            //Node node2 = null;

                            eventType = parser.nextTag(); //go inside <way>
                            while (KEY_ND.equals(parser.getName())) {
                                //Log.d("PARSER_TEST", "INSIDE ND");
                                if (nodeId1 == 0) {
                                    nodeId1 = Long.parseLong(parser.getAttributeValue(null, "ref"));
                                    //Log.d("PARSER_TEST", "ND REF:" + nodeId1);
                                    //node1 = model.getNodeById(nodeId1);
                                } else {
                                    nodeId2 = Long.parseLong(parser.getAttributeValue(null, "ref"));
                                    //Log.d("PARSER_TEST", "ND REF:" + nodeId2);
                                    //node2 = model.getNodeById(nodeId2);
                                    tmpEdges.add(new Edge(Edge.getNextId(), nodeId1, nodeId2));
                                    //Log.d("PARSER_TEST", "EDGE INSERTED:" + edgeId);
                                    nodeId1 = nodeId2;
                                    //node1 = node2;
                                }
                                eventType = parser.nextTag(); //temporary
                                eventType = parser.nextTag();
                                //Log.d("PARSER_TEST", "NEXT TAG:" + parser.getName());
                            }
                            while (KEY_TAG.equals(parser.getName())) {
                                //Log.d("PARSER_TEST", "INSIDE TAG");
                                if ("highway".equals(parser.getAttributeValue(null, "k"))) {
                                    highway = parser.getAttributeValue(null, "v");
                                }
                                if ("maxspeed".equals(parser.getAttributeValue(null, "k"))) {
                                    maxSpeed = Integer.parseInt(parser.getAttributeValue(null, "v"));
                                }
                                eventType = parser.nextTag();
                            }
                            if (!highway.isEmpty()) {
                                try {
                                    OSMClassLib.WayType wayType = OSMClassLib.WayType.valueOf(highway.toUpperCase());
                                    Way newWay = new Way(wayId, wayType, wayType.isCouldBeScenicRoute(), maxSpeed);

                                    waysNumber++;
                                    if (waysNumber >= WAYS_BUFFER_SIZE) {
                                        dbProvider.addWays(waysBuffer);
                                        waysBuffer.clear();
                                        waysNumber = 1;
                                    }
                                    waysBuffer.add(newWay);

                                    //model.addWay(newWay);

                                    for (Edge e : tmpEdges) {
                                        e.setWayId(wayId);
                                        //model.addEdge(e);
                                        //e.getStartNode().getEdges().add(e);
                                        //e.getEndNode().getEdges().add(e);
                                    }
                                    edgesNumber += tmpEdges.size();
                                    if (edgesNumber >= EDGES_BUFFER_SIZE) {
                                        dbProvider.addWays(waysBuffer);
                                        waysBuffer.clear();
                                        waysNumber = 0;
                                        dbProvider.addEdges(edgesBuffer);
                                        edgesBuffer.clear();
                                        edgesNumber = tmpEdges.size();
                                    }
                                    edgesBuffer.addAll(tmpEdges);
                                    //Log.d("PARSER_TEST", "WAY INSERTED:" + wayId);
                                } catch (IllegalArgumentException e) {
                                    //TODO:omit
                                }

                            }
                            break;
                        }

                        default:
                            eventType = parser.next();
                            break;
                    }
                    break;
                default:
                    eventType = parser.next();
                    break;
            }
        }

        if (nodesNumber > 0)
            dbProvider.addNodes(nodesBuffer);
        if (edgesNumber > 0)
            dbProvider.addEdges(edgesBuffer);
        if (waysNumber > 0)
            dbProvider.addWays(waysBuffer);
    }
}
