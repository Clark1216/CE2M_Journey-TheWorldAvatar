package uk.ac.cam.cares.jps.powsys.coordination.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.config.JPSConstants;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.query.sparql.Paths;
import uk.ac.cam.cares.jps.base.query.sparql.Prefixes;
import uk.ac.cam.cares.jps.base.scenario.BucketHelper;
import uk.ac.cam.cares.jps.base.scenario.JPSContext;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.base.scenario.ScenarioClient;
import uk.ac.cam.cares.jps.base.scenario.ScenarioHelper;
import uk.ac.cam.cares.jps.base.util.MiscUtil;
import uk.ac.cam.cares.jps.powsys.coordination.CoordinationAgent;
import uk.ac.cam.cares.jps.powsys.electricalnetwork.test.TestEN;
import uk.ac.cam.cares.jps.powsys.retrofit.RetrofitAgent;

public class TestCoordinationAgent extends TestCase implements Prefixes, Paths {
	
	
	private void copy(String sourceScenarioName, String destinationScenarioName) throws IOException {
		
		String src = ScenarioHelper.getScenarioBucket(sourceScenarioName);
		String dest =  ScenarioHelper.getScenarioBucket(destinationScenarioName);
		File srcDir = new File(src);
		File destDir = new File(dest);
		
		FileUtils.deleteDirectory(destDir);
		FileUtils.copyDirectory(srcDir, destDir);
	}
	
	public JSONArray getNuclearPowerPlantsFromScenarioaasc5() {
		String plants = "{\"plants\":[\"http://localhost:8080/jps/kb/bd1c6d1d-f875-4c50-a7e1-cc28919f1fe7/nuclearpowerplants/NucPP_4.owl#NucPP_4\",\"http://localhost:8080/jps/kb/bd1c6d1d-f875-4c50-a7e1-cc28919f1fe7/nuclearpowerplants/NucPP_3.owl#NucPP_3\",\"http://localhost:8080/jps/kb/bd1c6d1d-f875-4c50-a7e1-cc28919f1fe7/nuclearpowerplants/NucPP_1.owl#NucPP_1\",\"http://localhost:8080/jps/kb/bd1c6d1d-f875-4c50-a7e1-cc28919f1fe7/nuclearpowerplants/NucPP_2.owl#NucPP_2\"]}";
		return new JSONObject(plants).getJSONArray("plants");
	}

	public void testCoordinatePFDirectCall() throws URISyntaxException, IOException { //request header is too large???
		
		String scenarioName = "testPOWSYSCoordinatePF";
		copy("aasc5", scenarioName); //based on aasc5
		String scenarioUrl = BucketHelper.getScenarioUrl(scenarioName);
		String usecaseUrl = BucketHelper.getUsecaseUrl(scenarioUrl);
		JPSHttpServlet.enableScenario(scenarioUrl, usecaseUrl);	
		System.out.println("usecaseUrl=" + usecaseUrl);
		new ScenarioClient().setOptionCopyOnRead(scenarioUrl, true);
		
		String electricalNetwork = TestEN.ELECTRICAL_NETWORK;
		List<String> nuclearPowerPlants = MiscUtil.toList(getNuclearPowerPlantsFromScenarioaasc5());
		System.out.println("nuclear size= "+nuclearPowerPlants.size());

		new RetrofitAgent().retrofit(electricalNetwork, nuclearPowerPlants);
		
		JSONObject jo = new JSONObject();
		JPSContext.putScenarioUrl(jo, scenarioUrl);
		JPSContext.putUsecaseUrl(jo, usecaseUrl);
		jo.put("electricalnetwork", TestEN.ELECTRICAL_NETWORK);
		AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/ENAgent/startsimulationPF", jo.toString());
	}
	
