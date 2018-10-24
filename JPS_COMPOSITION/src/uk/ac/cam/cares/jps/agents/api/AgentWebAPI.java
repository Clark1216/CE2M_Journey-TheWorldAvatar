package uk.ac.cam.cares.jps.agents.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import uk.ac.cam.cares.jps.agents.ontology.ServiceBuilder;
import uk.ac.cam.cares.jps.base.config.KeyValueServer;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.composition.compositionengine.ServiceCompositionEngine;
import uk.ac.cam.cares.jps.composition.enginemodel.Graph;
import uk.ac.cam.cares.jps.composition.executor.ExecutionLayer;
import uk.ac.cam.cares.jps.composition.executor.ExecutorNew;
import uk.ac.cam.cares.jps.composition.executor.ExecutorProcessor;
import uk.ac.cam.cares.jps.composition.servicemodel.Service;
import uk.ac.cam.cares.jps.composition.util.FormatTranslator;
import uk.ac.cam.cares.jps.composition.util.OptimalPathSearcher;

@WebServlet(urlPatterns = {"/execute"})
public class AgentWebAPI extends HttpServlet {

	private static final long serialVersionUID = -860332795160247610L;
	Logger logger = LoggerFactory.getLogger(AgentWebAPI.class);
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String path = req.getServletPath();
		JSONObject jo = AgentCaller.readJsonParameter(req);

		logger.info( "path=" + path + ", query=" + jo);
		
		Service compositeAgent = new ServiceBuilder()
				.operation(null, "http://www.theworldavatar.com/Composite_Service_ODsMpRv")
				.input("http://www.theworldavatar.com/ontology/ontocitygml/OntoCityGML.owl#EnvelopeType", "region")
				.input("http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/plant.owl#Plant", "plant")
				.output("https://www.w3.org/ns/csvw#Table", "dispersiongrid")
				.output("http://www.theworldavatar.com/ontology/ontocitygml/OntoCityGML.owl#BuildingType", true, "buildings", true)
				.build();
		
		String hostPort = "http://" + KeyValueServer.get("host") + ":" + KeyValueServer.get("port");
		
		try {
			String result = composeAndExecute(compositeAgent, hostPort, jo.toString());
			
			logger.info("result = " + result);
					
			AgentCaller.printToResponse(result, resp);
							
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new JPSRuntimeException(e.getMessage(), e);
		}
	}

	public Object[] compose(Service compositeAgent, String hostPort) 
			throws JsonParseException, JsonMappingException, JSONException, URISyntaxException, IOException, Exception {

		ServiceCompositionEngine engine = new ServiceCompositionEngine(compositeAgent, hostPort);
		engine.start();
		Graph graph = engine.getGraph();
		ArrayList<Service> serviceEliminationList = OptimalPathSearcher.getAllServicesToBeDeleted(graph);
		ArrayList<String> eliminationList = new ArrayList<String>();
		for (Service service : serviceEliminationList) {
			eliminationList.add(service.getUri().toASCIIString());
		}
		
		showComposedAgents(graph, eliminationList);
		
		return new Object[] {graph, eliminationList};
	}
	
	public String composeAndExecute(Service compositeAgent, String hostPort, String jsonInputValues) 
				throws JsonParseException, JsonMappingException, JSONException, URISyntaxException, IOException, Exception {
	
		Object[] result = compose(compositeAgent, hostPort);
		Graph graph = (Graph) result[0];
		ArrayList<String> eliminationList = (ArrayList<String>) result[1];
		
		ExecutorProcessor processor = new ExecutorProcessor(FormatTranslator.convertGraphJavaClassTOJSON(graph), eliminationList);
		ArrayList<ExecutionLayer> executionChain = processor.generateExecutionChain();
		ExecutorNew executor = new ExecutorNew(executionChain); 
	
		return executor.execute(new JSONObject(jsonInputValues));
	}
	
	private void showComposedAgents(Graph graph, ArrayList<String> eliminationList) {
		
		logger.info("service pool:");
		for (Service current : graph.servicePool) {
			logger.info("" + current.uri);
		}
		
		logger.info("elimination list:");
		for (String current : eliminationList) {
			logger.info(current);
		}
	}
}
