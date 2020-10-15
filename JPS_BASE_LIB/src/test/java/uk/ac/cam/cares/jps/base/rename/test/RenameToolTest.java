package uk.ac.cam.cares.jps.base.rename.test;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.junit.Test;

import uk.ac.cam.cares.jps.base.rename.RenameTool;

public class RenameToolTest {

	
	//additional tests for private methods to build queries 
	
	@SuppressWarnings("unused")
	@Test
	public void testReplaceRdf4j() throws SQLException, ParseException {
	
		String dataSetURL = "http://localhost:8080/rdf4j-server/repositories/species/statements"; 
		String type = "rdf4j";
		
		String target = null;
		String replacement = null;
		
		if (false) {
			target = "http://www.w3.org/2008/05/skos#altLabel";
			replacement = "http://www.example.com/Test";
		}else {
			target = "http://www.example.com/Test";
			replacement = "http://www.w3.org/2008/05/skos#altLabel";
		}
		
		RenameTool.renameURI(dataSetURL, type, target, replacement);
		
		// test using query
		fail("Not yet implemented");
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testReplaceRdf4jString() throws SQLException, ParseException {
	
		String dataSetURL = "http://localhost:8080/rdf4j-server/repositories/species/statements"; 
		String type = "rdf4j";
		
		String target = null;
		String replacement = null;
		
		if (false) {
			target = "05/skos#altLabel";
			replacement = "/Test";
		}else {
			target = "/Test";
			replacement = "05/skos#altLabel";
		}
		
		RenameTool.renameURI(dataSetURL, type, target, replacement);
		
		// test using query
		fail("Not yet implemented");
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testReplaceBlazegraph() throws SQLException, ParseException {
	
		String dataSetURL = "http://localhost:8080/blazegraph/namespace/species/update"; 
		String type = "blazegraph";
		
		String target = null;
		String replacement = null;
		
		if (false) {
			target = "http://www.w3.org/2008/05/skos#altLabel";
			replacement = "http://www.example.com/Test";
		}else {
			target = "http://www.example.com/Test";
			replacement = "http://www.w3.org/2008/05/skos#altLabel";
		}
		
		RenameTool.renameURI(dataSetURL, type, target, replacement);

		// test using query
		fail("Not yet implemented");	
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testReplaceFuseki() throws SQLException, ParseException {
	
		String dataSetURL = "http://localhost:8080/fuseki/species/update";
		String type = "fuseki";
		
		String target = null;
		String replacement = null;
		
		if (false) {
			target = "http://www.w3.org/2008/05/skos#altLabel";
			replacement =  "http://www.example.com/Test";
		}else {
			target = "http://www.example.com/Test";
			replacement = "http://www.w3.org/2008/05/skos#altLabel";
		}
		
		RenameTool.renameURI(dataSetURL, type, target, replacement);
		
		// test using query
		fail("Not yet implemented");
	}	
	
	@SuppressWarnings("unused")
	@Test
	public void testReplaceLocal() throws SQLException, ParseException {
	
		String dataSetURL = "C:\\Users\\CLIN01\\Documents\\Codes\\JParkSimulator4-git\\JPS_BASE_LIB\\src\\test\\resources\\species.owl"; 
		String type = "owl-file";
		
		String target = null;
		String replacement = null;
		
		if (false) {
			target = "http://www.w3.org/2008/05/skos#altLabel";
			replacement =  "http://www.example.com/Test";
		}else {
			target = "http://www.example.com/Test";
			replacement = "http://www.w3.org/2008/05/skos#altLabel";
		}
		
		RenameTool.renameURI(dataSetURL, type, target, replacement);
		 
		// test using query
		fail("Not yet implemented");
	}
}