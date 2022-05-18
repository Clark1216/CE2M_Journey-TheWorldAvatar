package uk.ac.cam.cares.jps.virtualsensor.sparql;

import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.json.JSONArray;
import org.json.JSONObject;

import uk.ac.cam.cares.jps.virtualsensor.objects.Scope;
import uk.ac.cam.cares.jps.base.util.CRSTransformer;
import uk.ac.cam.cares.jps.virtualsensor.objects.DispSim;
import uk.ac.cam.cares.jps.virtualsensor.objects.Point;
import uk.ac.cam.cares.jps.virtualsensor.objects.Region;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Assignment;
import org.eclipse.rdf4j.sparqlbuilder.core.From;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.SubSelect;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;

public class DispSimSparql {
	public static String SimKey = "simIRI";
	public static String episode_iri = "http://www.theworldavatar.com/kb/agents/Service__Episode.owl#Service";
	
    private static Prefix p_dispsim = SparqlBuilder.prefix("dispsim",iri("http://www.theworldavatar.com/kb/ontodispersionsim/OntoDispersionSim.owl#"));
	private static Prefix p_citygml = SparqlBuilder.prefix("city",iri("http://www.theworldavatar.com/ontology/ontocitygml/OntoCityGML.owl#"));
	private static Prefix p_space_time_extended = SparqlBuilder.prefix("space_time_extended",iri("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#"));
	private static Prefix p_system = SparqlBuilder.prefix("system",iri("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#"));
	private static Prefix p_coordsys = SparqlBuilder.prefix("coordsys",iri("http://www.theworldavatar.com/ontology/ontocape/upper_level/coordinate_system.owl#"));
	private static Prefix p_space_time = SparqlBuilder.prefix("space_time",iri("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time.owl#"));
	private static Prefix p_msm = SparqlBuilder.prefix("msm",iri("http://www.theworldavatar.com/ontology/ontoagent/MSM.owl#"));
	private static Prefix p_time = SparqlBuilder.prefix("time",iri("http://www.w3.org/2006/time#"));
	
	private static Prefix[] prefixes = {p_dispsim,p_citygml,p_space_time_extended,p_system,p_coordsys,p_space_time};
	
    // rdf type
    private static Iri DispersionSim = p_dispsim.iri("DispersionSim");
    private static Iri Scope = p_dispsim.iri("Scope");
    private static Iri PointType = p_citygml.iri("PointType");
    private static Iri CoordinateValue = p_coordsys.iri("CoordinateValue");
    private static Iri StraightCoordinate = p_space_time.iri("StraightCoordinate");
    private static Iri ProjectedCoordinateSystem = p_space_time_extended.iri("ProjectedCoordinateSystem");
    
    //relations
    private static Iri hasNx = p_dispsim.iri("hasNx");
    private static Iri hasNy = p_dispsim.iri("hasNy");
    private static Iri hasScope = p_dispsim.iri("hasScope");
    private static Iri hasLowerCornerPoint = p_dispsim.iri("lowerCornerPoint");
    private static Iri hasUpperCornerPoint = p_dispsim.iri("upperCornerPoint");
    private static Iri hasGISCoordinateSystem = p_space_time_extended.iri("hasGISCoordinateSystem");
    private static Iri hasProjectedCoordinate_x = p_space_time_extended.iri("hasProjectedCoordinate_x");
    private static Iri hasProjectedCoordinate_y = p_space_time_extended.iri("hasProjectedCoordinate_y");
    private static Iri hasValue = p_system.iri("hasValue");
    private static Iri value = p_system.iri("value");
    private static Iri numericalValue = p_system.iri("numericalValue");
    private static Iri hasMainStation = p_dispsim.iri("hasMainStation"); 
    private static Iri hasSubStation = p_dispsim.iri("hasSubStation");
    private static Iri hasEmissionSource = p_dispsim.iri("hasEmissionSource"); 
    private static Iri hasNumSubStations = p_dispsim.iri("hasNumSubStations");
    private static Iri hasServiceAgent = p_dispsim.iri("hasServiceAgent");
    private static Iri hasHttpUrl = p_msm.iri("hasHttpUrl");
    private static Iri hasSimCRS = p_dispsim.iri("hasSimCRS");
    private static Iri hasScopeCRS = p_dispsim.iri("hasScopeCRS");
    private static Iri hasOutputPath = p_dispsim.iri("hasOutputPath");
    private static Iri hasDz = p_dispsim.iri("hasDz");
    private static Iri hasIndex = p_dispsim.iri("hasIndex");
    private static Iri hasTime = p_time.iri("hasTime");
    private static Iri numericPosition = p_time.iri("numericPosition");
    private static Iri hasAirQualityStation = p_dispsim.iri("hasAirQualityStation");
    
    //unit
    private static Iri dimensionless = iri("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/SI_unit/SI_unit.owl#dimensionless");
    private static Iri unit_m = iri("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/SI_unit/SI_unit.owl#m");
    
    //endpoint
    private static Iri sim_graph = p_dispsim.iri("Simulations");
    private static From FromGraph = SparqlBuilder.from(sim_graph);

