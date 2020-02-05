package uk.ac.cam.cares.jps.des;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.scenario.BucketHelper;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;

@WebServlet(urlPatterns = { "/DESCoordination" })

public class DESCoordination extends JPSHttpServlet{

	private static final long serialVersionUID = 1L;

	@Override
    protected void doHttpJPS(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        logger = LoggerFactory.getLogger(DistributedEnergySystem.class);
        super.doHttpJPS(request, response);
    }

    @Override
    protected JSONObject processRequestParameters(JSONObject requestParams,HttpServletRequest request) {
    	 JSONObject responseParams = requestParams;
 			
 	        String scenarioUrl = BucketHelper.getScenarioUrl();
 	        String usecaseUrl = BucketHelper.getUsecaseUrl();
 	        logger.info("DES scenarioUrl = " + scenarioUrl + ", usecaseUrl = " + usecaseUrl);
 	        responseParams.put("baseUrl",  QueryBroker.getLocalDataPath()+"/JPS_DES");
 	        AgentCaller.executeGetWithJsonParameter("JPS_DES/GetForecastData", requestParams.toString());
 	        
 	        String t =  AgentCaller.executeGetWithJsonParameter("JPS_DES/DESAgent", requestParams.toString());
 	        responseParams = new JSONObject(t);
 	        //header's way too large so shrink it to the first element. we only need the first element
 	        JSONObject jo = new JSONObject();
 	        String[] types = {"solar", "gridsupply", "industrial", "commercial", "residential"};
 	        List<String> l = Arrays.asList(types);
 	        for (String i: l ) {
 	        	System.out.println(responseParams.get(i));
 	        	JSONArray j =  (JSONArray) responseParams.get(i);
 	        	jo.put(i,j.get(0));
 	        }
 	        System.out.println(jo);
 			String v = AgentCaller.executeGetWithJsonParameter("JPS_DES/GetBlock", jo.toString());
 			System.out.println("Called GetBlock" + v);
 			JSONObject tempJO = new JSONObject(v);
 			responseParams.put("txHash", tempJO.get("txHash"));
 			responseParams.put("sandr", tempJO.get("sandr"));
 			System.gc();
    	return responseParams;
    }

}
