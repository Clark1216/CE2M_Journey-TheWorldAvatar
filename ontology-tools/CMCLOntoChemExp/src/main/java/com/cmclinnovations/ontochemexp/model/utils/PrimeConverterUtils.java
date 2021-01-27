package com.cmclinnovations.ontochemexp.model.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;

import com.cmclinnovations.ontochemexp.model.converter.prime.PrimeConverter;
import com.cmclinnovations.ontochemexp.model.exception.OntoChemExpException;

/**
 * A utility class that supports the following functionalities:<p>
 * 1. Helps convert a file system path to a URL.
 * In particular, it supports the following string conversion features:</p>
 * a) the replacement of each single space (' ') of a path with an underscore ('_').</br>
 * b) the replacement of each backslash with a front slash.</br>
 * c) the addition of the protocol 'file:/' at the beginning of a path.</br>
 * d) the extraction of an experiment's name from a file path.</br>
 * e) the formation of a URL, suitable to be used in an OWL file, by combining
 * a file path and name.</p>
 * 2. Splits a space separated string and put each split part as a key and
 * the in
 * a Hashmap and 
 * 
 * 
 * @author Feroz Farazi (msff2@cam.ac.uk)
 *
 */
public class PrimeConverterUtils extends PrimeConverter{
	
	static Logger logger = org.slf4j.LoggerFactory.getLogger(PrimeConverterUtils.class);
	/**
	 * Replaces each space character of a path
	 * with an underscore.
	 * 
	 * @param path a path that is being processed to get
	 * spaces replaced by underscores.
	 * 
	 * @return a URL generated by replacing spaces with underscores
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors
	 */
	public static String convertToURLString(String path) throws OntoChemExpException{
		String urlString ="";
		if(path!=null){
			urlString = path.replace(' ', '_');
		} else{
			logger.error("The input path is null.");
			throw new OntoChemExpException("A null input path has been provided.");
		}
		return urlString;
	}
	
	/**
	 * Replaces each backslash of a path with a front slash to
	 * make it suitable to be used as a URL.
	 * 
	 * @param path a path that is being converted to a URL
	 * @return a URL after replacing the backslashes with the front.
	 * slashes.
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors.
	 */
	public static String formatToURLSlash(String path) throws OntoChemExpException{
		if(path==null){
			logger.error("The input path is null.");
			throw new OntoChemExpException("A null input path has been provided.");
		}
		if(path.contains("\\")){
			path = path.replace("\\", "/");
		}
		return path;
	}

	/**
	 * Adds the protocol 'file:/' at the beginning of a file path
	 * to form a URL that can be used in an OWL file as a URL.
	 * 
	 * @param path an absolute file path that needs to be converted
	 * to a URL that can be used in an OWL file.
	 * @return an OWL file formatted URL.
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors.
	 */
	public static String addFileProtocol(String path) throws OntoChemExpException{
		if(path==null){
			logger.error("The input path is null.");
			throw new OntoChemExpException("A null input path has been provided.");
		}
		if(!path.contains("file:/")){
			path = "file:/"+path;
		}
		return path;
	}
	
	/**
	 * Forms a URL of a file based on the path where the file is stored plus
	 * the name of experiment, which is the name of the current PrIMe xml file.
	 * 
	 * @param primeFile The path to the PrIMe file being processed.
	 * @param owlFilePath The path to the file being processed.
	 * @return a string representing a URL.
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors.
	 */
	public static String formOwlUrl(String primeFile, String experimentABoxFilePath) throws OntoChemExpException {
		if (primeFile == null) {
			logger.error("Provided primeFile path is null.");
			throw new OntoChemExpException("Provided primeFile path is null.");
		}
		if (experimentABoxFilePath == null) {
			logger.error("Provided file path is null.");
			throw new OntoChemExpException("Provided file path is null.");
		}
		experimentABoxFilePath = ontoChemExpKB.getOntoChemExpKbURL();
		experimentABoxFilePath = ontoChemExpKB.getOntoChemExpKbURL()
				.concat(extractExperimentName(primeFile)).concat(opCtrl.getOwlFileExtension());
		experimentABoxFilePath = formatToURLSlash(experimentABoxFilePath);
		return experimentABoxFilePath;
	}
	
