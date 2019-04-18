package uk.ac.cam.cares.jps.powsys.nuclear.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.json.JSONObject;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.config.JPSConstants;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.scenario.BucketHelper;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.powsys.nuclear.NuclearAgentScenarioCapable;

public class TestNuclearAgentScenarioCapable extends TestCase {

	public void testStartSimulationAndProcessResultDirectCallForBaseScenario() throws NumberFormatException, IOException, URISyntaxException, InterruptedException {
		//why need to convert to localhost instead of twa??
		//small scenario is auto generated
		
		NuclearAgentScenarioCapable agent = new NuclearAgentScenarioCapable();
		
		String lotiri = "http://www.theworldavatar.com/kb/sgp/jurongisland/JurongIslandLandlots.owl";
		String iriofnetwork = "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork";
		String dataPath = QueryBroker.getLocalDataPath();
		agent.startSimulation(lotiri, iriofnetwork, dataPath, false);
		
		// copy existing result file from a previous simulation to the data bucket 
		String source = AgentLocator.getCurrentJpsAppDirectory(this) + "/testres/results.csv";
		File file = new File(source);
		String destinationUrl = dataPath + "/" + NuclearAgentScenarioCapable.AGENT_TAG + "/results.csv";
		new QueryBroker().put(destinationUrl, file);
		
		List<String> result = agent.processSimulationResult(dataPath);
		System.out.println(result);
		assertEquals(4, result.size());
	}
	
	public void testStartSimulationAndProcessResultAgentCallForTestScenario() throws NumberFormatException, IOException, URISyntaxException, InterruptedException {
		
		JSONObject jo = new JSONObject();
		jo.put("landlot", "http://www.theworldavatar.com/kb/sgp/jurongisland/JurongIslandLandlots.owl");
		jo.put("electricalnetwork", "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork");
		String scenarioUrl = BucketHelper.getScenarioUrl("testStartSimulationAndProcessResultAgentCallForTestScenario"); 
		JPSHttpServlet.enableScenario(scenarioUrl);	
		jo.put(JPSConstants.SCENARIO_URL, scenarioUrl);
		String usecaseUrl = BucketHelper.getUsecaseUrl();
		//usecaseUrl = "http://localhost:8080/JPS_SCENARIO/scenario/testStartSimulationAndProcessResultAgentCallForTestScenario/kb/d9fbd6f4-9e2f-4c63-9995-9ff88ab8900e";
		jo.put(JPSConstants.SCENARIO_USE_CASE_URL,  usecaseUrl);
		jo.put(JPSConstants.RUN_SIMULATION, false);
		JPSHttpServlet.enableScenario(scenarioUrl, usecaseUrl);	
		
		System.out.println("json input parameter=" + jo);
		// start simulation (since parameter JPSConstants.SCENARIO_USE_CASE_URL is set, GAMS is not started)
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/NuclearAgent/startsimulation", jo.toString());
		System.out.println("result from startsimulation=" + resultStart);
		
		// copy existing result file from a previous simulation to the data bucket 
		String source = AgentLocator.getCurrentJpsAppDirectory(this) + "/testres/results.csv";
		File file = new File(source);
		String destinationUrl = QueryBroker.getLocalDataPath() + "/" + NuclearAgentScenarioCapable.AGENT_TAG + "/results.csv";
		new QueryBroker().put(destinationUrl, file);
		
		// process the simulation result
		jo = new JSONObject();
		jo.put(JPSConstants.SCENARIO_URL, scenarioUrl);
		jo.put(JPSConstants.SCENARIO_USE_CASE_URL,  usecaseUrl);	
		String resultProcess = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/NuclearAgent/processresult", jo.toString());
		System.out.println("result from processsimulationresult=" + resultProcess);
	}
}