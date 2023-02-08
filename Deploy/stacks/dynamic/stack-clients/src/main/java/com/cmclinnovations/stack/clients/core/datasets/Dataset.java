package com.cmclinnovations.stack.clients.core.datasets;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.json.JSONArray;

import com.cmclinnovations.stack.clients.blazegraph.BlazegraphClient;
import com.cmclinnovations.stack.clients.blazegraph.Namespace;
import com.cmclinnovations.stack.clients.geoserver.GeoServerStyle;
import com.cmclinnovations.stack.clients.postgis.PostGISClient;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.cam.cares.jps.base.derivation.ValuesPattern;

public class Dataset {

    public static final String NAME_KEY = "name";

    private String name;
    private String description;

    private final Path datasetDirectory;

    private final String database;
    private final Namespace namespace;
    private final String workspaceName;

    private final List<String> externalDatasets;
    private final List<DataSubset> dataSubsets;
    private final List<GeoServerStyle> geoserverStyles;
    private final List<String> ontopMappings;

    private final boolean skip;

    // for dcat cataloging
    private boolean exists; // used to determine whether this dataset exists in the catalog
    private String iri; // catalog iri

    @JsonCreator
    public Dataset(@JsonProperty(value = NAME_KEY) @JacksonInject(NAME_KEY) String name,
            @JsonProperty(value = "datasetDirectory") Path datasetDirectory,
            @JsonProperty(value = "database") String database,
            @JsonProperty(value = "namespace") Namespace namespace,
            @JsonProperty(value = "workspace") String workspaceName,
            @JsonProperty(value = "externalDatasets") List<String> externalDatasets,
            @JsonProperty(value = "dataSubsets") List<DataSubset> dataSubsets,
            @JsonProperty(value = "styles") List<GeoServerStyle> geoserverStyles,
            @JsonProperty(value = "mappings") List<String> ontopMappings,
            @JsonProperty(value = "skip") boolean skip) {
        this.name = name;
        this.datasetDirectory = datasetDirectory;
        this.database = database;
        this.namespace = namespace;
        this.workspaceName = workspaceName;
        this.externalDatasets = externalDatasets;
        this.dataSubsets = dataSubsets;
        this.geoserverStyles = geoserverStyles;
        this.ontopMappings = ontopMappings;
        this.skip = skip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return (null != description) ? description : getName();
    }

    public Path getDirectory() {
        return Path.of("/inputs", "data").resolve((null != datasetDirectory) ? datasetDirectory : Path.of(name));
    }

    public String getDatabase() {
        return getName();
    }

    public String getNamespace() {
        return getName();
    }

    public Properties getNamespaceProperties() {
        if (null != namespace && null != namespace.getProperties()) {
            return namespace.getProperties();
        } else {
            return new Properties();
        }
    }

    public String getWorkspaceName() {
        return getName();
    }

    public List<String> getExternalDatasets() {
        return (null != externalDatasets) ? externalDatasets : Collections.emptyList();
    }

    public List<DataSubset> getDataSubsets() {
        return (null != dataSubsets) ? dataSubsets : Collections.emptyList();
    }

    public List<GeoServerStyle> getGeoserverStyles() {
        return (null != geoserverStyles) ? geoserverStyles : Collections.emptyList();
    }

    public List<String> getOntopMappings() {
        return (null != ontopMappings) ? ontopMappings : Collections.emptyList();
    }

    public boolean isSkip() {
        return skip;
    }