	/**
	 * Forms a URL of a file based on the path where the file is stored plus
	 * the name of experiment, which is the name of the current PrIMe xml file.
	 * 
	 * @param primeFile The path to the PrIMe file being processed.
	 * @param owlFilePath The path to the file being processed.
	 * @return a string representing a URL.
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors.
	 */
	public static String formOwlFileSaveUrl(String primeFile, String owlFilePath) throws OntoChemExpException {
		if (primeFile == null) {
			logger.error("Provided primeFile path is null.");
			throw new OntoChemExpException("Provided primeFile path is null.");
		}
		if (owlFilePath == null) {
			logger.error("Provided file path is null.");
			throw new OntoChemExpException("Provided file path is null.");
		}
		owlFilePath = owlFilePath.concat("/").concat(ontoChemExpKB.getOntoChemExpKbRootDirectory())
				.concat(extractExperimentName(primeFile)).concat(opCtrl.getOwlFileExtension());
		owlFilePath = formatToURLSlash(owlFilePath);
		owlFilePath = addFileProtocol(owlFilePath);
		return owlFilePath;
	}

	
	/**
	 * Forms the base URL for an OWL ontology.
	 * 
	 * @param primeFile The path to the PrIMe file being processed.
	 * @return a string representing the base URL.
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors.
	 */
	public static String formBaseURL(String primeFile) throws OntoChemExpException {
		if (primeFile == null) {
			logger.error("Provided primeFile path is null.");
			throw new OntoChemExpException("Provided primeFile path is null.");
		}
		return ontoChemExpKB.getOntoChemExpKbURL()
				.concat(extractExperimentName(primeFile)).concat(opCtrl.getOwlFileExtension());
	}
	
	/**
	 * Forms an OWL formatted URL of a file based on the path where the file is stored plus
	 * the name of the file.
	 * 
	 * @param owlFilePath The path to the file being processed
	 * @return a string representing a URL
	 * @throws OntoChemExpException a specialised exception designed to deal with
	 * prime to ontology generation related errors
	 */
	public static String formOwlUrl(String owlFilePath) throws OntoChemExpException {
		if (owlFilePath == null) {
			logger.error("Provided owlFilePath path is null.");
			throw new OntoChemExpException("Provided owlFilePath path is null.");
		}
		owlFilePath = formatToURLSlash(owlFilePath);
		owlFilePath = addFileProtocol(owlFilePath);
		return owlFilePath;
	}
	
	/**
	 * Extracts the name of the experiment being processed from the 
	 * primeFile path.
	 * 
	 * @param primeFile The primeFile path.
	 * @return String returns a string that is the name of the current experiment
	 * being processed.
	 * @throws OntoChemExpException
	 */
	public static String extractExperimentName(String primeFile) throws OntoChemExpException {
		if(experimentName!=null && !experimentName.isEmpty()){
			return experimentName;
		}
		if (!primeFile.contains(FRONTSLASH)) {
			logger.error("Unexpected primeFile path.");
			throw new OntoChemExpException("Unexpected primeFile path.");
		}
		if(primeFile.endsWith(".xml")){
			primeFile = primeFile.substring(0, primeFile.lastIndexOf(".xml"));
		}
		String tokens[] = primeFile.split(FRONTSLASH.concat(FRONTSLASH));
		if(tokens.length<2){
			logger.error("The primeFile path is unexpectedly short.");
			throw new OntoChemExpException("The primeFile path is unexpectedly short.");
		}
		System.out.println(tokens[tokens.length-1]);
		return tokens[tokens.length-1];
	}

	/**
	 * Checks if the current ontology contains an IRI. If an IRI is available
	 * it is returned, otherwise the OntoException exception is thrown.
	 * 
	 * @param ontology
	 * @return IRI the IRI of the input ontology 
	 * @throws OntoChemExpException
	 */
	public static IRI readOntologyIRI(OWLOntology ontology) throws OntoChemExpException{
		if(ontology.getOntologyID().getOntologyIRI().isPresent()){
			return ontology.getOntologyID().getOntologyIRI().get();
		} else{
			logger.error("The OWL file does not contain an IRI.");
			throw new OntoChemExpException("The OWL file does not contain an IRI.");
		}
	}
	
