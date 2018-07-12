/*
 * 
 */
package uk.ac.cam.ceb.como.compchem.ontology.query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

import org.apache.jena.rdf.model.ModelFactory;

import org.apache.jena.util.FileManager;

/**
 * The Class CompChemQuery.
 *
 * @author nk510
 *  <p>This code runs sparql queries on RDF files generated by using XSLT transformations. RDF
 *         files are Abox of Ontochem ontology, and stored in 'ontochem_abox'
 *         folder. To query it, we use Jena api. Java code searches all sparql
 *         queries saved in folder 'sparql_query', runs it and results of these
 *         queries are saved in 'sparql_results' folder by using json format.</p> 
 */

public class CompChemQuery {
	
	/** The Constant TBOX_SOURCE. */
	public static final String TBOX_SOURCE = "./src/test/resources/ontology/ontochem_ontology/ontochem.spin.rdf";
	public static final String ABOX_SOURCE="./src/test/resources/ontology/ontochem_abox/";
	public static final String TRAGET_FOLDER="./src/test/resources/ontology/sparql_results/";
	public static final String SPARQL_FOLDER ="./src/test/resources/ontology/sparql_query/";
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	
	public static void main(String[] args) throws IOException {
		
		long startT = System.nanoTime();
		
		File[] sparqlFileList = getFileList(SPARQL_FOLDER);
		
		File[] aboxFileList = getFileList(ABOX_SOURCE);

		for (File aboxFile : aboxFileList) {
			
			for(File sparqlFile: sparqlFileList) {
				
			OntModel model = getOntModel(TBOX_SOURCE, aboxFile.getAbsolutePath());
			
			String q = FileUtils.readFileToString(sparqlFile);
			
			performQuery(model, q, aboxFile.getName(), TRAGET_FOLDER);
			
			}
		}
		
		long endT = System.nanoTime();
		System.out.println("time: " + (endT-startT)/1000000000 + " sec.");
		
	}
	
	/**
	 * Gets the ont model.
	 *
	 * @param tboxSource the ontochem ontology (tbox source)
	 * @param aboxSource the ontochem data assertions (abox source)
	 * @return model Gets instance of OntModel.
	 */

	public static OntModel getOntModel(String tboxSource, String aboxSource) {

		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);

		FileManager.get().readModel(model, aboxSource);
		FileManager.get().readModel(model, tboxSource);

		return model;

	}

	/**
	 * Gets the ont model.
	 *
	 * @param aboxSource the ontochem data assertions (abox source)
	 * @return model Gets instance of OntModel.
	 */

	public static OntModel getOntModel(String aboxSource) {

		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);

		FileManager.get().readModel(model, aboxSource);
		
		return model;

	}
	
	/**
	 * Perform query.
	 *
	 * @param model the instance of OntModel
	 * @param queryString the query string
	 * @param fileName the file name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	
	public static void performQuery(OntModel model, String queryString, String fileName, String targetFolder) throws IOException {
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, model);		
		
		FileOutputStream fileOutputStream = null;
		
		try {

			ResultSet resultSet = qexec.execSelect();
			
			while(resultSet.hasNext()) {			 
		    
		    fileOutputStream=new FileOutputStream(new File(targetFolder+ StringUtils.substringBefore(fileName, ".")  +".json"),false);
	       		
			ResultSetFormatter.outputAsJSON(fileOutputStream, resultSet);

			}

		} finally {
			qexec.close();
			fileOutputStream.close();
		}	
	}
	
	/**
	 * Gets the file list.
	 *
	 * @author nk510
	 * @param folderPath the folder path
	 * @return the file list
	 */
	
	public static File[] getFileList(String folderPath) {

		File dir = new File(folderPath);
		
		File[] fileList = dir.listFiles(new FilenameFilter(){
			
			public boolean accept(File dir, String name) {
				
			return (name.endsWith(".sparql") || name.endsWith(".owl"));			
			}
		});

		return fileList;		
	}
	
	/**
	 * Gets json file list.
	 *
	 * @author nk510
	 * @param folderPath the folder path
	 * @return the file list
	 */
	
	public static File[] getJSONFileList(String folderPath) {

		File dir = new File(folderPath);
		
		File[] fileList = dir.listFiles(new FilenameFilter(){
			
			public boolean accept(File dir, String name) {
				
			return (name.endsWith(".json"));			
			}
		});

		return fileList;		
	}	
}