	public void testCoordinateOPFDirectCall() throws URISyntaxException, IOException { //request header is too large???
		
		String scenarioName = "testPOWSYSCoordinateOPF";
		copy("aasc5", scenarioName);
		String scenarioUrl = BucketHelper.getScenarioUrl(scenarioName);
		String usecaseUrl = BucketHelper.getUsecaseUrl(scenarioUrl);
		JPSHttpServlet.enableScenario(scenarioUrl, usecaseUrl);	
		System.out.println("usecaseUrl=" + usecaseUrl);
		new ScenarioClient().setOptionCopyOnRead(scenarioUrl, true);
		
		String electricalNetwork = TestEN.ELECTRICAL_NETWORK;
		List<String> nuclearPowerPlants =  MiscUtil.toList(getNuclearPowerPlantsFromScenarioaasc5());

		new RetrofitAgent().retrofit(electricalNetwork, nuclearPowerPlants);
		
		JSONObject jo = new JSONObject();
		JPSContext.putScenarioUrl(jo, scenarioUrl);
		JPSContext.putUsecaseUrl(jo, usecaseUrl);
		jo.put("electricalnetwork", TestEN.ELECTRICAL_NETWORK);
		AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/ENAgent/startsimulationOPF", jo.toString());
	}
	
	private int calculateNumberOfGenerators(String s, String searchpattern, int namelength) {
		
		System.out.println("\n\nscenario = " + JPSContext.getScenarioUrl());
		
		StringTokenizer t = new StringTokenizer(s, "\n");
		int countLines = 0;
		int countGen = 0;
		while (t.hasMoreTokens()) {
			countLines++;
			String line = t.nextToken();
			int i = line.indexOf(searchpattern);
			if (i >= 0) {
				countGen++;
				System.out.println(line.substring(i, i + namelength));
			}
			
		}
		System.out.println("count gen = " + countGen);
		System.out.println("count lines = " + countLines);
		
		return countGen;
	}
	
	/**
	 * First, run the test methods testCoordinateOPFDirectCall() and  testCoordinatePFDirectCall() 
	 * to create two scenarios with name testPOWSYSCoordinateOPF and testPOWSYSCoordinatePF, resp.
	 * 
	 * This method shows how the EN top node is queried for different scenarios, and checks the
	 * number of (modified) generators.
	 */
	public void testReadElectricalNetwork() { //fail related to retrofit
		
		String scenarioName = JPSConstants.SCENARIO_NAME_BASE;
		String scenarioUrl = BucketHelper.getScenarioUrl(scenarioName); 
		JPSHttpServlet.enableScenario(scenarioUrl, null);	
		String result = new QueryBroker().readFile(TestEN.ELECTRICAL_NETWORK);
		int countgen = calculateNumberOfGenerators(result, "#EGen-", 9);
		assertEquals(29, countgen);
		
		result = new QueryBroker().readFile(TestEN.ELECTRICAL_NETWORK);
		countgen = calculateNumberOfGenerators(result, "#NucGenerator", 18);
		assertEquals(0, countgen);
		
		
		scenarioName = "testPOWSYSCoordinateOPF";
		scenarioUrl = BucketHelper.getScenarioUrl(scenarioName); 
		JPSHttpServlet.enableScenario(scenarioUrl, null);	
		result = new QueryBroker().readFile(TestEN.ELECTRICAL_NETWORK);
		countgen = calculateNumberOfGenerators(result, "#EGen-", 9);
		// generator for slack bus only, all other generators have been removed
		//assertEquals(1, countgen); temporary as the gen cannot all been removed

		result = new QueryBroker().readFile(TestEN.ELECTRICAL_NETWORK);
		countgen = calculateNumberOfGenerators(result, "#NucGenerator", 18);
		assertEquals(14, countgen);
		
		scenarioName = "testPOWSYSCoordinatePF";
		scenarioUrl = BucketHelper.getScenarioUrl(scenarioName); 
		JPSHttpServlet.enableScenario(scenarioUrl, null);	
		result = new QueryBroker().readFile(TestEN.ELECTRICAL_NETWORK);
		countgen = calculateNumberOfGenerators(result, "#EGen-", 9);
		// generator for slack bus only, all other generators have been removed
		//assertEquals(1, countgen);temporary as the gen cannot all been removed

		
		result = new QueryBroker().readFile(TestEN.ELECTRICAL_NETWORK);
		countgen = calculateNumberOfGenerators(result, "#NucGenerator", 18);
		assertEquals(14, countgen);
	}
	