    /** 
     * Initialise Episode/ADMS agent
     */
    public static void InitService(String service_iri_string, String httpURL) {
    	Iri service_iri = iri(service_iri_string);
    	TriplePattern service_tp = service_iri.has(hasHttpUrl,iri(httpURL));
    	ModifyQuery modify = Queries.MODIFY();
    	modify.prefix(p_msm,p_dispsim).with(sim_graph).insert(service_tp).where();
    	SparqlGeneral.performUpdate(modify);
    }
    
    public static String GetServiceURL(String sim_iri_string) {
    	Iri sim_iri = iri(sim_iri_string);
    	String queryKey = "url";
    	Variable url = SparqlBuilder.var(queryKey);
    	
    	SelectQuery query = Queries.SELECT();
    	Iri[] predicates = {hasServiceAgent,hasHttpUrl};
    	GraphPattern queryPattern = SparqlGeneral.GetQueryGraphPattern(query, predicates, null, sim_iri,url);
    	
    	query.prefix(p_msm,p_dispsim).from(FromGraph).where(queryPattern).select(url);
    	String result = SparqlGeneral.performQuery(query).getJSONObject(0).getString(queryKey);
    	return result;
    }
    
    /**
	 * Initialise a simulation on triple-store
	 */
	public static String InitSim(DispSim sim) {
		int sim_index = GetNumSim()+1;
		String sim_iri_string = "http://www.theworldavatar.com/kb/ontodispersionsim/OntoDispersionSim.owl#sim"+sim_index;
		
		// ensure sim iri is unique
		while (CheckSimExist(sim_iri_string)) {
			sim_index += sim_index;
			sim_iri_string = "http://www.theworldavatar.com/kb/ontodispersionsim/OntoDispersionSim.owl#sim"+sim_index;
		}
		
		String sim_id = "sim" + sim_index;
    	Iri sim_iri = iri(sim_iri_string);
    	Iri nx = p_dispsim.iri(sim_id+"Nx");
    	Iri nxValue = p_dispsim.iri(sim_id+"NxValue");
    	Iri nyValue = p_dispsim.iri(sim_id+"NyValue");
    	Iri ny = p_dispsim.iri(sim_id+"Ny");
    	Iri numsub = p_dispsim.iri(sim_id+"NumSubStation");
    	Iri numsubvalue = p_dispsim.iri(sim_id+"NumSubStationValue");
    	Iri simCRS = p_dispsim.iri(sim_id+"SimCRS");
    	Iri simCRSValue = p_dispsim.iri(sim_id+"SimCRSValue");
    	
    	Iri scope = p_dispsim.iri(sim_id+"Scope");
    	Iri ScopeCRS = p_dispsim.iri(sim_id+"ScopeCRS");
    	Iri ScopeCRSValue = p_dispsim.iri(sim_id+"ScopeCRSValue");
    	Iri lowerCorner = p_dispsim.iri(sim_id+"LowerCorner");
    	Iri lowerCornerCoordinates = p_dispsim.iri(sim_id+"LowerCornerCoordinates");
    	Iri upperCorner = p_dispsim.iri(sim_id+"UpperCorner");
    	Iri upperCornerCoordinates = p_dispsim.iri(sim_id+"UpperCornerCoordinates");
    	
    	Iri lowerCornerX = p_dispsim.iri(sim_id+"LowerCornerX");
    	Iri lowerCornerXValue = p_dispsim.iri(sim_id+"LowerCornerXValue");
    	Iri lowerCornerY = p_dispsim.iri(sim_id+"LowerCornerY");
    	Iri lowerCornerYValue = p_dispsim.iri(sim_id+"LowerCornerYValue");
    	
    	Iri upperCornerX = p_dispsim.iri(sim_id+"UpperCornerX");
    	Iri upperCornerXValue = p_dispsim.iri(sim_id+"UpperCornerXValue");
    	Iri upperCornerY = p_dispsim.iri(sim_id+"UpperCornerY");
    	Iri upperCornerYValue = p_dispsim.iri(sim_id+"UpperCornerYValue");
    	
    	ModifyQuery modify = Queries.MODIFY();
    	
    	TriplePattern sim_tp = sim_iri.isA(DispersionSim).andHas(hasNx,nx).andHas(hasNy,ny)
    			.andHas(hasScope,scope).andHas(hasNumSubStations,numsub).andHas(hasServiceAgent,iri(sim.getServiceAgent()))
    			.andHas(hasSimCRS,simCRS);
    	
    	TriplePattern simCRS_tp = simCRS.has(hasValue,simCRSValue);
    	TriplePattern simCRSValue_tp = simCRSValue.has(value,sim.getSimCRS());
    	
    	TriplePattern scope_tp = scope.isA(Scope).andHas(hasLowerCornerPoint,lowerCorner).andHas(hasUpperCornerPoint,upperCorner).andHas(hasScopeCRS,ScopeCRS);
    	
    	// scope CRS
    	TriplePattern ScopeCRS_tp = ScopeCRS.has(hasValue,ScopeCRSValue);
    	TriplePattern ScopeCRSValue_tp =  ScopeCRSValue.has(value,sim.getScope().getSrsName());
    	
    	// lower corner
    	TriplePattern lowercorner_tp = lowerCorner.isA(PointType).andHas(hasGISCoordinateSystem,lowerCornerCoordinates);
    	TriplePattern lowercoord_tp = lowerCornerCoordinates.isA(ProjectedCoordinateSystem).andHas(hasProjectedCoordinate_x,lowerCornerX).andHas(hasProjectedCoordinate_y,lowerCornerY);
    	TriplePattern lowerxcoord_tp = lowerCornerX.isA(StraightCoordinate).andHas(hasValue,lowerCornerXValue);
    	TriplePattern lowerycoord_tp = lowerCornerY.isA(StraightCoordinate).andHas(hasValue,lowerCornerYValue);
    	TriplePattern vlowerxcoord_tp = lowerCornerXValue.isA(CoordinateValue).andHas(numericalValue, sim.getScope().getLowerCorner().getX());
    	TriplePattern vlowerycoord_tp = lowerCornerYValue.isA(CoordinateValue).andHas(numericalValue, sim.getScope().getLowerCorner().getY());
    	
    	// repeat for upper corner
    	TriplePattern uppercorner_tp = upperCorner.isA(PointType).andHas(hasGISCoordinateSystem,upperCornerCoordinates);
    	TriplePattern uppercoord_tp = upperCornerCoordinates.isA(ProjectedCoordinateSystem).andHas(hasProjectedCoordinate_x,upperCornerX).andHas(hasProjectedCoordinate_y,upperCornerY);
    	TriplePattern upperxcoord_tp =  upperCornerX.isA(StraightCoordinate).andHas(hasValue,upperCornerXValue);
    	TriplePattern upperycoord_tp = upperCornerY.isA(StraightCoordinate).andHas(hasValue,upperCornerYValue);
    	TriplePattern vupperxcoord_tp =  upperCornerXValue.isA(CoordinateValue).andHas(numericalValue, sim.getScope().getUpperCorner().getX());
    	TriplePattern vupperycoord_tp = upperCornerYValue.isA(CoordinateValue).andHas(numericalValue, sim.getScope().getUpperCorner().getY());
    	// scope done
    	
    	// number of sub stations (min 1 for Episode, 0 for ADMS)
    	SparqlGeneral.InsertScalarTP(modify,numsub, numsubvalue, sim.getNumSubStations(), dimensionless);
    	
    	// model grid information
    	SparqlGeneral.InsertScalarTP(modify,nx, nxValue, sim.getNx(), dimensionless);
    	SparqlGeneral.InsertScalarTP(modify,ny, nyValue, sim.getNy(), dimensionless);
    	
    	// dz is non-uniform
    	for (int i = 0; i < sim.getDz().length; i++) {
    		Iri dz =  p_dispsim.iri(sim_id+"Dz"+i);
    		Iri dzValue = p_dispsim.iri(sim_id+"Dz"+i+"Value");
    		
    		Iri dzIndex = p_dispsim.iri(sim_id+"DzIndex"+i);
    		Iri dzIndexValue = p_dispsim.iri(sim_id+"DzIndexValue"+i);
    		
    		// dz value
    		TriplePattern dz_tp = sim_iri.has(hasDz,dz);
    		SparqlGeneral.InsertScalarTP(modify, dz, dzValue, sim.getDz()[i], unit_m);
    		
    		// position in the array
    		TriplePattern dzIndex_tp = dz.has(hasIndex,dzIndex);
    		SparqlGeneral.InsertScalarTP(modify, dzIndex, dzIndexValue, i, dimensionless);
    		
    		modify.insert(dz_tp,dzIndex_tp);
    	}

    	TriplePattern[] combined_tp = {sim_tp,scope_tp,lowercorner_tp,lowercoord_tp,lowerxcoord_tp,lowerycoord_tp,vlowerxcoord_tp,vlowerycoord_tp,
    			uppercorner_tp,uppercoord_tp,upperxcoord_tp,upperycoord_tp,vupperxcoord_tp,vupperycoord_tp,simCRS_tp,simCRSValue_tp,ScopeCRS_tp,ScopeCRSValue_tp};
    	
    	modify.prefix(prefixes).with(sim_graph).where().insert(combined_tp);
    	SparqlGeneral.performUpdate(modify);
    	return sim_iri_string;
    }
	
