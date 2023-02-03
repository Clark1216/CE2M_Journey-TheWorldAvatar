package com.cmclinnovations.aermod;

import org.apache.jena.base.Sys;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import uk.ac.cam.cares.jps.base.query.AccessAgentCaller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class BuildingsTest {

    String simulationDirectory = "C:\\Users\\KNAG01\\Dropbox (Cambridge CARES)\\IRP3 CAPRICORN shared folder\\KNAGARAJAN\\Projects\\Dispersion\\Data\\20\\";
    //    Two equivalent polygons which define a rectangular region within Jurong Island. The values in wkt are in EPSG:4326/WGS84 coordinates
    //    while those in wkt2 are in EPSG:3857 coordinates.
    // For EPSG:4326/Wgs84 format, longitude is specified before latitude.
    String wkt = "POLYGON ((103.672485 1.259121, 103.716345 1.259121, 103.716345 1.281174, 103.672485 1.281174, 103.672485 1.259121))" ;
    String wkt2 = "POLYGON ((11540768.2395 140175.9917, 11540768.2395 142631.5238, 11545650.7123 142631.5238, 11545650.7123 140175.9917, 11540768.2395 140175.9917))" ;

    String wkt3 = "POLYGON ((103.651 1.217, 103.742 1.217, 103.742 1.308, 103.651 1.308, 103.651 1.217))" ;
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(),4326);
    Polygon scope = (Polygon) new WKTReader(geometryFactory).read(wkt3);
    int nx = 100;
    int ny = 100;

    private QueryClient queryClient;




    Buildings bp = new Buildings() ;

    public BuildingsTest() throws ParseException {  }


    /* Test the initialization code within the constructor of the BuildingsPlantItems class. */
    @Test
    public void testInit() throws org.opengis.util.FactoryException, FactoryException, TransformException, ParseException {

        int centreZoneNumber = (int) Math.ceil((scope.getCentroid().getCoordinate().getX() + 180)/6);
        System.out.println(centreZoneNumber);
        int srid;
        if (scope.getCentroid().getCoordinate().getY() < 0) {
            srid = Integer.valueOf("327" + centreZoneNumber);
        } else {
            srid = Integer.valueOf("326" + centreZoneNumber);
        }

        int numStacks = 569;
        int numBuildings = 4979;
        bp.init(simulationDirectory, scope, nx, ny, srid);
        Assertions.assertTrue(bp.locindex > -1);
//        bp.getStacksBuildings();
        bp.getProperties();

        Assertions.assertEquals(bp.StackEmissions.size(),numStacks);
        Assertions.assertEquals(bp.BPIPPRMStackInput.size(),1+numStacks);
        Assertions.assertEquals(bp.BuildingVertices.size(),numBuildings);


        int res = bp.createBPIPPRMInput();
        Assertions.assertEquals(res,0);
        int res2 = bp.createAERMODSourceInput();
        Assertions.assertEquals(res2,0);
//        int rds = bp.runBPIPPRM();
//        Assertions.assertEquals(rds,0);
//        int res = bp.run();
//        Assertions.assertEquals(res,0);




//        try {
//            bp.init(simulationDirectory, scope, nx, ny, srid);
//            int res = bp.run();
//            Assertions.assertEquals(res,0);
//        } catch (Exception e) {
//            System.out.println(e);
//        }


    }



    @Test
    public void testCoordinates() {

        double tol = 0.01;
        double error_x,error_y;

        int centreZoneNumber = (int) Math.ceil((scope.getCentroid().getCoordinate().getX() + 180)/6);
        System.out.println(centreZoneNumber);
        int srid;
        if (scope.getCentroid().getCoordinate().getY() < 0) {
            srid = Integer.valueOf("327" + centreZoneNumber);
        } else {
            srid = Integer.valueOf("326" + centreZoneNumber);
        }

        String UTMCoordSys = "EPSG:" + srid;

//Test conversion from EPSG:4326 to UTM coordinates, which is EPSG:32648 in Singapore's case
        List<List<Double>> inputcoordinates = new ArrayList<>();
        List<Double> inputcoords = new ArrayList<>(Arrays.asList(103.69,1.27));
        inputcoordinates.add(inputcoords);
        List<List<Double>> outputCoordinates = Buildings.convertCoordinates(inputcoordinates,"EPSG:4326",UTMCoordSys);

        for (List<Double> outcoord:outputCoordinates) {
            System.out.println(outcoord.get(0));
            System.out.println(outcoord.get(1));
        }

        error_x = outputCoordinates.get(0).get(0) - 354252.595175792;
        error_y = outputCoordinates.get(0).get(1) - 140410.3230744134;

        Assertions.assertTrue(error_x < tol);
        Assertions.assertTrue(error_y < tol);

        inputcoordinates.clear();
        inputcoords.clear();
        outputCoordinates.clear();
// Test conversion from EPSG:24500 to EPSG:4326
        inputcoords = new ArrayList<>(Arrays.asList(17612.50301,28401.53841));
        inputcoordinates.add(inputcoords);
        outputCoordinates = Buildings.convertCoordinates(inputcoordinates,"EPSG:24500","EPSG:4326");

        for (List<Double> outcoord:outputCoordinates) {
            System.out.println(outcoord.get(0));
            System.out.println(outcoord.get(1));
        }

        error_x = outputCoordinates.get(0).get(0) - 103.7399612;
        error_y = outputCoordinates.get(0).get(1) - 1.2731394;

        Assertions.assertTrue(error_x < tol);
        Assertions.assertTrue(error_y < tol);

        inputcoordinates.clear();
        inputcoords.clear();
        outputCoordinates.clear();

//        Test conversion from EPSG:24500 to UTM coordinates
        inputcoords = new ArrayList<>(Arrays.asList(17612.50301,28401.53841));
        inputcoordinates.add(inputcoords);
        outputCoordinates = Buildings.convertCoordinates(inputcoordinates,"EPSG:24500",UTMCoordSys);

        for (List<Double> outcoord:outputCoordinates) {
            System.out.println(outcoord.get(0));
            System.out.println(outcoord.get(1));
        }

        error_x = outputCoordinates.get(0).get(0) - 359812.2475178047;
        error_y = outputCoordinates.get(0).get(1) - 140754.64070724646;

        Assertions.assertTrue(error_x < tol);
        Assertions.assertTrue(error_y < tol);

        inputcoordinates.clear();
        inputcoords.clear();
        outputCoordinates.clear();



    }

    @Test
    public void testQuery() {
        StringBuilder sb = new StringBuilder("PREFIX ocgml: <http://www.theworldavatar.com/ontology/ontocitygml/citieskg/OntoCityGML.owl#>\n");
        sb.append("SELECT ?polygonData WHERE {\n");
        sb.append("<http://www.theworldavatar.com:83/citieskg/namespace/jriEPSG24500/sparql/surfacegeometry/UUID_6c0444ae-b5ab-40d2-840e-82dda85bc2e6/> ocgml:GeometryType ?polygonData.\n");
//        sb.append("<http://www.theworldavatar.com:83/citieskg/namespace/jriEPSG24500/sparql/surfacegeometry/UUID_01d45708-9d63-4026-83a0-0d249f8d9859/> ocgml:GeometryType ?polygonData.\n");
        sb.append("} \n");
        JSONArray queryResult = AccessAgentCaller.queryStore("jriEPSG24500", sb.toString());

        String res = queryResult.getJSONObject(0).getString("polygonData");
        System.out.println(res);
        Assertions.assertFalse(res.contains("#"));

//        System.out.println(queryResult.getJSONObject(0).getString("polygonData"));
    }

    @Test
    public void testQuery2() {
        StringBuilder sb = new StringBuilder("PREFIX ocgml: <http://www.theworldavatar.com/ontology/ontocitygml/citieskg/OntoCityGML.owl#>\n");
        sb.append("SELECT ?polygonData ?objectIRI ?geometricIRI \n");
        sb.append("WHERE { { ?geometricIRI ocgml:GeometryType ?polygonData ; \n");
        sb.append("ocgml:cityObjectId ?objectIRI . } \n");
        sb.append("VALUES ?objectIRI {<http://www.theworldavatar.com:83/citieskg/namespace/jriEPSG24500/sparql/cityfurniture/UUID_4de83001-0c75-4155-b835-e21f1a46ac77/> } }\n");
        sb.append("ORDER BY ?polygonData");
        JSONArray queryResult = AccessAgentCaller.queryStore("jriEPSG24500", sb.toString());
        int nw = 0;
        for (int i = 0; i < queryResult.length(); i++) {
            String vertex = queryResult.getJSONObject(i).getString("polygonData");
            if (!vertex.contains("#")){
                nw++;
                continue;
            }
        }
        System.out.println(nw);

    }

    @Test
    public void testrun() throws IOException {
        int rds = Buildings.runBPIPPRM(simulationDirectory+"bpipprm\\");
        Assertions.assertEquals(rds,0);
    }
    @Test
    public void testStackBuildingQueryusingQueryClient() {

        int numStacks = 570;
        int numBuildings = 4979;

        JSONArray StackIRIQueryResult = QueryClient.StackQuery("jibusinessunits");
        Assertions.assertEquals(numStacks,StackIRIQueryResult.length());
        JSONArray BuildingIRIQueryResult = QueryClient.BuildingQuery("jibusinessunits");
        Assertions.assertEquals(numBuildings,BuildingIRIQueryResult.length());

    }

    @Test
    public void testgetProperties() {

    }



}