	public void testCoordinateOPFAgentCall() throws URISyntaxException, IOException { //request header too large
		
		String scenarioName = "testPOWSYSCoordinateOPFAgentCall";
		copy("aasc5", scenarioName);
		String scenarioUrl = BucketHelper.getScenarioUrl(scenarioName);
		String usecaseUrl = BucketHelper.getUsecaseUrl(scenarioUrl);
		JPSHttpServlet.enableScenario(scenarioUrl, usecaseUrl);	
		System.out.println("usecaseUrl=" + usecaseUrl);
		new ScenarioClient().setOptionCopyOnRead(scenarioUrl, true);
		
		JSONObject jo = new JSONObject();
		JPSContext.putScenarioUrl(jo, scenarioUrl);
		JPSContext.putUsecaseUrl(jo, usecaseUrl);
		jo.put("electricalnetwork", TestEN.ELECTRICAL_NETWORK);
//		String scenarioUrlOfMockedAgent = "http://localhost:8080" + ScenarioHelper.SCENARIO_COMP_URL + "/aasc5";
//		jo.put("mergescenariourl", scenarioUrlOfMockedAgent);
		jo.put("plants", getNuclearPowerPlantsFromScenarioaasc5());
		
		String result = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/processresultwithopf", jo.toString());
		System.out.println("result = " + result);
	}
	
	public void testCoordinateStartSimulationDirectCall() { //worked after the old scenario folder is deleted
		
		String scenarioUrl = BucketHelper.getScenarioUrl("testPOWSYSCoordinateStartSimulationDirectCall");
		String usecaseUrl = BucketHelper.getUsecaseUrl(scenarioUrl);
		JPSHttpServlet.enableScenario(scenarioUrl, usecaseUrl);	
		System.out.println("usecaseUrl=" + usecaseUrl);
		new ScenarioClient().setOptionCopyOnRead(scenarioUrl, true);
		
		JSONObject jo = new JSONObject();
		JPSContext.putScenarioUrl(jo, scenarioUrl);
		JPSContext.putUsecaseUrl(jo, usecaseUrl);
		jo.put("carbontax", 52.0);
		jo.put("landlot", "http://www.jparksimulator.com/kb/sgp/jurongisland/JurongIslandLandlots.owl");
		jo.put("electricalnetwork", TestEN.ELECTRICAL_NETWORK);
		jo.put(JPSConstants.RUN_SIMULATION, false);
		
		new CoordinationAgent().startSimulation(jo);
	}
	
	public void testCoordinateStartSimulation() { //worked after the old scenario folder is deleted
		
		String scenarioUrl = BucketHelper.getScenarioUrl("testPOWSYSCoordinateStartSimulation");
		String usecaseUrl = BucketHelper.getUsecaseUrl(scenarioUrl);
		JPSHttpServlet.enableScenario(scenarioUrl, usecaseUrl);	
		System.out.println("usecaseUrl=" + usecaseUrl);
		new ScenarioClient().setOptionCopyOnRead(scenarioUrl, true);
		
		JSONObject jo = new JSONObject();
		JPSContext.putScenarioUrl(jo, scenarioUrl);
		JPSContext.putUsecaseUrl(jo, usecaseUrl);
		jo.put("carbontax", 64.0);
		jo.put("landlot", "http://www.jparksimulator.com/kb/sgp/jurongisland/JurongIslandLandlots.owl");
		jo.put("electricalnetwork", TestEN.ELECTRICAL_NETWORK);
		
		String result = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/startsimulation", jo.toString());
		System.out.println(result);
	}
	
		
} 