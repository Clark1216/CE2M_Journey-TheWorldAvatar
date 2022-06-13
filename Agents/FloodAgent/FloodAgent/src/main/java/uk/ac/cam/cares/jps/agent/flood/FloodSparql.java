package uk.ac.cam.cares.jps.agent.flood;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.sparqlbuilder.core.OrderCondition;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfLiteral.StringLiteral;
import org.json.JSONArray;
import org.json.JSONObject;

import uk.ac.cam.cares.jps.agent.flood.objects.Station;
import uk.ac.cam.cares.jps.agent.flood.sparqlbuilder.ServicePattern;
import uk.ac.cam.cares.jps.agent.flood.sparqlbuilder.ValuesPattern;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.interfaces.StoreClientInterface;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesSparql;

/**
 * contains a collection of methods to query and update the KG
 * @author Kok Foong Lee
 *
 */
public class FloodSparql {
    private StoreClientInterface storeClient;
    
    // prefix
 	private static String ontostation = "https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontostation/OntoStation.owl#";
    private static Prefix p_station = SparqlBuilder.prefix("station",iri(ontostation));
    private static Prefix p_time = SparqlBuilder.prefix("time", iri("http://www.w3.org/2006/time#"));
    private static Prefix p_geo = SparqlBuilder.prefix("geo",iri("http://www.bigdata.com/rdf/geospatial#"));
    
    // classes
    private static Iri Station = p_station.iri("Station");
    private static Iri Instant = p_time.iri("Instant");
	
    // properties
    private static Iri hasCoordinates = p_station.iri("hasCoordinates");
    private static Iri measures = iri("http://environment.data.gov.uk/flood-monitoring/def/core/measures");
    private static Iri hasTime = p_time.iri("hasTime");
    private static Iri inXSDDate = p_time.iri("inXSDDate");
    private static Iri stationReference = iri("http://environment.data.gov.uk/flood-monitoring/def/core/stationReference");
    private static Iri lat_lon = iri("http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon");
    // made up by KFL, purely for mapbox requirement
    private static Iri hasVisID = iri("http://environment.data.gov.uk/flood-monitoring/def/core/visID"); 
    
    private static Iri unitName = iri("http://environment.data.gov.uk/flood-monitoring/def/core/unitName");
    private static Iri parameterName = iri("http://environment.data.gov.uk/flood-monitoring/def/core/parameterName");
	private static Iri qualifier = iri("http://environment.data.gov.uk/flood-monitoring/def/core/qualifier");
    
    // Logger for reporting info/errors
    private static final Logger LOGGER = LogManager.getLogger(FloodSparql.class);
    
	public FloodSparql(StoreClientInterface storeClient) {
		this.storeClient = storeClient;
	}
	
	/**
	 * returns a list of stations
	 * it is assumed that there is only one RDF collection in the namespace
	 * a good illustration of how an RDF collection look like 
	 * http://www-kasm.nii.ac.jp/~koide/SWCLOS2/Manual/07RDFCollection.htm
	 * @return
	 */
	List<String> getStations() {
		SelectQuery query = Queries.SELECT();
		
		Variable station = query.var();
		
		GraphPattern queryPattern = query.var().has(RDF.FIRST, station);
		
		query.select(station).where(queryPattern);
		
	    @SuppressWarnings("unchecked")
		List<String> stations = storeClient.executeQuery(query.getQueryString()).toList().stream()
	    .map(stationiri -> ((HashMap<String,String>) stationiri).get(station.getQueryString().substring(1))).collect(Collectors.toList());
	    
	    return stations;
	}
	
	void addStationRdfType(List<String> stations) {
		ModifyQuery modify = Queries.MODIFY();
		
		for (String station : stations) {
			modify.insert(iri(station).isA(Station));
		}
		
		modify.prefix(p_station);
		
		storeClient.executeUpdate(modify.getQueryString());
	}
	
