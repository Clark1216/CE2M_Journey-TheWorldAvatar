package com.cmclinnovations.ontochem.model.tboxes;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileFilter;

import org.jfree.ui.ExtensionFileFilter;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;

import com.cmclinnovations.ontochem.model.exception.TBoxManagementException;
import com.cmclinnovations.ontochem.model.utils.TBoxManagementUtils;
import com.cmclinnovations.ontochem.model.utils.TBoxDialogs;

/**
 * This class implemented the methods that were provided in the ITBoxGeneration</br> 
 * interface, namely following methods were implemented:</br>
 * 1. generateClass</br>
 * 2. generateSubClass</br>
 * 3. generateDataProperty</br>
 * 4. generateObjectProperty</br>
 * 5. generateUnionOfRanges</br>
 * 6. generateUnionOfDomains</br>
 * 7. readTBoxTemplate</br>
 * 
 * @author msff2
 *
 */
 public class TBoxGeneration implements ITBoxGeneration{
	static Logger logger = org.slf4j.LoggerFactory.getLogger(TBoxGeneration.class);
	ITBoxManagement iTBoxManagement;
	public static String owlFilePath; 
	
	
	public static void main(String[] args) {
		File folder = TBoxDialogs.selectFileDialog(new File(System.getProperty("user.home")), new FileFilter[]{new ExtensionFileFilter("Comma-separated Value", "csv")}, false);
		if (folder == null) {
		} else if (!folder.exists()) {
			TBoxDialogs.showErrorDialog("Selected folder does not exist.", "Read");
		} else{
			owlFilePath = folder.getAbsolutePath().replace(".csv", ".owl");
			ITBoxGeneration iTBoxGeneration = new TBoxGeneration();
			try{
				iTBoxGeneration.readTBoxTemplate(folder.toString());
			} catch(IOException e){
				logger.error("IOException occured.");
				e.printStackTrace();
			} catch(TBoxManagementException e){
				logger.error("OntoException occured.");
				e.printStackTrace();
			} catch(OWLOntologyCreationException e){
				logger.error("OWLOntologyCreationException occured.");
				e.printStackTrace();
			} catch(OWLOntologyStorageException e){
				logger.error("OWLOntologyStorageException occured.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Takes the path plus name of a CSV file containing the TBox template.</br>
	 * 
	 * @param csvFileNamePlusPath
	 */
	public void readTBoxTemplate(String csvFileNamePlusPath) throws IOException, TBoxManagementException, OWLOntologyCreationException, OWLOntologyStorageException{
		if(csvFileNamePlusPath == null || csvFileNamePlusPath.isEmpty()){
			logger.error("No file has been found in the path specied.");
		}
		logger.info("TBox generator started running...");
		iTBoxManagement = new TBoxManagement();
		iTBoxManagement.init();
		readCSVTemplate(csvFileNamePlusPath);
		iTBoxManagement.saveOntology(owlFilePath);
		logger.info("TBox generation FINISHED.");
	}
	
	/**
	 * Reads a CSV template with inputs for creating TBoxes.
	 * 
	 * @param csvFileNamePlusPath
	 * @throws IOException
	 */
	private void readCSVTemplate(String csvFileNamePlusPath) throws IOException, TBoxManagementException{
		BufferedReader brSource = TBoxManagementUtils.openSourceFile(csvFileNamePlusPath);
		String line;
		line = brSource.readLine();
		if(line!=null){
			processHeader(line);
		}
		while((line=brSource.readLine())!=null){
			processLine(line);
		}
	}
	
	/**
	 * Processes the header of the CSV file being read.
	 * 
	 * @param line represents the header of the CSV file
	 * @throws IOException
	 */
	private void processHeader(String line) throws IOException, TBoxManagementException{
		if(line.isEmpty()){
			logger.error("The header is empty.");
			throw new IOException("TBox generation stopped proceeding as the header is empty.");
		}
		String[] tokens = line.split(",");
		processHeader(tokens);
	}
 
	/**
	 * Checks if the header contains all the construct name. If it does</br>
	 * not find the right construct in the right position, it throws an</br>
	 * exception. It expects the following position, construct pairs:
	 * 0 - source
	 * 1 - type
	 * 2 - target
	 * 3 - relation
	 * 4 - domain
	 * 5 - range
	 * 
	 * @param tokens
	 */
	private void processHeader(String[] tokens) throws TBoxManagementException{
		int tokenNumber = 0;
		for (String token : tokens) {
			switch (++tokenNumber) {
			case 1:
				checkIfSourceAppears(token);
				break;
			case 2:
				checkIfTypeAppears(token);
				break;
			case 3:
				checkIfTargetAppears(token);
				break;
			case 4:
				checkIfRelationAppears(token);
				break;
			case 5:
				checkIfDomainAppears(token);
				break;
			case 6:
				checkIfRangeAppears(token);
				break;
			}
		}
	}
	
	/**
	 * Checks if the source column appears in the header. If it does not</br>
	 * appear in the header, it throws an exception.
	 * 
	 * @param token
	 * @throws TBoxManagementException
	 */
	private void checkIfSourceAppears(String token) throws TBoxManagementException{
		if (!token.equalsIgnoreCase("source")){
			throw new TBoxManagementException("The source column is missing in the header.");
		}
	}
	
	/**
	 * Checks if the type column appears in the header. If it does not</br>
	 * appear in the header, it throws an exception.
	 * 
	 * @param token
	 * @throws TBoxManagementException
	 */
	private void checkIfTypeAppears(String token) throws TBoxManagementException{
		if (!token.equalsIgnoreCase("type")){
			throw new TBoxManagementException("The type column is missing in the header.");
		}
	}
	
	/**
	 * Checks if the target column appears in the header. If it does not</br>
	 * appear in the header, it throws an exception.
	 * 
	 * @param token
	 * @throws TBoxManagementException
	 */
	private void checkIfTargetAppears(String token) throws TBoxManagementException{
		if (!token.equalsIgnoreCase("target")){
			throw new TBoxManagementException("The target column is missing in the header.");
		}
	}
	
	/**
	 * Checks if the relation column appears in the header. If it does not</br>
	 * appear in the header, it throws an exception.
	 * 
	 * @param token
	 * @throws TBoxManagementException
	 */
	private void checkIfRelationAppears(String token) throws TBoxManagementException{
		if (!token.equalsIgnoreCase("relation")){
			throw new TBoxManagementException("The relation column is missing in the header.");
		}
	}
	
	/**
	 * Checks if the domain column appears in the header. If it does not</br>
	 * appear in the header, it throws an exception.
	 * 
	 * @param token
	 * @throws TBoxManagementException
	 */
	private void checkIfDomainAppears(String token) throws TBoxManagementException{
		if (!token.equalsIgnoreCase("domain")){
			throw new TBoxManagementException("The domain column is missing in the header.");
		}
	}
	
	/**
	 * Checks if the range column appears in the header. If it does not</br>
	 * appear in the header, it throws an exception.
	 * 
	 * @param token
	 * @throws TBoxManagementException
	 */
	private void checkIfRangeAppears(String token) throws TBoxManagementException{
		if (!token.equalsIgnoreCase("range")){
			throw new TBoxManagementException("The range column is missing in the header.");
		}
	}
	
	/**
	 * Processes a line that contains either a class with or without the name
	 * of its parent, a data property or an object property. Both properties
	 * may or may not come with domain(s) and range(s).
	 * 
	 * @param line represents a TBox originating from the CSV file
	 * @throws IOException
	 * @throws TBoxManagementException
	 */
	private void processLine(String line) throws IOException, TBoxManagementException{
		if(line.isEmpty()){
			logger.info("It encountered an empty line.");
			return;
		}
		readEachColumn(line);
	}
	
	/**
	 * Reads each column and determines the action whether to create a class,
	 * a data property or an object property.
	 * 
	 * @param line represents a TBox originating from the CSV file
	 * @throws IOException
	 * @throws TBoxManagementException 
	 */
	private void readEachColumn(String line) throws IOException, TBoxManagementException{
		String[] tokens = line.split(",");
		int tokenNumber = 0;
		for(String token: tokens){
			switch (++tokenNumber) {
			case 1:
				readSourceColumn(tokens[tokenNumber - 1]);
				break;
			case 2: readTypeColumn(tokens, tokenNumber - 1);
				break;
			}
		}
	}
	
	/**
	 * Reads the first column and checks if it is empty. In case it is empty it
	 * throws an exception, otherwise it allows proceeding to the next column.
	 * 
	 * @param firstColumn represents the value of the source in any row
	 * except the header
	 * @throws IOException
	 * @throws TBoxManagementException
	 */
	private void readSourceColumn(String firstColumn) throws IOException, TBoxManagementException{
		if(firstColumn == null || firstColumn.isEmpty()){
			throw new IOException("The first column of a row is empty.");
		}
	}
	
	/**
	 * Reads the second column and checks if it is empty. In case it is empty it
	 * throws an exception, otherwise it allows proceeding to the next column.
	 * 
	 * @param secondColumn represents the value of the type of source in any
	 * row except the header
	 * @throws IOException
	 * @throws TBoxManagementException
	 */
	private void readTypeColumn(String[] tokens, int tokenNumber) throws IOException, TBoxManagementException{
		if(tokens[tokenNumber] == null || tokens[tokenNumber].isEmpty()){
			throw new IOException("The second column of a row is empty.");
		}
		decideTypeOfConstruct(tokens, tokenNumber);
	}
	
	/**
	 * Decides whether to create a class, data property or object property.</br>
	 * Following the decision it calls the appropriate method to do it.</br>
	 * For creating a data property, it passes 1 as the first parameter of the</br>
	 * generateProperty method.</br>
	 * For creating an object property, it passes 2 as the first parameter of</br>
	 * the generateProperty method. 
	 * 
	 * @param tokens
	 * @param tokenNumber
	 * @throws IOException
	 * @throws TBoxManagementException
	 */
	private void decideTypeOfConstruct(String[] tokens, int tokenNumber) throws IOException, TBoxManagementException{
		if(tokens[tokenNumber].toLowerCase().equalsIgnoreCase("class")){
			if(tokens.length>tokenNumber+1){
				if(!tokens[tokenNumber+1].isEmpty()){
					generateClass(tokens[tokenNumber-1], tokens[tokenNumber+1]);
				}
			} else{
				generateClass(tokens[tokenNumber-1], null);
			}
		} else if(tokens[tokenNumber].toLowerCase().equalsIgnoreCase("data property")){
			generateProperty(1, tokens);
		} else if(tokens[tokenNumber].toLowerCase().equalsIgnoreCase("object property")){
			generateProperty(2, tokens);
		}
	}
	
	/**
	 * Processes and extracts the following info to send this to the 
	 * generateDataProperty method:
	 * 1. Property name;</br>
	 * 2. Domain, which can have multiple values; </br> 
	 * 3. Range, which can have multiple values.
	 * 
	 * @param tokens
	 * @throws IOException
	 * @throws TBoxManagementException
	 */
	private void generateProperty(int propertyType, String[] tokens) throws IOException, TBoxManagementException {
		int i = 0;
		String propertyName = "";
		String domain = "";
		String range = "";
		for(String token: tokens){
			if(++i==1){
				propertyName = token;
			} if(i==5){
				domain = token;
			} if (i==6){
				range = token;
			}
		}
		callPropertyGenerateor(propertyType, propertyName, domain, range);
	}
	
	/**
	 * Calls either the data property generator or the object property</br> 
	 * generator based on the value of the property type. If its value</br>
	 * is 1, it calls the generateDataProperty method and if its value</br>
	 * is 2, it calls the generateObjectProperty method.
	 * 
	 * @param propertyType
	 * @param strings
	 * @throws IOException
	 * @throws TBoxManagementException
	 */
	private void callPropertyGenerateor(int propertyType, String...strings) throws IOException, TBoxManagementException {
		if(propertyType==1){
			generateDataProperty(strings[0], strings[1], strings[2]);
		}
		if(propertyType==2){
			generateObjectProperty(strings[0], strings[1], strings[2]);
		}
	}
	
	/**
	 * Generates an OWL class with the name originated from the current</br>
	 * template CSV file. If the parent name is also provided, it generates</br>
	 * a subclass of relation between the class and its parent.
	 * 
	 * @param className
	 * @param parentName
	 * @throw IOException
	 * @throws TBoxManagementException 
	 *
	 */
	public void generateClass(String className, String parentName) throws IOException, TBoxManagementException{
		iTBoxManagement.createOWLClass(className, parentName);
	}
	
	/**
	 * Generates an OWL data property with the name originated from the current</br>
	 * template CSV file. If the domain(s) and range(s) are provided, it</br>
	 * generates them as well.
	 * 
	 * @param propertyName
	 * @param domain
	 * @param range
	 */
	public void generateDataProperty(String propertyName, String domain, String range) throws IOException, TBoxManagementException{
		iTBoxManagement.createOWLDataProperty(propertyName, domain, range);
	}
	
	public void generateObjectProperty(String propertyName, String domain, String range) throws IOException, TBoxManagementException{
		iTBoxManagement.createOWLObjectProperty(propertyName, domain, range);
	}
 }