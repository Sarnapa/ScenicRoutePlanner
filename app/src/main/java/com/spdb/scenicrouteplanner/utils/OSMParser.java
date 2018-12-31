package com.spdb.scenicrouteplanner.utils;

import android.util.Log;

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
import java.util.HashMap;
import java.util.Iterator;

public class OSMParser {

    private static final String KEY_OSM = "osm";
    private static final String KEY_NODE = "node";
    private static final String KEY_WAY = "way";
    private static final String KEY_REL = "relation";
    private static final String KEY_TAG = "tag";
    private static final String KEY_ND = "nd";

    public Model parseOSMFile(String filePath) {
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = new BufferedInputStream(new FileInputStream(filePath));
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            return processParsing(parser);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            //TODO: FileNotFound
            e.printStackTrace();
        }
        return null;
    }

    private Model processParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
        Model model = new Model();
        int eventType = parser.getEventType();
        String elemName;
        /*String elemName = parser.getName();
        if(!KEY_OSM.equals(elemName)){
            Log.e("OSMParser","Invalid file format.");
            throw new IOException("Invalid file format.");
        } else {
            eventType = parser.next();
        }*/
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    elemName = parser.getName();
                    switch (elemName) {
                        case KEY_NODE: {
                            long id = Long.parseLong(parser.getAttributeValue(null, "id"));
                            double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
                            double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
                            model.getNodes().put(id, new Node(id, new GeoCoords(lat, lon)));
                            eventType = parser.next();
                            break;
                        }

                        case KEY_WAY: {
                            long wayId = Long.parseLong(parser.getAttributeValue(null, "id"));
                            //Log.d("PARSER_TEST", "INSIDE WAY:"+ wayId);
                            String highway = "";
                            int maxSpeed = 0;

                            long nodeId1;
                            Node node1 = null;
                            long nodeId2;
                            Node node2 = null;
                            long edgeId;
                            Edge newEdge = null;

                            eventType = parser.nextTag(); //go inside <way>
                            while (KEY_ND.equals(parser.getName())) {
                                //Log.d("PARSER_TEST", "INSIDE ND");
                                if (node1 == null) {
                                    nodeId1 = Long.parseLong(parser.getAttributeValue(null, "ref"));
                                    //Log.d("PARSER_TEST", "ND REF:" + nodeId1);
                                    node1 = model.getNodes().get(nodeId1);
                                } else {
                                    nodeId2 = Long.parseLong(parser.getAttributeValue(null, "ref"));
                                    //Log.d("PARSER_TEST", "ND REF:" + nodeId2);
                                    edgeId = Edge.getNextId();
                                    node2 = model.getNodes().get(nodeId2);
                                    newEdge = new Edge(edgeId, wayId, node1, node2);
                                    Log.d("PARSER_TEST", "EDGE INSERTED:" + edgeId);
                                    model.getEdges().put(edgeId, newEdge);
                                    node1.getEdges().add(newEdge);
                                    node2.getEdges().add(newEdge);
                                    nodeId1 = nodeId2;
                                    node1 = node2;
                                }
                                eventType = parser.nextTag(); //tu byl problem, tymczasowe rozwiazanie
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
                                    model.getWays().put(wayId, new Way(wayId, wayType, false, maxSpeed));
                                    Log.d("PARSER_TEST", "WAY INSERTED:" + wayId);
                                } catch (IllegalArgumentException e) {
                                    //TODO:jakos ladniej tego enuma?
                                    e.printStackTrace();
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
        updateEdgeInfo(model);
        return model;
    }

    private void updateEdgeInfo(Model inputModel) {
        inputModel.getEdges().entrySet();
        long wayId;
        Way way;
        Edge currentEdge;
        long currentEdgeId;

        Iterator it = inputModel.getEdges().entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) it.next();
            currentEdge = (Edge) entry.getValue();
            currentEdgeId = (Long) entry.getKey();
            wayId = currentEdge.getWayId();
            way = inputModel.getWays().get(wayId);
            if (way != null) {
                currentEdge.setWayInfo(way);
            } else {
                it.remove();
                Log.d("PARSER_TEST", "EDGE REMOVED:" + currentEdgeId);
                //System.out.println("EDGE REMOVED:"+ currentEdgeId);
            }
        }
    }
}