	/**
	 * returns all the measures in the endpoint, each station may measure 1-4 quantities
	 * ignores stations without coordinates
	 * @return
	 */
	List<String> getMeasures() {
        SelectQuery query = Queries.SELECT();
		
		Variable measure = query.var();
		Variable station = query.var();
		Variable coord = query.var();
				
		GraphPattern queryPattern = station.has(measures, measure)
				.andHas(hasCoordinates, coord);
		
		query.select(measure).where(queryPattern).prefix(p_station);
		
	    @SuppressWarnings("unchecked")
		List<String> measure_iri_list = storeClient.executeQuery(query.getQueryString()).toList().stream()
	    .map(datairi -> ((HashMap<String,String>) datairi).get(measure.getQueryString().substring(1))).collect(Collectors.toList());
	    
	    return measure_iri_list;
	}
	
	/**
	 * similar function as above but only query for measures for the given stations
	 * @param stations
	 * @return
	 */
	Map<String, List<String>> getMeasures(Map<String, Station> stations) {
		SelectQuery query = Queries.SELECT();
		
		Variable measure = query.var();
		Variable station = query.var();
				
		GraphPattern queryPattern = station.has(measures, measure);
		List<String> stationIri_list = new ArrayList<>(stations.keySet());
		ValuesPattern stationPattern = new ValuesPattern(station, stationIri_list.stream().map(s -> iri(s)).collect(Collectors.toList()));
		
		query.select(measure,station).where(queryPattern, stationPattern);
		
		JSONArray queryResult = storeClient.executeQuery(query.getQueryString());
		
		Map<String, List<String>> station_measure_map = new HashMap<>();
		for (int i = 0; i < queryResult.length(); i++) {
			String stationIri = queryResult.getJSONObject(i).getString(station.getQueryString().substring(1));
			String measureIri = queryResult.getJSONObject(i).getString(measure.getQueryString().substring(1));
			
			if (station_measure_map.containsKey(stationIri)) {
				station_measure_map.get(stationIri).add(measureIri);
			} else {
				List<String> newMeasureList = new ArrayList<>();
				newMeasureList.add(measureIri);
				station_measure_map.put(stationIri, newMeasureList);
			}
		}
	    
	    return station_measure_map;
	}
	
