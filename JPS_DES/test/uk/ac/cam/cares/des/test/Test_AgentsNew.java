package uk.ac.cam.cares.des.test;

import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.json.JSONObject;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.des.n.CommercialAgent;
import uk.ac.cam.cares.jps.des.n.DESAgentNew;
import uk.ac.cam.cares.jps.des.n.IndustrialAgent;
import uk.ac.cam.cares.jps.des.n.ResidentialAgent;
import uk.ac.cam.cares.jps.des.n.SolarAgent;

public class Test_AgentsNew extends TestCase{
	/** tests if Residential Agent calls successfully. 
	 * Residential Agent requires caresjpsutil library. 
	 * 
	 */
	public void testResidentialAgent() {
		String iriofnetworkdistrict =  "http://www.theworldavatar.com/kb/sgp/singapore/District-001.owl#District-001";
		new ResidentialAgent().extractResidentialData(iriofnetworkdistrict,"C:\\JPS_DATA\\workingdir\\JPS_SCENARIO\\scenario\\DESTest");
		try {
			String result = new DESAgentNew().runPythonScript("residential.py", "C:\\JPS_DATA\\workingdir\\JPS_SCENARIO\\scenario\\DESTest");
			System.out.println(result);
			}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	/** test Residential agent calls through Agent successfully
	 * dumps result in JPS Scenarios folder
	 */
	public void testResidentialAgentCaller() {
		JSONObject jo = new JSONObject().put("district", "http://www.theworldavatar.com/kb/sgp/singapore/District-001.owl#District-001");
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_DES/ResidentialAgent", jo.toString());
		assertNotNull(resultStart);
	}
	/** tests of Commercial Agent runs successfully
	 * 
	 */
	public void testCommercialAgent() {
		 String irioftempF="http://www.theworldavatar.com/kb/sgp/singapore/SGTemperatureForecast-001.owl#SGTemperatureForecast-001";
	     String iriofirrF="http://www.theworldavatar.com/kb/sgp/singapore/SGSolarIrradiationForecast-001.owl#SGSolarIrradiationForecast-001";
	    String baseUrl = "C:\\JPS_DATA\\workingdir\\JPS_SCENARIO\\scenario\\DESTest\\commercial";
	     new DESAgentNew().queryForIrradTemp(irioftempF,iriofirrF, baseUrl);
		String iriofnetwork = "http://www.theworldavatar.com/kb/sgp/singapore/singaporeelectricalnetwork/SingaporeElectricalNetwork.owl#SingaporeElectricalNetwork";
		new CommercialAgent().queryForBuildingConstants(iriofnetwork, baseUrl);
        try {
			String result = new DESAgentNew().runPythonScript("commercial.py", "C:\\JPS_DATA\\workingdir\\JPS_SCENARIO\\scenario\\DESTest\\commercial");
			System.out.println(result);
			}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	/** test Commercial agent calls through Agent successfully
	 * dumps result in JPS Scenarios folder
	 */
	public void testCommercialAgentCaller() {
		JSONObject jo = new JSONObject().put("electricalnetwork", "http://www.theworldavatar.com/kb/sgp/singapore/singaporeelectricalnetwork/SingaporeElectricalNetwork.owl#SingaporeElectricalNetwork");
		jo.put("temperatureforecast", "http://www.theworldavatar.com/kb/sgp/singapore/SGTemperatureForecast-001.owl#SGTemperatureForecast-001");
		jo.put("irradiationforecast", "http://www.theworldavatar.com/kb/sgp/singapore/SGSolarIrradiationForecast-001.owl#SGSolarIrradiationForecast-001");
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_DES/CommercialAgent", jo.toString());
		assertNotNull(resultStart);
	}
	/** tests if Industrial Agent calls successfully. 
	 * Industrial Agent requires caresjpsutil library. 
	 * 
	 */
	public void testIndustrialAgent() {
	    String baseUrl = "C:\\JPS_DATA\\workingdir\\JPS_SCENARIO\\scenario\\DESTest\\industrial";
		String iriofnetwork = "http://www.theworldavatar.com/kb/sgp/singapore/singaporeelectricalnetwork/SingaporeElectricalNetwork.owl#SingaporeElectricalNetwork";
		String irioftempF="http://www.theworldavatar.com/kb/sgp/singapore/SGTemperatureForecast-001.owl#SGTemperatureForecast-001";
	    String iriofirrF="http://www.theworldavatar.com/kb/sgp/singapore/SGSolarIrradiationForecast-001.owl#SGSolarIrradiationForecast-001";
	    
		new DESAgentNew().queryForIrradTemp(irioftempF,iriofirrF, baseUrl);
        
        OntModel model = DESAgentNew.readModelGreedy(iriofnetwork);
		IndustrialAgent ic = new IndustrialAgent();
		ic.queryForChemicalConstants(model, baseUrl);
        ic.queryForFuelCellConstants(model, baseUrl);
		
       try {
			String result = new DESAgentNew().runPythonScript("industrial.py", baseUrl);
			System.out.println(result);
			}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	/** test Industrial agent calls through Agent successfully
	 * dumps result in JPS Scenarios folder
	 */
	public void testIndustrialAgentCaller() {
		JSONObject jo = new JSONObject().put("electricalnetwork", "http://www.theworldavatar.com/kb/sgp/singapore/singaporeelectricalnetwork/SingaporeElectricalNetwork.owl#SingaporeElectricalNetwork");
		jo.put("temperatureforecast", "http://www.theworldavatar.com/kb/sgp/singapore/SGTemperatureForecast-001.owl#SGTemperatureForecast-001");
		jo.put("irradiationforecast", "http://www.theworldavatar.com/kb/sgp/singapore/SGSolarIrradiationForecast-001.owl#SGSolarIrradiationForecast-001");
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_DES/IndustrialAgent", jo.toString());
		assertNotNull(resultStart);
	}
	/** tests if Solar Radiation Agent calls successfully. 
	 * Solar Radiation Agent requires caresjpsutil library. 
	 *  
	 */
	public void testSolarAgent() {
		SolarAgent sa = new SolarAgent();
		String iriofnetwork = "http://www.theworldavatar.com/kb/sgp/singapore/singaporeelectricalnetwork/SingaporeElectricalNetwork.owl#SingaporeElectricalNetwork";
		String baseUrl = "C:\\JPS_DATA\\workingdir\\JPS_SCENARIO\\scenario\\DESTest\\solar";
		String irioftempF="http://www.theworldavatar.com/kb/sgp/singapore/SGTemperatureForecast-001.owl#SGTemperatureForecast-001";
	    String iriofirrF="http://www.theworldavatar.com/kb/sgp/singapore/SGSolarIrradiationForecast-001.owl#SGSolarIrradiationForecast-001";
	    
		new DESAgentNew().queryForIrradTemp(irioftempF,iriofirrF, baseUrl);
        
		OntModel model = DESAgentNew.readModelGreedy(iriofnetwork);
		sa.provideGenlist(model, baseUrl);
		 try {
				String result = new DESAgentNew().runPythonScript("solarRadiation.py", baseUrl);
				System.out.println(result);
				}
			catch (Exception ex) {
				ex.printStackTrace();
			}
	}
	/** test Solar agent calls through Agent successfully
	 * dumps result in JPS Scenarios folder
	 */
	public void testSolarAgentCaller() {
		JSONObject jo = new JSONObject().put("electricalnetwork", "http://www.theworldavatar.com/kb/sgp/singapore/singaporeelectricalnetwork/SingaporeElectricalNetwork.owl#SingaporeElectricalNetwork");
		jo.put("temperatureforecast", "http://www.theworldavatar.com/kb/sgp/singapore/SGTemperatureForecast-001.owl#SGTemperatureForecast-001");
		jo.put("irradiationforecast", "http://www.theworldavatar.com/kb/sgp/singapore/SGSolarIrradiationForecast-001.owl#SGSolarIrradiationForecast-001");
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_DES/SolarAgent", jo.toString());
		assertNotNull(resultStart);
	}
	public void testSystemAgent() {
		try {
			String irioftempF="http://www.theworldavatar.com/kb/sgp/singapore/SGTemperatureForecast-001.owl#SGTemperatureForecast-001";
		    String iriofirrF="http://www.theworldavatar.com/kb/sgp/singapore/SGSolarIrradiationForecast-001.owl#SGSolarIrradiationForecast-001";
		    String iriofnetworkdistrict =  "http://www.theworldavatar.com/kb/sgp/singapore/District-001.owl#District-001";
		    new ResidentialAgent().extractResidentialData(iriofnetworkdistrict,"C:\\JPS_DATA\\workingdir\\JPS_SCENARIO\\scenario\\DESTest\\Overall");
				 
			new DESAgentNew().queryForIrradTemp(irioftempF,iriofirrF, "C:\\JPS_DATA\\workingdir\\JPS_SCENARIO\\scenario\\DESTest\\Overall");
			String result = new DESAgentNew().runPythonScript("system.py", "C:\\JPS_DATA\\workingdir\\JPS_SCENARIO\\scenario\\DESTest\\Overall");
			System.out.println(result);
			}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