    public String getQueryStringForCataloging() {
        // makes a sparql query and determine which dataset is already initialised
        // sets the iri if it is already initialised so that the timestamp can be modified
        setDatasetExistsAndIris();
        
        List<TriplePattern> insertTriples = new ArrayList<>();
        List<TriplePattern> deleteTriples = new ArrayList<>();

        LocalDateTime currentTime = LocalDateTime.now();
        
        SelectQuery query = Queries.SELECT(); //used to generate variable programmatically
        Iri catalogIri;
        if (!exists) {
            catalogIri = Rdf.iri(SparqlConstants.CREDO_NAMESPACE + UUID.randomUUID());

            insertTriples.add(catalogIri.isA(Rdf.iri(SparqlConstants.CATALOG))
            .andHas(Rdf.iri(SparqlConstants.TITLE), getName())
            .andHas(Rdf.iri(SparqlConstants.DESCRIPTION), getDescription())
            .andHas(Rdf.iri(SparqlConstants.MODIFIED), Rdf.literalOfType(currentTime.toString(), Rdf.iri(SparqlConstants.DATETIME))));
        } else {
            catalogIri = Rdf.iri(iri);
            Variable catalogTime = query.var();
            deleteTriples.add(catalogIri.has(Rdf.iri(SparqlConstants.MODIFIED), catalogTime));

            insertTriples.add(catalogIri
            .has(Rdf.iri(SparqlConstants.MODIFIED), Rdf.literalOfType(currentTime.toString(), Rdf.iri(SparqlConstants.DATETIME))));
        }

        
        getDataSubsets().stream().forEach(dataSubset -> {
            if (!dataSubset.getExists()) {
                String dataSubSetType = dataSubset.getRdfType();
                if (dataSubset.getRdfType() == null) {
                    dataSubSetType = SparqlConstants.DATASET;
                }
    
                Iri dataSetIri = Rdf.iri(SparqlConstants.CREDO_NAMESPACE + UUID.randomUUID());
                insertTriples.add(catalogIri.has(Rdf.iri(SparqlConstants.HAS_PART), dataSetIri)); 
                insertTriples.add(dataSetIri.isA(Rdf.iri(dataSubSetType))
                .andHas(Rdf.iri(SparqlConstants.MODIFIED), Rdf.literalOfType(currentTime.toString(), Rdf.iri(SparqlConstants.DATETIME)))
                .andHas(Rdf.iri(SparqlConstants.TITLE), dataSubset.getName())
                .andHas(Rdf.iri(SparqlConstants.DESCRIPTION), dataSubset.getDescription()));
    
                String kgUrl = BlazegraphClient.getInstance().getEndpoint().getUrl(getNamespace());
                Iri blazegraphService = Rdf.iri(SparqlConstants.CREDO_NAMESPACE + UUID.randomUUID());
    
                // append triples
                insertTriples.add(blazegraphService.isA(Rdf.iri(SparqlConstants.BLAZEGRAPH))
                .andHas(Rdf.iri(SparqlConstants.ENDPOINT_URL), kgUrl));
                
                Iri postgisService = null;
                if (dataSubset.usesPostGIS()) {
                    String jdbcUrl = PostGISClient.getInstance().getEndpoint().getJdbcURL(getDatabase());
    
                    // append triples
                    postgisService = Rdf.iri(SparqlConstants.CREDO_NAMESPACE + UUID.randomUUID());
                    insertTriples.add(postgisService.isA(Rdf.iri(SparqlConstants.POSTGIS))
                    .andHas(Rdf.iri(SparqlConstants.HAS_TIMESERIES_SCHEMA), SparqlConstants.TIMESERIES_SCHEMA)
                    .andHas(Rdf.iri(SparqlConstants.ENDPOINT_URL), jdbcUrl));
                } 
                
                // implementation not complete until we figure out the external URLs
                if (dataSubset.usesGeoServer()) {
                    if (postgisService == null) {
                        throw new RuntimeException("postgisService is null, a datasubset that uses geoserver but does not use postgis?");
                    } else {
                        Iri geoserverService = Rdf.iri(SparqlConstants.CREDO_NAMESPACE + UUID.randomUUID());
    
                        // append triples
                        insertTriples.add(geoserverService.isA(Rdf.iri(SparqlConstants.GEOSERVER))
                        .andHas(Rdf.iri(SparqlConstants.USES_DATABASE), postgisService));
                    }
                }
    
                if (!getOntopMappings().isEmpty()) {
                    if (postgisService == null) {
                        throw new RuntimeException("postgisService is null, a datasubset that uses ontop but does not use postgis?");
                    } else {
                        Iri ontopService = Rdf.iri(SparqlConstants.CREDO_NAMESPACE + UUID.randomUUID());
    
                        insertTriples.add(ontopService.isA(Rdf.iri(SparqlConstants.ONTOP))
                        .andHas(Rdf.iri(SparqlConstants.USES_DATABASE), postgisService));
                    }
                }
            } else {
                Variable dataSubsetTime = query.var();
                deleteTriples.add(Rdf.iri(dataSubset.getIri()).has(Rdf.iri(SparqlConstants.MODIFIED), dataSubsetTime));
                insertTriples.add(Rdf.iri(dataSubset.getIri())
                .has(Rdf.iri(SparqlConstants.MODIFIED), Rdf.literalOfType(currentTime.toString(), Rdf.iri(SparqlConstants.DATETIME))));
            }
        });

        ModifyQuery modify = Queries.MODIFY();
        TriplePattern[] insertTriplesAsArray = insertTriples.toArray(new TriplePattern[insertTriples.size()]);
        modify.insert(insertTriplesAsArray);

        if (!deleteTriples.isEmpty()) {
            TriplePattern[] deleteTriplesAsArray = deleteTriples.toArray(new TriplePattern[deleteTriples.size()]);
            modify.delete(deleteTriplesAsArray).where(deleteTriplesAsArray);
        }
        
        return modify.getQueryString();
    }

