package uk.ac.cam.cares.jps.powsys.nuclear.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.config.JPSConstants;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.scenario.BucketHelper;
import uk.ac.cam.cares.jps.base.scenario.JPSContext;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.base.scenario.ScenarioClient;
import uk.ac.cam.cares.jps.powsys.electricalnetwork.test.TestEN;
import uk.ac.cam.cares.jps.powsys.nuclear.NuclearAgent;

public class TestNuclear extends TestCase {
	
	private JSONArray getSubstitutionalGenerators() {
		JSONArray ja = new JSONArray();
		for(int x=24;x<=29;x++) {
			String r=String.format("%03d", x);
			ja.put("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/EGen-"+r+".owl#EGen-"+r);
		}
		return ja;
	}

	public void testStartSimulationAndProcessResultDirectCallForBaseScenario() throws NumberFormatException, IOException, URISyntaxException, InterruptedException { //not tested yet
		NuclearAgent agent = new NuclearAgent();
		JSONObject jofornuc = new JSONObject();
		JSONArray ja = new JSONArray();
//		ja.put("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/EGen-006.owl#EGen-006");
//		ja.put("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/EGen-007.owl#EGen-007");
//		ja.put("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/EGen-016.owl#EGen-016");
//		ja.put("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/EGen-017.owl#EGen-017");
		jofornuc.put("substitutionalgenerators", getSubstitutionalGenerators());
		
		String lotiri = "http://www.jparksimulator.com/kb/sgp/jurongisland/JurongIslandLandlots.owl";
		String iriofnetwork = "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork";
		String dataPath = QueryBroker.getLocalDataPath();
		List<Object> listofplant=  jofornuc.getJSONArray("substitutionalgenerators").toList();
		System.out.println(dataPath);
		agent.startSimulation(lotiri, iriofnetwork,(ArrayList)listofplant, dataPath, false);
		
		// copy existing result file from a previous simulation to the data bucket 
		String source = AgentLocator.getCurrentJpsAppDirectory(this) + "/res" + "/results.csv";
		//String source = AgentLocator.getCurrentJpsAppDirectory(this) + "/res" + "/results_secondrun.csv";
		File file = new File(source);
		String destinationUrl = dataPath + "/" + NuclearAgent.AGENT_TAG + "/results.csv";
		new QueryBroker().putLocal(destinationUrl, file);
		
		List<String> result = agent.processSimulationResult(dataPath);
		System.out.println("result from processsimulationresult=" + result);
		assertEquals(4, result.size());
	}
	
