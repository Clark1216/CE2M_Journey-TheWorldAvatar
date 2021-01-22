package uk.ac.cam.cares.jps.virtualsensor.agents;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SystemUtils;
import org.json.JSONObject;

import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.util.CommandHelper;

@WebServlet("/SpeedLoadMapAgent")
public class SpeedLoadMapAgent extends HttpServlet {
	private static final Path slmDir = Paths.get("python", "ADMS-speed-load-map");
	private static final String slmScript = "ADMS-Map-SpeedTorque-NOxSoot.py";
	private static final Path pyrelpath = SystemUtils.IS_OS_LINUX ? Paths.get("bin","python") : Paths.get("Scripts","python.exe");
	//temporary hard code
	private static final Path venvPath = Paths.get("D:","JPS","data","env",pyrelpath.toString()); 
	
	private String getSurogateValues(String inputs) {
		//@todo [AC] - detect if, python virtual environment exists in the slmDir and create it first, if necessary
		Path slmWorkingDir =  Paths.get(AgentLocator.getCurrentJpsAppDirectory(this), slmDir.toString());
		ArrayList<String> args = new ArrayList<String>();

		args.add(venvPath.toString());
		args.add(slmScript);
		args.add(inputs);

		return CommandHelper.executeCommands(slmWorkingDir.toString(), args);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		JSONObject jo = AgentCaller.readJsonParameter(request);
		JSONObject in= new JSONObject();
		/*
		 * http://betterboat.com/average-boat-speed/ assume fastest medium boat 
		 * max speed= 25knot max rpm= 2500 rpm torque=constant=250Nm then 1knot=100 rpm rpm=
		 * https://www.marineinsight.com/shipping-news/worlds-fastest-ship-built-tasmania-christened-argentinas-president/->fastest=58.1 knot
		 * knot*2500/58.1 roughly 1 ship 33 kg/h 1 boat= 1.1338650741577147e-05*3600 = 0.041
		 * kg/h NO2 (comparison of NO2
		 * https://pdfs.semanticscholar.org/1bd2/52f2ae1ede131d0ef84ee21c84a73fb6b374.pdf) 
		 * 1 boat mass flux=0.0192143028723584 kg/s 

		 */
		double valuecalc=jo.getDouble("speed")*2500/58.1;
		if(valuecalc>2500) {
			valuecalc=2500;
		}
		String type=jo.getString("type").toLowerCase();
		JSONObject speedob= new JSONObject();		
		speedob.put("value", valuecalc); //600-2500
		speedob.put("unit", "RPM");
		JSONObject torob= new JSONObject();
		torob.put("value", 250); //50-550 range
		torob.put("unit", "Nm");
		in.put("speed", speedob);
		in.put("torque", torob);

		JSONObject json = crankUpRealShipModel(type, getSurogateValues(in.toString().replace("\"", "'")));
		
		AgentCaller.writeJsonParameter(response, json);
		
	}

	private JSONObject crankUpRealShipModel(String type, String newjsonfile) {
		JSONObject json = new JSONObject(newjsonfile);
		
		// these scaling factors are purely to make the results fall within the reasonable range
		for(int gas=0;gas<json.getJSONArray("pollutants").length();gas++) {
			JSONObject pollutantmass=json.getJSONArray("pollutants").getJSONObject(gas);
			Double oldvaluemixmass= pollutantmass.getDouble("value");
			if(type.contains("cargo")) {
				pollutantmass.put("value",oldvaluemixmass*322);	
			}
			else if(type.contains("tanker")) {
				pollutantmass.put("value",oldvaluemixmass*430);	
				
			}
			else if(type.contains("container")) {
				pollutantmass.put("value",oldvaluemixmass*580);	
			}
			else if(type.contains("passenger")) {
				pollutantmass.put("value",oldvaluemixmass*697);	
			}
			else  {
				pollutantmass.put("value",oldvaluemixmass*300);	
			}
		}
		
		
		
		for(int part=0;part<json.getJSONArray("particle").length();part++) {
			JSONObject particlemass=json.getJSONArray("particle").getJSONObject(part).getJSONObject("emission_rate");
			Double oldvaluemixmass= particlemass.getDouble("value");
			JSONObject particleD=json.getJSONArray("particle").getJSONObject(part).getJSONObject("diameter");
			Double oldvaluemixD= particleD.getDouble("value");
			particleD.put("value",(double)Math.round(oldvaluemixD * 1000d) / 1000d);
			if(type.contains("cargo")) {
				
				particlemass.put("value",oldvaluemixmass*322);
			}
			else if(type.contains("tanker")) {
				particlemass.put("value",oldvaluemixmass*430);	
				
			}
			else if(type.contains("container")) {
				particlemass.put("value",oldvaluemixmass*580);	
			}
			else if(type.contains("passenger")) {
				particlemass.put("value",oldvaluemixmass*697);	
			}
			else  {
				particlemass.put("value",oldvaluemixmass*300);	
			}
		}
		
		
		
		JSONObject mixturemass=json.getJSONObject("mixture").getJSONObject("massflux");
		Double oldvaluemixmass= mixturemass.getDouble("value");
		if(type.contains("cargo")) {
			mixturemass.put("value",oldvaluemixmass*322);	
		}
		else if(type.contains("tanker")) {
			mixturemass.put("value",oldvaluemixmass*430);	
			
		}
		else if(type.contains("container")) {
			mixturemass.put("value",oldvaluemixmass*580);	
		}
		else if(type.contains("passenger")) {
			mixturemass.put("value",oldvaluemixmass*697);	
		}
		else  {
			mixturemass.put("value",oldvaluemixmass*300);	
		}
		return json;
	}
	
	
}
