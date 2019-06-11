package uk.ac.cam.cares.jps.misc.powerplants.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import uk.ac.cam.cares.jps.base.query.SparqlOverHttpService;
import uk.ac.cam.cares.jps.base.util.MiscUtil;


public class PowerPlantQueries {
	
	public static final String SPARQL_PREFIXES = "PREFIX : <http://www.theworldavatar.com/kb/powerplants/>\r\n"
			+ "PREFIX powerplant: <http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#>\r\n"
			+ "PREFIX system_v1: <http://www.theworldavatar.com/ontology/ontoeip/upper_level/system_v1.owl#>\r\n"
			+ "PREFIX spacetimeext: <http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#>\r\n"
			+ "PREFIX system: <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>\r\n"
			+ "PREFIX system_realization: <http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_realization.owl#>\r\n"
			+ "PREFIX system_performance: <http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_performance.owl#>\r\n"
			+ "PREFIX technical_system: <http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#>\r\n";
	
	public static final String SPARQL_PLANT = SPARQL_PREFIXES
			+ "SELECT ?emissionvaluenum \r\n" 
			+ "WHERE {\r\n"
			+ "<%s> technical_system:realizes ?generation . ?generation system_performance:hasEmission ?emission . ?emission system:hasValue ?emissionvalue . ?emissionvalue system:numericalValue ?emissionvaluenum .\r\n"
			+ "<%s> system_realization:designCapacity ?capa . ?capa  system:hasValue ?capavalue . ?capavalue system:numericalValue ?capavaluenum .\r\n"
			+ "}";

	public static final String SPARQL_ALL_PLANTS = SPARQL_PREFIXES
			+ "SELECT ?plant \r\n" + "WHERE {\r\n" + "?plant a powerplant:PowerPlant .\r\n" + "}";
	
	public static final String SPARQL_PLANT_UPDATE_EMISSION = SPARQL_PREFIXES 
			+ "DELETE { ?emissionvalue system:numericalValue ?emissionvaluenum .} "
			+ "INSERT { ?emissionvalue system:numericalValue %f .} "
			+ "WHERE { <%s> technical_system:realizes ?generation . ?generation system_performance:hasEmission ?emission . ?emission system:hasValue ?emissionvalue . "
			+ "?emissionvalue system:numericalValue ?emissionvaluenum . }";
	
	SparqlOverHttpService sparqlsService = null;

	public PowerPlantQueries(SparqlOverHttpService sparqlService) {
		this.sparqlsService = sparqlService;
	}
	
	private SparqlOverHttpService getSparqlsService() {
		return sparqlsService;
	}

	public List<String> queryAllPowerplants() {

		List<String> plantList = new ArrayList<String>();
		
		System.out.println(SPARQL_ALL_PLANTS);
		
		String result = getSparqlsService().executeGet(SPARQL_ALL_PLANTS);
		
	
		StringTokenizer tokenizer = new StringTokenizer(result, "\r\n");
		tokenizer.nextElement(); // remove the header
		
		
		while (tokenizer.hasMoreElements()) {
			String plant = "" + tokenizer.nextElement();
			System.out.println(plant);
			plantList.add(plant);
		}
		
		System.out.println("number of power plants = " + plantList.size());
		
		return plantList;
	}

	public double queryEmission(String iri) {

		String query = MiscUtil.format(SPARQL_PLANT, iri, iri);

		//System.out.println(query);
		
		String result = getSparqlsService().executeGet(query);
		StringTokenizer tokenizer = new StringTokenizer(result, "\r\n");
		tokenizer.nextToken();
		double emission = 0.;
		if (tokenizer.hasMoreTokens()) {
			emission = Double.valueOf(tokenizer.nextToken());
		}
		
		System.out.println(iri +", emission = " + emission);

		return emission;
	}

	
	public void updateEmission(String iri, double emission) {
		
		String query = MiscUtil.format(SPARQL_PLANT_UPDATE_EMISSION, emission, iri);
		//System.out.println(query);
		
		getSparqlsService().executePost(query);
	}
	
	public void loopOnPlants(int numberPlants, boolean select, boolean insert, double emission) {
		
		List<String> plants = queryAllPowerplants();

		long start = System.currentTimeMillis();
		
		double sumEmission = 0;
		int i = 0;
		for (String current : plants) {
			i++;
			System.out.println(i + " " + current);
			if (insert) {
				updateEmission(current, emission);
			}
			if (select) {
				double queriedEmission = queryEmission(current);
				sumEmission += queriedEmission;
			}
			if (i == numberPlants) {
				break;
			}
		}
		
		long stop = System.currentTimeMillis();

		System.out.println("elapsed time in milli = " + (stop - start));
		System.out.println("number of queried or updated plants = " + i);

		if (select) {
			System.out.println("sum of queried emissions (after possible update) = " + sumEmission);
		}
	}
}
