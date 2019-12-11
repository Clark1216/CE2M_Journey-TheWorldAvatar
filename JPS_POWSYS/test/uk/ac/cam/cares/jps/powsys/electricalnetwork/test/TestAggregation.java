package uk.ac.cam.cares.jps.powsys.electricalnetwork.test;

import org.json.JSONObject;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.scenario.BucketHelper;
import uk.ac.cam.cares.jps.base.scenario.JPSContext;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.powsys.electricalnetwork.AggregationEmissionAgent;

public class TestAggregation extends TestCase{

	public void testsumagg() {
		JSONObject x= new AggregationEmissionAgent().sumEmissionResult("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork");
		int size=x.getJSONArray("plant").length();
		System.out.println(x.getJSONArray("plant").get(2));
		System.out.println("total actco2 for plant 1= " + x.getJSONArray("emission").getDouble(2));
		if(x.getJSONArray("plant").get(2).toString().contains("http://www.theworldavatar.com/kb/powerplants/PowerSeraya_Pulau_Seraya_CCGT_Cogen_Power_Plant_Singapore.owl#PowerSeraya_Pulau_Seraya_CCGT_Cogen_Power_Plant_Singapore")){
			System.out.println(("goes here!!"));
			assertEquals(131.04400696870363, x.getJSONArray("emission").getDouble(2), 0.1);
			
		}
	}
	
	public void testfullfunction() {
		JSONObject jo = new JSONObject();
		jo.put("electricalnetwork", TestEN.ELECTRICAL_NETWORK);
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/AggregationEmissionAgent/aggregateemission", jo.toString());
		System.out.println("result end="+resultStart);
	}

	
	public void testfullfunctionwithscenario() { //keep in mind to change the availble scenario first to check this
		JSONObject jo = new JSONObject();
		jo.put("electricalnetwork", TestEN.ELECTRICAL_NETWORK);
		//String scenarioUrl = BucketHelper.getScenarioUrl("testPOWSYSNuclearStartSimulationAndProcessResultAgentCallForTestScenario10");
		String scenarioUrl = BucketHelper.getScenarioUrl("testPOWSYSNuclearStartSimulationAndProcessResultAgentCallForTestScenario10updatenewload2");
		JPSContext.putScenarioUrl(jo, scenarioUrl);
		String usecaseUrl = BucketHelper.getUsecaseUrl(scenarioUrl);
		JPSContext.putUsecaseUrl(jo, usecaseUrl);
		JPSHttpServlet.enableScenario(scenarioUrl, usecaseUrl);	
		
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/AggregationEmissionAgent/aggregateemission", jo.toString());
		System.out.println("result end="+resultStart);
	}
	
//	public void testupdatefunction() {
//        LocalOntologyModelManager lomm = new LocalOntologyModelManager();
//        ServletConfig ctx = ServletTestHelper.getServletConfig();
//        ServletContextEvent event = new ServletContextEvent(ctx.getServletContext());
//        //Test Initialisation: baseChimney, species & concepts loaded
//        try {
//            lomm.contextInitialized(event);
//        } catch (Exception e) {
//            throw new JPSRuntimeException(e);
//        } finally {
//		JSONObject jo = new AggregationEmissionAgent().updateEmission(TestEN.ELECTRICAL_NETWORK);
//		System.out.println("result end="+jo.toString());
//        }
//	}
	
	
}