	public static Scope GetScope(String sim_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		
		SelectQuery query = Queries.SELECT();
		
		//variables to query
		Variable lowerx = SparqlBuilder.var(Region.keyLowerx);
		Variable lowery = SparqlBuilder.var(Region.keyLowery);
		Variable upperx = SparqlBuilder.var(Region.keyUpperx);
		Variable uppery = SparqlBuilder.var(Region.keyUppery);
		Variable crs = SparqlBuilder.var(Region.keySrsname);
		
		// intermediate variables
		Variable scope = query.var();
		Variable lowerCornerCoordinates = query.var();
		Variable upperCornerCoordinates = query.var();
		
		Iri[] sim2scope_predicates = {hasScope};
		GraphPattern sim2envelope = SparqlGeneral.GetQueryGraphPattern(query, sim2scope_predicates, null, sim_iri, scope);
		
		Iri[] scope2srs_predicates = {hasScopeCRS,hasValue,value};
		GraphPattern scope2srs = SparqlGeneral.GetQueryGraphPattern(query, scope2srs_predicates, null, scope, crs);
		
		Iri[] scope2lowercoord_predicates = {hasLowerCornerPoint,hasGISCoordinateSystem};
		GraphPattern scope2lowercoord = SparqlGeneral.GetQueryGraphPattern(query, scope2lowercoord_predicates, null, scope, lowerCornerCoordinates);
		
		Iri[] coord2x_predicates = {hasProjectedCoordinate_x,hasValue,numericalValue};
		GraphPattern lower2x = SparqlGeneral.GetQueryGraphPattern(query, coord2x_predicates, null, lowerCornerCoordinates, lowerx);
		
		Iri[] coord2y_predicates = {hasProjectedCoordinate_y,hasValue,numericalValue};
		GraphPattern lower2y = SparqlGeneral.GetQueryGraphPattern(query, coord2y_predicates, null, lowerCornerCoordinates, lowery);
		
		Iri[] scope2uppercoord_predicates = {hasUpperCornerPoint,hasGISCoordinateSystem};
		GraphPattern scope2uppercoord = SparqlGeneral.GetQueryGraphPattern(query, scope2uppercoord_predicates, null, scope, upperCornerCoordinates);
		
		GraphPattern upper2x = SparqlGeneral.GetQueryGraphPattern(query, coord2x_predicates, null, upperCornerCoordinates, upperx);
		GraphPattern upper2y = SparqlGeneral.GetQueryGraphPattern(query, coord2y_predicates, null, upperCornerCoordinates, uppery);
		
		GraphPattern queryPattern = GraphPatterns.and(sim2envelope,scope2lowercoord,lower2x,lower2y,scope2uppercoord,upper2x,upper2y,scope2srs);
		
		query.prefix(prefixes).from(FromGraph).select(lowerx,lowery,upperx,uppery,crs).where(queryPattern);
		JSONObject queryResult = SparqlGeneral.performQuery(query).getJSONObject(0);
		
		Scope sc = new Scope();
		Point lowerCorner = new Point();
		lowerCorner.setX(queryResult.getDouble(Region.keyLowerx));
		lowerCorner.setY(queryResult.getDouble(Region.keyLowery));
		lowerCorner.setSrsname(queryResult.getString(Region.keySrsname));
		
		Point upperCorner = new Point();
		upperCorner.setX(queryResult.getDouble(Region.keyUpperx));
		upperCorner.setY(queryResult.getDouble(Region.keyUppery));
		upperCorner.setSrsname(queryResult.getString(Region.keySrsname));

		sc.setLowerCorner(lowerCorner);
		sc.setUpperCorner(upperCorner);
		sc.setSrsName(queryResult.getString(Region.keySrsname));
		
		return sc;
	}
	
