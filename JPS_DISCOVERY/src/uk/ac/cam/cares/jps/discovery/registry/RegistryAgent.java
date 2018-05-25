package uk.ac.cam.cares.jps.discovery.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.discovery.Agent;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.discovery.factory.DiscoveryFactory;

@WebServlet(urlPatterns = {"/register", "/deregister", "/agents"})
public class RegistryAgent extends HttpServlet {

	private static final long serialVersionUID = -1084832972879292460L;

	Logger logger = LoggerFactory.getLogger(RegistryAgent.class);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String path = req.getServletPath();
		logger.info("RegistryAgent is called, path = " + path);

		if ("/register".equals(path)) {
			String serializedDescr = req.getParameter("agent");
			Agent description = DiscoveryFactory.getSerializer().<Agent>convertFrom(serializedDescr, Agent.class).get();
			DiscoveryFactory.getRegistry().register(description);
		} else if ("/deregister".equals(path)) {
			String agentAddress = req.getParameter("agentname");
			DiscoveryFactory.getRegistry().deregister(agentAddress);
		} else if ("/agents".equals(path)) {
			AgentCaller.printToResponse(getAllAgentNames(), resp);
		}
	}	
	
	private List<String> getAllAgentNames() throws IOException {
		
		List<String> result = new ArrayList<String>();
		
		Collection<Agent> list = DiscoveryFactory.getRegistry().getAllAgents();
		for (Agent current : list) {
			result.add(current.getName());
		}
		
		return result;
	}
}
