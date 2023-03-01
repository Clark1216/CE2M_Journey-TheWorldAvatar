package uk.ac.cam.cares.jps.powsys.retrofit.test;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.jena.ontology.OntModel;
import java.util.ArrayList;

import uk.ac.cam.cares.jps.base.query.JenaHelper;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.query.AccessAgentCaller;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

import uk.ac.cam.cares.jps.powsys.retrofit.GeneralRetrofitAgent;
import uk.ac.cam.cares.jps.powsys.retrofit.BusInfo;
import uk.ac.cam.cares.jps.powsys.retrofit.GeneratorInfo;

public class TestGeneralRetrofitAgent {
	private String jpsENIRI, testENIRI;
	private JSONArray busIRI = new JSONArray();
	private JSONArray genIRI = new JSONArray();
	private List<String[]> expectedBusQuery = new ArrayList<>();

	private <T> boolean compareQueryResult(List<T> a, List<T> b) {

	}

	@Before
	public void setUp () {
		//TODO Fill up the IRI in the variables.
	}


	@Test
	public void testQueryBuses () {
		OntModel model = JenaHelper.createModel(jpsENIRI);
		GeneralRetrofitAgent gra = new GeneralRetrofitAgent();

		//TODO get test output from testing in Blazegraph
		List<BusInfo> expected = new ArrayList<>();

		List<BusInfo> actual = gra.queryBuses(model);
		assertTrue(compareQueryResult(expected, actual));
	}
	
	@Test
	public void testDeletePowerGeneratorsFromElectricalNetwork () {
		GeneralRetrofitAgent gra = new GeneralRetrofitAgent();
		List<String> input = new ArrayList<>();
		gra.deletePowerGeneratorsFromElectricalNetwork(testENIRI, input);

		AccessAgentCaller caller = new AccessAgentCaller();
		//TODO Figure out the required query depending on testENIRI
		JSONArray result = caller.queryStore();

		//TODO get test output from testing in Blazegraph
		List<BusInfo> expected = new ArrayList<>();

		assertTrue(compareQueryResult(expected, actual));
	}

	@Test
	public void testCompletePowerGenerator () {

	}

	@Test
	public void testAddGeneratorsToElectricalNetwork () {

	}

	@Test
	public void testConnectGeneratorToOptimalBus () {

	}

	@Test
	public void testConnectNuclearPowerGeneratorsOfPlantsToOptimalBus () {

	}


}