	public static int GetNumSubStations(String sim_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		String queryKey = "numsub";
		Variable numsub = SparqlBuilder.var(queryKey);
		
		SelectQuery query = Queries.SELECT();
		
		Iri[] numsub_predicates = {hasNumSubStations,hasValue,numericalValue};
	    GraphPattern queryPattern = SparqlGeneral.GetQueryGraphPattern(query, numsub_predicates, null, sim_iri, numsub);
		
		query.select(numsub).prefix(p_system,p_dispsim).from(FromGraph).where(queryPattern);
		int result = SparqlGeneral.performQuery(query).getJSONObject(0).getInt(queryKey);
		return result;
	}
	
	/** 
	 * Insert main weather station used for this simulation
	 */
	public static void AddMainStation(String sim_iri_string, String station_iri_string) {
		Iri sim_iri = iri(sim_iri_string);

		// delete existing main station
		SubSelect sub = GraphPatterns.select();
		Variable oldmain = sub.var();
		TriplePattern delete_tp = sim_iri.has(hasMainStation,oldmain);
		sub.select(oldmain).where(delete_tp);
		ModifyQuery deleteQuery = Queries.MODIFY();
		deleteQuery.prefix(p_dispsim).from(sim_graph).where(sub).delete(delete_tp);
		SparqlGeneral.performUpdate(deleteQuery);
		
		//insert new station
		TriplePattern insert_tp = sim_iri.has(hasMainStation,iri(station_iri_string));
		ModifyQuery insertQuery = Queries.MODIFY();
		insertQuery.prefix(p_dispsim).with(sim_graph).insert(insert_tp);
		SparqlGeneral.performUpdate(insertQuery);
	}
	
	public static String GetMainStation(String sim_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		String stationKey = "station";
		Variable station = SparqlBuilder.var(stationKey);
		GraphPattern queryPattern = sim_iri.has(hasMainStation,station);
		SelectQuery query = Queries.SELECT();
		query.select(station).from(FromGraph).where(queryPattern).prefix(p_dispsim);
		String result = SparqlGeneral.performQuery(query).getJSONObject(0).getString(stationKey);
		return result;
	}
	
