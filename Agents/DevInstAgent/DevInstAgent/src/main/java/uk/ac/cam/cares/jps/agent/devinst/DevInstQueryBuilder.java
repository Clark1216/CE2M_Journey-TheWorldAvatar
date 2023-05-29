package uk.ac.cam.cares.jps.agent.devinst;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;

import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.PropertyPaths;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DevInstQueryBuilder {
    private RemoteStoreClient storeClient;
    private JSONObject IRIMap;
    
    // prefix
	private static final String ONTODEV = "https://www.theworldavatar.com/kg/ontodevice/";
    private static final Prefix P_DEV = SparqlBuilder.prefix("ontodevice",iri(ONTODEV));
    private static final Prefix P_SAREF = SparqlBuilder.prefix("saref", iri("https://saref.etsi.org/core/"));
    private static final Prefix P_AGENT = SparqlBuilder.prefix("ontoagent",iri("https://www.theworldavatar.com/kg/ontoagent/"));
    private static final Prefix P_OM = SparqlBuilder.prefix("om", iri("http://www.ontology-of-units-of-measure.org/resource/om-2/"));

    //classes
    /*
     * Each smart sensor is composed of 1 microcontrller
     * Each microcontroller can be composed of several sensor
     * Each sensor can be a collective of several sensor (Composition Sensor)
     * (eg. a temperature sensor composed of several thermometer)
     * Each Composition Sensor measures 1 raw reading type (a thermometer only measures temperture)
     * Each reading can be derived to other variable.
     *  - several types of readings can be derived to 1 variable
     *  - several derived variable can be derived from 1 reading type
     */
    private static String sensLabel;
    private static Iri SmartSensor_UUID;
    private static Iri MicroController_UUID;
    private static Map<String, Iri> Sensor_UUID_Map = new HashMap<>();
    private static Map<String, Iri> SensorComp_UUID_Map = new HashMap<>();
    private static Map<String, String> SensorCompToSensorMap = new HashMap<>();

    private static Map<String, Iri> Measurement_UUID_Map; //Can one sensor have more than one measurement type?
    private static List<String> AdditionalQuery;

    //TODO Implement iterator(?) for Composition sensor and measurement units
    //TODO This includes changing the tmeplate .json file, add unts, unitsIRI, sensorType, sensorTypeIRI

    private static Map<String, String> SensorTypeMap;
    private static Map<String, Iri> SensorTypeIRIMap;
    
    private static Map<String, Iri> MeasurementTypeMap;
    private static Map<String, String> MeasurementToUnitMap;
    private static Map<String, Iri> UnitIRIMap;

    private static final Iri SmartSensor = P_DEV.iri("SmartSensor");
    private static final Iri MicroController = P_DEV.iri("MicroController");
    private static final Iri Sensor = P_SAREF.iri("Sensor");

    //properties
    private static final Iri consistsOf = P_SAREF.iri("consistsOf");
    private static final Iri sendsSignalTo = P_DEV.iri("sendsSignalTo");
    private static final Iri measures = P_DEV.iri("measures");
    private static final Iri hasUnit = P_OM.iri("hasUnit");
    private static final Iri symbol = P_OM.iri("symbol");
    
    //LOGGER
    private static final Logger LOGGER = LogManager.getLogger(DevInstAgentLauncher.class);
    
    
    //Methods
    public DevInstQueryBuilder (RemoteStoreClient storeClient) {
        this.storeClient = storeClient;
    }

    void InsertDevice(JSONObject desc) {
        //Get data from descriptor
        parseJSON(desc);

        //Write query & Update DB   
        InstantiateDevices();
    }

    void parseJSON (JSONObject desc) {
        JSONObject MicroController = desc.getJSONObject("MicroController");
        IRIMap = desc.getJSONObject("IRIMapper");
        
        //smartSensor data
        sensLabel = MicroController.getString("label");
        
        SmartSensor_UUID = getIri(IRIMap.getString(MicroController.getString("name")));
        
        MicroController_UUID =  getIri(IRIMap.getString(MicroController.getString("type")));

        JSONObject MainSensorMap = desc.getJSONObject("MainSensorMap");
        for (String MainSensorName : MainSensorMap.keySet()) {
            Iri MainSensor_UUID = getIri(MainSensorName);
            Sensor_UUID_Map.put(MainSensorName, MainSensor_UUID);

            JSONObject MainSensor = MainSensorMap.getJSONObject(MainSensorName);
            for(String SensorName: MainSensor.keySet()){
                JSONObject Sensor = MainSensor.getJSONObject(SensorName);
                SensorCompToSensorMap.put(SensorName, MainSensorName);

                //String Sensor_UUID_String = Sensor.getString("name");
                //TODO Check IRIMAp first, then if its null, default to oontodevice
                //Iri Sensor_UUID = P_DEV.iri(Sensor_UUID_String);
                Iri Sensor_UUID = getIri(SensorName);
                SensorComp_UUID_Map.put(SensorName, Sensor_UUID);
                
                String Measurement_ID_String = Sensor.getJSONObject("output").getString("fieldname");
                Iri Measurement_UUID = getIri(Measurement_ID_String);
                Measurement_UUID_Map.put(SensorName, Measurement_UUID);

                String MeasurementType_ID_String = Sensor.getJSONObject("output").getString("type");
                Iri MeasurementType_UUID = getIri(MeasurementType_ID_String);
                MeasurementTypeMap.put(SensorName, MeasurementType_UUID);

                String SensorCompType_String = Sensor.getString("type");
                //TODO Check IRIMAp first, then if its null, default to oontodevice
                SensorTypeMap.put(SensorName, SensorCompType_String);
                Iri SensorType_IRI = iri(IRIMap.getString(SensorCompType_String));
                SensorTypeIRIMap.put(SensorCompType_String, SensorType_IRI);

                String unit = Sensor.getJSONObject("output").getString("unit");
                MeasurementToUnitMap.put(Measurement_ID_String, unit);
                //TODO Check IRIMAp first, then if its null, default to oontodevice OR OM
                Iri unit_IRI = getIri(unit);
                UnitIRIMap.put(unit, unit_IRI);

            }
        }


        JSONArray additional = desc.getJSONArray("AdditionalQuery");
        for(int i = 0; i < additional.length(); i++) {
            String query = additional.getString(i);
            AdditionalQuery.add(query);
        }


    }

    void InstantiateDevices() {
        ModifyQuery modify = Queries.MODIFY();
        modify.prefix(P_AGENT, P_DEV, P_OM, P_SAREF);

        //Instantiate the microcontroller, sensors, variable instances

        modify.insert(SmartSensor_UUID.isA(SmartSensor));
        modify.insert(SmartSensor_UUID.has(RDFS.LABEL, Rdf.literalOf(sensLabel)));
        
        modify.insert(MicroController_UUID.isA(MicroController));
        
        for(String SensorName : Sensor_UUID_Map.keySet()){
            Iri UUID = Sensor_UUID_Map.get(SensorName);
            modify.insert(UUID.isA(Sensor));
        }

        for(String SensorCompName : SensorComp_UUID_Map.keySet()){
            Iri UUID = SensorComp_UUID_Map.get(SensorCompName);
            Iri Type_UUID = SensorTypeIRIMap.get(SensorTypeMap.get(SensorCompName));
            modify.insert(UUID.isA(Type_UUID));

            Iri Measurement_UUID = Measurement_UUID_Map.get(SensorCompName);
            Iri MeasurementType_IRI = MeasurementTypeMap.get(SensorCompName);
            modify.insert(Measurement_UUID.isA(MeasurementType_IRI));
        }


        //TODO Add agent instantiation

        //Connect properties
        //Connect the microcontroller and sensor instances

        modify.insert(SmartSensor_UUID.has(consistsOf, MicroController_UUID));
        for(String SensorName : Sensor_UUID_Map.keySet()){
            Iri UUID = Sensor_UUID_Map.get(SensorName);
            modify.insert(SmartSensor_UUID.has(consistsOf, UUID));
            modify.insert(UUID.has(sendsSignalTo, MicroController_UUID));
        }

        for(String SensorCompName : SensorCompToSensorMap.keySet()){
            String MainSensorName = SensorCompToSensorMap.get(SensorCompName);
            Iri MainSensor_UUID = Sensor_UUID_Map.get(MainSensorName);
            Iri CompSensor_UUID = SensorComp_UUID_Map.get(SensorCompName);

            modify.insert(MainSensor_UUID.has(consistsOf, CompSensor_UUID));

            Iri Measurement_UUID = Measurement_UUID_Map.get(SensorCompName);
            modify.insert(CompSensor_UUID.has(measures, Measurement_UUID));

        }
        
        //Add aditional query
        for(String query: AdditionalQuery){
            String[] splits = query.split("\\s+");
            List<Iri> listIRI = new ArrayList<>();
            for(int i=0; i < 3; i++){
                Iri component = iri(splits[i]);
                listIRI.add(component);
            }
            TriplePattern triple = GraphPatterns.tp(listIRI.get(0), listIRI.get(1), listIRI.get(2));
            modify.insert(triple);
        }

        storeClient.executeUpdate(modify.getQueryString());
    }

    /*
     * Find the IRI containing the same ID in the KG
     * If more than 1 is found throws error
     * IF none is find, throws error
     */
    private Iri findIriMatch (String ID){
        SelectQuery query = Queries.SELECT();
        JSONArray queryResult = new JSONArray();
        List<String> result = new ArrayList<>();
        query.where(query.var().has(query.var(), query.var()) );
        try{
            queryResult = storeClient.executeQuery(query.getQueryString());
        }catch (Exception e) {
            throw new JPSRuntimeException("Failed to execute query.", e);
        }

        for(int i =0 ; i < queryResult.length(); i++){
            JSONObject row = queryResult.getJSONObject(i);
            for (String key : row.keySet()){
                String iriFound = row.getString(key);
                if (iriFound.contains(ID) && !result.contains(iriFound)){
                    result.add(iriFound);
                }
            }
        }


        if (result.size() > 1) {
            throw new JPSRuntimeException("More than 1 possible IRI match is found: " + result + ". Resolve by specifying in IRIMapper.");
        }

        if (result.size() == 0) {
            throw new JPSRuntimeException("No possible match found for ID: " + ID + ". Resolve by specifying in IRIMapper.");
        }
        
        return iri(result.get(0));

    }

    /*
     * Generate new IRI
     */
    private Iri genIRI (String ID, Prefix prefix) {
        return prefix.iri(ID + "_" + UUID.randomUUID());
    }

    /*
     * Find/generate the IRI
     * IRIMapper takes priority, if IRIMapper has IRI, use the specified IRI
     * If IRIMapper is empty, then check if IRI exist
     * Check is done by ID in the json file
     * All IRI generated should contain the same ID name, but different UUID
     * IF IRI exist, throw error, tell user to specify IRI in IRIMapper if IRI found is not the same object
     * Generate IRI if no IRI is found in mapper and KG
     * 
     */
    private Iri getIri(String ID) {
        if (IRIMap.has(ID)){
            String iriString = IRIMap.getString(ID);
            
            if (iriString.toLowerCase().equals("gen")){
                //Defaults to Ontodevice
                return genIRI(ID, P_DEV);
            }
            else if (iriString.toLowerCase().equals("find")){
                return findIriMatch(ID);
            }
            else {
                return iri(iriString);
            }
        }
        else{
            throw new JPSRuntimeException("IRI is not in IRIMapper. Please provide either the IRI or the following keyword:" +
            "find : Find the IRI in the knowledge graph with the same ID. " + 
            "gen: Generate new IRI ending with random UUID. Based on ontodevice."
            );
        }

    }


}
