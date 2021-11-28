package uk.ac.cam.cares.derivation.asynexample;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import uk.ac.cam.cares.jps.base.agent.AsynAgent;
import uk.ac.cam.cares.jps.base.derivation.DerivationClient;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.interfaces.StoreClientInterface;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;

/**
 * This difference agent takes two inputs as maximum value and minimum value and compute their difference.
 * @author Jiaru Bai (jb2197@cam.ac.uk)
 *
 */
public class DifferenceAgent extends AsynAgent {
	
	private static final Logger LOGGER = LogManager.getLogger(DifferenceAgent.class);
	
	private static final long serialVersionUID = 1L;
	
	SparqlClient sparqlClient;

	public DifferenceAgent(StoreClientInterface storeClient) {
		super(storeClient);
		this.sparqlClient = new SparqlClient(storeClient);
	}
	
	@Override
	public List<String> setupJob(JSONObject requestParams) {
		List<String> createdInstances = new ArrayList<String>();
		
		// get the input from the KG
		String maxvalue_iri = requestParams.getJSONObject(DerivationClient.AGENT_INPUT_KEY).getString(SparqlClient.MaxValue.getQueryString().replaceAll(SparqlClient.prefix+":", SparqlClient.namespace));
		String minvalue_iri = requestParams.getJSONObject(DerivationClient.AGENT_INPUT_KEY).getString(SparqlClient.MinValue.getQueryString().replaceAll(SparqlClient.prefix+":", SparqlClient.namespace));
		
		// compute difference
		Integer diff = sparqlClient.getValue(maxvalue_iri) - sparqlClient.getValue(minvalue_iri);
		
		// create new instances in KG
		createdInstances.add(sparqlClient.createDifference());
		sparqlClient.addValueInstance(createdInstances.get(0), diff);
		
		return createdInstances;
	}
	
	@Override
	public void init() throws ServletException {
		LOGGER.info("\n---------------------- Difference Agent has started ----------------------\n");
		System.out.println("\n---------------------- Difference Agent has started ----------------------\n");
		ScheduledExecutorService exeService = Executors.newSingleThreadScheduledExecutor();
		
		Config.initProperties();
		
		RemoteStoreClient kbClient = new RemoteStoreClient(Config.sparqlEndpointQuery, Config.sparqlEndpointUpdate);
		DifferenceAgent diffAgent = new DifferenceAgent(kbClient);
		
		exeService.scheduleAtFixedRate(() -> {
			try {
				diffAgent.monitorDerivation(Config.agentIriDifference);
			} catch (JPSRuntimeException e) {
				e.printStackTrace();
			}
		}, Config.initDelayAgentDifference, Config.periodAgentDifference, TimeUnit.SECONDS);
		LOGGER.info("\n---------------------- Difference Agent is monitoring derivation instance ----------------------\n");
		System.out.println("\n---------------------- Difference Agent is monitoring derivation instance ----------------------\n");
	}
}
