package uk.ac.cam.cares.jps.agent.dashboard.stack.sparql.utils;

import uk.ac.cam.cares.jps.agent.dashboard.utils.StringHelper;

/**
 * A class that generates the required SPARQL queries.
 *
 * @author qhouyee
 */
public class SparqlQuery {
    public static final String FACILITY_NAME = "facilityname";
    public static final String ROOM_NAME = "roomname";
    public static final String ELEMENT_NAME = "elementname";
    public static final String ELEMENT_TYPE = "elementtype";
    public static final String MEASURE = "measure";
    public static final String MEASURE_NAME = "measurename";
    public static final String UNIT = "unit";
    public static final String TIME_SERIES = "timeseries";

    /**
     * Generate a simple SPARQL query for facilities. This query is meant to detect which namespace contains the
     * building hierarchy. Even one result will indicate the namespace contains the hierarchy.
     *
     * @return The query for execution.
     */
    public static String genSimpleFacilityQuery() {
        StringBuilder query = new StringBuilder();
        query.append(genPrefixes())
                .append("SELECT DISTINCT")
                .append(StringHelper.formatSparqlVarName(FACILITY_NAME))
                .append(" WHERE {")
                .append(genFacilitySyntax())
                // Limit the results to reduce performance overhead
                .append("} LIMIT 1");
        return query.toString();
    }

    /**
     * Generates a dynamic query for all measures associated with rooms
     * that can be retrieved from the current and external namespace holding the time series triples.
     *
     * @return The query for execution.
     */
    public static String genFacilityRoomMeasureQuery() {
        StringBuilder query = new StringBuilder();
        query.append(genPrefixes())
                .append("SELECT DISTINCT")
                .append(StringHelper.formatSparqlVarName(FACILITY_NAME))
                .append(StringHelper.formatSparqlVarName(ROOM_NAME))
                .append(StringHelper.formatSparqlVarName(MEASURE))
                .append(StringHelper.formatSparqlVarName(MEASURE_NAME))
                .append(StringHelper.formatSparqlVarName(UNIT))
                .append(StringHelper.formatSparqlVarName(TIME_SERIES))
                .append(" WHERE {")
                // Query to get assets within a facility
                .append(genFacilitySyntax())
                .append("?room ontobim:hasIfcRepresentation/rdfs:label").append(StringHelper.formatSparqlVarName(ROOM_NAME)).append(".")
                // Possible measures are temperature and relative humidity
                .append("{?room ontodevice:hasTemperature/om:hasValue").append(StringHelper.formatSparqlVarName(MEASURE)).append("}")
                .append("UNION {?room ontodevice:hasRelativeHumidity/om:hasValue").append(StringHelper.formatSparqlVarName(MEASURE)).append("}")
                .append(genCommonMeasureSyntax())
                .append("}");
        return query.toString();
    }

