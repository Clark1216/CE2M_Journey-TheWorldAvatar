package uk.ac.cam.cares.jps.agent.assetmanager;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.base.Sys;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;
import static org.mockito.ArgumentMatchers.isA;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.semanticweb.owlapi.util.IRIComparator;
import org.springframework.test.context.transaction.BeforeTransaction;

import com.github.stefanbirkner.systemlambda.SystemLambda;

import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;
import wiremock.com.jayway.jsonpath.internal.function.text.Length;
import wiremock.org.eclipse.jetty.util.ajax.JSON;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class AssetManagerAgentTest {
    // Temporary folder to place a properties file
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Container
	private GenericContainer<?> blazegraph = new GenericContainer<>(DockerImageName.parse("ghcr.io/cambridge-cares/blazegraph_for_tests:1.0.0"))
													 .withExposedPorts(9999);

    private AssetManagerAgent agent;
    RemoteStoreClient storeClient;
    
    JSONObject exampleRequest;
    JSONObject exampleFind;
    JSONObject exampleEmptyMap;
    JSONObject exampleAddQuery;

    //Blazegraph endpoint
    private String sparql_endpoint;
    private String ontodev_endpoint = "http://10.25.188.58:3838/blazegraph/namespace/ontodevice/sparql";
    private String saref_endpoint = "http://10.25.188.58:3838/blazegraph/namespace/ontodevice/sparql";

    private String reqBody = "com.bigdata.rdf.store.AbstractTripleStore.textIndex=false\r\n"+
    "com.bigdata.rdf.store.AbstractTripleStore.axiomsClass=com.bigdata.rdf.axioms.NoAxioms\r\n"+
    "com.bigdata.rdf.sail.isolatableIndices=false\r\n"+
    "com.bigdata.rdf.sail.truthMaintenance=false\r\n"+
    "com.bigdata.rdf.store.AbstractTripleStore.justify=false\r\n"+
    "com.bigdata.rdf.sail.namespace=testDeriv\r\n"+
    "com.bigdata.namespace.testDeriv.spo.com.bigdata.btree.BTree.branchingFactor=1024\r\n"+
    "com.bigdata.rdf.store.AbstractTripleStore.quads=false\r\n"+
    "com.bigdata.namespace.testDeriv.lex.com.bigdata.btree.BTree.branchingFactor=400\r\n"+
    "com.bigdata.journal.Journal.groupCommit=false\r\n"+
    "com.bigdata.rdf.store.AbstractTripleStore.geoSpatial=false\r\n"+
    "com.bigdata.rdf.store.AbstractTripleStore.statementIdentifiers=false";


    @Rule
    private void writePropertyFile(String filepath, List<String> properties) throws IOException {
        // Overwrite potentially existing properties file
        FileWriter writer = new FileWriter(filepath, false);
        // Populate file
        for (String s : properties) {
            writer.write(s + "\n");
        }
        // Close the file and return the file
        writer.close();
    }

    @Before
    public void initializeLauncher() throws IOException {
        // Create a properties file that points to a dummy mapping folder //
        // Create an empty folder
        String folderName = "config";
        File mappingFolder;
        mappingFolder = folder.newFolder(folderName);

        // Filepath for the properties file
        
        String agentPropFile = Paths.get(folder.getRoot().toString(), "agent.properties").toString();
        String ontoMapPropFile = Paths.get(folder.getRoot().toString(), "ontologyMap.properties").toString();
        String tsSearchPropFile = Paths.get(folder.getRoot().toString(), "tsSearch.properties").toString();
        

        String[] clientPropParam = {"sparql.query.endpoint="+sparql_endpoint, "sparql.update.endpoint="+sparql_endpoint};
        writePropertyFile(agentPropFile, Arrays.asList(clientPropParam));

        // Create an empty folder for storing manuals
        String manualsName = "manuals";
        File manualFolder;
        manualFolder = folder.newFolder(manualsName);

        //Set the RemoteStoreClient
        storeClient =  new RemoteStoreClient(sparql_endpoint, sparql_endpoint);
        //storeClient = queryBuilder.storeClient;
        // To create testAgent without an exception being thrown, SystemLambda is used to mock an environment variable
        // To mock the environment variable, a try catch need to be used
        try {
        	SystemLambda.withEnvironmentVariable("AGENTPROPERTIES", mappingFolder.getCanonicalPath()).execute(() -> {
                agent = new AssetManagerAgent();
        	 });
        }
        // There should not be any exception thrown as the agent is initiated correctly
        catch (Exception e) {
            System.out.println(e);
            throw new IOException(e);
        }

        
    }
    
    public static JSONObject parseJSONFile(String filename) throws JSONException, IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        return new JSONObject(content);
    }

    @Before
    public void createExampleData () throws IOException{
        File file = new File("./src/test/java/uk/ac/cam/cares/jps/agent/devinst/exampleRequests/exampleRequest.json");
        //for(String fileNames : file.list()) System.out.println(fileNames);
        //System.out.println(file.exists());
        JSONObject desc = parseJSONFile("./src/test/java/uk/ac/cam/cares/jps/agent/devinst/exampleRequests/exampleRequest.json");
        //exampleRequest.put(KEY_CLIENTPROPERTY, KEY_CLIENTPROPERTY);
        //exampleRequest.put(KEY_DESCRIPTOR, desc);
    }

    @Ignore("Test containers requires docker to function")
    @Before
    public void startContainers() throws IOException, URISyntaxException {
		try {
			// Start Blazegraph container
			blazegraph.start();
		} catch (Exception e) {
			throw new JPSRuntimeException("Docker container startup failed. Please try running tests again");
		}

        //URI for blazegraph endpoint
        URIBuilder builder = new URIBuilder().setScheme("http").setHost(blazegraph.getHost()).setPort(blazegraph.getFirstMappedPort()).setPath("/blazegraph/namespace");

        // create a new namespace (endpoint) on blazegraph with geospatial enabled
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost postRequest = new HttpPost(builder.build());
		postRequest.setEntity(new StringEntity(reqBody, ContentType.DEFAULT_TEXT));
		CloseableHttpResponse response = httpclient.execute(postRequest);
		
        //For some reason the getFirstHeader/ getLastHeader returns null
		//sparql_endpoint = response.getFirstHeader("Location").getValue();
        sparql_endpoint = new URIBuilder().setScheme("http").setHost(blazegraph.getHost()).setPort(blazegraph.getFirstMappedPort()).setPath("/blazegraph/namespace/testDeriv/sparql").toString();

    }

    @Ignore("Test containers requires docker to function")
    @Before
    public void initializeAgent() throws IOException {
        // Create a properties file that points to a dummy mapping folder //
        // Create an empty folder
        String folderName = "mappings";
        File mappingFolder = folder.newFolder(folderName);

        // Filepath for the properties file
        
        String clientPropFile = Paths.get(folder.getRoot().toString(), "client.properties").toString();
        
        String[] clientPropParam = {"sparql.query.endpoint="+sparql_endpoint, "sparql.update.endpoint="+sparql_endpoint, "ontodev.query.endpoint="+ontodev_endpoint, "saref.query.endpoint="+saref_endpoint};
        writePropertyFile(clientPropFile, Arrays.asList(clientPropParam));

        //Set the RemoteStoreClient
        storeClient =  new RemoteStoreClient(sparql_endpoint, sparql_endpoint);
        //ontodevClient = new RemoteStoreClient(ontodev_endpoint);
        //sarefClient = new RemoteStoreClient(saref_endpoint);
        //storeClient = queryBuilder.storeClient;
        // To create testAgent without an exception being thrown, SystemLambda is used to mock an environment variable
        // To mock the environment variable, a try catch need to be used
        /*
        try {
        	SystemLambda.withEnvironmentVariable("TEST_MAPPINGS", mappingFolder.getCanonicalPath()).execute(() -> {
                 queryBuilder = new DevInstQueryBuilder(storeClient, ontodevClient, sarefClient);
        	 });
        }
        // There should not be any exception thrown as the agent is initiated correctly
        catch (Exception e) {
            System.out.println(e);
            throw new IOException(e);
        }
        // Set the mocked time series client
        //testAgent.setTsClient(mockTSClient);

        */
    }

}