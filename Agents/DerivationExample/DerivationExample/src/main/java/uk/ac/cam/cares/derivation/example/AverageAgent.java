package uk.ac.cam.cares.derivation.example;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.ws.rs.BadRequestException;

import uk.ac.cam.cares.jps.base.agent.DerivationAgent;
import uk.ac.cam.cares.jps.base.derivation.DerivationInputs;
import uk.ac.cam.cares.jps.base.derivation.DerivationOutputs;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

/**
 * This is an example of an agent updating a derivation that has a time series data
 * The derivation needs to be initialised with createDerivationWithTimeSeries
 * It is not required to give a proper HTTP response to the DerivationClient as this agent does not write any new instances
 * @author Kok Foong Lee
 *
 */
@WebServlet(urlPatterns = {AverageAgent.URL_AVERAGE})
public class AverageAgent extends DerivationAgent {
	private static final long serialVersionUID = 1L;
	public static final String URL_AVERAGE = "/AverageAgent";

	@Override
	public DerivationOutputs processRequestParameters(DerivationInputs derivationInputs) {
		Config.initProperties();
		
		// set up remote store client to point to triple store
        RemoteStoreClient storeClient = new RemoteStoreClient(Config.kgurl,Config.kgurl,Config.kguser,Config.kgpassword);
    	SparqlClient sparqlClient = new SparqlClient(storeClient);
    	
		if (validateInput(derivationInputs, sparqlClient)) {
			String inputdata_iri = derivationInputs.getIris(SparqlClient.getRdfTypeString(SparqlClient.InputData)).get(0);
			
			if (InstancesDatabase.Average == null) {
				InstancesDatabase.Average = sparqlClient.getAverageIRI();
			}
			
			TimeSeriesClient<Instant> tsClient = new TimeSeriesClient<Instant>(storeClient, Instant.class, Config.dburl, Config.dbuser, Config.dbpassword);
			
			// get average from input
			double average = tsClient.getAverage(inputdata_iri);
			
			// update average table
			List<Instant> time_column = Arrays.asList(Instant.now());
	    	List<List<?>> values = new ArrayList<>();
	    	List<Double> value_column = Arrays.asList(average);
	    	values.add(value_column);
			TimeSeries<Instant> ts = new TimeSeries<Instant>(time_column, Arrays.asList(InstancesDatabase.Average), values);
			
			tsClient.addTimeSeriesData(ts);
			DerivationOutputs derivationOutputs = new DerivationOutputs(
				SparqlClient.getRdfTypeString(SparqlClient.Average), InstancesDatabase.Average);
			return derivationOutputs;
		} else {
			throw new BadRequestException("Input validation failed.");
		}
	}
	
	private boolean validateInput(DerivationInputs derivationInputs, SparqlClient sparqlClient) {
		boolean valid = false;

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
}
