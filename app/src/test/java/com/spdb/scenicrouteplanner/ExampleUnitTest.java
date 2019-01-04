package com.spdb.scenicrouteplanner;

import com.spdb.scenicrouteplanner.model.Edge;
import com.spdb.scenicrouteplanner.model.Model;
import com.spdb.scenicrouteplanner.service.OSMService;
import com.spdb.scenicrouteplanner.utils.AStar;
import com.spdb.scenicrouteplanner.utils.OSMParser;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void parser_test() {
        try {
            OSMParser parser = new OSMParser();
            Model model = parser.parseOSMFile("./sampledata/osm");
            model.printAll();

            AStar alg = new AStar();
            List<Edge> path = alg.aStar(model.getNodeById(new Long("3854331236")), model.getNodeById(new Long("316886476")));
            Double length = 0.0;
            for(Edge e: path){
                System.out.println("ASTAR:" + e.getId() + "way:" + e.getWayInfo().getId() + " start:" + e.getStartNode().getId() + " end:" + e.getEndNode().getId());
                length+= e.getLength();
            }

            System.out.println("ASTAR: length:" + length);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}