	/**
	 * Creates and returns an instance of the BufferedReader class.
	 * It takes the absolute file path including the file name as input.
	 * 
	 * @param filePathPlusName
	 *            the path plus name of the file being read
	 * @return
	 * @throws IOException
	 */
	public static BufferedReader openSourceFile(String filePathPlusName)
			throws IOException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(
				filePathPlusName), "UTF-8"));
	}
	
	
	public static String retrieveSpeciesIRI(String speciesFileIRI) throws OntoChemExpException {
		if (speciesFileIRI.trim().startsWith("<") || speciesFileIRI.trim().endsWith(">")) {
			speciesFileIRI = speciesFileIRI.replace("<", "").replace(">", "");
		}
		String uniqueSpeciesIRI = new String();
		String queryString = formSpeciesIRIQuery(speciesFileIRI);
		List<List<String>> testResults = queryRepository(ontoChemExpKB.getOntoSpeciesUniqueSpeciesIRIKBServerURL(), ontoChemExpKB.getOntoSpeciesUniqueSpeciesIRIKBRepositoryID(), queryString);
		if (testResults.size() == 2) {
			uniqueSpeciesIRI = testResults.get(1).get(0);
		}
		return uniqueSpeciesIRI;
	}
	
	private static String formSpeciesIRIQuery(String partialSpeciesIRI) {
		String queryString = "PREFIX OntoSpecies: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#> \n";
		queryString = queryString.concat("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
		queryString = queryString.concat("SELECT ?species \n");
		queryString = queryString.concat("WHERE { \n");
		queryString = queryString.concat("    ?species rdf:type OntoSpecies:Species . \n");
		queryString = queryString.concat("    FILTER regex(str(?species), \"").concat(partialSpeciesIRI).concat("\", \"i\") \n");
		queryString = queryString.concat("}");
		return queryString;
	}
	
	/**
	 * Query a given repository using a given SPARQL query string. 
	 * 
	 * @param serverURL
	 * @param repositoryID
	 * @param queryString
	 * @return
	 * @throws OntoChemExpException
	 */
	public static List<List<String>> queryRepository(String serverURL, String repositoryID, String queryString) throws OntoChemExpException {
		List<List<String>> processedResultList = new ArrayList<List<String>>();
		
		try {
			Repository repo = new HTTPRepository(serverURL, repositoryID);
			repo.initialize();
			RepositoryConnection con = repo.getConnection();
			
			try {
				TupleQuery queryResult = con.prepareTupleQuery(queryString);
				try (TupleQueryResult result = queryResult.evaluate()) {
					processResult(result, processedResultList);
				} finally {
					repo.shutDown();
				}
			} catch (Exception e) {
				logger.error("Exception occurred.");
				e.printStackTrace();
				throw new OntoChemExpException("Exception occurred");
			} finally {
				logger.info("Executed the command to close the connection to the repository.");
				con.close();
			}
		} catch (RDF4JException e) {
			logger.error("RDF4JException occurred.");
			e.printStackTrace();
			throw new OntoChemExpException("RDF4JException occurred.");
		}
		return processedResultList;
	}
	
	private static void processResult(TupleQueryResult result, List<List<String>> processedResultList) {
		List<String> columnTitles = new ArrayList<>();
		for (String bindingName : result.getBindingNames()) {
			columnTitles.add(bindingName);
		}
		processedResultList.add(columnTitles);
		while (result.hasNext()) {
			BindingSet solution = result.next();
			
			List<String> processedResult = new ArrayList<>();
			for (String bindingName : solution.getBindingNames()) {
				processedResult.add(removeDataType(solution.getValue(bindingName).toString()));
			}
			processedResultList.add(processedResult);
		}
	}
	
	/**
	 * Removes the following XML Schema data types from a string:</br>
	 * 1. string</br>
	 * 2. integer</br>
	 * 3. float</br>
	 * 4. double.
	 * 
	 * @param value
	 * @return
	 */
	private static String removeDataType(String value) {
		String stringType = "^^<http://www.w3.org/2001/XMLSchema#string>";
		String integerType = "^^<http://www.w3.org/2001/XMLSchema#integer>";
		String floatType = "^^<http://www.w3.org/2001/XMLSchema#float>";
		String doubleType = "^^<http://www.w3.org/2001/XMLSchema#double>";
		if (value.contains(stringType)) {
			value = value.replace(stringType, "");
		} else if (value.contains(integerType)) {
			value = value.replace(integerType, "");
			value = replaceInvertedComma(value);
		} else if (value.contains(floatType)) {
			value = value.replace(floatType, "");
			value = replaceInvertedComma(value);
		} else if (value.contains(doubleType)) {
			value = value.replace(doubleType, "");
			value = replaceInvertedComma(value);
		} else if (value.startsWith("\"") || value.endsWith("\"")) {
			value = value.replace("\"", "");
		}
		return value;
	}

	/**
	 * Removes inverted commas from a string.
	 * 
	 * @param value
	 * @return
	 */
	private static String replaceInvertedComma(String value) {
		if (value.contains("\"")) {
			value = value.replace("\"", "");
		}
		return value;
	}
}