	/**
	 * 
	 */
	public static void AddSubStations(String sim_iri_string, String[] station_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		
		// find old stations to delete
		SubSelect sub = GraphPatterns.select();
		Variable stations = sub.var();
		TriplePattern deletePattern = sim_iri.has(hasSubStation,stations);
		sub.select(stations).where(deletePattern);
		
		ModifyQuery deleteQuery = Queries.MODIFY();
		deleteQuery.prefix(p_dispsim).where(sub).delete(deletePattern).with(sim_graph);
		SparqlGeneral.performUpdate(deleteQuery);
		
		// new stations to add
		TriplePattern[] insert_tp = new TriplePattern[station_iri_string.length];
		for (int i = 0; i < station_iri_string.length; i++) {
			Iri station_iri = iri(station_iri_string[i]);
			insert_tp[i] = sim_iri.has(hasSubStation,station_iri);
		}
		
		ModifyQuery insertQuery = Queries.MODIFY();
		insertQuery.prefix(p_dispsim).with(sim_graph).insert(insert_tp);
		SparqlGeneral.performUpdate(insertQuery);
	}
	
	public static String[] GetSubStations(String sim_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		String stationKey = "station";
		Variable station = SparqlBuilder.var(stationKey);
		GraphPattern queryPattern = sim_iri.has(hasSubStation,station);
		SelectQuery query = Queries.SELECT();
		query.select(station).from(FromGraph).where(queryPattern).prefix(p_dispsim);
		
		JSONArray queryResult = SparqlGeneral.performQuery(query);
		String[] stationIRI = new String[queryResult.length()];
		
		for (int i = 0; i < queryResult.length(); i++) {
			stationIRI[i] = queryResult.getJSONObject(i).getString(stationKey);
		}
		
		return stationIRI;
	}
	
	public static void AddEmissionSources(String sim_iri_string, String[] ship_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		
		// old ship IRIs to delete
		SubSelect sub = GraphPatterns.select();
		Variable oldships = sub.var();
		TriplePattern deletePattern = sim_iri.has(hasEmissionSource,oldships);
		sub.select(oldships).where(deletePattern);
		
		ModifyQuery deleteQuery = Queries.MODIFY();
		deleteQuery.prefix(p_dispsim).where(sub).delete(deletePattern).with(sim_graph);
		SparqlGeneral.performUpdate(deleteQuery);
		
		// add new ships
		TriplePattern[] insert_tp = new TriplePattern[ship_iri_string.length];
		for (int i = 0; i < ship_iri_string.length; i++) {
			Iri ship_iri = iri(ship_iri_string[i]);
			insert_tp[i] = sim_iri.has(hasEmissionSource,ship_iri);
		}
		
		ModifyQuery insertQuery = Queries.MODIFY();
		insertQuery.prefix(p_dispsim).with(sim_graph).insert(insert_tp);
		SparqlGeneral.performUpdate(insertQuery);
	}
	
	public static String[] GetEmissionSources(String sim_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		String shipKey = "ship";
		Variable ship = SparqlBuilder.var(shipKey);
		GraphPattern queryPattern = sim_iri.has(hasEmissionSource,ship);
		SelectQuery query = Queries.SELECT();
		query.select(ship).from(FromGraph).where(queryPattern).prefix(p_dispsim);
		
		JSONArray queryResult = SparqlGeneral.performQuery(query);
		String[] shipIRI = new String[queryResult.length()];
		
		for (int i = 0; i < queryResult.length(); i++) {
			shipIRI[i] = queryResult.getJSONObject(i).getString(shipKey);
		}
		
		return shipIRI;
	}
	
	public static void AddOutputPath(String sim_iri_string,String outputPath_string,long timeStamp) {
		Iri sim_iri = iri(sim_iri_string);
		
		// ensure data path is a valid uri
		Iri outputPath = iri(new File(outputPath_string).toURI().toString());

		// get number of existing outputs to write IRI for timestamp
		int numOutput = GetNumOutput(sim_iri_string);
		Iri time_iri = iri(sim_iri_string + "TimeStamp" + String.valueOf(numOutput+1));
		
		ModifyQuery insertQuery = Queries.MODIFY();
		
		// insert location
		TriplePattern insertPath = sim_iri.has(hasOutputPath,outputPath);
		
		//tag with timestamp
		TriplePattern time_tp = outputPath.has(hasTime,time_iri);
		TriplePattern timevalue_tp = time_iri.isA(p_time.iri("TimePosition"))
				.andHas(p_time.iri("hasTRS"),iri("http://dbpedia.org/resource/Unix_time"))
				.andHas(numericPosition, timeStamp);
		
		insertQuery.prefix(p_time,p_dispsim).with(sim_graph).insert(insertPath,time_tp,timevalue_tp).where();
		SparqlGeneral.performUpdate(insertQuery);
	}
	
	/**
	 * sometimes the slurm api submits the same job twice, this ensures the same sim does not get annotated twice
	 * @param outputPath_string
	 * @return
	 */
	public static boolean CheckOutputPathExist(String sim_iri_string,String outputPath_string) {
		// ensure data path is a valid uri
		String outputPath = new File(outputPath_string).toURI().toString();
		
        String query = String.format("ask {<%s> "
        		+ "<http://www.theworldavatar.com/kb/ontodispersionsim/OntoDispersionSim.owl#hasOutputPath>"
        		+ "<%s>}",sim_iri_string,outputPath);
		
		boolean outputExist = SparqlGeneral.performQuery(query).getJSONObject(0).getBoolean("ASK");
		return outputExist;
	}
	
