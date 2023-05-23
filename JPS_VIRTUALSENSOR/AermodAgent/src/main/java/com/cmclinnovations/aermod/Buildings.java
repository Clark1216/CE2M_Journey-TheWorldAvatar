package com.cmclinnovations.aermod;

import geotrellis.proj4.CRS;
import geotrellis.proj4.Transform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;

import org.locationtech.jts.io.WKTReader;
import scala.Tuple2;
import uk.ac.cam.cares.jps.base.query.RemoteRDBStoreClient;
import uk.ac.cam.cares.jps.base.util.CRSTransformer;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.io.parser.JSONParser;
import java.net.URI;
import java.net.URISyntaxException;

import com.cmclinnovations.aermod.objects.Building;
import org.apache.http.client.utils.URIBuilder;
import com.cmclinnovations.stack.clients.gdal.GDALClient;
import com.cmclinnovations.stack.clients.gdal.Ogr2OgrOptions;
import com.cmclinnovations.stack.clients.geoserver.GeoServerClient;
import com.cmclinnovations.stack.clients.geoserver.GeoServerVectorSettings;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Buildings {

    private static final Logger LOGGER = LogManager.getLogger(Buildings.class);

    // TODO: The next 4 variables need to be updated with additional entries for
    // locations other than Jurong Island.
    // Use the full endpoint for the local blazegraph and only the namespace for TWA
    // blazegraph.
    // If the emissions rates of one or more pollutant sources vary with time, the
    // OCGML IRI and emissions rates of each pollutant
    // source may be obtained from an input json file instead of a blazegraph
    // namespace.
    public static String[] StackQueryEndpoint = { "jibusinessunits", "aermodInput.json" };
    public static String[] GeospatialQueryEndpoint = { "jriEPSG24500", "pirmasensEPSG32633" };
    public static String[] DatabaseCRS = { "EPSG:24500", "EPSG:32633" };
    public static int[] propertiesMethod = { 1, 2 };

    // These values are taken from bboxfinder.com and are in EPSG:4326/WGS84 format.
    public static List<String> boundaryPolygons = new ArrayList<>(
            Arrays.asList(
                    "POLYGON ((103.650684 1.216988, 103.743038 1.216988, 103.743038 1.308804, 103.650684 1.308804, 103.650684 1.216988))",
                    "POLYGON((7.52 49.19, 7.52 49.25, 7.67 49.25, 7.67 49.19, 7.52 49.19))"));

    public int locindex = -1;
    public String StackQueryIRI;
    public String GeospatialQueryIRI;
    public int propertiesMethodIndex;

    // Coordinate reference systems used by database (DatabaseCoordSys) and
    // AERMOD(UTMCoordSys)

    public String DatabaseCoordSys, UTMCoordSys;

    /*
     * Each element of StackProperties contains the (x,y) UTM coordinates of the
     * center of the base polygon of the stack and the stack height.
     * Each element of StackPropertiesOriginal contains the (x,y) coordinates of the
     * center of the base polygon in the database coordinate system.
     */
    public List<String> StackProperties = new ArrayList<>();
    public List<Double> StackEmissions = new ArrayList<>();
    public List<Double> StackDiameter = new ArrayList<>();
    public List<String> StackPropertiesOriginal = new ArrayList<>();
    public List<List<Double>> StackEmissionsTimeSeries = new ArrayList<>();
    public boolean queryEmissionsData = false;
    public List<String> timeStamps = new ArrayList<>();
    public List<Double> receptorHeights = new ArrayList<>();
    public List<Double> sensorLongitude = new ArrayList<>();
    public List<Double> sensorLatitude = new ArrayList<>();
    public List<Double> sensorHeight = new ArrayList<>();
    public List<List<Double>> sensorProperties = new ArrayList<>();

    /*
     * Each element of BuildingProperties contains the (x,y) coordinates of the
     * center of the base polygon of the building and the building height.
     * Each element of BuildingVertices contains the coordinates of the vertices of
     * the base polygon.
     */
    public List<String> BuildingVertices = new ArrayList<>();
    public List<String> BuildingProperties = new ArrayList<>();

    // Variables used to run AERMOD and its preprocessors
    public List<List<String>> BPIPPRMBuildingInput = new ArrayList<>();
    public List<String> BPIPPRMStackInput = new ArrayList<>();

    public Path simulationDirectory;
    public Path bpipprmDirectory;
    public Path aermodDirectory;
    public Path aermetDirectory;
    public Path aermapDirectory;
    public Polygon scope;
    public int nx;
    public int ny;

    public boolean aermodInputCreated = false;
    private List<Building> buildings = new ArrayList<>();

    public void init(Path simulationDirectory, Polygon scope, int srid, int nx, int ny) throws ParseException {

        this.simulationDirectory = simulationDirectory;
        this.bpipprmDirectory = simulationDirectory.resolve("bpipprm");
        this.bpipprmDirectory.toFile().mkdir();
        this.aermodDirectory = simulationDirectory.resolve("aermod");
        this.aermetDirectory = simulationDirectory.resolve("aermet");
        this.aermapDirectory = simulationDirectory.resolve("aermap");
        this.aermapDirectory.toFile().mkdir();
        this.scope = scope;
        this.nx = nx;
        this.ny = ny;

        // Determine namespace to query based on input polygon

        // The boundary.covers(scope) test works correctly only if boundary and scope
        // have the same srid. Hence, srid of scope must be 4326.//

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Polygon boundary = null;
        for (int i = 0; i < boundaryPolygons.size(); i++) {
            String wkt = boundaryPolygons.get(i);
            boundary = (Polygon) new WKTReader(geometryFactory).read(wkt);
            if (boundary.covers(scope)) {
                locindex = i;
                break;
            }
        }

        if (locindex >= 0 && scope.getSRID() == 4326) {
            StackQueryIRI = StackQueryEndpoint[locindex];
            GeospatialQueryIRI = GeospatialQueryEndpoint[locindex];
            DatabaseCoordSys = DatabaseCRS[locindex];
            propertiesMethodIndex = propertiesMethod[locindex];
        } else if (locindex == -1) {
            LOGGER.info("Input polygon not found in any namespace." +
                    "AERMOD will be run for ships only without buildings and plant items.");
        } else if (scope.getSRID() != 4326) {
            LOGGER.info("Input scope does not have 4326 as its srid." +
                    "AERMOD will be run for ships only without buildings and plant items.");
        }
        UTMCoordSys = "EPSG:" + srid;

        if (StackQueryIRI.contains(".json"))
            queryEmissionsData = true;

        // First value hard-coded in timestamps. The date follows that specified in
        // aermet.inp and raob_soundings15747.FSL.
        timeStamps.add("2022-09-23 00:00:00");

    }

    public int run() {
        try {

            switch (propertiesMethodIndex) {
                case 1:
                    getProperties();
                    break;
                case 2:
                    getProperties2();
                    break;
                default:
                    LOGGER.error("Cannot call getProperties method");
                    return 1;
            }

            if (createPlantItemsGeoServerLayer() != 0) {
                LOGGER.error("Failed to create GeoServer Layer for plant items, terminating");
                return 1;
            }

            boolean includeElev = Boolean.parseBoolean(EnvConfig.INCLUDE_ELEVATION);

            if (includeElev) {

                // if (getElevationRasterData() != 0) {
                // LOGGER.error("Failed to create raster file for elevation data.");
                // return 1;
                // }

                if (copyCachedAERMAPOutput() == 0) {
                    LOGGER.info("Successfully copied user-supplied AERMAP output files.");
                } else {
                    // run AERMAP to generate elevation input for AERMOD
                    if (createAERMAPInputFile() != 0) {
                        LOGGER.error("Failed to create AERMAP input file, terminating");
                        return 1;
                    }

                    if (createAERMAPSourceInput() != 0) {
                        LOGGER.error("Failed to create AERMAP source input, terminating");
                        return 1;
                    }
                    if (createAERMAPReceptorInput(nx, ny) != 0) {
                        LOGGER.error("Failed to create AERMAP receptor input, terminating");
                        return 1;
                    }

                    if (runAERMAP() != 0) {
                        LOGGER.error("Failed to run AERMAP, terminating");
                        return 1;
                    }

                }

                // Virtual sensors part is not cached because one might want to vary their
                // locations. Also,
                // this part will eventually be moved to a separate agent.

                if (createAERMAPVirtualSensorInputFiles() != 0) {
                    LOGGER.error("Failed to create AERMAP input for virtual sensors, terminating");
                }

                if (runAERMAPforVirtualSensors() != 0) {
                    LOGGER.error("Failed to run AERMAP for virtual sensors, terminating");
                }

                if (processAERMAPOutput() != 0) {
                    LOGGER.error("Failed to process AERMAP output, terminating");
                    return 1;
                }
            } else {
                // This method should be called only if AERMAP was not run.
                if (updateElevationData() != 0) {
                    LOGGER.error("Failed to update elevation data, terminating");
                    return 1;
                }

                if (createAERMODReceptorInput(nx, ny) != 0) {
                    LOGGER.error("Failed to create AERMOD receptor input file, terminating");
                    return 1;
                }

                if (createAERMODVirtualReceptorInput() != 0) {
                    LOGGER.error("Failed to create AERMOD receptor input file, terminating");
                    return 1;
                }

            }

            if (createBPIPPRMInput() != 0) {
                LOGGER.error("Failed to create BPIPPRM input, terminating");
                return 1;
            }

            if (runBPIPPRM() != 0) {
                LOGGER.error("Failed to run BPIPPRM, terminating");
                return 1;
            }

            if (createAERMODBuildingsInput() != 0) {
                LOGGER.error("Failed to create AERMOD buildings input file, terminating");
                return 1;
            }
            if (createAERMODSourceInput() != 0) {
                LOGGER.error("Failed to create AERMOD sources input file, terminating");
                return 1;
            }

            if (addAERMODReceptorInput() != 0) {
                LOGGER.error("Failed to write additional receptor.dat files, terminating");
                return 1;
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return 1;
        }

        return 0;
    }

    // The String inputs must be of a format similar to "EPSG:4326".
    public static List<List<Double>> convertCoordinates(List<List<Double>> inputcoordinates, String inputCRS,
            String outputCRS) {

        List<List<Double>> outputcoordinates = new ArrayList<>();

        String inputSys = inputCRS.split(":")[1];
        int inputCode = Integer.valueOf(inputSys);
        String outputSys = outputCRS.split(":")[1];
        int outputCode = Integer.valueOf(outputSys);

        CRS sourceCRS = CRS.fromEpsgCode(inputCode);
        CRS targetCRS = CRS.fromEpsgCode(outputCode);

        var convert = Transform.apply(sourceCRS, targetCRS);

        for (int i = 0; i < inputcoordinates.size(); i++) {
            double xi = inputcoordinates.get(i).get(0);
            double yi = inputcoordinates.get(i).get(1);
            Tuple2<Object, Object> res;
            res = convert.apply(xi, yi);
            double xt = (double) res._1();
            double yt = (double) res._2();
            outputcoordinates.add(Arrays.asList(xt, yt));
        }

        return outputcoordinates;
    }

    /**
     * Create a polygon with the given points
     * 
     * @param points points of the polygon as a string
     * @return a polygon
     */
    private Geometry toPolygon(String points) {
        int ind = 0;
        GeometryFactory gF = new GeometryFactory();

        String[] arr = points.split("#");

        Coordinate[] coordinates = new Coordinate[(arr.length) / 3];

        for (int i = 0; i < arr.length; i += 3) {
            coordinates[ind] = new Coordinate(Double.valueOf(arr[i]), Double.valueOf(arr[i + 1]),
                    Double.valueOf(arr[i + 2]));
            ind++;
        }

        return gF.createPolygon(coordinates);
    }

    /**
     * Converts an array of coordinates into a string
     * 
     * @param coordinates array of footprint coordinates
     * @return coordinates as a string
     */
    private String coordinatesToString(Coordinate[] coordinates) {
        String output = "";

        for (int i = 0; i < coordinates.length; i++) {
            output = output + "#" + Double.toString(coordinates[i].getX()) + "#"
                    + Double.toString(coordinates[i].getY()) + "#" + Double.toString(coordinates[i].getZ());
        }

        return output.substring(1, output.length());
    }

    /**
     * Inflates a polygon
     * 
     * @param geom     polygon geometry
     * @param distance buffer distance
     * @return inflated polygon
     */
    private Geometry inflatePolygon(Geometry geom, Double distance) {
        ArrayList<Double> zCoordinate = getPolygonZ(geom);
        BufferParameters bufferParameters = new BufferParameters();
        bufferParameters.setEndCapStyle(BufferParameters.CAP_ROUND);
        bufferParameters.setJoinStyle(BufferParameters.JOIN_MITRE);
        Geometry buffered = BufferOp.bufferOp(geom, distance, bufferParameters);
        buffered.setUserData(geom.getUserData());
        setPolygonZ(buffered, zCoordinate);
        return buffered;
    }

    /**
     * Deflates a polygon
     * 
     * @param geom     polygon geometry
     * @param distance buffer distance
     * @return deflated polygon
     */
    private Geometry deflatePolygon(Geometry geom, Double distance) {
        ArrayList<Double> zCoordinate = getPolygonZ(geom);
        BufferParameters bufferParameters = new BufferParameters();
        bufferParameters.setEndCapStyle(BufferParameters.CAP_ROUND);
        bufferParameters.setJoinStyle(BufferParameters.JOIN_MITRE);
        Geometry buffered = BufferOp.bufferOp(geom, distance * -1, bufferParameters);
        buffered.setUserData(geom.getUserData());
        setPolygonZ(buffered, zCoordinate);
        return buffered;
    }

    /**
     * Extract the z coordinates of the polygon vertices
     * 
     * @param geom polygon geometry
     * @return the z coordinates of the polygon vertices
     */
    private static ArrayList<Double> getPolygonZ(Geometry geom) {
        Coordinate[] coordinates = geom.getCoordinates();
        ArrayList<Double> output = new ArrayList<>();

        for (int i = 0; i < coordinates.length; i++) {
            output.add(coordinates[i].getZ());
        }

        return output;
    }

    /**
     * Sets a polygon's z coordinates to the values from zInput
     * 
     * @param geom   polygon geometry
     * @param zInput ArrayList of values representing z coordinates
     */
    private void setPolygonZ(Geometry geom, ArrayList<Double> zInput) {
        Double newZ = Double.NaN;

        for (int i = 0; i < zInput.size(); i++) {
            if (!zInput.get(i).isNaN()) {
                newZ = zInput.get(i);
                break;
            }
        }

        if (newZ.isNaN()) {
            newZ = 10.0;
        }

        if (geom.getNumPoints() < zInput.size()) {
            while (geom.getNumPoints() != zInput.size()) {
                zInput.remove(zInput.size() - 1);
            }
        } else {
            while (geom.getNumPoints() != zInput.size()) {
                zInput.add(1, newZ);
            }
        }

        Collections.replaceAll(zInput, Double.NaN, newZ);
        geom.apply(new CoordinateSequenceFilter() {
            @Override
            public void filter(CoordinateSequence cSeq, int i) {
                cSeq.getCoordinate(i).setZ(zInput.get(i));
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public boolean isGeometryChanged() {
                return false;
            }
        });
    }

    /**
     * Returns the ground geometry's exterior ring
     * 
     * @param geometry    ground geometry
     * @param polygonType polygon datatype, such as "<...\POLYGON-3-45-15>"
     * @return ground geometry with no holes
     */
    private String ignoreHole(String geometry, String polygonType) {
        int num;
        int ind;
        int count = 1;

        String[] split = polygonType.split("-");

        if (split.length < 4) {
            return geometry;
        }

        num = Integer.parseInt(split[2]);

        ind = geometry.indexOf("#");

        while (count != num) {
            ind = geometry.indexOf("#", ind + 1);
            count++;
        }
        return geometry.substring(0, ind);
    }

    /**
     * Extracts the footprint of the building from its ground surface geometries
     * 
     * @param results JSONArray of the query results for ground surface geometries
     * @return footprint as a string
     */
    private LinearRing extractFootprint(JSONArray results) {
        double distance = 0.00001;
        double increment = 0.00001;

        Polygon footprintPolygon;
        LinearRing footprintRing;
        Coordinate[] footprintCoordinates;
        ArrayList<Geometry> geometries = new ArrayList<>();
        GeometryFactory geoFac = new GeometryFactory();
        GeometryCollection geoCol;
        Geometry merged;
        Geometry temp;
        String geoType;

        if (results.length() == 1) {
            footprintPolygon = (Polygon) toPolygon(ignoreHole(results.getJSONObject(0).get("polygonData").toString(),
                    results.getJSONObject(0).get("datatype").toString()));
        }

        else {
            for (int i = 0; i < results.length(); i++) {
                temp = toPolygon(ignoreHole(results.getJSONObject(i).get("polygonData").toString(),
                        results.getJSONObject(i).get("datatype").toString()));
                if (!temp.isValid()) {
                    temp = GeometryFixer.fix(temp);
                }
                geometries.add(temp);
            }

            geoCol = (GeometryCollection) geoFac.buildGeometry(geometries);

            merged = geoCol.union();

            geoType = merged.getGeometryType();

            while (geoType != "Polygon" || deflatePolygon(merged, distance).getGeometryType() != "Polygon") {
                distance += increment;

                for (int i = 0; i < geometries.size(); i++) {
                    temp = inflatePolygon(geometries.get(i), distance);
                    if (!temp.isValid()) {
                        temp = GeometryFixer.fix(temp);
                    }
                    geometries.set(i, temp);
                }

                geoCol = (GeometryCollection) geoFac.buildGeometry(geometries);
                merged = geoCol.union();
                geoType = merged.getGeometryType();
            }

            footprintPolygon = (Polygon) deflatePolygon(merged, distance);

            if (!footprintPolygon.isValid()) {
                footprintPolygon = (Polygon) GeometryFixer.fix(footprintPolygon);
            }
        }

        footprintRing = footprintPolygon.getExteriorRing();

        return footprintRing;
    }

    public JSONArray getBuildingsNearPollutantSources() throws org.apache.jena.sparql.lang.sparql_11.ParseException {

        // Determine bounding box for geospatial query by finding the minimum and
        // maximum x and y coordinates of pollutant sources
        // in the original coordinate system.
        double xMin = 0.0;
        double xMax = 0.0;
        double yMin = 0.0;
        double yMax = 0.0;
        double zAve = 0.0;
        int numberStacks = StackPropertiesOriginal.size();

        for (int i = 0; i < numberStacks; i++) {
            String[] averageCoordinate = StackPropertiesOriginal.get(i).split("#");
            zAve += Double.parseDouble(averageCoordinate[2]);
            if (i == 0) {
                xMin = Double.parseDouble(averageCoordinate[0]);
                xMax = Double.parseDouble(averageCoordinate[0]);
                yMin = Double.parseDouble(averageCoordinate[1]);
                yMax = Double.parseDouble(averageCoordinate[1]);
            } else {
                xMin = Math.min(xMin, Double.parseDouble(averageCoordinate[0]));
                xMax = Math.max(xMax, Double.parseDouble(averageCoordinate[0]));
                yMin = Math.min(yMin, Double.parseDouble(averageCoordinate[1]));
                yMax = Math.max(yMax, Double.parseDouble(averageCoordinate[1]));
            }
        }

        zAve /= numberStacks;

        if (numberStacks == 1) {
            double expandRange = 10.0;
            xMin -= expandRange;
            xMax += expandRange;
            yMin -= expandRange;
            yMax += expandRange;
        }

        String polygonPoints = String.valueOf(xMin) + "#" + String.valueOf(yMin) + "#" + String.valueOf(zAve) +
                "#" + String.valueOf(xMax) + "#" + String.valueOf(yMin) + "#" + String.valueOf(zAve) + "#"
                + String.valueOf(xMax) +
                "#" + String.valueOf(yMax) + String.valueOf("#") + String.valueOf(zAve) + "#" + String.valueOf(xMin) +
                "#" + String.valueOf(yMax) + "#" + String.valueOf(zAve) + "#" + String.valueOf(xMin) + "#"
                + String.valueOf(yMin) + "#" +
                String.valueOf(zAve);

        double buffer = 200.0;

        Polygon envelopePolygon = (Polygon) toPolygon(polygonPoints);

        Geometry surroundingRing = ((Polygon) inflatePolygon(envelopePolygon, buffer)).getExteriorRing();

        Coordinate[] surroundingCoordinates = surroundingRing.getCoordinates();

        String boundingBox = coordinatesToString(surroundingCoordinates);

        String[] points = boundingBox.split("#");

        String lowerPoints = points[0] + "#" + points[1] + "#" + 0 + "#";

        String lowerBounds = lowerPoints + lowerPoints + lowerPoints + lowerPoints + lowerPoints;
        lowerBounds = lowerBounds.substring(0, lowerBounds.length() - 1);

        String upperPoints = points[6] + "#" + points[7] + "#" + String.valueOf(Double.parseDouble(points[8]) + 100)
                + "#";

        String upperBounds = upperPoints + upperPoints + upperPoints + upperPoints + upperPoints;
        upperBounds = upperBounds.substring(0, upperBounds.length() - 1);

        JSONArray BuildingsQueryResult = null;
        try {
            BuildingsQueryResult = BuildingsQueryClient.getBuildingsWithinBounds(GeospatialQueryIRI, lowerBounds,
                    upperBounds);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }

        return BuildingsQueryResult;

    }

    public void getProperties2() throws org.apache.jena.sparql.lang.sparql_11.ParseException {

        // Populate a list of chemical plant items (StackIRIString) for which geometric
        // properties will be queried
        // from OCGML. Also determine the pollutant emissions rate in tons/yr for each
        // plant item.

        List<String> StackIRIString = new ArrayList<>();
        List<List<Double>> emissionsOfEveryStack = new ArrayList<>();

        if (queryEmissionsData) {
            // Read emissions data from aermodInput.json
            String jsonString = null;

            try (InputStream is = getClass().getClassLoader().getResourceAsStream("aermodInput.json")) {
                jsonString = IOUtils.toString(is, StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONObject obj = new JSONObject(jsonString);
            JSONArray sourceInfo = obj.getJSONArray("sourceData");

            for (int i = 0; i < sourceInfo.length(); i++) {
                JSONObject sourceObj = sourceInfo.getJSONObject(i);
                StackIRIString.add(sourceObj.getString("buildingIRI"));
                JSONArray emissionValues = sourceObj.getJSONArray("Emissions");
                List<Double> tmp = IntStream.range(0, emissionValues.length())
                        .mapToObj(j -> emissionValues.getDouble(j)).collect(Collectors.toList());
                emissionsOfEveryStack.add(tmp);
                if (i == 0) {
                    JSONArray tmps = sourceObj.getJSONArray("Timestamps");
                    timeStamps = IntStream.range(0, tmps.length())
                            .mapToObj(j -> tmps.getString(j)).collect(Collectors.toList());
                }
            }

            JSONArray receptorFlagHeights = obj.getJSONArray("receptorHeights");
            receptorHeights = IntStream.range(0, receptorFlagHeights.length())
                    .mapToObj(j -> receptorFlagHeights.getDouble(j)).collect(Collectors.toList());

            JSONObject sensorLocations = obj.getJSONObject("sensorLocations");
            JSONArray sensorX = sensorLocations.getJSONArray("Longitude");
            JSONArray sensorY = sensorLocations.getJSONArray("Latitude");
            JSONArray sensorH = sensorLocations.getJSONArray("Height");

            for (int i = 0; i < sensorX.length(); i++) {
                sensorLongitude.add(sensorX.getDouble(i));
                sensorLatitude.add(sensorY.getDouble(i));
                sensorHeight.add(sensorH.getDouble(i));
                sensorProperties.add(Arrays.asList(sensorX.getDouble(i), sensorY.getDouble(i), sensorH.getDouble(i)));
            }

        } else {
            // Query constant emissions values from blazegraph
            JSONArray StackIRIQueryResult = BuildingsQueryClient.StackQuery(StackQueryIRI);
            StackIRIString = IntStream
                    .range(0, StackIRIQueryResult.length())
                    .mapToObj(i -> StackIRIQueryResult.getJSONObject(i).getString("IRI"))
                    .collect(Collectors.toList());

            List<Double> tmp = IntStream
                    .range(0, StackIRIQueryResult.length())
                    .mapToObj(i -> StackIRIQueryResult.getJSONObject(i).getDouble("emission"))
                    .collect(Collectors.toList());
            for (int i = 0; i < tmp.size(); i++) {
                List<Double> tmp2 = Arrays.asList(tmp.get(i));
                emissionsOfEveryStack.add(tmp2);
            }
        }

        // StackIRIString and emissionsOfEveryStack populated

        StackIRIString = StackIRIString.stream().map(i -> i.replace("cityobject", "building"))
                .collect(Collectors.toList());
        JSONArray StackGeometricQueryResult = BuildingsQueryClient.BuildingGeometricQuery2(GeospatialQueryIRI,
                StackIRIString);

        String objectIRIPrev = StackGeometricQueryResult.getJSONObject(0).getString("objectIRI");
        int numberStacks = 0;
        // Determine indices at which data for a new object starts
        List<Integer> resultIndices = new ArrayList<>();
        resultIndices.add(0);

        for (int i = 0; i < StackGeometricQueryResult.length(); i++) {
            String objectIRI = StackGeometricQueryResult.getJSONObject(i).getString("objectIRI");
            if (!objectIRI.equals(objectIRIPrev)) {
                resultIndices.add(i);
                objectIRIPrev = objectIRI;
            }
        }

        for (int i = 0; i < resultIndices.size(); i++) {
            // Determine range of indices for each object
            int firstIndex = resultIndices.get(i);
            int lastIndex;
            if (i == resultIndices.size() - 1) {
                lastIndex = StackGeometricQueryResult.length();
            } else {
                lastIndex = resultIndices.get(i + 1);
            }

            // Process results for each object.

            JSONArray groundSurfaces = new JSONArray();

            double StackEastUTM = 0.0;
            double StackNorthUTM = 0.0;
            boolean includeObject = true;
            String objectIRI = "";
            double radius = 0.0;
            double aveRoofz = 0.0;
            double aveGroundz = 0.0;
            int numberRoofSurfaces = 0;
            int numberGroundSurfaces = 0;
            double height = 0.0;
            for (int k = firstIndex; k < lastIndex; k++) {
                JSONObject result = StackGeometricQueryResult.getJSONObject(k);
                int objectClassId = result.getInt("objectClassId");
                objectIRI = result.getString("objectIRI");
                String polygonVertex = result.getString("polygonData");
                if (!polygonVertex.contains("#")) {
                    continue;
                }
                String[] vertexCoordinates = polygonVertex.split("#");
                List<Double> xcoord = new ArrayList<>();
                List<Double> ycoord = new ArrayList<>();
                List<Double> zcoord = new ArrayList<>();
                for (int j = 0; j < vertexCoordinates.length; j += 3) {
                    xcoord.add(Double.parseDouble(vertexCoordinates[j]));
                    ycoord.add(Double.parseDouble(vertexCoordinates[j + 1]));
                    zcoord.add(Double.parseDouble(vertexCoordinates[j + 2]));
                }
                double polyMinZ = Collections.min(zcoord);
                double polyMaxZ = Collections.max(zcoord);
                double polyAveZ = zcoord.stream().mapToDouble(a -> a).average().orElse(0.0);

                if (objectClassId == 33) {
                    aveRoofz += polyAveZ;
                    numberRoofSurfaces++;
                } else if (objectClassId == 35) {
                    aveGroundz += polyAveZ;
                    groundSurfaces.put(result);
                    numberGroundSurfaces++;
                }
            }

            aveRoofz /= numberRoofSurfaces;
            aveGroundz /= numberGroundSurfaces;
            height = aveRoofz - aveGroundz;
            LinearRing footPrint = extractFootprint(groundSurfaces);
            String exteriorGroundVertices = coordinatesToString(footPrint.getCoordinates());
            buildings.add(new Building(footPrint, height, DatabaseCoordSys));
            String[] edgeCoordinates = exteriorGroundVertices.split("#");
            int numPoints = edgeCoordinates.length / 3;
            double aveX = 0.0;
            double aveY = 0.0;
            double aveZ = 0.0;
            for (int k = 0; k < edgeCoordinates.length; k += 3) {
                aveX += Double.parseDouble(edgeCoordinates[k]);
                aveY += Double.parseDouble(edgeCoordinates[k + 1]);
                aveZ += Double.parseDouble(edgeCoordinates[k + 2]);
            }
            aveX /= numPoints;
            aveY /= numPoints;
            aveZ /= numPoints;

            List<List<Double>> inputcoordinates = new ArrayList<>();
            List<Double> inputcoords = new ArrayList<>(Arrays.asList(aveX, aveY));
            inputcoordinates.add(inputcoords);
            List<List<Double>> outputCoordinates = convertCoordinates(inputcoordinates, DatabaseCoordSys, "EPSG:4326");

            Geometry point = new GeometryFactory().createPoint(new Coordinate(outputCoordinates.get(0).get(0),
                    outputCoordinates.get(0).get(1)));

            if (!scope.covers(point)) {
                System.out.println(aveX + ", " + aveY);
                includeObject = false;
                break;
            }

            for (int k = 0; k < edgeCoordinates.length; k += 3) {
                double dx = Double.parseDouble(edgeCoordinates[k]) - aveX;
                double dy = Double.parseDouble(edgeCoordinates[k + 1]) - aveY;
                double dist = Math.sqrt(dx * dx + dy * dy);
                radius += dist;
            }
            radius /= numPoints;

            outputCoordinates.clear();
            outputCoordinates = convertCoordinates(inputcoordinates, DatabaseCoordSys, UTMCoordSys);

            StackEastUTM = outputCoordinates.get(0).get(0);
            StackNorthUTM = outputCoordinates.get(0).get(1);

            if (includeObject) {
                numberStacks++;
                String InputLine = "\'Stk" + numberStacks + "\'" + " " + "BASE_ELEVATION " +
                        height + " " + StackEastUTM + " " + StackNorthUTM + " \n";
                BPIPPRMStackInput.add(InputLine);
                StringBuffer averageCoordinate = new StringBuffer();
                averageCoordinate.append(StackEastUTM).append("#").append(StackNorthUTM).append("#").append(height);
                StackProperties.add(averageCoordinate.toString());
                StringBuffer averageCoordinateOriginal = new StringBuffer();
                averageCoordinateOriginal.append(aveX).append("#").append(aveY).append("#").append(aveZ);
                StackPropertiesOriginal.add(averageCoordinateOriginal.toString());

                // Search for IRI in StackIRIString
                int ind = StackIRIString.indexOf(objectIRI);
                List<Double> emissions = emissionsOfEveryStack.get(ind);
                StackEmissionsTimeSeries.add(emissions);
                StackDiameter.add(2 * radius);

            }

        }

        JSONArray BuildingIRIQueryResult = getBuildingsNearPollutantSources();
        List<String> BuildingIRIString = new ArrayList<>();

        // Remove IRIs of pollutant sources
        for (int i = 0; i < BuildingIRIQueryResult.length(); i++) {
            String buildingIRI = BuildingIRIQueryResult.getJSONObject(i).getString("cityObject");
            if (!StackIRIString.contains(buildingIRI.replace("cityobject", "building"))) {
                BuildingIRIString.add(buildingIRI.replace("cityobject", "building"));
            }
        }

        JSONArray BuildingGeometricQueryResult = BuildingsQueryClient.BuildingGeometricQuery2(GeospatialQueryIRI,
                BuildingIRIString);
        // The center of a building's base needs to be within the criticalDistance of
        // the center of a pollutant source's base
        // in order for the building to be included in the BPIPPRM input.
        double criticalDistance = 200.0;

        objectIRIPrev = BuildingGeometricQueryResult.getJSONObject(0).getString("objectIRI");
        int numberBuildings = 0;

        // Determine indices at which data for a new object starts
        resultIndices.clear();
        resultIndices.add(0);

        for (int i = 0; i < BuildingGeometricQueryResult.length(); i++) {
            String objectIRI = BuildingGeometricQueryResult.getJSONObject(i).getString("objectIRI");
            if (!objectIRI.equals(objectIRIPrev)) {
                resultIndices.add(i);
                objectIRIPrev = objectIRI;
            }
        }

        for (int i = 0; i < resultIndices.size(); i++) {
            // Determine range of indices for each object
            int firstIndex = resultIndices.get(i);
            int lastIndex;
            if (i == resultIndices.size() - 1) {
                lastIndex = BuildingGeometricQueryResult.length();
            } else {
                lastIndex = resultIndices.get(i + 1);
            }

            // Process results for each object;
            double minZ = 0.0;
            double maxZ = 0.0;
            double minAveZ = 0.0;
            double BuildingEastUTM = 0.0;
            double BuildingNorthUTM = 0.0;
            boolean includeObject = true;
            double aveRoofz = 0.0;
            double aveGroundz = 0.0;
            int numberRoofSurfaces = 0;
            int numberGroundSurfaces = 0;
            double height = 0.0;
            JSONArray groundSurfaces = new JSONArray();

            for (int k = firstIndex; k < lastIndex; k++) {
                JSONObject result = BuildingGeometricQueryResult.getJSONObject(k);
                int objectClassId = result.getInt("objectClassId");
                String polygonVertex = result.getString("polygonData");
                if (!polygonVertex.contains("#")) {
                    continue;
                }
                String[] vertexCoordinates = polygonVertex.split("#");
                List<Double> xcoord = new ArrayList<>();
                List<Double> ycoord = new ArrayList<>();
                List<Double> zcoord = new ArrayList<>();
                for (int j = 0; j < vertexCoordinates.length; j += 3) {
                    xcoord.add(Double.parseDouble(vertexCoordinates[j]));
                    ycoord.add(Double.parseDouble(vertexCoordinates[j + 1]));
                    zcoord.add(Double.parseDouble(vertexCoordinates[j + 2]));
                }
                double polyMinZ = Collections.min(zcoord);
                double polyMaxZ = Collections.max(zcoord);
                double polyAveZ = zcoord.stream().mapToDouble(x -> x).average().orElse(0.0);

                if (objectClassId == 33) {
                    aveRoofz += polyAveZ;
                    numberRoofSurfaces++;
                } else if (objectClassId == 35) {
                    aveGroundz += polyAveZ;
                    groundSurfaces.put(result);
                    numberGroundSurfaces++;
                }

            }

            aveRoofz /= numberRoofSurfaces;
            aveGroundz /= numberGroundSurfaces;
            height = aveRoofz - aveGroundz;
            LinearRing footPrint = extractFootprint(groundSurfaces);
            buildings.add(new Building(footPrint, height, DatabaseCoordSys));
            String exteriorGroundVertices = coordinatesToString(footPrint.getCoordinates());
            String[] edgeCoordinates = exteriorGroundVertices.split("#");
            int numPoints = edgeCoordinates.length / 3;
            double aveX = 0.0;
            double aveY = 0.0;
            double aveZ = 0.0;
            for (int k = 0; k < edgeCoordinates.length; k += 3) {
                aveX += Double.parseDouble(edgeCoordinates[k]);
                aveY += Double.parseDouble(edgeCoordinates[k + 1]);
                aveZ += Double.parseDouble(edgeCoordinates[k + 2]);
            }
            aveX /= numPoints;
            aveY /= numPoints;
            aveZ /= numPoints;

            for (int k = 0; k < StackPropertiesOriginal.size(); k++) {
                String[] averageCoord = StackPropertiesOriginal.get(k).split("#");
                double dx = aveX - Double.parseDouble(averageCoord[0]);
                double dy = aveY - Double.parseDouble(averageCoord[1]);
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > criticalDistance) {
                    includeObject = false;
                    break;
                }
            }

            List<List<Double>> inputcoordinates = new ArrayList<>();
            List<Double> inputcoords = new ArrayList<>(Arrays.asList(aveX, aveY));
            inputcoordinates.add(inputcoords);
            List<List<Double>> outputCoordinates = convertCoordinates(inputcoordinates, DatabaseCoordSys, "EPSG:4326");

            Geometry point = new GeometryFactory().createPoint(new Coordinate(outputCoordinates.get(0).get(0),
                    outputCoordinates.get(0).get(1)));

            if (!scope.covers(point)) {
                System.out.println(aveX + ", " + aveY);
                includeObject = false;
                break;
            }

            outputCoordinates.clear();
            outputCoordinates = convertCoordinates(inputcoordinates, DatabaseCoordSys, UTMCoordSys);

            BuildingEastUTM = outputCoordinates.get(0).get(0);
            BuildingNorthUTM = outputCoordinates.get(0).get(1);

            if (includeObject) {
                numberBuildings++;
                StringBuffer averageCoordinate = new StringBuffer();
                averageCoordinate.append(BuildingEastUTM).append("#").append(BuildingNorthUTM).append("#")
                        .append(height);
                BuildingProperties.add(averageCoordinate.toString());

                List<String> buildInfo = new ArrayList<>();

                String InputLine = "\'Build" + numberBuildings + "\' " + "1 " + "BASE_ELEVATION" + " \n";
                buildInfo.add(InputLine);

                String[] BaseVertices = edgeCoordinates;
                int numCorners = BaseVertices.length / 3;
                InputLine = numCorners + " " + height + " \n";
                buildInfo.add(InputLine);

                inputcoordinates.clear();
                inputcoords.clear();
                outputCoordinates.clear();

                for (int j = 0; j < BaseVertices.length; j += 3) {
                    inputcoords = new ArrayList<>(Arrays.asList(Double.parseDouble(BaseVertices[j]),
                            Double.parseDouble(BaseVertices[j + 1])));
                    inputcoordinates.add(inputcoords);
                }

                outputCoordinates = convertCoordinates(inputcoordinates, DatabaseCoordSys, UTMCoordSys);
                for (int j = 0; j < outputCoordinates.size(); j++) {
                    Double VertexEastUTM = outputCoordinates.get(j).get(0);
                    Double VertexNorthUTM = outputCoordinates.get(j).get(1);
                    InputLine = VertexEastUTM + " " + VertexNorthUTM + " \n";
                    buildInfo.add(InputLine);
                }

                BPIPPRMBuildingInput.add(buildInfo);

            }

        }

    }

    /*
     * Get geometrical and geospatial properties of stacks and buildings.
     * This method queries all the surfaces of each stack and building. It assumes
     * that each structure has a single
     * base surface which can be identified as the one which has a constant
     * z-coordinate that is less than or equal to
     * the z-coordinates of vertices on all other surfaces.
     */
    public void getProperties() throws org.apache.jena.sparql.lang.sparql_11.ParseException {

        // Populate a list of chemical plant items (StackIRIString) for which geometric
        // properties will be queried
        // from OCGML. Also determine the pollutant emissions rate in tons/yr for each
        // plant item.

        // Query Individual CO2 Emissions of plant items
        JSONArray StackIRIQueryResult = BuildingsQueryClient.StackQuery(StackQueryIRI);
        List<String> StackIRIString = IntStream
                .range(0, StackIRIQueryResult.length())
                .mapToObj(i -> StackIRIQueryResult.getJSONObject(i).getString("IRI"))
                .collect(Collectors.toList());

        List<List<Double>> emissionsOfEveryStack = new ArrayList<>();
        List<Double> tmp = IntStream
                .range(0, StackIRIQueryResult.length())
                .mapToObj(i -> StackIRIQueryResult.getJSONObject(i).getDouble("emission"))
                .collect(Collectors.toList());
        for (int i = 0; i < tmp.size(); i++) {
            List<Double> tmp2 = Arrays.asList(tmp.get(i));
            emissionsOfEveryStack.add(tmp2);
        }

        // StackIRIString and emissionsOfEveryStack populated

        JSONArray StackGeometricQueryResult = BuildingsQueryClient.StackGeometricQuery(GeospatialQueryIRI,
                StackIRIString);

        String objectIRIPrev = StackGeometricQueryResult.getJSONObject(0).getString("objectIRI");
        int numberStacks = 0;

        // Determine indices at which data for a new object starts
        List<Integer> resultIndices = new ArrayList<>();
        resultIndices.add(0);

        for (int i = 0; i < StackGeometricQueryResult.length(); i++) {
            String objectIRI = StackGeometricQueryResult.getJSONObject(i).getString("objectIRI");
            if (!objectIRI.equals(objectIRIPrev)) {
                resultIndices.add(i);
                objectIRIPrev = objectIRI;
            }
        }

        for (int i = 0; i < resultIndices.size(); i++) {
            // Determine range of indices for each object
            int firstIndex = resultIndices.get(i);
            int lastIndex;
            if (i == resultIndices.size() - 1) {
                lastIndex = StackGeometricQueryResult.length();
            } else {
                lastIndex = resultIndices.get(i + 1);
            }

            // Process results for each object;
            double minZ = 0.0;
            double maxZ = 0.0;
            double minAveZ = 0.0;
            double StackEastUTM = 0.0;
            double StackNorthUTM = 0.0;
            boolean includeObject = true;
            String objectIRI = "";
            double radius = 0.0;
            for (int k = firstIndex; k < lastIndex; k++) {
                JSONObject result = StackGeometricQueryResult.getJSONObject(k);
                String polygonVertex = result.getString("polygonData");
                objectIRI = result.getString("objectIRI");
                if (!polygonVertex.contains("#")) {
                    continue;
                }
                String[] vertexCoordinates = polygonVertex.split("#");
                List<Double> xcoord = new ArrayList<>();
                List<Double> ycoord = new ArrayList<>();
                List<Double> zcoord = new ArrayList<>();
                for (int j = 0; j < vertexCoordinates.length; j += 3) {
                    xcoord.add(Double.parseDouble(vertexCoordinates[j]));
                    ycoord.add(Double.parseDouble(vertexCoordinates[j + 1]));
                    zcoord.add(Double.parseDouble(vertexCoordinates[j + 2]));
                }
                double polyMinZ = Collections.min(zcoord);
                double polyMaxZ = Collections.max(zcoord);
                double polyAveZ = zcoord.stream().mapToDouble(a -> a).average().orElse(0.0);

                if (k == firstIndex) {
                    minZ = polyMinZ;
                    maxZ = polyMaxZ;
                    minAveZ = polyAveZ;
                } else {
                    minZ = Math.min(minZ, polyMinZ);
                    maxZ = Math.max(maxZ, polyMaxZ);
                    minAveZ = Math.min(minAveZ, polyAveZ);
                }

                if (polyMinZ == polyMaxZ && minZ == polyMinZ) {
                    double aveX = xcoord.stream().mapToDouble(d -> d).average().orElse(0.0);
                    double aveY = ycoord.stream().mapToDouble(d -> d).average().orElse(0.0);

                    List<List<Double>> inputcoordinates = new ArrayList<>();
                    List<Double> inputcoords = new ArrayList<>(Arrays.asList(aveX, aveY));
                    inputcoordinates.add(inputcoords);
                    List<List<Double>> outputCoordinates = convertCoordinates(inputcoordinates, DatabaseCoordSys,
                            "EPSG:4326");

                    Geometry point = new GeometryFactory().createPoint(new Coordinate(outputCoordinates.get(0).get(0),
                            outputCoordinates.get(0).get(1)));

                    if (!scope.covers(point)) {
                        System.out.println(aveX + ", " + aveY);
                        includeObject = false;
                        break;
                    }

                    radius = 0.0;
                    for (int j = 0; j < xcoord.size(); j++) {
                        double dx = xcoord.get(j) - aveX;
                        double dy = ycoord.get(j) - aveY;
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        radius = radius + dist;
                    }
                    radius /= xcoord.size();

                    outputCoordinates.clear();
                    outputCoordinates = convertCoordinates(inputcoordinates, DatabaseCoordSys, UTMCoordSys);

                    StackEastUTM = outputCoordinates.get(0).get(0);
                    StackNorthUTM = outputCoordinates.get(0).get(1);
                }

            }

            if (includeObject) {
                numberStacks++;
                double height = maxZ - minZ;
                String InputLine = "\'Stk" + numberStacks + "\'" + " " + "BASE_ELEVATION " +
                        height + " " + StackEastUTM + " " + StackNorthUTM + " \n";
                BPIPPRMStackInput.add(InputLine);
                StringBuffer averageCoordinate = new StringBuffer();
                averageCoordinate.append(StackEastUTM).append("#").append(StackNorthUTM).append("#").append(height);
                StackProperties.add(averageCoordinate.toString());

                // Search for IRI in StackIRIString
                int ind = StackIRIString.indexOf(objectIRI);
                List<Double> emissions = emissionsOfEveryStack.get(ind);
                StackEmissionsTimeSeries.add(emissions);
                StackDiameter.add(2 * radius);
            }

        }

        // TODO: This part of getProperties needs to be updated. The
        // getBuildingsWithinBounds method may need to
        // be updated in order for it to work correctly for Jurong Island.

        JSONArray BuildingIRIQueryResult = getBuildingsNearPollutantSources();

        List<String> BuildingIRIString = new ArrayList<>();

        // Remove IRIs of pollutant sources
        for (int i = 0; i < BuildingIRIQueryResult.length(); i++) {
            String buildingIRI = BuildingIRIQueryResult.getJSONObject(i).getString("cityObject");
            if (!StackIRIString.contains(buildingIRI.replace("cityobject", "cityfurniture"))) {
                BuildingIRIString.add(buildingIRI.replace("cityobject", "building"));
            }
        }

        // JSONArray BuildingIRIQueryResult =
        // BuildingsQueryClient.BuildingQuery(StackQueryIRI);

        JSONArray BuildingGeometricQueryResult = BuildingsQueryClient.BuildingGeometricQuery(GeospatialQueryIRI,
                BuildingIRIString);

        objectIRIPrev = BuildingGeometricQueryResult.getJSONObject(0).getString("objectIRI");
        int numberBuildings = 0;

        // Determine indices at which data for a new object starts
        resultIndices.clear();
        resultIndices.add(0);

        for (int i = 0; i < BuildingGeometricQueryResult.length(); i++) {
            String objectIRI = BuildingGeometricQueryResult.getJSONObject(i).getString("objectIRI");
            if (!objectIRI.equals(objectIRIPrev)) {
                resultIndices.add(i);
                objectIRIPrev = objectIRI;
            }
        }

        for (int i = 0; i < resultIndices.size(); i++) {
            // Determine range of indices for each object
            int firstIndex = resultIndices.get(i);
            int lastIndex;
            if (i == resultIndices.size() - 1) {
                lastIndex = BuildingGeometricQueryResult.length();
            } else {
                lastIndex = resultIndices.get(i + 1);
            }

            // Process results for each object;
            double minZ = 0.0;
            double maxZ = 0.0;
            double minAveZ = 0.0;
            double BuildingEastUTM = 0.0;
            double BuildingNorthUTM = 0.0;
            boolean includeObject = true;
            int basePolygonIndex = -1;
            for (int k = firstIndex; k < lastIndex; k++) {
                JSONObject result = BuildingGeometricQueryResult.getJSONObject(k);
                String polygonVertex = result.getString("polygonData");
                result.getString("objectIRI");
                if (!polygonVertex.contains("#")) {
                    continue;
                }
                String[] vertexCoordinates = polygonVertex.split("#");
                List<Double> xcoord = new ArrayList<>();
                List<Double> ycoord = new ArrayList<>();
                List<Double> zcoord = new ArrayList<>();
                for (int j = 0; j < vertexCoordinates.length; j += 3) {
                    xcoord.add(Double.parseDouble(vertexCoordinates[j]));
                    ycoord.add(Double.parseDouble(vertexCoordinates[j + 1]));
                    zcoord.add(Double.parseDouble(vertexCoordinates[j + 2]));
                }
                double polyMinZ = Collections.min(zcoord);
                double polyMaxZ = Collections.max(zcoord);
                double polyAveZ = zcoord.stream().mapToDouble(x -> x).average().orElse(0.0);

                if (k == firstIndex) {
                    minZ = polyMinZ;
                    maxZ = polyMaxZ;
                    minAveZ = polyAveZ;
                } else {
                    minZ = Math.min(minZ, polyMinZ);
                    maxZ = Math.max(maxZ, polyMaxZ);
                    minAveZ = Math.min(minZ, polyAveZ);
                }

                if (polyMinZ == polyMaxZ && minZ == polyMinZ) {
                    double aveX = xcoord.stream().mapToDouble(d -> d).average().orElse(0.0);
                    double aveY = ycoord.stream().mapToDouble(d -> d).average().orElse(0.0);

                    List<List<Double>> inputcoordinates = new ArrayList<>();
                    List<Double> inputcoords = new ArrayList<>(Arrays.asList(aveX, aveY));
                    inputcoordinates.add(inputcoords);
                    List<List<Double>> outputCoordinates = convertCoordinates(inputcoordinates, DatabaseCoordSys,
                            "EPSG:4326");

                    Geometry point = new GeometryFactory().createPoint(new Coordinate(outputCoordinates.get(0).get(0),
                            outputCoordinates.get(0).get(1)));

                    if (!scope.covers(point)) {
                        System.out.println(aveX + ", " + aveY);
                        includeObject = false;
                        break;
                    }

                    outputCoordinates.clear();
                    outputCoordinates = convertCoordinates(inputcoordinates, DatabaseCoordSys, UTMCoordSys);

                    BuildingEastUTM = outputCoordinates.get(0).get(0);
                    BuildingNorthUTM = outputCoordinates.get(0).get(1);
                    basePolygonIndex = k;
                }

            }

            if (includeObject) {
                numberBuildings++;
                double height = maxZ - minZ;
                StringBuffer averageCoordinate = new StringBuffer();
                averageCoordinate.append(BuildingEastUTM).append("#").append(BuildingNorthUTM).append("#")
                        .append(height);
                BuildingProperties.add(averageCoordinate.toString());

                List<String> buildInfo = new ArrayList<>();

                String InputLine = "\'Build" + numberBuildings + "\' " + "1 " + "BASE_ELEVATION" + " \n";
                buildInfo.add(InputLine);
                String BasePolygonVertices = BuildingGeometricQueryResult.getJSONObject(basePolygonIndex)
                        .getString("polygonData");

                String[] BaseVertices = BasePolygonVertices.split("#");
                int numCorners = BaseVertices.length / 3;
                InputLine = numCorners + " " + height + " \n";
                buildInfo.add(InputLine);
                List<List<Double>> inputcoordinates = new ArrayList<>();

                for (int j = 0; j < BaseVertices.length; j += 3) {
                    List<Double> inputcoords = new ArrayList<>(Arrays.asList(Double.parseDouble(BaseVertices[j]),
                            Double.parseDouble(BaseVertices[j + 1])));
                    inputcoordinates.add(inputcoords);
                }

                List<List<Double>> outputCoordinates = convertCoordinates(inputcoordinates, DatabaseCoordSys,
                        UTMCoordSys);
                for (int j = 0; j < outputCoordinates.size(); j++) {
                    Double VertexEastUTM = outputCoordinates.get(j).get(0);
                    Double VertexNorthUTM = outputCoordinates.get(j).get(1);
                    InputLine = VertexEastUTM + " " + VertexNorthUTM + " \n";
                    buildInfo.add(InputLine);
                }
                BuildingVertices.add(BasePolygonVertices);
                BPIPPRMBuildingInput.add(buildInfo);

            }

        }

    }

    public int createPlantItemsGeoServerLayer() {

        // define a list of (longitude, latitude) coordinates
        List<List<Double>> utmCoordinates = new ArrayList<>();

        for (int i = 0; i < StackProperties.size(); i++) {
            String[] avecoord = StackProperties.get(i).split("#");
            double StackEastUTM = Double.parseDouble(avecoord[0]);
            double StackNorthUTM = Double.parseDouble(avecoord[1]);
            List<Double> tmp = Arrays.asList(StackEastUTM, StackNorthUTM);
            utmCoordinates.add(tmp);
        }
        List<List<Double>> LonLatCoords = convertCoordinates(utmCoordinates, UTMCoordSys, "ESPG:4326");

        // create a JSONObject that represents a GeoJSON Feature Collection
        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");
        JSONArray features = new JSONArray();

        // loop through the coordinates and add them as GeoJSON Points to the Feature
        // Collection
        for (List<Double> coordinate : LonLatCoords) {
            JSONObject geometry = new JSONObject();
            geometry.put("type", "Point");
            geometry.put("coordinates", new JSONArray(coordinate));
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            feature.put("geometry", geometry);
            features.put(feature);
        }
        featureCollection.put("features", features);

        LOGGER.info("Uploading plant items GeoJSON to PostGIS");
        GDALClient gdalclient = GDALClient.getInstance();
        gdalclient.uploadVectorStringToPostGIS(EnvConfig.DATABASE, EnvConfig.SOURCE_LAYER, featureCollection.toString(),
                new Ogr2OgrOptions(), true);

        LOGGER.info("Creating plant items layer in Geoserver");
        GeoServerClient geoserverclient = GeoServerClient.getInstance();
        geoserverclient.createWorkspace(EnvConfig.GEOSERVER_WORKSPACE);
        geoserverclient.createPostGISLayer(EnvConfig.GEOSERVER_WORKSPACE, EnvConfig.DATABASE, EnvConfig.SOURCE_LAYER,
                new GeoServerVectorSettings());

        // convert the Feature Collection to a JSON string
        // String geojsonString = featureCollection.toString();
        // System.out.println(geojsonString);

        return 0;

    }

    public int copyCachedAERMAPOutput() {

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("receptor.dat")) {
            Files.copy(is, aermodDirectory.resolve("receptor.dat"));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.info("Failed to copy receptor.dat. AERMAP will be run to generate this file.");
            return 1;
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("buildingSources.dat")) {
            Files.copy(is, aermapDirectory.resolve("buildingSources.dat"));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.info("Failed to copy buildingSources.dat. AERMAP will be run to generate this file.");
            return 1;
        }

        return 0;
    }

    public int createAERMAPInputFile() {

        int centreZoneNumber = (int) Math.ceil((scope.getCentroid().getCoordinate().getX() + 180) / 6);
        String templateContent;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("aermap.inp")) {
            templateContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            String simGrid = String.format("%f %f %f %f %d %d", 0.0, 0.0, 0.0, 0.0, centreZoneNumber, 0);
            templateContent = templateContent.replace("REPLACED_BY_AERMOD_AGENT", simGrid);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("Failed to read aermap.inp file");
            return 1;
        }
        return writeToFile(aermapDirectory.resolve("aermap.inp"), templateContent);
    }

    public int createAERMAPVirtualSensorInputFiles() {

        // Update ANCHORXY option in aermapVirtualSensor.inp
        int centreZoneNumber = (int) Math.ceil((scope.getCentroid().getCoordinate().getX() + 180) / 6);
        String templateContent;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("aermapVirtualSensor.inp")) {
            templateContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            String simGrid = String.format("%f %f %f %f %d %d", 0.0, 0.0, 0.0, 0.0, centreZoneNumber, 0);
            templateContent = templateContent.replace("REPLACED_BY_AERMOD_AGENT", simGrid);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("Failed to read aermapVirtualSensor.inp file");
            return 1;
        }
        int res = writeToFile(aermapDirectory.resolve("aermapVirtualSensor.inp"), templateContent);
        if (res > 0)
            return res;

        // Write out receptor data for virtual sensors.

        StringBuilder sb = new StringBuilder();

        List<List<Double>> inputCoordinates = new ArrayList<>();
        List<Double> sensorZ = new ArrayList<>();

        for (int i = 0; i < sensorProperties.size(); i++) {
            inputCoordinates.add(Arrays.asList(sensorProperties.get(i).get(0), sensorProperties.get(i).get(1)));
            sensorZ.add(sensorProperties.get(i).get(2));
        }

        List<List<Double>> outputCoordinates = convertCoordinates(inputCoordinates, "EPSG:4326", UTMCoordSys);

        String prefix = "RE DISCCART ";
        for (int i = 0; i < outputCoordinates.size(); i++) {
            List<Double> outCoord = outputCoordinates.get(i);
            String line = prefix + outCoord.get(0) + " " + outCoord.get(1) + " " + 0.0 + " " + sensorZ.get(i);
            sb.append(line + " \n");
        }

        return writeToFile(aermapDirectory.resolve("aermapVirtualReceptors.dat"), sb.toString());

    }

    // This method should only be called if AERMAP was not run.
    public int createAERMODVirtualReceptorInput() {

        // Write out receptor data for virtual sensors.

        StringBuilder sb = new StringBuilder();

        List<List<Double>> inputCoordinates = new ArrayList<>();
        List<Double> sensorZ = new ArrayList<>();

        for (int i = 0; i < sensorProperties.size(); i++) {
            inputCoordinates.add(Arrays.asList(sensorProperties.get(i).get(0), sensorProperties.get(i).get(1)));
            sensorZ.add(sensorProperties.get(i).get(2));
        }

        List<List<Double>> outputCoordinates = convertCoordinates(inputCoordinates, "EPSG:4326", UTMCoordSys);

        String prefix = "RE DISCCART ";
        for (int i = 0; i < outputCoordinates.size(); i++) {
            List<Double> outCoord = outputCoordinates.get(i);
            String line = prefix + outCoord.get(0) + " " + outCoord.get(1) + " " + 0.0 + " " + sensorZ.get(i);
            sb.append(line + " \n");
        }

        return writeToFile(aermodDirectory.resolve("virtualReceptors.dat"), sb.toString());

    }

    public int createAERMAPSourceInput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < StackProperties.size(); i++) {
            String[] avecoord = StackProperties.get(i).split("#");
            double StackEastUTM = Double.parseDouble(avecoord[0]);
            double StackNorthUTM = Double.parseDouble(avecoord[1]);
            String stkId = "Stk" + (i + 1);
            sb.append(String.format("SO LOCATION %s POINT %f %f %f \n", stkId, StackEastUTM, StackNorthUTM, 0.0));
        }

        for (int i = 0; i < BuildingProperties.size(); i++) {
            String[] avecoord = BuildingProperties.get(i).split("#");
            double BuildEastUTM = Double.parseDouble(avecoord[0]);
            double BuildNorthUTM = Double.parseDouble(avecoord[1]);
            String buildId = "Build" + (i + 1);
            sb.append(String.format("SO LOCATION %s POINT %f %f %f \n", buildId, BuildEastUTM, BuildNorthUTM, 0.0));
        }

        return writeToFile(aermapDirectory.resolve("aermapSources.dat"), sb.toString());
    }

    public int createAERMAPReceptorInput(int nx, int ny) {

        List<Double> xDoubles = new ArrayList<>();
        List<Double> yDoubles = new ArrayList<>();

        for (int i = 0; i < scope.getCoordinates().length; i++) {

            double xc = scope.getCoordinates()[i].x;
            double yc = scope.getCoordinates()[i].y;
            List<Double> inputcoords = Arrays.asList(xc, yc);
            List<List<Double>> inputcoordinates = Arrays.asList(inputcoords);
            List<List<Double>> outputcoordinates = convertCoordinates(inputcoordinates, "EPSG:4326", UTMCoordSys);
            xDoubles.add(outputcoordinates.get(0).get(0));
            yDoubles.add(outputcoordinates.get(0).get(1));
        }

        double xlo = Collections.min(xDoubles);
        double xhi = Collections.max(xDoubles);
        double ylo = Collections.min(yDoubles);
        double yhi = Collections.max(yDoubles);

        double dx = (xhi - xlo) / nx;
        double dy = (yhi - ylo) / ny;

        StringBuilder sb = new StringBuilder("RE GRIDCART POL1 STA \n");
        String rec = String.format("                 XYINC %f %d %f %f %d %f", xlo, nx, dx, ylo, ny, dy);
        sb.append(rec + " \n");
        sb.append("RE GRIDCART POL1 END \n");

        return writeToFile(aermapDirectory.resolve("aermapReceptors.dat"), sb.toString());
    }

    public int getElevationRasterData() {

        List<byte[]> elevData = BuildingsQueryClient.getElevationData();

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(aermapDirectory.resolve("elevation.tif").toString()));
            out.writeObject(elevData);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }

        return 0;

    }

    public int runAERMAP() {

        // Read data file names from aermap input file. Assuming that there is no "CO"
        // before the "DATAFILE" keyword.
        List<String> dataFiles = new ArrayList<>();
        Path filepath = aermapDirectory.resolve("aermap.inp");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath.toString()));
            String line = reader.readLine();
            while (line != null) {
                if (line.contains("DATAFILE")) {
                    line = line.trim();
                    String[] linesplit = line.split("\\s+");
                    dataFiles.add(linesplit[1]);

                }
                if (line.contains("CO FINISHED"))
                    break;
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return 1;
        }

        if (dataFiles.size() == 0) {
            LOGGER.error("No elevation data files specified in aermap.inp");
            return 1;
        }

        for (int i = 0; i < dataFiles.size(); i++) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dataFiles.get(i))) {
                Files.copy(inputStream, aermapDirectory.resolve(dataFiles.get(i)));
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                LOGGER.error("Failed to copy the data file" + dataFiles.get(i));
                return 1;
            }
        }

        try {
            Process process = Runtime.getRuntime().exec(new String[] { EnvConfig.AERMAP_EXE, "aermap.inp" }, null,
                    aermapDirectory.toFile());

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Read the output from the command
            LOGGER.info("Here is the standard output of AERMAP:\n");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                LOGGER.info(s);
            }

            // Read any errors from the attempted command
            LOGGER.info("Here is the standard error of AERMAP (if any):\n");
            while ((s = stdError.readLine()) != null) {
                LOGGER.info(s);
            }

            if (process.waitFor() != 0) {
                return 1;
            }

        } catch (IOException e) {
            LOGGER.error("Error executing aermap");
            LOGGER.error(e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Error executing aermap");
            LOGGER.error(e.getMessage());
            return 1;
        }

        return 0;
    }

    public int runAERMAPforVirtualSensors() {

        // Need to copy elevation data files if using cached AERMAP output for regular
        // receptors

        // Read data file names from aermap input file. Assuming that there is no "CO"
        // before the "DATAFILE" keyword.
        List<String> dataFiles = new ArrayList<>();
        Path filepath = aermapDirectory.resolve("aermapVirtualSensor.inp");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath.toString()));
            String line = reader.readLine();
            while (line != null) {
                if (line.contains("DATAFILE")) {
                    line = line.trim();
                    String[] linesplit = line.split("\\s+");
                    dataFiles.add(linesplit[1]);

                }
                if (line.contains("CO FINISHED"))
                    break;
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return 1;
        }

        if (dataFiles.size() == 0) {
            LOGGER.error("No elevation data files specified in aermapVirtualSensor.inp");
            return 1;
        }

        for (int i = 0; i < dataFiles.size(); i++) {

            Path filePath = aermapDirectory.resolve(dataFiles.get(i));
            File dataFile = new File(filePath.toString());

            if (dataFile.exists() && dataFile.isFile()) {
                continue;
            } else {
                try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dataFiles.get(i))) {
                    Files.copy(inputStream, aermapDirectory.resolve(dataFiles.get(i)));
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                    LOGGER.error("Failed to copy the data file" + dataFiles.get(i));
                    return 1;
                }
            }
        }

        try {
            Process process = Runtime.getRuntime().exec(
                    new String[] { EnvConfig.AERMAP_EXE, "aermapVirtualSensor.inp" }, null, aermapDirectory.toFile());

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Read the output from the command
            LOGGER.info("Here is the standard output of AERMAP for virtual sensors:\n");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                LOGGER.info(s);
            }

            // Read any errors from the attempted command
            LOGGER.info("Here is the standard error of AERMAP (if any) for virtual sensors:\n");
            while ((s = stdError.readLine()) != null) {
                LOGGER.info(s);
            }

            if (process.waitFor() != 0) {
                return 1;
            }

        } catch (IOException e) {
            LOGGER.error("Error executing aermap for virtual sensors");
            LOGGER.error(e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Error executing aermap for virtual sensors");
            LOGGER.error(e.getMessage());
            return 1;
        }

        return 0;

    }

    public int processAERMAPOutput() {
        // Update BPIPPRMBuildingInput and BPIPPRMStackInput
        Path filepath = aermapDirectory.resolve("buildingSources.dat");

        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath.toString()));
            String line = reader.readLine();
            while (line != null) {
                if (line.isBlank() || line.substring(0, 2).equals("**"))
                    ;
                else if (line.contains("ELEVUNIT"))
                    sb.append(line + "\n");
                else if (line.contains("STK")) {
                    sb.append(line + "\n");
                    line = line.trim();
                    String[] StackInfo = line.split("\\s+");
                    String StackElevation = StackInfo[StackInfo.length - 1];
                    int StackNum = Integer
                            .parseInt(StackInfo[2].substring(StackInfo[2].indexOf("STK") + 3, StackInfo[2].length()));
                    String StackLine = BPIPPRMStackInput.get(StackNum - 1);
                    StackLine = StackLine.replace("BASE_ELEVATION", StackElevation);
                    BPIPPRMStackInput.set(StackNum - 1, StackLine);
                    String stackProp = StackProperties.get(StackNum - 1);
                    String propElevation = "#" + StackElevation;
                    stackProp += propElevation;
                    StackProperties.set(StackNum - 1, stackProp);
                } else if (line.contains("BUILD")) {
                    line = line.trim();
                    String[] buildInfo = line.split("\\s+");
                    String buildElevation = buildInfo[buildInfo.length - 1];
                    int buildNum = Integer
                            .parseInt(buildInfo[2].substring(buildInfo[2].indexOf("BUILD") + 5, buildInfo[2].length()));
                    String BuildLine = BPIPPRMBuildingInput.get(buildNum - 1).get(0);
                    BuildLine = BuildLine.replace("BASE_ELEVATION", buildElevation);
                    BPIPPRMBuildingInput.get(buildNum - 1).set(0, BuildLine);
                }
                line = reader.readLine();
            }
            reader.close();
            LOGGER.info(sb.toString());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return 1;
        }
        return 0;
    }

    // This method should be called only if AERMAP was not run.
    public int updateElevationData() {

        int numberBuildings = BPIPPRMBuildingInput.size();
        for (int i = 0; i < numberBuildings; i++) {
            String BuildLine = BPIPPRMBuildingInput.get(i).get(0);
            BuildLine = BuildLine.replace("BASE_ELEVATION", "0.0");
            BPIPPRMBuildingInput.get(i).set(0, BuildLine);
        }

        int numberStacks = BPIPPRMStackInput.size();
        for (int i = 0; i < numberStacks; i++) {
            String StackLine = BPIPPRMStackInput.get(i);
            StackLine = StackLine.replace("BASE_ELEVATION", "0.0");
            BPIPPRMStackInput.set(i, StackLine);
            String stackProp = StackProperties.get(i);
            String propElevation = "#0.0";
            stackProp += propElevation;
            StackProperties.set(i, stackProp);

        }
        return 0;
    }

    /* Write out data to BPIPPRM input file and run this program. */
    public int createBPIPPRMInput() {

        List<String> frontmatter = new ArrayList<>();
        frontmatter.add("\'BPIPPRM test run\' \n");
        frontmatter.add("\'p\' \n");
        frontmatter.add("\' METERS    \'  1.0  \n");
        frontmatter.add("\'UTMY \'  0.0 \n");

        StringBuilder sb = new StringBuilder();

        for (String st : frontmatter) {
            sb.append(st);
        }

        int numberBuildings = BPIPPRMBuildingInput.size();
        sb.append(numberBuildings + " \n");
        for (int i = 0; i < numberBuildings; i++) {
            for (int j = 0; j < BPIPPRMBuildingInput.get(i).size(); j++) {
                sb.append(BPIPPRMBuildingInput.get(i).get(j));
            }
        }
        int numberStacks = BPIPPRMStackInput.size();
        sb.append(numberStacks + " \n");
        for (int i = 0; i < numberStacks; i++) {
            sb.append(BPIPPRMStackInput.get(i));
        }
        return writeToFile(bpipprmDirectory.resolve("bpipprm.inp"), sb.toString());

    }

    private int writeToFile(Path path, String content) {

        try {
            boolean res = Files.deleteIfExists(path);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            LOGGER.info("Writing file: {}", path);
            writer.write(content);
            return 0;
        } catch (IOException e) {
            String errmsg = "Failed to write " + path.getFileName();
            LOGGER.error(errmsg);
            LOGGER.error(e.getMessage());
            return 1;
        }
    }

    public int runBPIPPRM() {
        try {
            Process process = Runtime.getRuntime().exec(
                    new String[] { EnvConfig.BPIPPRM_EXE, "bpipprm.inp", "building.dat", "buildings_summary.dat" },
                    null, bpipprmDirectory.toFile());
            if (process.waitFor() != 0) {
                return 1;
            }
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Read the output from the command
            LOGGER.info("Here is the standard output of the command:\n");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                LOGGER.info(s);
            }

            // Read any errors from the attempted command
            LOGGER.info("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                LOGGER.info(s);
            }
        } catch (IOException e) {
            return 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
        return 0;
    }

    public int createAERMODBuildingsInput() {

        StringBuilder sb = new StringBuilder();
        if (locindex == -1)
            return writeToFile(aermodDirectory.resolve("buildings.dat"), sb.toString());

        Path filepath = bpipprmDirectory.resolve("building.dat");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath.toString()));
            String line = reader.readLine();
            while (line != null) {
                line = line.stripLeading();
                if (line.length() > 2 && line.substring(0, 2).equals("SO"))
                    sb.append(line + "\n");
                line = reader.readLine();
            }
            reader.close();
            LOGGER.info(sb.toString());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        return writeToFile(aermodDirectory.resolve("buildings.dat"), sb.toString());
    }

    public int createAERMODSourceInput() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < StackProperties.size(); i++) {
            String[] avecoord = StackProperties.get(i).split("#");
            double StackEastUTM = Double.parseDouble(avecoord[0]);
            double StackNorthUTM = Double.parseDouble(avecoord[1]);
            double StackHeight = Double.parseDouble(avecoord[2]);
            double StackBaseElevation = Double.parseDouble(avecoord[3]);
            // This emissions value is overwritten by the one specified in the
            // hourlyEmissions.dat file.
            double massFlowrateInTonYr = 195.0;
            double massFlowrateInGs = massFlowrateInTonYr * 1000 * 1000 / (365 * 24 * 60 * 60);
            double gasTemperatureKelvin = 533.15;
            double Diameter = StackDiameter.get(i);
            /*
             * The following code calculates the exit velocity based on the ideal gas law
             * assuming
             * that the pollutant stream consists of only one component.
             */
            /*
             * double atmosphericPressurePa = 101325;
             * double gasConstantJoulemolKelvin = 8.314;
             * double molarMassCO2gmol = 44.01;
             * double molarMassNO2gmol = 46.005;
             * double volumetricFlowRatem3s = (massFlowrateInGs / molarMassNO2gmol) *
             * gasConstantJoulemolKelvin * gasTemperatureKelvin / atmosphericPressurePa;
             * // Hard-coded value
             * double AlternativeVolumetricFlowRatem3s = 5.42183;
             * double stackAream2 = (Math.PI / 4) * Diameter * Diameter;
             * double velocityms = AlternativeVolumetricFlowRatem3s / stackAream2;
             */
            double velocityms = 10.0;

            String stkId = "Stk" + (i + 1);
            sb.append(String.format("SO LOCATION %s POINT %f %f %f \n", stkId, StackEastUTM, StackNorthUTM,
                    StackBaseElevation));
            sb.append("SO HOUREMIS hourlyEmissions.dat " + stkId + " \n");
            sb.append(String.format("SO SRCPARAM %s %f %f %f %f %f \n", stkId,
                    massFlowrateInGs, StackHeight, gasTemperatureKelvin, velocityms, Diameter));
        }

        StringBuilder sbe = new StringBuilder();

        // Check if input timestamps start from an hour after midnight
        // If so include additional timestamps in the hourlyEmissions.dat file to
        // maintain consistency with
        // input file containing surface weather data.

        LocalDateTime ldp = LocalDateTime.parse(timeStamps.get(0), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int pyear = ldp.getYear();
        int pmonth = ldp.getMonthValue();
        int pday = ldp.getDayOfMonth();
        int firstHour = ldp.getHour();
        String pys = String.valueOf(pyear).substring(2);
        String pline = "SO HOUREMIS " + pys + " " + pmonth + " " + pday;

        if (firstHour > 0) {
            for (int i = 0; i < firstHour; i++) {
                for (int j = 0; j < StackProperties.size(); j++) {
                    String stkId = "Stk" + (j + 1);
                    Double massFlowRateInTonYr = 150.0;
                    double massFlowrateInGs = massFlowRateInTonYr * 1000 * 1000 / (365 * 24 * 60 * 60);
                    double gasTemperatureKelvin = 533.15;
                    double velocityms = 10.0;
                    String newLine = pline + " " + (i + 1) + " " + stkId + " " + massFlowrateInGs + " "
                            + gasTemperatureKelvin + " " + velocityms;
                    sbe.append(newLine + "\n");

                }
            }
        }

        for (int i = 0; i < timeStamps.size(); i++) {

            String line = "SO HOUREMIS ";
            LocalDateTime ldt = LocalDateTime.parse(timeStamps.get(i),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            int year = ldt.getYear();
            int month = ldt.getMonthValue();
            int day = ldt.getDayOfMonth();
            // Adding one because the hour value in the user specified timestamps ranges
            // between 0 and 23.
            // However, the values of hour in the hourlyEmissions.dat and the
            // weather_template.144 files
            // should range between 1 and 24. Otherwise, AERMOD reports a fatal date/time
            // mismatch error.
            int hour = ldt.getHour() + 1;

            String ys = String.valueOf(year).substring(2);
            String ms = String.valueOf(month);
            /*
             * May not be required
             * if (ms.substring(0,1).equals("0")) {
             * ms = ms.substring(1);
             * }
             */

            line = line + ys + " " + month + " " + day + " " + hour;

            for (int j = 0; j < StackProperties.size(); j++) {
                String stkId = "Stk" + (j + 1);
                Double massFlowRateInTonYr = StackEmissionsTimeSeries.get(j).get(i);
                double massFlowrateInGs = massFlowRateInTonYr * 1000 * 1000 / (365 * 24 * 60 * 60);
                double gasTemperatureKelvin = 533.15;
                double velocityms = 10.0;
                String newLine = line + " " + stkId + " " + massFlowrateInGs + " " + gasTemperatureKelvin + " "
                        + velocityms;
                sbe.append(newLine + "\n");

            }
        }

        // This part ensures that one will not encounter an EOF error due to different
        // numbers of data points in the
        // AERMET_SURF.SFC and hourlyEmissions.dat file.

        LocalDateTime lde = LocalDateTime.parse(timeStamps.get(timeStamps.size() - 1),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int year = lde.getYear();
        int month = lde.getMonthValue();
        int day = lde.getDayOfMonth();
        int hour = lde.getHour() + 1;
        String ys = String.valueOf(year).substring(2);
        String line = "SO HOUREMIS " + ys + " " + month + " " + day;

        while (hour < 24) {
            hour++;
            for (int j = 0; j < StackProperties.size(); j++) {
                String stkId = "Stk" + (j + 1);
                Double massFlowRateInTonYr = 150.0;
                double massFlowrateInGs = massFlowRateInTonYr * 1000 * 1000 / (365 * 24 * 60 * 60);
                double gasTemperatureKelvin = 533.15;
                double velocityms = 10.0;
                String newLine = line + " " + hour + " " + stkId + " " + massFlowrateInGs + " " + gasTemperatureKelvin
                        + " " + velocityms;
                sbe.append(newLine + "\n");

            }

        }

        writeToFile(aermodDirectory.resolve("hourlyEmissions.dat"), sbe.toString());

        return writeToFile(aermodDirectory.resolve("plantSources.dat"), sb.toString());

    }

    public int createAERMODReceptorInput(int nx, int ny) {

        List<Double> xDoubles = new ArrayList<>();
        List<Double> yDoubles = new ArrayList<>();

        for (int i = 0; i < scope.getCoordinates().length; i++) {

            double xc = scope.getCoordinates()[i].x;
            double yc = scope.getCoordinates()[i].y;

            List<Double> inputcoords = Arrays.asList(xc, yc);
            List<List<Double>> inputcoordinates = Arrays.asList(inputcoords);
            List<List<Double>> outputcoordinates = convertCoordinates(inputcoordinates, "EPSG:4326", UTMCoordSys);

            xDoubles.add(outputcoordinates.get(0).get(0));
            yDoubles.add(outputcoordinates.get(0).get(1));
        }

        double xlo = Collections.min(xDoubles);
        double xhi = Collections.max(xDoubles);
        double ylo = Collections.min(yDoubles);
        double yhi = Collections.max(yDoubles);

        double dx = (xhi - xlo) / nx;
        double dy = (yhi - ylo) / ny;

        StringBuilder sb = new StringBuilder("RE GRIDCART POL1 STA \n");
        String rec = String.format("                 XYINC %f %d %f %f %d %f", xlo, nx, dx, ylo, ny, dy);
        sb.append(rec + " \n");
        sb.append("RE GRIDCART POL1 END \n");

        return writeToFile(aermodDirectory.resolve("receptor.dat"), sb.toString());
    }

    // This method adds additional receptor input files for the various
    // user-specified flagpole heights.
    // Need to update the AERMOD main input files in addition to creating additional
    // receptor files
    public int addAERMODReceptorInput() {
        String templateContent;
        try (InputStream inputStream = new FileInputStream(aermodDirectory.resolve("receptor.dat").toFile())) {
            templateContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("Failed to read receptor.dat file");
            return 1;
        }

        double eps = 1.0e-6;
        int numberPerRow = 5;
        int res = 0;

        List<String> fileNames = new ArrayList<>();

        for (int k = 0; k < receptorHeights.size(); k++) {
            double height = receptorHeights.get(k);
            String netid = "POL" + (k + 1);
            if (Math.abs(height) < eps)
                continue;

            String line = "   GRIDCART " + netid + "     FLAG   " + "ROW_NUMBER  ";

            for (int i = 0; i < numberPerRow; i++) {
                line += height;
                line += " ";
            }

            StringBuilder sb = new StringBuilder();

            for (int j = 0; j < ny; j++) {
                String newLine = line.replace("ROW_NUMBER", String.valueOf(j + 1));
                for (int i = 0; i < (nx / numberPerRow); i++) {
                    sb.append(newLine + "\n");
                }
            }
            sb.append("RE GRIDCART " + netid + " END");
            String newContent = templateContent.replace("RE ELEVUNIT METERS", " ");
            newContent = newContent.replace("RE GRIDCART POL1 END", sb.toString());
            newContent = newContent.replaceAll("POL1", netid);
            String fileName = "receptor_" + height + ".dat";
            fileNames.add(fileName);
            int r1 = writeToFile(aermodDirectory.resolve(fileName), newContent);
            res = Math.max(res, r1);
        }

        // Copy and update the main AERMOD input file

        String replaceLine = "   INCLUDED receptor.dat";

        StringBuilder sbf = new StringBuilder(replaceLine + " \n");

        for (int i = 0; i < fileNames.size(); i++) {
            String fileString = "   INCLUDED " + fileNames.get(i);
            sbf.append(fileString + " \n");
        }

        String inputContent;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("aermod.inp")) {
            inputContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            inputContent = inputContent.replace(replaceLine, sbf.toString().stripTrailing());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("Failed to read aermod.inp file");
            return 1;
        }

        int r2 = writeToFile(aermodDirectory.resolve("aermod.inp"), inputContent);
        if (r2 == 0)
            aermodInputCreated = true;
        res = Math.max(res, r2);

        return res;
    }

    JSONObject getBuildingsGeoJSON() {
        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");

        JSONArray features = new JSONArray();
        this.buildings.stream().forEach(building -> {
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");

            JSONObject properties = new JSONObject();
            properties.put("color", "#666666");
            properties.put("opacity", 0.66);
            properties.put("base", 0);
            properties.put("height", building.getHeight());
            feature.put("properties", properties);

            JSONObject geometry = new JSONObject();
            geometry.put("type", "Polygon");
            JSONArray coordinates = new JSONArray();

            JSONArray footprintPolygon = new JSONArray();
            String srid = building.getSrid();
            for (Coordinate coordinate : building.getFootprint().getCoordinates()) {
                JSONArray point = new JSONArray();
                double[] xyOriginal = { coordinate.getX(), coordinate.getY() };
                double[] xyTransformed = CRSTransformer.transform(srid, "EPSG:4326", xyOriginal);
                point.put(xyTransformed[0]).put(xyTransformed[1]);
                footprintPolygon.put(point);
            }
            coordinates.put(footprintPolygon);
            geometry.put("coordinates", coordinates);

            feature.put("geometry", geometry);
            features.put(feature);
        });

        featureCollection.put("features", features);

        return featureCollection;
    }
}