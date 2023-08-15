package uk.ac.cam.cares.jps.agent.dashboard.stack.sparql.utils;

/**
 * A class that generates the required SPARQL queries.
 *
 * @author qhouyee
 */
public class SparqlQuery {

    /**
     * Generate a simple SPARQL query for facilities. This query is meant to detect which namespace contains the
     * building hierarchy. Even one result will indicate the namespace contains the hierarchy.
     *
     * @return The query for execution.
     */
    public static String genSimpleFacilityQuery() {
        StringBuilder query = new StringBuilder();
        query.append(genPrefixes())
                .append("SELECT DISTINCT ?facilityname ")
                .append("WHERE {")
                .append(genFacilitySyntax())
                // Limit the results to reduce performance overhead
                .append("} LIMIT 1");
        return query.toString();
    }

    /**
     * Generates a dynamic query for all measures that can be retrieved from the current namespace
     * and external namespace holding the time series triples.
     *
     * @param endpoint The non-spatial zone SPARQL endpoint.
     * @return The query for execution.
     */
    public static String genFacilityMeasureQuery(String endpoint) {
        StringBuilder query = new StringBuilder();
        query.append(genPrefixes())
                .append("SELECT DISTINCT ?facilityname ?elementname ?elementtype ?measure ?measurename ?timeseries ")
                .append("WHERE {")
                // Query to get assets within a facility
                .append(genFacilitySyntax())
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
                .append("{?element ontodevice:measures/om:hasValue ?measure.            ?measure rdfs:label ?measurename.}")
                .append("UNION {?element ontodevice:observes ?measure.                  ?measure rdfs:label?measurename.}")
                .append("UNION {?sensor ontodevice:measures/om:hasValue ?measure.   ?measure rdfs:label ?measurename.}")
                .append("UNION {?sensor ontodevice:observes ?measure.               ?measure rdfs:label ?measurename.}")
                .append("} UNION {")
                // For non-sensor devices which is being measured by sensors attached to them
                .append("?element ontodevice:hasOperatingRange/ssn:hasOperatingProperty/ontodevice:hasQuantity/om:hasValue ?measure. ")
                .append("?measure rdfs:label ?measurename.")
                .append("}")
                // Once retrieved, all measures has a time series
                .append("?measure ontotimeseries:hasTimeSeries ?timeseries.")
                .append("}")
                // If there is a sub element name, append it to the element label to get element name
                // Else, element label should be the element name
                .append("BIND(IF(BOUND(?subelementname), CONCAT(?elementlabel, \" \", ?subelementname), ?elementlabel) AS ?elementname)")
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
                .append("?facility rdfs:label ?facilityname;")
                .append("   ontobim:hasRoom ?room.")
                .append("?room rdf:type ontobim:Room;")
                .append("   bot:containsElement ?element.")
                .append("?element rdfs:label ?elementlabel;")
                .append("   rdf:type ?elementtype.");
        return query;
    }
}