	/**
	 * returns a list of output path and time stamp, filter with lower bound given
	 * @param sim_iri_string
	 * @param time_lb
	 * @return
	 */
	
	public static JSONArray GetOutputPathAndTime(String sim_iri_string, long time_lb) {
		SelectQuery query = Queries.SELECT();
		
		Iri sim_iri = iri(sim_iri_string);
		
		Variable outputPath = SparqlBuilder.var("outputPath");
		Variable timeStamp = SparqlBuilder.var("timestamp");
		
		TriplePattern output_gp = sim_iri.has(hasOutputPath,outputPath);
		
		Iri[] path2time_predicates = {hasTime,numericPosition};
		GraphPattern path2time_gp = SparqlGeneral.GetQueryGraphPattern(query, path2time_predicates, null, outputPath,timeStamp);
		
		GraphPatternNotTriples queryPattern = GraphPatterns.and(output_gp,path2time_gp)
				.filter(Expressions.gt(timeStamp, time_lb));
		
		query.from(FromGraph).select(outputPath,timeStamp).where(queryPattern).prefix(p_dispsim,p_time);

		JSONArray queryResult = SparqlGeneral.performQuery(query);
		
		return queryResult;
	}
	
	/**
	 * Returns number of output files associated with this simulation
	 * @param sim_iri_string
	 * @return
	 */
	public static int GetNumOutput(String sim_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		
		String queryKey = "num";
		
		SelectQuery query = Queries.SELECT();
		Variable outputPath = query.var();
		Variable numOutput = SparqlBuilder.var(queryKey);
		
	    Assignment assign = Expressions.count(outputPath).as(numOutput);
	    GraphPattern queryPattern = sim_iri.has(hasOutputPath,outputPath);
	    
	    query.from(FromGraph).select(assign).where(queryPattern).prefix(p_dispsim);
	    
	    int result = SparqlGeneral.performQuery(query).getJSONObject(0).getInt(queryKey);
	    
		return result;
	}
	
	public static String GetSimCRS(String sim_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		String queryKey = "crs";
		Variable crs = SparqlBuilder.var(queryKey);
		
		SelectQuery query = Queries.SELECT();
		Iri[] predicates = {hasSimCRS,hasValue,value};
		GraphPattern queryPattern = SparqlGeneral.GetQueryGraphPattern(query, predicates, null, sim_iri,crs);
		
		query.from(FromGraph).select(crs).prefix(p_system,p_dispsim).where(queryPattern);
		
		String result = SparqlGeneral.performQuery(query).getJSONObject(0).getString(queryKey);
		return result;
	}
	
	public static double[] GetDz(String sim_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		SelectQuery query = Queries.SELECT();
		
		String indexKey = "index";
		String dzKey = "dzValue";
		
		Variable dz = query.var();
		Variable dzValue = SparqlBuilder.var(dzKey);
		Variable index = SparqlBuilder.var(indexKey);
		
		GraphPattern sim2dz_gp = sim_iri.has(hasDz,dz);
		
		Iri[] dz2value_pred = {hasValue,numericalValue};
		GraphPattern dz2value_gp = SparqlGeneral.GetQueryGraphPattern(query, dz2value_pred, null, dz,dzValue);
		
		Iri[] dz2index_pred = {hasIndex,hasValue,numericalValue};
		GraphPattern dz2index_gp = SparqlGeneral.GetQueryGraphPattern(query, dz2index_pred, null, dz,index);
		
		query.prefix(p_dispsim,p_system).where(sim2dz_gp,dz2value_gp,dz2index_gp).from(FromGraph).select(dzValue,index);
		JSONArray queryResult = SparqlGeneral.performQuery(query);
		
		double[] vdz = new double [queryResult.length()];
		
		// results are not returned in numerical order
		for (int i=0; i<queryResult.length(); i++) {
			int vindex = queryResult.getJSONObject(i).getInt(indexKey);
			vdz[vindex] = queryResult.getJSONObject(i).getDouble(dzKey);
		}
		return vdz;
	}
	
	public static int GetNx(String sim_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		SelectQuery query = Queries.SELECT();
		
		String queryKey = "nx";
		Variable nx = SparqlBuilder.var(queryKey);
		
		Iri[] predicates = {hasNx,hasValue,numericalValue};
		GraphPattern queryPattern = SparqlGeneral.GetQueryGraphPattern(query, predicates, null, sim_iri, nx);
		
		query.from(FromGraph).select(nx).where(queryPattern).prefix(p_dispsim,p_system);
		
		int nxValue = SparqlGeneral.performQuery(query).getJSONObject(0).getInt(queryKey);
		
		return nxValue;
	}
	
	public static int GetNy(String sim_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		SelectQuery query = Queries.SELECT();
		
		String queryKey = "ny";
		Variable ny = SparqlBuilder.var(queryKey);
		
		Iri[] predicates = {hasNy,hasValue,numericalValue};
		GraphPattern queryPattern = SparqlGeneral.GetQueryGraphPattern(query, predicates, null, sim_iri, ny);
		
		query.from(FromGraph).select(ny).where(queryPattern).prefix(p_dispsim,p_system);
		
		int nyValue = SparqlGeneral.performQuery(query).getJSONObject(0).getInt(queryKey);
		
		return nyValue;
	}
	