    /**
     * goes through catalog name and dataset names and check if they exist
     */
    private void setDatasetExistsAndIris() {
        List<String> dataSubsetNames = getDataSubsets().stream().map(DataSubset::getName).collect(Collectors.toList());

        SelectQuery query = Queries.SELECT();
        Variable catalogVar = query.var();
        Variable catalogNameVar = query.var();
        Variable dataSubsetVar = query.var();
        Variable dataSubsetNameVar = query.var();

        GraphPattern mainQuery = GraphPatterns.and(catalogVar.isA(Rdf.iri(SparqlConstants.CATALOG))
        .andHas(Rdf.iri(SparqlConstants.HAS_PART), dataSubsetVar)
        .andHas(Rdf.iri(SparqlConstants.TITLE), catalogNameVar),
        dataSubsetVar.has(Rdf.iri(SparqlConstants.TITLE), dataSubsetNameVar));

        ValuesPattern catalogValuesPattern = new ValuesPattern(catalogNameVar, Rdf.literalOf(getName()));
        ValuesPattern datasetValuesPattern = new ValuesPattern(dataSubsetNameVar, dataSubsetNames.stream().map(Rdf::literalOf).collect(Collectors.toList()));

        query.where(mainQuery, catalogValuesPattern, datasetValuesPattern).select(catalogVar, dataSubsetVar, dataSubsetNameVar);

        JSONArray queryResult = BlazegraphClient.getInstance().getRemoteStoreClient("kb").executeQuery(query.getQueryString());
        
        // catalog exists
        // used later to generate SPARQL update
        if (!queryResult.isEmpty()) {
            iri = queryResult.getJSONObject(0).getString(catalogVar.getQueryString().substring(1));
            exists = true;

            // then check each individual dataset
            Map<String, DataSubset> nameToDataSubsetMap = new HashMap<>();
            getDataSubsets().stream().forEach(dataSubset -> nameToDataSubsetMap.put(dataSubset.getName(), dataSubset));

            for (int i = 0; i < queryResult.length(); i++) {
                String queriedDatasubset = queryResult.getJSONObject(i).getString(dataSubsetVar.getQueryString().substring(1));
                String queriedDatasubsetName = queryResult.getJSONObject(i).getString(dataSubsetNameVar.getQueryString().substring(1));

                nameToDataSubsetMap.get(queriedDatasubsetName).setIri(queriedDatasubset);
                nameToDataSubsetMap.get(queriedDatasubsetName).setExists(true);
            }
        }
    }

    private class SparqlConstants {
        static final String CREDO_NAMESPACE = "http://theworldavatar.com/ontology/ontocredo/ontocredo.owl#";
        static final String DCAT_NAMESPACE = "http://www.w3.org/ns/dcat#";
        static final String DCTERMS_NAMESPACE = "http://purl.org/dc/terms/";
        static final String DATASET = DCAT_NAMESPACE + "Dataset";
        static final String TITLE = DCTERMS_NAMESPACE + "title";
        static final String DESCRIPTION = DCTERMS_NAMESPACE + "description";
        static final String CATALOG = DCAT_NAMESPACE + "Catalog";
        static final String MODIFIED = DCTERMS_NAMESPACE + "modified";
        static final String HAS_PART = DCTERMS_NAMESPACE + "hasPart";
        static final String DATETIME = "http://www.w3.org/2001/XMLSchema#dateTime";
        static final String BLAZEGRAPH = CREDO_NAMESPACE + "Blazegraph";
        static final String POSTGIS = CREDO_NAMESPACE + "PostGIS";
        static final String GEOSERVER = CREDO_NAMESPACE + "GeoServer";
        static final String ENDPOINT_URL = DCAT_NAMESPACE + "endpointURL";
        static final String TIMESERIES_SCHEMA = "timeseries";
        static final String HAS_TIMESERIES_SCHEMA = CREDO_NAMESPACE + "hasTimeSeriesSchema";
        static final String USES_DATABASE = CREDO_NAMESPACE + "usesDatabase";
        static final String ONTOP = CREDO_NAMESPACE + "Ontop";
    }
}
