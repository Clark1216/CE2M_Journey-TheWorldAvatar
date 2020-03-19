package uk.ac.cam.cares.jps.powsys.electricalnetwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Property;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.JenaHelper;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.powsys.listener.LocalOntologyModelManager;

@WebServlet(urlPatterns = {"/AggregationEmissionAgent/aggregateemission"})
public class AggregationEmissionAgent extends JPSHttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID =  6859324316966357379L;;
    private static final String EM_RATE = "_EmissionRate";
    //both only called by front end javascript; update to chimney, then query to sum to give to front end

    String genInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
            + "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
            + "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
            + "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
            + "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
            + "PREFIX j9:<http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_performance.owl#> "
            + "PREFIX technical_system:<http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#> "
            + "SELECT ?entity ?V_Actual_CO2_Emission ?V_Design_CO2_Emission ?plant "

            + "WHERE {?entity  a  j1:PowerGenerator  ."
            + "?entity   j2:isSubsystemOf ?plant ." // plant
            + "?entity   technical_system:realizes ?generation ."
            + "?generation j9:hasEmission ?emission ."

            + "?emission a j9:Actual_CO2_Emission ."
            + "?emission   j2:hasValue ?valueemission ."
            + "?valueemission   j2:numericalValue ?V_Actual_CO2_Emission ." //


            + "?generation j9:hasEmission ?v_emission ."
            + "?v_emission a j9:CO2_emission ."
            + "?v_emission   j2:hasValue ?valueemission_d ."
            + "?valueemission_d   j2:numericalValue ?V_Design_CO2_Emission ." //
			+ "Filter (?V_Actual_CO2_Emission > 0.0 && ?V_Design_CO2_Emission > 0.0) ." //eliminate generator with 0 emission
            + "}";

    String plantInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#> "
            + "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
            + "PREFIX j3:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/plant.owl#> "
            + "SELECT ?chimney "
            + "WHERE {?entity  a  j1:PowerPlant  ."
            + "?entity   j2:hasSubsystem ?chimney ."
           // + "?chimney  a j3:Pipe ."
            + "}";
    
    String chimneyiriInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#> "
            + "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
            + "PREFIX j3:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/plant.owl#> "
            + "PREFIX j4:<http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#> "
            + "PREFIX j5:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
            + "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/chemical_process_system.owl#> "
            + "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
            + "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/material.owl#> "
            + "PREFIX j9:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
            + "SELECT ?vheightchimney ?vdiameterchimney ?vmassf ?vtemp ?vdens "
            + "WHERE {?entity  a  j3:Pipe  ."
            + "?entity   j3:hasHeight ?heightchimney ."
            + "?heightchimney  j2:hasValue ?vheightchimney ."
            + "?entity   j3:hasInsideDiameter ?diameterchimney ."
            + "?diameterchimney  j2:hasValue ?vdiameterchimney ."
            + "?entity   j4:realizes ?proc ."
            + "?proc j5:hasOutput ?waste ."
            + "?waste j6:refersToGeneralizedAmount ?genwaste ."
            + "?genwaste   j2:hasProperty ?massf ."
            + "?massf   j2:hasValue ?vmassf ."
            + "?genwaste   j2:hasSubsystem ?matamount ."
            + "?matamount   j7:refersToMaterial ?mat ."
            + "?mat   j8:thermodynamicBehavior ?thermo ."
            + "?thermo   j9:has_temperature ?temp ."
            + "?temp  j2:hasValue ?vtemp ."
            + "?thermo   j9:has_density ?dens ."
            + "?dens  j2:hasValue ?vdens ."
            
            + "}";

    public static OntModel readModelGreedy(String iriofnetwork) {
        String electricalnodeInfo = "PREFIX j1:<http://www.jparksimulator.com/ontology/ontoland/OntoLand.owl#> "
                + "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
                + "SELECT ?component "
                + "WHERE {?entity  a  j2:CompositeSystem  ." + "?entity   j2:hasSubsystem ?component ." + "}";

        QueryBroker broker = new QueryBroker();
        return broker.readModelGreedy(iriofnetwork, electricalnodeInfo);
    }


    @Override
    protected void setLogger() {
        logger = LoggerFactory.getLogger(AggregationEmissionAgent.class);
    }
    Logger logger = LoggerFactory.getLogger(AggregationEmissionAgent.class);
    @Override
    protected JSONObject processRequestParameters(JSONObject requestParams) {

        String iriofnetwork = requestParams.getString("electricalnetwork");
        JSONObject result=updateEmission(iriofnetwork);
        List<Object> chimneylist = result.getJSONArray("chimney").toList();
        List<Object> desco2list = result.getJSONArray("designemission").toList();
        double totalemissionactual=0.0;
        double totalemissiondesign=0.0;
        String parametername = "CO2"; //hard coded at the moment
        Map hmap = LocalOntologyModelManager.getSpeciesMap();
        OntModel jenaOwlModel = JenaHelper.createModel();
        for (int x=0;x<chimneylist.size();x++) {
        	String iriofchimney=chimneylist.get(x).toString();
        	System.out.println("what is iri of chimney:"+iriofchimney);
        	jenaOwlModel.read(iriofchimney);
            Individual valueofspeciesemissionrate = jenaOwlModel
                    .getIndividual(iriofchimney.split("#")[0] + "#V_" + hmap.get(parametername) + EM_RATE);
        	Double val=valueofspeciesemissionrate.getPropertyValue((Property) LocalOntologyModelManager.getConcept(LocalOntologyModelManager.CPT_NUMVAL)).asLiteral().getDouble();
        	totalemissionactual=totalemissionactual+val;
        	totalemissiondesign=totalemissiondesign+Double.valueOf(desco2list.get(x).toString());

        }
        JSONObject newresult= new JSONObject();
        newresult.put("actual",Double.toString(totalemissionactual/1000000*3600)); //from kg/s back to ton/hr
        newresult.put("design",Double.toString(totalemissiondesign));
        
        
        return newresult;
    }

    public static List<String[]> provideGenlist(String iriofnetwork) {
        String gennodeInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
                + "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
                + "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
                + "PREFIX j4:<http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#> "
                + "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
                + "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
                + "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
                + "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
                + "PREFIX j9:<http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#> "
                + "SELECT ?entity "
                + "WHERE {?entity  a  j1:PowerGenerator  ."
                + "FILTER EXISTS {?entity j2:isSubsystemOf ?plant } " //filtering gen 001 as it is slackbus
                + "}";


        OntModel model = ENAgent.readModelGreedy(iriofnetwork);
        ResultSet resultSet = JenaHelper.query(model, gennodeInfo);
        String result = JenaResultSetFormatter.convertToJSONW3CStandard(resultSet);
        String[] keys = JenaResultSetFormatter.getKeys(result);
        List<String[]> resultListfromquery = JenaResultSetFormatter.convertToListofStringArrays(result, keys);

        return resultListfromquery;
    }

    public JSONObject sumEmissionResult(String ENIRI) {
        List<String[]> genList = provideGenlist(ENIRI);
        QueryBroker broker = new QueryBroker();
        List<String> plantunique = new ArrayList<String>();
        List<String> emplantunique = new ArrayList<String>();

        for (int d = 0; d < genList.size(); d++) {
        	String result = broker.queryFile(genList.get(d)[0], genInfo);
            String[] keys = JenaResultSetFormatter.getKeys(result);
            List<String[]> resultList = JenaResultSetFormatter.convertToListofStringArrays(result, keys);
			if (resultList.size() > 0) {
				if (!plantunique.contains(resultList.get(0)[3])) { // plant=resultList.get(0)[3]
					plantunique.add(resultList.get(0)[3]);
				}
				emplantunique.add(resultList.get(0)[2] + "separate" + resultList.get(0)[1] + "separate" + resultList.get(0)[3]);
			}
        }

        int sizeofplant = plantunique.size();
        System.out.println("uniqueplantsize= " + sizeofplant);
        Double[] plantactco2 = new Double[sizeofplant];
        Double[] plantdesco2 = new Double[sizeofplant];
        int index = 0;
        for (int t = 0; t < plantunique.size(); t++) {
            plantactco2[index] = 0.0;
            plantdesco2[index] = 0.0;

            for (int x = 0; x < emplantunique.size(); x++) {
                String name = emplantunique.get(x).split("separate")[2];
                String value = emplantunique.get(x).split("separate")[1];
                String desvalue = emplantunique.get(x).split("separate")[0];

                if (name.contains(plantunique.get(t))) {
                    plantactco2[index] = plantactco2[index] + Double.valueOf(value);
                    plantdesco2[index] = plantdesco2[index] + Double.valueOf(desvalue);
                    System.out.println("name="+name);
                    System.out.println(value);
                    System.out.println(desvalue);
                }
            }

            index++;
        }

        JSONObject ans = new JSONObject();
        JSONArray plant = new JSONArray();
        JSONArray chimney = new JSONArray();
        JSONArray emission = new JSONArray();
        JSONArray desemission = new JSONArray();
        for (int f = 0; f < plantunique.size(); f++) {
            String result = broker.queryFile(plantunique.get(f), plantInfo);
            System.out.println("filequery= "+plantunique.get(f));
            String[] keys = JenaResultSetFormatter.getKeys(result);
            List<String[]> resultList = JenaResultSetFormatter.convertToListofStringArrays(result, keys);
            String iriofchimney;
            if(resultList.size()>0) {
            	iriofchimney=resultList.get(0)[0];
            }
            else {
            	String plantname=plantunique.get(f).split("#")[1];
            	iriofchimney="http://www.theworldavatar.com/kb/powerplants/"+plantname+"/Chimney-001.owl#Chimney-001";
            	//iriofchimney= QueryBroker.getIriPrefix() + "/powerplants/"+plantname+"/Chimney-001.owl#Chimney-001";
            	String sparqlStart = "PREFIX OCPSYST:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> \r\n"
    					+ "INSERT DATA { \r\n";
    			StringBuffer b = new StringBuffer();
    			b.append("<" + plantunique.get(f) + "> OCPSYST:hasSubsystem <" + iriofchimney + "> . \r\n");

    			String sparql = sparqlStart + b.toString() + "} \r\n";
    			new QueryBroker().updateFile(plantunique.get(f), sparql);
    			b = new StringBuffer();
            }
			if (!AgentLocator.isJPSRunningForTest()) {
				chimney.put(iriofchimney);
			} else {
				chimney.put("http://localhost:8080/kb" + iriofchimney.split("kb")[1]);
			}
            
            
            plant.put(plantunique.get(f));
            emission.put(plantactco2[f]);
            desemission.put(plantdesco2[f]);
        }
        ans.put("plant", plant);
        ans.put("emission", emission);
        ans.put("designemission", desemission);
        ans.put("chimney", chimney);

//		System.out.println(plantunique.get(2));
//		System.out.println("total actco2 for plant 1= " + plantactco2[2]);

        return ans;
    }


    public JSONObject updateEmission(String ENIRI) {//read from the generator to write it to chimney 
    	String chimneyiriName = null;
        
    	JSONObject ans = sumEmissionResult(ENIRI);
        List<Object> chimneylist = ans.getJSONArray("chimney").toList();
        List<Object> emissionlist = ans.getJSONArray("emission").toList();
        int size = chimneylist.size();
        for (int d = 0; d < size; d++) {
			try {
				chimneyiriName=chimneylist.get(d).toString();
				OntModel jenaOwlModel = LocalOntologyModelManager.createChimneyModelForChimneyIRI(chimneyiriName);
				startConversion(jenaOwlModel, chimneyiriName, emissionlist.get(d).toString());
			} catch (IOException e) {
				throw new JPSRuntimeException(e);
			}
        }
		return ans;


    }

    private void startConversion(OntModel jenaOwlModel, String iriOfChimney, String emission)
            throws IOException {
        doConversion(jenaOwlModel, iriOfChimney, emission);
        
        // save the updated model
        LocalOntologyModelManager.saveToOwl(jenaOwlModel, iriOfChimney); // for each owl file

    }

    private void doConversion(OntModel jenaOwlModel, String iriofchimney, String emission) throws JSONException {
    	
    	/**asumption data added to be used by adms (https://www.steelcon.com/en/soedertaejle/):
    	 * diameter=3m
    	 * height= 110m 
    	 * Tout=587oC (from the wiki ccgt)
    	 * density=1.225 kg/m3 (near air)
    	 * efflux rate= 210.3 kg/s (after rough calculation from the wikipedia formula)
    	 */
    	
    	
    	
        Map hmap = LocalOntologyModelManager.getSpeciesMap();
        //reset all the emission rate to be zero
        for (int b = 0; b < hmap.size(); b++) {
            String ks = (String) hmap.get(hmap.keySet().toArray()[b].toString());
            Individual valueofspeciesemissionrate = jenaOwlModel.getIndividual(iriofchimney.split("#")[0] + "#V_" + ks + EM_RATE);
            valueofspeciesemissionrate.setPropertyValue((Property) LocalOntologyModelManager.getConcept(LocalOntologyModelManager.CPT_NUMVAL),
                    jenaOwlModel.createTypedLiteral(Double.valueOf(0)));
        }

      //update the necessary values needed
        String parametername = "CO2"; //hard coded at the moment
        
        Double parametervalue = Double.valueOf(emission)*1000000/3600; //ton per hour to g/s
        if (hmap.get(parametername) != null) {
            Individual valueofspeciesemissionrate = jenaOwlModel
                    .getIndividual(iriofchimney.split("#")[0] + "#V_" + hmap.get(parametername) + EM_RATE);
            valueofspeciesemissionrate.setPropertyValue(
                    (Property) LocalOntologyModelManager.getConcept(LocalOntologyModelManager.CPT_NUMVAL),
                    jenaOwlModel.createTypedLiteral(parametervalue));
        }

        ResultSet resultSet = JenaHelper.query(jenaOwlModel, chimneyiriInfo);
		String result = JenaResultSetFormatter.convertToJSONW3CStandard(resultSet);
		String[] keys = JenaResultSetFormatter.getKeys(result);
		List<String[]> resultListfromquery = JenaResultSetFormatter.convertToListofStringArrays(result, keys);
		DatatypeProperty numval = jenaOwlModel.getDatatypeProperty(
				"http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#numericalValue");
		Individual vheightchimney = jenaOwlModel.getIndividual(resultListfromquery.get(0)[0]);
		vheightchimney.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(new Double(110.0)));
		Individual vdiameterchimney = jenaOwlModel.getIndividual(resultListfromquery.get(0)[1]);
		vdiameterchimney.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(new Double(3.0)));
		Individual vmassf = jenaOwlModel.getIndividual(resultListfromquery.get(0)[2]);
		vmassf.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(new Double(210.3)));
		Individual vtemp = jenaOwlModel.getIndividual(resultListfromquery.get(0)[3]);
		vtemp.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(new Double(587.0)));
		Individual vdens = jenaOwlModel.getIndividual(resultListfromquery.get(0)[4]);
		vdens.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(new Double(1.225)));
		
		

    }

}