	/**
	 * original data has lat and lon on different triples
	 * Blazegraph requires them to be in the form of lat#lon
	 * visID is purely for visualisation purpose
	 */
	void addBlazegraphCoordinatesAndVisID() {
		Iri lat_prop = iri("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
		Iri lon_prop = iri("http://www.w3.org/2003/01/geo/wgs84_pos#long");
		
		// first query both lat and lon for each station
		SelectQuery query = Queries.SELECT();
		
		Variable station = query.var();
		Variable lat = query.var();
		Variable lon = query.var();
		
		GraphPattern queryPattern = GraphPatterns.and(station.has(lat_prop,lat)
				.andHas(lon_prop,lon));
		
		query.where(queryPattern).select(station,lat,lon);
		
		JSONArray queryResult = storeClient.executeQuery(query.getQueryString());
		
		// then add the combined literal and upload it
		List<String> latlon = new ArrayList<>(queryResult.length());
		List<String> stations = new ArrayList<>(queryResult.length());
		List<Integer> visID = new ArrayList<>(queryResult.length());
		
		for (int i = 0; i < queryResult.length(); i++) {
			latlon.add(i,queryResult.getJSONObject(i).getString(lat.getQueryString().substring(1)) +
					"#" + queryResult.getJSONObject(i).getString(lon.getQueryString().substring(1)));
			stations.add(i, queryResult.getJSONObject(i).getString(station.getQueryString().substring(1)));
			visID.add(i,i);
		}
		
		ModifyQuery modify = Queries.MODIFY();
		modify.prefix(p_station);
		// one triple per station
		for (int i = 0; i < queryResult.length(); i++) {
			// blazegraph's custom literal type
			StringLiteral coordinatesLiteral = Rdf.literalOfType(latlon.get(i), lat_lon);
			modify.insert(iri(stations.get(i)).has(hasCoordinates,coordinatesLiteral));
			modify.insert(iri(stations.get(i)).has(hasVisID,visID.get(i)));
		}
		
		storeClient.executeUpdate(modify.getQueryString());
	}
	
	/**
	 * add a measure that was not present in the initial RDF file, but present
	 * in the data downloaded later
	 * adds a triple <station> <measures> <measure>
	 * @param station
	 * @param measure
	 */
	void addMeasureToStation(String station, String measure, String unit,
			String paramName, String qual) {
		ModifyQuery modify = Queries.MODIFY();
		modify.insert(iri(station).has(measures,iri(measure)));
		modify.insert(iri(measure).has(unitName, unit)
				.andHas(parameterName, paramName)
				.andHas(qualifier,qual));
		storeClient.executeUpdate(modify.getQueryString());
	}
	
	/** 
	 * performs a very simple check on whether stations are already initialised
	 * with time series
	 * @return
	 */
	boolean areStationsInitialised() {
		SelectQuery query = Queries.SELECT();
		Variable station = query.var();
		Variable measure = query.var();
		Variable timeseries = query.var();
		
		Iri hasTimeSeries = iri(TimeSeriesSparql.ns_ontology + "hasTimeSeries");
		
		GraphPattern queryPattern = GraphPatterns.and(station.has(measures, measure).andIsA(Station),
				measure.has(hasTimeSeries,timeseries));
		
		query.prefix(p_station).where(queryPattern).limit(10);
		
		JSONArray queryResult = storeClient.executeQuery(query.getQueryString());
		
		if (queryResult.length() >= 1) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * adds triples to say the database contains data of this date
	 * @param date
	 */
	void addUpdateDate(LocalDate date) {
		Iri stations = iri("http://environment.data.gov.uk/flood-monitoring/id/stations");
		Iri instant = iri("http://environment.data.gov.uk/flood-monitoring/id/stations/time");
		
		ModifyQuery modify = Queries.MODIFY();
		modify.insert(stations.has(hasTime,instant));
		modify.insert(instant.isA(Instant).andHas(inXSDDate, Rdf.literalOfType(date.toString(), XSD.DATE)));
		modify.prefix(p_time);
		
		storeClient.executeUpdate(modify.getQueryString());
	}
	
	LocalDate getLatestUpdate() {
		Iri stations = iri("http://environment.data.gov.uk/flood-monitoring/id/stations");
		SelectQuery query = Queries.SELECT();
        Variable instant = query.var();
        Variable date = query.var();
		
		GraphPattern queryPattern = GraphPatterns.and(stations.has(hasTime,instant),
				instant.has(inXSDDate, date));
		
		// descending date
		OrderCondition dateDesc = SparqlBuilder.desc(date);
		
		query.select(date).prefix(p_time).where(queryPattern).orderBy(dateDesc).limit(1);
		
		JSONArray queryResult = storeClient.executeQuery(query.getQueryString());
		
		try {
			String latestDate = queryResult.getJSONObject(0).getString(date.getQueryString().substring(1));
			return LocalDate.parse(latestDate);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			LOGGER.error("Failed to query latest update date");
			throw new JPSRuntimeException(e);
		}
	}
	
	/**
	 * returns true if provided data on the given date exists
	 * @param date
	 * @return
	 */
	boolean checkUpdateDateExists(LocalDate date) {
		Iri stations = iri("http://environment.data.gov.uk/flood-monitoring/id/stations");
		SelectQuery query = Queries.SELECT();
		Variable instant = query.var();
		
		GraphPattern queryPattern = GraphPatterns.and(stations.has(hasTime,instant),
				instant.has(inXSDDate, Rdf.literalOfType(date.toString(), XSD.DATE)));
		
		query.prefix(p_time).where(queryPattern);
		
		JSONArray queryResult = storeClient.executeQuery(query.getQueryString());
		
		if (queryResult.length() == 1)
			return true;
		else {
			return false;
		}
	}
	
	/**
	 * returns a map of station iri to station object
	 */
	Map<String, Station> getStationsWithCoordinates(String southwest, String northeast) {
		Iri lat_prop = iri("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
		Iri lon_prop = iri("http://www.w3.org/2003/01/geo/wgs84_pos#long");
		Iri river_prop = iri("http://environment.data.gov.uk/flood-monitoring/def/core/riverName");
		Iri catchment_prop = iri("http://environment.data.gov.uk/flood-monitoring/def/core/catchmentName");
		Iri town_prop = iri("http://environment.data.gov.uk/flood-monitoring/def/core/town");
		Iri dateOpen_prop = iri("http://environment.data.gov.uk/flood-monitoring/def/core/dateOpened");
		
		SelectQuery query = Queries.SELECT();
		
		// station properties
		Variable lat = query.var();
		Variable lon = query.var();
		Variable station = query.var();
		Variable ref = query.var();
		Variable id = query.var();
		Variable river = query.var();
		Variable catchment = query.var();
		Variable town = query.var();
		Variable dateOpened = query.var();
		Variable label = query.var();
		
		// measure properties
		Variable measure = query.var();
		// e.g. table name: Water Level (Tidal Level), param = Water Level,
    	// qual (param subtype) = Tidal Level
    	Variable param = query.var();
    	Variable qual = query.var();
    	Variable unit = query.var();
		
		GraphPattern queryPattern = GraphPatterns.and(station.has(lat_prop,lat)
				.andHas(lon_prop,lon).andHas(stationReference,ref).andHas(hasVisID, id).andHas(measures, measure));
		
		GraphPattern stationProperties = GraphPatterns.and(station.has(iri(RDFS.LABEL), label).optional(),
				station.has(river_prop, river).optional(),
				station.has(catchment_prop, catchment).optional(),
				station.has(town_prop, town).optional(),
				station.has(dateOpen_prop, dateOpened).optional());
		
		GraphPattern measurePropertiesPattern = measure.has(parameterName,param).andHas(qualifier,qual).andHas(unitName, unit);
		
		// restrict query location
		if (southwest != null && northeast != null) {
			GraphPattern coordinatesPattern = GraphPatterns.and(station.has(p_geo.iri("search"), "inRectangle")
					.andHas(p_geo.iri("searchDatatype"),lat_lon)
					.andHas(p_geo.iri("predicate"), hasCoordinates)
					.andHas(p_geo.iri("spatialRectangleSouthWest"), southwest)
					.andHas(p_geo.iri("spatialRectangleNorthEast"), northeast));

	    	GraphPattern geoPattern = new ServicePattern(p_geo.iri("search").getQueryString()).service(coordinatesPattern);
	    	query.where(queryPattern,geoPattern,stationProperties,measurePropertiesPattern).prefix(p_geo,p_station);
		} else {
			query.where(queryPattern,stationProperties,measurePropertiesPattern).prefix(p_station);
		}
		
		query.select(station,lat,lon,ref,id,river,catchment,town,dateOpened,label,measure,param,qual,unit);
		
		JSONArray queryResult = storeClient.executeQuery(query.getQueryString());
		
		Map<String, Station> station_map = new HashMap<>(); // iri to station object map
		for (int i = 0; i < queryResult.length(); i++) {
			String stationIri = queryResult.getJSONObject(i).getString(station.getQueryString().substring(1));
			String measureIri = queryResult.getJSONObject(i).getString(measure.getQueryString().substring(1));
    		String measureName = queryResult.getJSONObject(i).getString(param.getQueryString().substring(1));
    		String subTypeName = queryResult.getJSONObject(i).getString(qual.getQueryString().substring(1));
    		String unitName = queryResult.getJSONObject(i).getString(unit.getQueryString().substring(1));
    		
    		Station stationObject;
    		if (station_map.containsKey(stationIri)) {
    			stationObject = station_map.get(stationIri);
    		} else {
    			stationObject = new Station(stationIri);
    			station_map.put(stationIri, stationObject);
    			
    			// station properties are unique, only need to set once
    			stationObject.setIdentifier(queryResult.getJSONObject(i).getString(ref.getQueryString().substring(1)));
    			stationObject.setLat(queryResult.getJSONObject(i).getDouble(lat.getQueryString().substring(1)));
    			stationObject.setLon(queryResult.getJSONObject(i).getDouble(lon.getQueryString().substring(1)));
    			stationObject.setVisId(queryResult.getJSONObject(i).getInt(id.getQueryString().substring(1)));
    			
    			// optional station properties
    			if (queryResult.getJSONObject(i).has(river.getQueryString().substring(1))) {
    				stationObject.setRiver(queryResult.getJSONObject(i).getString(river.getQueryString().substring(1)));
    			}
    			if (queryResult.getJSONObject(i).has(catchment.getQueryString().substring(1))) {
    				stationObject.setCatchment(queryResult.getJSONObject(i).getString(catchment.getQueryString().substring(1)));
    			}
    			if (queryResult.getJSONObject(i).has(town.getQueryString().substring(1))) {
    				stationObject.setTown(queryResult.getJSONObject(i).getString(town.getQueryString().substring(1)));
    			}
    			if (queryResult.getJSONObject(i).has(dateOpened.getQueryString().substring(1))) {
    				stationObject.setDateOpened(queryResult.getJSONObject(i).getString(dateOpened.getQueryString().substring(1)));
    			}
    			if (queryResult.getJSONObject(i).has(label.getQueryString().substring(1))) {
    				stationObject.setLabel(queryResult.getJSONObject(i).getString(label.getQueryString().substring(1)));
    			}
    		}
			
			// measure properties
			// stations may measure more than 1 properties
			stationObject.addMeasure(measureIri);
			stationObject.setMeasureName(measureIri, measureName);
    		stationObject.setMeasureSubTypeName(measureIri, subTypeName);
    		stationObject.setMeasureUnit(measureIri, unitName);
		}
				
		return station_map;
	}
    
    boolean checkStationExists(String station) {
    	SelectQuery query = Queries.SELECT();
    	
    	GraphPattern queryPattern = iri(station).isA(Station);
    	
    	query.prefix(p_station).where(queryPattern);
    	
	    if(storeClient.executeQuery(query.getQueryString()).length() == 1) {
	    	return true;
	    } else {
	    	return false;
	    }
    }
    
    void addNewStation(String station, double lat, double lon, String name) {
    	ModifyQuery modify = Queries.MODIFY();
    	Iri station_iri = iri(station);
    	
    	// blazegraph coordinates
    	String blazegraph_latlon = String.valueOf(lat) + "#" + String.valueOf(lon);
    	StringLiteral coordinatesLiteral = Rdf.literalOfType(blazegraph_latlon, 
    			lat_lon);
    	modify.insert(station_iri.has(hasCoordinates,coordinatesLiteral));
    	
    	modify.insert(station_iri.isA(Station));
    	modify.insert(station_iri.has(stationReference, name));
    	modify.insert(station_iri.has(hasVisID, getNumID()+1));
    	modify.prefix(p_station);
    	
    	storeClient.executeUpdate(modify.getQueryString());
    }
    
    /**
     * returns number of IDs currently in the kg, to generate a unique ID
     */
    int getNumID() {
    	SelectQuery query = Queries.SELECT();
    	
    	GraphPattern queryPattern = query.var().has(hasVisID,query.var());
    	
    	query.where(queryPattern);
    	
    	JSONArray queryResult = storeClient.executeQuery(query.getQueryString());
    	
    	return queryResult.length();
    }
}