    /**
     * Generates a dynamic query for all measures associated with assets
     * that can be retrieved from the current and external namespace holding the time series triples.
     *
     * @param endpoint The non-spatial zone SPARQL endpoint.
     * @return The query for execution.
     */
    public static String genFacilityAssetMeasureQuery(String endpoint) {
        StringBuilder query = new StringBuilder();
        query.append(genPrefixes())
                .append("SELECT DISTINCT")
                .append(StringHelper.formatSparqlVarName(FACILITY_NAME))
                .append(StringHelper.formatSparqlVarName(ELEMENT_NAME))
                .append(StringHelper.formatSparqlVarName(ELEMENT_TYPE))
                .append(StringHelper.formatSparqlVarName(MEASURE))
                .append(StringHelper.formatSparqlVarName(MEASURE_NAME))
                .append(StringHelper.formatSparqlVarName(UNIT))
                .append(StringHelper.formatSparqlVarName(TIME_SERIES))
                .append(" WHERE {")
                // Query to get assets within a facility
                .append(genFacilitySyntax())
                .append("?room bot:containsElement ?element.")
                .append("?element rdfs:label ?elementlabel;")
                .append("   rdf:type").append(StringHelper.formatSparqlVarName(ELEMENT_TYPE)).append(".")
                // Query to retrieve the time series associated with devices at a separate endpoint
                .append(" SERVICE <").append(endpoint).append(">{")
                // The below line performs a recursive query to retrieve all sub devices in the possible permutations of:
                // Device sendsSignalTo subDevice; sendsSignalTo/consistsOf subDevice; consistsOf subDevice;
                // consistsOf/sendsSignalTo subDevice; consistsOf/sendsSignalTo/consistsOf subDevice
                .append("{")
                // Sensors may be linked to the element in two ways
                // First way is through sendsSignalTo and consistsOf
                .append("{?element ontodevice:sendsSignalTo*/saref:consistsOf*/ontodevice:sendsSignalTo*/saref:consistsOf* ?sensor.}")
                // Second way is through consistsOf and isAttachedTo
                .append("UNION { ?subelement rdfs:label ?subelementname; ^saref:consistsOf ?element; ^ontodevice:isAttachedTo ?sensor.}")
                // Retrieve the measure and its name associated with either the element or their subdevices
                .append("{?element ontodevice:measures/om:hasValue").append(StringHelper.formatSparqlVarName(MEASURE)).append(".}")
                .append("UNION {?element ontodevice:observes").append(StringHelper.formatSparqlVarName(MEASURE)).append(".}")
                .append("UNION {?sensor ontodevice:measures/om:hasValue").append(StringHelper.formatSparqlVarName(MEASURE)).append(".}")
                .append("UNION {?sensor ontodevice:observes").append(StringHelper.formatSparqlVarName(MEASURE)).append(".}")
                .append("} UNION {")
                // For non-sensor devices which is being measured by sensors attached to them
                .append("?element ontodevice:hasOperatingRange/ssn:hasOperatingProperty/ontodevice:hasQuantity/om:hasValue").append(StringHelper.formatSparqlVarName(MEASURE)).append(".")
                .append("}")
                .append(genCommonMeasureSyntax())
                .append("}")
                // If there is a sub element name, append it to the element label to get element name
                // Else, element label should be the element name
                .append("BIND(IF(BOUND(?subelementname), CONCAT(?elementlabel, \" \", ?subelementname), ?elementlabel) AS").append(StringHelper.formatSparqlVarName(ELEMENT_NAME)).append(")")
                .append("}");
        return query.toString();
    }

    /**
     * Generate the prefixes for all queries.
     *
     * @return The prefixes as a string builder.
     */
    private static StringBuilder genPrefixes() {
        StringBuilder query = new StringBuilder();
        query.append("PREFIX bot:<https://w3id.org/bot#>")
                .append("PREFIX ontobim:<https://www.theworldavatar.com/kg/ontobim/>")
                .append("PREFIX ontodevice:<https://www.theworldavatar.com/kg/ontodevice/>")
                .append("PREFIX ontotimeseries:<https://www.theworldavatar.com/kg/ontotimeseries/>")
                .append("PREFIX om:<http://www.ontology-of-units-of-measure.org/resource/om-2/>")
                .append("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>")
                .append("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>")
                .append("PREFIX saref:<https://saref.etsi.org/core/>")
                .append("PREFIX ssn:<http://www.w3.org/ns/ssn/systems/>");
        return query;
    }

    /**
     * Generate the common facility syntax for the corresponding queries.
     *
     * @return The syntax as a string builder.
     */
    private static StringBuilder genFacilitySyntax() {
        StringBuilder query = new StringBuilder();
        query.append("?building rdf:type bot:Building;")
                .append("   ontobim:hasFacility ?facility.")
                .append("?facility rdfs:label").append(StringHelper.formatSparqlVarName(FACILITY_NAME)).append(";")
                .append("   ontobim:hasRoom ?room.")
                .append("?room rdf:type ontobim:Room.");
        return query;
    }

    /**
     * Generate the common measure syntax for the corresponding queries.
     *
     * @return The syntax as a string builder.
     */
    private static StringBuilder genCommonMeasureSyntax() {
        StringBuilder query = new StringBuilder();
        // Retrieves measure name
        query.append(StringHelper.formatSparqlVarName(MEASURE)).append(" rdfs:label").append(StringHelper.formatSparqlVarName(MEASURE_NAME)).append(";")
                // Retrieves time series IRI
                .append("ontotimeseries:hasTimeSeries").append(StringHelper.formatSparqlVarName(TIME_SERIES)).append(".")
                // Retrieves unit if it is available
                .append("OPTIONAL{").append(StringHelper.formatSparqlVarName(MEASURE)).append(" om:hasUnit/om:symbol").append(StringHelper.formatSparqlVarName(UNIT)).append("}");
        return query;
    }
}
