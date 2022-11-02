package uk.ac.cam.cares.derivation.example;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.ws.rs.BadRequestException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import uk.ac.cam.cares.jps.base.agent.DerivationAgent;
import uk.ac.cam.cares.jps.base.derivation.DerivationClient;
import uk.ac.cam.cares.jps.base.derivation.DerivationInputs;
import uk.ac.cam.cares.jps.base.derivation.DerivationOutputs;
import uk.ac.cam.cares.jps.base.interfaces.TripleStoreClientInterface;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

/**
 * this agent queries the maximum value from an input time series table using
 * TimeSeriesClient and writes a new MaxValue instance in the KG
 * In the HTTP response, it writes the newly created instances so that the
 * DerivationClient knows what instances to link
 * 
 * @author Kok Foong Lee
 * @author Jiaru Bai
 *
 */
@WebServlet(urlPatterns = {MaxValueAgent.URL_MAXVALUE})
public class MaxValueAgent extends DerivationAgent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String URL_MAXVALUE = "/MaxValueAgent";
	private static final Logger LOGGER = LogManager.getLogger(MinValueAgent.class);
	
	TripleStoreClientInterface storeClient;
	SparqlClient sparqlClient;

	public MaxValueAgent() {
		LOGGER.info("MaxValueAgent is initialised.");
	}

	@Override
	public void processRequestParameters(DerivationInputs derivationInputs, DerivationOutputs derivationOutputs) {
		LOGGER.info("Received request: " + derivationInputs.toString());
    	
    	if (validateInput(derivationInputs, sparqlClient)) {
    		String inputdata_iri = derivationInputs.getIris(SparqlClient.getRdfTypeString(SparqlClient.InputData)).get(0);
    		
    		// query from RDB using TimeSeries Client
    		TimeSeriesClient<Instant> tsClient = new TimeSeriesClient<Instant>(storeClient, Instant.class, Config.dburl, Config.dbuser, Config.dbpassword);
    		Integer maxvalue = (int) tsClient.getMaxValue(inputdata_iri);
			
			// write the output triples to derivationOutputs
			String max_iri = SparqlClient.namespace + UUID.randomUUID().toString();
			derivationOutputs.createNewEntity(max_iri, SparqlClient.getRdfTypeString(SparqlClient.MaxValue));
			derivationOutputs.addTriple(max_iri, RDF.TYPE.toString(), OWL.NAMEDINDIVIDUAL.toString());
			String value_iri = SparqlClient.namespace + UUID.randomUUID().toString();
			derivationOutputs.createNewEntity(value_iri, SparqlClient.getRdfTypeString(SparqlClient.ScalarValue));
			derivationOutputs.addTriple(sparqlClient.addValueInstance(max_iri, value_iri, maxvalue));
			LOGGER.info(
					"Created a new max value instance <" + max_iri + ">, and its value instance <" + value_iri + ">");
    	} else {
			throw new BadRequestException("Input validation failed.");
		}
	}
	
	private boolean validateInput(DerivationInputs derivationInputs, SparqlClient sparqlClient) {
		boolean valid = false;
        LOGGER.debug("Checking input for MaxValue agent");
		
		List<String> inputData = derivationInputs.getIris(SparqlClient.getRdfTypeString(SparqlClient.InputData));
		if (inputData.size() == 1) {
			String inputDataIri = inputData.get(0);
			if (sparqlClient.isInputData(inputDataIri)) {
				valid = true;
			} else {
				throw new BadRequestException("Incorrect rdf:type for input");
			}
		} else {
			throw new BadRequestException("Incorrect number of inputs");
		}
		
		return valid;
	}

	@Override
	public void init() throws ServletException {
		// initialise all clients
		Config.initProperties();
		this.storeClient = new RemoteStoreClient(Config.kgurl, Config.kgurl, Config.kguser, Config.kgpassword);
		this.sparqlClient = new SparqlClient(this.storeClient);
		super.devClient = new DerivationClient(this.storeClient, InitialiseInstances.derivationInstanceBaseURL);
	}
}
