package uk.ac.cam.cares.jps.wte.test;

import java.io.IOException;

import org.apache.jena.ontology.OntModel;
import org.json.JSONObject;
import org.json.JSONStringer;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.scenario.ScenarioClient;
import uk.ac.cam.cares.jps.wte.WastetoEnergyAgent;
import uk.ac.cam.cares.jps.wte.visualization.WTEVisualization;

public class TestVisualization  extends TestCase {
	public String WasteTopNode = "http://www.theworldavatar.com/kb/sgp/singapore/wastenetwork/SingaporeWasteSystem.owl#SingaporeWasteSystem";
	public void testFCQueryDirect(){
		WTEVisualization a = new WTEVisualization();
		JSONObject jo = new JSONObject();
		OntModel model = WastetoEnergyAgent.readModelGreedy(WasteTopNode);
		try {
			String g = a.createMarkers(model, jo);
			System.out.println(g);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void testFCQueryAgent(){
		JSONObject jo = new JSONObject().put("wastenetwork",
				"http://www.theworldavatar.com/kb/sgp/singapore/wastenetwork/SingaporeWasteSystem.owl#SingaporeWasteSystem");
		try {
			String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_WTE/WTEVisualization/createMarkers", jo.toString());
			System.out.println(resultStart);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void testOnsiteDirect(){ //returns null in base case because it only returns 
		// OnSiteWasteTreatment-0
		WTEVisualization a = new WTEVisualization();
		JSONObject jo = new JSONObject();
		OntModel model = WastetoEnergyAgent.readModelGreedy(WasteTopNode);
		try {
			String g = a.searchOnsite(model, jo);
			System.out.println(g);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void testOnsQueryAgent(){
		JSONObject jo = new JSONObject().put("wastenetwork",
				"http://www.theworldavatar.com/kb/sgp/singapore/wastenetwork/SingaporeWasteSystem.owl#SingaporeWasteSystem");
		try {
			String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_WTE/WTEVisualization/queryOnsite", jo.toString());
			System.out.println(resultStart);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void testreadInputsDirect(){
		WTEVisualization a = new WTEVisualization();
		OntModel model = WastetoEnergyAgent.readModelGreedy(WasteTopNode);
		String g = a.readInputs(model);
		JSONObject jo = new JSONObject(g);
		System.out.println(g);
	}
	public void testreadInputsAgent(){
		JSONObject jo = new JSONObject().put("wastenetwork",
				"http://www.theworldavatar.com/kb/sgp/singapore/wastenetwork/SingaporeWasteSystem.owl#SingaporeWasteSystem");
		try {
			String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_WTE/WTEVisualization/readInputs", jo.toString());
			System.out.println(resultStart);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void testScenarioCoordination() throws IOException { 
		String scenarioName = "testFW53228637-70fd-4332-a811-b3575b5272f9";
		String json = new JSONStringer().object()
				.key("wastenetwork").value(WasteTopNode)
				.endObject().toString();
		String result = new ScenarioClient().call(scenarioName, "http://localhost:8080/JPS_WTE/WTEVisualization/queryOnsite", json);
		System.out.println(result);
	}
}