	public static int GetNumSim() {
		SelectQuery query = Queries.SELECT();
		String queryKey = "numsim";
		Variable numsim = SparqlBuilder.var(queryKey);
		Variable sim = query.var();
		GraphPattern queryPattern = sim.isA(DispersionSim);
		Assignment assign = Expressions.count(sim).as(numsim);
		
		query.from(FromGraph).prefix(p_dispsim).where(queryPattern).select(assign);
		
		int result = SparqlGeneral.performQuery(query).getJSONObject(0).getInt(queryKey);
		return result;
	}
	
	public static boolean CheckSimExist(String sim_iri_string) {
		// ask query is not supported by SparqlBuilder, hence hardcode
		String query = String.format("ask {<%s> a <http://www.theworldavatar.com/kb/ontodispersionsim/OntoDispersionSim.owl#DispersionSim>}",sim_iri_string);
		
		boolean simExist = SparqlGeneral.performQuery(query).getJSONObject(0).getBoolean("ASK");
		return simExist;
	}
	
	/**
	 * centre needs to be in epsg:4326, assume input from map
	 * @param centre
	 * @param dimension
	 * @return
	 */
	public static Scope createScopeEpisode(double[] centre, double[] dimension) {
    	// first determine UTM zone of centre
        // Determine zone based on longitude, the size of each UTM zone is 6 degrees
        int centreZoneNumber = (int) Math.ceil((centre[0] + 180)/6);
        String localCRS;
        if (centre[1] < 0) {
        	localCRS = "EPSG:327" + centreZoneNumber;
        } else {
        	localCRS = "EPSG:326" + centreZoneNumber;
        }
        
        // currently not considering cases where simulation domain spans across two zones
        double[] centre_m = CRSTransformer.transform(CRSTransformer.EPSG_4326, localCRS, centre);
        
        Scope sc = new Scope();
        
        Point upperCorner = new Point();
        upperCorner.setX(centre_m[0]+dimension[0]/2);
        upperCorner.setY(centre_m[1]+dimension[1]/2);
        upperCorner.setSrsname(localCRS);
        
        Point lowerCorner = new Point();
        lowerCorner.setX(centre_m[0]-dimension[0]/2);
        lowerCorner.setY(centre_m[1]-dimension[1]/2);
        lowerCorner.setSrsname(localCRS);
        
        sc.setLowerCorner(lowerCorner);
        sc.setUpperCorner(upperCorner);
        sc.setSrsName(localCRS);
        
        return sc;
    }
	
	/**
	 * initialise a simulation on the triple-store
	 * creates an artificial emission source and two virtual weather stations
	 * @param centre
	 * @param dimension
	 */
	public static String CreateDispSim(Point centre, double[] dimension) {
		Point centre_copy = centre; // from front end, so it's in latlng, epsg4326
		
		// first determine UTM zone of centre
        // Determine zone based on longitude, the size of each UTM zone is 6 degrees
        int centreZoneNumber = (int) Math.ceil((centre_copy.getX() + 180)/6);
        String localCRS;
        if (centre_copy.getY() < 0) {
        	localCRS = "EPSG:327" + centreZoneNumber;
        } else {
        	localCRS = "EPSG:326" + centreZoneNumber;
        }
        
        //create 1 dummy ship at the centre
        int mmsi, al, aw, ts; double ss, cu, lat, lon; String type;
        mmsi = 1;
        type = "unknown type";
        al = 37;
        aw = 8;
        ss = 0.1;
        cu = 220.2;
        lat = centre_copy.getY();
        lon = centre_copy.getX();
        ts = 1;
        int shipindex = ShipSparql.GetNumShips() + 1;
        ShipSparql.createShip(shipindex,mmsi,type,al,aw,ss,cu,lat,lon,ts);
        
        // create main station at the centre
        int numstn = SensorSparql.GetNumWeatherStation();
        double stnhgt = 10; // fixed height for now
        double[] xyz_main = {centre_copy.getX(),centre_copy.getY(),stnhgt}; //centre_copy in epsg4326
        SensorSparql.createWeatherStation(numstn+1, xyz_main);
        
        // need to convert centre to meters
        centre_copy.transform(localCRS);
        
        // create scope for Episode simulation
        Scope sc = new Scope();
        
        Point upperCorner = new Point();
        upperCorner.setX(centre_copy.getX()+dimension[0]/2);
        upperCorner.setY(centre_copy.getY()+dimension[1]/2);
        upperCorner.setSrsname(localCRS);
        
        Point lowerCorner = new Point();
        lowerCorner.setX(centre_copy.getX()-dimension[0]/2);
        lowerCorner.setY(centre_copy.getY()-dimension[1]/2);
        lowerCorner.setSrsname(localCRS);
        
        sc.setLowerCorner(lowerCorner);
        sc.setUpperCorner(upperCorner);
        sc.setSrsName(localCRS);
        
        // create sub station 100m away from bottom corner
        double[] xy_sub = {lowerCorner.getX()+100 ,lowerCorner.getY()+100};
        xy_sub = CRSTransformer.transform(sc.getSrsName(), CRSTransformer.EPSG_4326, xy_sub);//to latlng
        double[] xyz_sub = {xy_sub[0],xy_sub[1],stnhgt};
        SensorSparql.createWeatherStation(numstn+2, xyz_sub);
        
        double[] dz = {10,10,15,25,40,100,300,500,500,500,500,500,500};
        DispSim sim = new DispSim();
    	sim.setScope(sc);
    	sim.setNx(10);
    	sim.setNy(10);
    	sim.setNumSubStations(1);
    	sim.setServiceAgent(episode_iri);
    	sim.setSimCRS(localCRS);
    	sim.setDz(dz);
    	
    	String sim_iri = DispSimSparql.InitSim(sim);
		DispSimSparql.InitService(episode_iri, "http://localhost:8080/JPS_VIRTUALSENSOR/EpisodeAgent");
    	return sim_iri;
	}
	