	public void xxxtestProcessResultDirectCallForScenarioaasc5() throws NumberFormatException, IOException, URISyntaxException, InterruptedException {
		
		String scenarioUrl = BucketHelper.getScenarioUrl("aasc5"); 
		
		String usecaseUrl = scenarioUrl + "/kb/bd1c6d1d-f875-4c50-a7e1-cc28919f1fe7";
		
		JPSHttpServlet.enableScenario(scenarioUrl, usecaseUrl);
		
		NuclearAgent agent = new NuclearAgent();
		String dataPath = QueryBroker.getLocalDataPath();
		List<String> result = agent.processSimulationResult(dataPath);
		System.out.println("result from processsimulationresult=" + result);
		assertEquals(4, result.size());
	}
	
public void testStartSimulationAndProcessResultAgentCallForTestScenario() throws NumberFormatException, IOException, URISyntaxException, InterruptedException {
		JSONArray ja = getSubstitutionalGenerators();
		
		JSONObject jo = new JSONObject();
		jo.put("landlot", "http://www.jparksimulator.com/kb/sgp/jurongisland/JurongIslandLandlots.owl");
		jo.put("electricalnetwork", TestEN.ELECTRICAL_NETWORK);
		jo.put("substitutionalgenerators", ja);
		
		String scenarioUrl = BucketHelper.getScenarioUrl("testPOWSYSNuclearStartSimulationAndProcessResultAgentCallForTestScenarionewmod2"); //don't use the close existing scenario name again!!
		scenarioUrl = BucketHelper.getScenarioUrl("testPOWSYSNuclearStartSimulationAndProcessResultAgentCallForTestScenarionewmodloadpoint2"); //don't use the close existing scenario name again!!
		//String scenarioUrl = BucketHelper.getScenarioUrl("testPOWSYSNuclearoilremovedserver3");
		JPSHttpServlet.enableScenario(scenarioUrl);	
		
		new ScenarioClient().setOptionCopyOnRead(scenarioUrl, true);
		
		JPSContext.putScenarioUrl(jo, scenarioUrl);
		String usecaseUrl = BucketHelper.getUsecaseUrl();
		System.out.println(usecaseUrl);
		//usecaseUrl = "http://localhost:8080" + ScenarioHelper.SCENARIO_COMP_URL + "/testStartSimulationAndProcessResultAgentCallForTestScenario/kb/d9fbd6f4-9e2f-4c63-9995-9ff88ab8900e";
		JPSContext.putUsecaseUrl(jo, usecaseUrl);
		
		//jo.put(JPSConstants.RUN_SIMULATION, false);
		jo.put(JPSConstants.RUN_SIMULATION, true);
		
		JPSHttpServlet.enableScenario(scenarioUrl, usecaseUrl);	
		
		System.out.println("json input parameter=" + jo);
		// start simulation (since parameter JPSConstants.SCENARIO_USE_CASE_URL is set, GAMS is not started)
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/NuclearAgent/startsimulation", jo.toString());
		System.out.println("result from startsimulation=" + resultStart);
		
//
//		jo = new JSONObject(resultStart);
//		assertEquals(4, jo.getJSONArray("plants").length());
	}
public void testCoordinateRetroFitNuclearDirectCall() throws NumberFormatException, IOException, URISyntaxException, InterruptedException {
	JSONArray ja = getSubstitutionalGenerators();
	
	JSONObject jo = new JSONObject();
	jo.put("landlot", "http://www.jparksimulator.com/kb/sgp/jurongisland/JurongIslandLandlots.owl");
	jo.put("electricalnetwork", TestEN.ELECTRICAL_NETWORK);
	jo.put("substitutionalgenerators", ja);	
	String scenarioUrl = BucketHelper.getScenarioUrl("testPOWSYSNuclearStartSimulationAndProcessResultAgentCallForTestScenario"); 
	JPSHttpServlet.enableScenario(scenarioUrl);	
	new ScenarioClient().setOptionCopyOnRead(scenarioUrl, true);
	
	JPSContext.putScenarioUrl(jo, scenarioUrl);
	String usecaseUrl = BucketHelper.getUsecaseUrl(scenarioUrl);
	System.out.println(usecaseUrl);
	//usecaseUrl = "http://localhost:8080" + ScenarioHelper.SCENARIO_COMP_URL + "/testStartSimulationAndProcessResultAgentCallForTestScenario/kb/d9fbd6f4-9e2f-4c63-9995-9ff88ab8900e";
	
	
	System.out.println("json input parameter=" + jo);
	// start simulation (since parameter JPSConstants.SCENARIO_USE_CASE_URL is set, GAMS is not started)
	String nuclearPowerPlants= AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/NuclearAgent/startsimulation", jo.toString());
//	List<String> plants = MiscUtil.toList(new JSONObject(nuclearPowerPlants).getJSONArray("plants"));
//	System.out.println("nuclear size= "+plants.size());
//	System.out.println(ja);
//
//	new RetrofitAgent().retrofit(TestEN.ELECTRICAL_NETWORK, plants,MiscUtil.toList(ja));
	// generator for slack bus only, all other generators have been removed
	//assertEquals(1, countgen); temporary as the gen cannot all been removed

}
	public void testcallNewNuclearAgentCSVInput() throws IOException, InterruptedException, NumberFormatException, URISyntaxException {
		JSONObject result = new JSONObject();
		JSONArray ja = new JSONArray();
		ja.put("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/EGen-006.owl#EGen-006");
		ja.put("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/EGen-007.owl#EGen-007");
		ja.put("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/EGen-016.owl#EGen-016");
		ja.put("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/EGen-017.owl#EGen-017");
		result.put("substitutionalgenerators", ja);
		NuclearAgent agent = new NuclearAgent();
		
		String lotiri = "http://www.jparksimulator.com/kb/sgp/jurongisland/JurongIslandLandlots.owl";
		String iriofnetwork = "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork";
		
		ArrayList<String> listofplant= new ArrayList<String>();
		for (int c=0;c<result.getJSONArray("substitutionalgenerators").length();c++) {
			listofplant.add(result.getJSONArray("substitutionalgenerators").getString(c));
		}
		
		String dataPath = QueryBroker.getLocalDataPath();
		System.out.println("what is dataPath="+dataPath);
		//agent.startSimulation(lotiri, iriofnetwork,listofplant, dataPath, false);
		agent.prepareCSVPartialRemaining(listofplant,iriofnetwork,dataPath);
		File file = new File(dataPath+"/parameters_req_existing.csv");
		assertTrue(file.exists());

	}
	
}