	public static void AddAirQualityStation(String sim_iri_string, String station_iri_string) {
		Iri sim_iri = iri(sim_iri_string);
		Iri station_iri = iri(station_iri_string);
		
		TriplePattern insert_tp = sim_iri.has(hasAirQualityStation,station_iri);
		
		ModifyQuery modify = Queries.MODIFY();
		modify.prefix(p_dispsim).insert(insert_tp).with(sim_graph).where();
		SparqlGeneral.performUpdate(modify);
	}
	
	/** 
	 * returns all sim IRI in KG
	 */
	public static String[] GetAllSimIri() {
		String queryKey = "sim";
		Variable sim = SparqlBuilder.var(queryKey);
		GraphPattern queryPattern = sim.isA(DispersionSim);
		
		SelectQuery query = Queries.SELECT();
		query.from(FromGraph).prefix(p_dispsim).where(queryPattern);
		
		JSONArray queryResult = SparqlGeneral.performQuery(query);
		
		String[] sims = new String[queryResult.length()];
		
		for (int i=0; i<sims.length; i++) {
			sims[i] = queryResult.getJSONObject(i).getString(queryKey);
		}
		return sims;
	}
	
	/**
	 * returns sim IRI if a sim exists at point p
	 * @param p
	 */
	public static List<String> GetSimIRIForCoordinates(Point p) {
		// retrieve all sim IRI, get the scopes for each sim, and check if the point lies within it
		String[] sims = GetAllSimIri();
		
		// result
		List<String> result = new ArrayList<String>();
		
		for (int i=0; i<sims.length; i++) {
			Scope sc = GetScope(sims[i]);
			
			if (sc.isWithinScope(p)) {
				result.add(sims[i]);
			}
		}

		return result;
	}

	/**
	 * Returns simulation IRI linked to the given station
	 * @param station_iri
	 */
	public static String GetSimForSensor(String station_iri_string) {
		String queryKey = "sim";
		Variable sim = SparqlBuilder.var(queryKey);
		Iri station_iri = iri(station_iri_string);
		
		GraphPattern queryPattern = sim.has(hasAirQualityStation,station_iri);
		
		SelectQuery query = Queries.SELECT();
		query.from(FromGraph).prefix(p_dispsim).select(sim).where(queryPattern);
		
		String sim_iri = SparqlGeneral.performQuery(query).getJSONObject(0).getString(queryKey);
		
		return sim_iri;
	}
	
	/**
	 * returns all air quality stations linked to given sim
	 */
	public static String[] GetAirQualityStations(String sim_iri_string) {
		SelectQuery query = Queries.SELECT();
		String queryKey = "station";
		Iri sim_iri = iri(sim_iri_string);
		Variable station = SparqlBuilder.var(queryKey);
		
		GraphPatternNotTriples queryPattern = GraphPatterns.and(sim_iri.has(hasAirQualityStation,station));
		
		query.from(FromGraph).select(station).where(queryPattern).prefix(p_dispsim);
		
		JSONArray queryResult = SparqlGeneral.performQuery(query);
		
		String[] stations = null;
		if (queryResult.length()>0) {
		    stations = new String[queryResult.length()];
		    for (int i=0; i<queryResult.length(); i++) {
		    	stations[i] = queryResult.getJSONObject(i).getString(queryKey);
		    }
		}
		return stations;
	}
	
	/** 
	 * reset endpoint
	 */
	public static void ResetEndpoint() {
		String simgraph = "http://www.theworldavatar.com/kb/ontodispersionsim/OntoDispersionSim.owl#Simulations";
		String weathergraph = "http://www.theworldavatar.com/ontology/ontostation/OntoStation.owl#WeatherStations";
		String airqualitygraph = "http://www.theworldavatar.com/ontology/ontostation/OntoStation.owl#AirQualityStations";
		String shipgraph = "http://www.theworldavatar.com/ontology/ontoship/OntoShip.owl#Ships";
		
		String queryTemplate = "clear graph <%s>";
		
		String query1 = String.format(queryTemplate,simgraph);
		String query2 = String.format(queryTemplate, weathergraph);
		String query3 = String.format(queryTemplate, airqualitygraph);
		String query4 = String.format(queryTemplate, shipgraph);
		
		SparqlGeneral.performUpdate(query1);
		SparqlGeneral.performUpdate(query2);
		SparqlGeneral.performUpdate(query3);
		SparqlGeneral.performUpdate(query4);
	}
}
