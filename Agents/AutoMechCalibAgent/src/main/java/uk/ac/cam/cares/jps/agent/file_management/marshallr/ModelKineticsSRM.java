package uk.ac.cam.cares.jps.agent.file_management.marshallr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.cmclinnovations.ontochem.model.converter.owl.OwlConverter;
import com.cmclinnovations.ontochem.model.exception.OntoException;

import uk.ac.cam.cares.jps.agent.file_management.MoDSInputsState;
import uk.ac.cam.cares.jps.agent.file_management.mods.models.Model;
import uk.ac.cam.cares.jps.agent.file_management.mods.parameters.Parameter;
import uk.ac.cam.cares.jps.agent.json.parser.JSonRequestParser;
import uk.ac.cam.cares.jps.agent.mechanism.coordination.AutoMechCalibAgentException;
import uk.ac.cam.cares.jps.agent.mechanism.coordination.Property;
import uk.ac.cam.cares.jps.kg.OntoChemExpKG;
import uk.ac.cam.cares.jps.kg.OntoKinKG;
import uk.ac.cam.cares.jps.kg.OntoChemExpKG.DataTable;

public class ModelKineticsSRM extends MoDSMarshaller implements IModel {
	private static Logger logger = LoggerFactory.getLogger(ModelKineticsSRM.class);
	private int numOfReactions;
	private String modelName = new String();
	private LinkedHashMap<String, String> activeParameters = new LinkedHashMap<String, String>(); // linkedHashMap? 
	private List<String> passiveParameters = new ArrayList<>();
	private List<String> outputResponses = new ArrayList<>();
	private List<String> expFiles = new ArrayList<>();
	private List<String> modelFiles = new ArrayList<>();
	private List<String> caseNames = new ArrayList<>();
	private String ignDelayMethod = "2";
	private String ignDelaySpecies = "AR";
	
	public String getIgnDelayMethod() {
		return ignDelayMethod;
	}

	public void setIgnDelayMethod(String ignDelayMethod) {
		this.ignDelayMethod = ignDelayMethod;
	}

	public String getIgnDelaySpecies() {
		return ignDelaySpecies;
	}

	public void setIgnDelaySpecies(String ignDelaySpecies) {
		this.ignDelaySpecies = ignDelaySpecies;
	}
	
	/**
	 * Collect all information required by MoDS to execute the model kineticsSRM. 
	 * The information required: 
	 * 1. model name - the name of the executable;
	 * 2. active parameters - the optimisable parameters of the model;
	 * 3. passive parameters - the experiment settings;
	 * 4. output response - the experimental observations;
	 * 5. exp files - list of files that contains experiment data and model to be calibrated;
	 * 6. case names - list of individual experiment;
	 * Input to this function: 
	 * 1. experimentIRI - get all experimental observations, i.e., passive parameters and output responses. 
	 * Also, set up case name list
	 * 2. mechanismIRI - download mechanism file
	 * 3. reactionIRIList - get list of reactions to be optimised, i.e., active parameters
	 * 
	 * @param experimentIRI
	 * @param mechanismIRI
	 * @param reactionIRIList
	 * @return
	 * @throws IOException
	 * @throws AutoMechCalibAgentException
	 */
	@Override
	public ExecutableModel formExecutableModel(List<String> experimentIRI, String mechanismIRI, List<String> reactionIRIList) 
			throws IOException, AutoMechCalibAgentException {
		// check if the target folder exist
		checkFolderPath(folderTemporaryPath);
		
		// create ontology kg instance for query
		OntoKinKG ontoKinKG = new OntoKinKG();
		// query active parameters
		LinkedHashMap<String, String> activeParameters = ontoKinKG.queryReactionsToOptimise(mechanismIRI, reactionIRIList);
		// collect experiment information
		List<List<String>> headers = new ArrayList<List<String>>();
		List<List<String>> dataCollection = new ArrayList<List<String>>();
		for (String experiment : experimentIRI) {
			OntoChemExpKG ocekg = new OntoChemExpKG();
			DataTable dataTable = ocekg.formatExperimentDataTable(experiment);
			headers.add(dataTable.getTableHeader());
			dataCollection.addAll(dataTable.getTableData());
		}
		for (int i = 1; i < headers.size(); i++) {
			if (!headers.get(i).equals(headers.get(i-1))) {
				logger.error("The heasers of all experimental data tables should be consistent.");
			}
		}
//		if (!headers.get(0).equals(headers.get(1)) || !headers.get(1).equals(headers.get(2))) {
//			logger.error("The heasers of all experimental data tables should be consistent.");
//		}
		// form exp data csv file
		List<String[]> dataLines;
		dataLines = new ArrayList<>();
		dataLines.add(headers.get(0).toArray(new String[0]));
		List<String> caseList = new ArrayList<>();
		int i = 0;
		for (List<String> dataSingleLine : dataCollection) {
			dataLines.add(dataSingleLine.toArray(new String[0]));
			// generate the list of cases
			caseList.add(Property.MODEL_KINETICS.getPropertyName().concat("_case_"+i));
			i += 1;
		}
		File expDataCSV = new File(folderTemporaryPath.concat(FRONTSLASH).concat(Property.MODEL_KINETICS.getPropertyName().concat(UNDERSCORE+FILE_MODEL_EXPDATA_SUFFIX)));
		try (PrintWriter pw = new PrintWriter(expDataCSV)) {
			dataLines.stream()
			.map(this::convertToCSV)
			.forEach(pw::println);
		}
		
		// obtain passive parameters and output responses from header of exp data csv file
		List<String> passiveParameters = new ArrayList<>();
		List<String> outputResponses = new ArrayList<>();
		for (String param : headers.get(0)) {
			if (param.contains("Igni") && param.contains("Delay")) {
				outputResponses.add(param);
			} else if (param.contains("Unit")) {
			} else if (param.contains("Phi")) {
			} else {
				passiveParameters.add(param);
			}
		}
		
		// TODO download mechanism owl file, this part should be modified to do the conversion of OWL to XML on the fly
		String mechanismXML = folderTemporaryPath.concat(FRONTSLASH).concat(FILE_MECHANISM);
		MechanismDownload mechanismDownload = new MechanismDownload();
		try {
			String mechanismWebPath = mechanismIRI.substring(0, mechanismIRI.indexOf("#"))
					.replace("/kb/", "/data/").replace(".owl", "/mechanism.xml");
			mechanismDownload.obtainMechanism(mechanismWebPath, mechanismXML);
		} catch (SAXException | ParserConfigurationException | TransformerFactoryConfigurationError
				| TransformerException e) {
			e.printStackTrace();
		}
		
		
		/**
		 * Owl converter that converts OWL to XML file
		 */
//		OwlConverter owlConverter = new OwlConverter();
//		ArrayList<String> mechanismOwlFiles = new ArrayList<>();
//		mechanismOwlFiles.add(mechanismIRI);
//		try {
//			owlConverter.convert(mechanismOwlFiles, folderTemporaryPath);
//		} catch (OWLOntologyCreationException | OntoException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		
		
		
		
		// create model instance
		ExecutableModel kineticsSRM = new ExecutableModel();
		
		// set up model name
		kineticsSRM.setModelName(Property.MODEL_KINETICS.getPropertyName());
		
		// set up model active parameters
		kineticsSRM.setActiveParameters(activeParameters);
		
		// set up model passive parameters
		kineticsSRM.setPassiveParameters(passiveParameters);
		
		// set up model output response
		kineticsSRM.setOutputResponses(outputResponses);
		
		// set up model exp files
		List<String> expFiles = new ArrayList<>();
		expFiles.add(expDataCSV.getName());
		expFiles.add(mechanismXML.substring(mechanismXML.lastIndexOf(FRONTSLASH)));
		kineticsSRM.setExpFiles(expFiles);
		
		// set up model case names
		kineticsSRM.setCaseNames(caseList);
		
		// query number of reactions
		List<List<String>> numOfReactionsResults = ontoKinKG.queryNumOfReactions(mechanismIRI);
		numOfReactions = Integer.parseInt(numOfReactionsResults.get(1).get(0));
		
		logger.info("Executable model kineticsSRM is prepared. ");
		return kineticsSRM;
	}
	
	/**
	 * Form all files required by MoDS to execute the model kineticsSRM. This method 
	 * replace the method in IModel. 
	 * 
	 * @param exeModel
	 * @param ignDelayOption
	 * @return
	 * @throws IOException
	 * @throws AutoMechCalibAgentException
	 */
	@Override
	public List<String> formFiles(ExecutableModel exeModel, String otherOptions) throws IOException, AutoMechCalibAgentException {
		// check if the target folder exist
		checkFolderPath(folderInitialPath);
		checkFolderPath(folderAllPath);
		
		// get the basic information of executable kineticsSRM
		modelName = exeModel.getModelName();
		activeParameters = exeModel.getActiveParameters();
		expFiles = exeModel.getExpFiles();
		caseNames = exeModel.getCaseNames();
		outputResponses = exeModel.getOutputResponses();
		passiveParameters = exeModel.getPassiveParameters();
		
		// set up the ignition delay option that will be used for generating InputParams.xml file
		String method = JSonRequestParser.getIgnDelayMethod(otherOptions);
		if (method != null && !method.isEmpty()) {
			setIgnDelayMethod(method);
		}
		String species = JSonRequestParser.getIgnDelaySpecies(otherOptions);
		if (species != null && !species.isEmpty()) {
			setIgnDelaySpecies(species);
		}
		
		// process the active parameters to be only the equation of reactions
		List<String> processedActiveParam = new ArrayList<>();
		for (String activeParamNo : activeParameters.keySet()) {
			processedActiveParam.add(activeParameters.get(activeParamNo));
		}
		
		// create list to store all files used/produced when executing kineticsSRM model
		// get the name of files in the initial folder
		List<String> folderInitialFiles = createFolderInitial(processedActiveParam);
		// get the name of files in the all folder
		List<String> folderAllFiles = createFolderAll(processedActiveParam);
		// name the output file of the model
		String outputFile = Property.MODEL_KINETICS_OUTPUT.getPropertyName();
		// append all names to modelFiles
		modelFiles.addAll(folderInitialFiles);
		modelFiles.addAll(folderAllFiles);
		modelFiles.add(outputFile);
		
		logger.info("Files required by "+modelName+" is prepared. ");
		
		return modelFiles;
	}
	
	/**
	 * Create the files used during the 'initial read' when executing the kineticsSRM model. 
	 * 
	 * @param initial
	 * @param modelName
	 * @param processedActiveParam
	 * @param caseNames
	 * @param expFiles
	 * @return
	 * @throws IOException
	 * @throws AutoMechCalibAgentException
	 */
	@Override
	public List<String> createFolderInitial(List<String> processedActiveParam) throws IOException, AutoMechCalibAgentException {
		// set the active parameter csv file and passive parameter csv file path
		File activeParameterFilePath = new File(folderInitialPath
				.concat(FRONTSLASH+FILE_MODS_PREFIX+UNDERSCORE+modelName+UNDERSCORE+FILE_MODS_ACTIVE_SUFFIX));
		File passiveParametersAndOutputsFilePath = new File(folderInitialPath
				.concat(FRONTSLASH+FILE_MODS_PREFIX+UNDERSCORE+modelName+UNDERSCORE+FILE_MODS_PASSIVE_SUFFIX));
				
		// get the filePath of experimental data
		File expData = null;
		for (String expFilePath : expFiles) {
			if (expFilePath.contains(FILE_MODEL_EXPDATA_SUFFIX) && expFilePath.contains(modelName)) {
				expData = new File(folderTemporaryPath.concat(FRONTSLASH+expFilePath));
			}
		}
		
		// create files in the initial folder
		List<String> initialFiles = new ArrayList<>();
		String initialActiveFile = createActiveParametersFile(activeParameterFilePath, processedActiveParam, expData, caseNames);
		String initialPassiveFile = createPassiveParametersAndOutputsFile(passiveParametersAndOutputsFilePath, expData, caseNames);
		initialFiles.add(initialActiveFile);
		initialFiles.add(initialPassiveFile);
		
		logger.info("Folder /Initial required by "+modelName+" is prepared. ");
		
		return initialFiles;
	}
	
	/**
	 * Create the files used during the 'working write' when executing the kineticsSRM model. 
	 * 
	 * @param initial
	 * @param modelName
	 * @param processedActiveParam
	 * @param caseNames
	 * @param expFiles
	 * @return
	 * @throws IOException
	 * @throws AutoMechCalibAgentException
	 */
	@Override
	public List<String> createFolderAll(List<String> processedActiveParam) throws IOException, AutoMechCalibAgentException {
		// set the mechanism file and inputParams.xml file path
		File copyOfMechanismFilePath = new File(folderAllPath.concat(FRONTSLASH+FILE_MECHANISM));
		File inputParamsFilePath = new File(folderAllPath.concat(FRONTSLASH+FILE_KINETICS_INPUTPARAMS));
		
		// get the filePath of experimental data and mechanism
		File expData = null;
		File mechanism = null;
		for (String expFilePath : expFiles) {
			if (expFilePath.contains(FILE_MODEL_EXPDATA_SUFFIX) && expFilePath.contains(modelName)) {
				expData = new File(folderTemporaryPath.concat(FRONTSLASH+expFilePath));
			} else if (expFilePath.contains(FILE_MECHANISM)) {
				mechanism = new File(folderTemporaryPath.concat(FRONTSLASH+expFilePath));
			}
		}
		
		// create files in the initial folder
		List<String> allFiles = new ArrayList<>();
		allFiles.add(copyMechanismFile(copyOfMechanismFilePath, mechanism));
		allFiles.add(createInputParamsFile(inputParamsFilePath, expData));
		
		logger.info("Folder /All required by "+modelName+" is prepared. ");
		
		return allFiles;
	}
	
	/**
	 * Set up all the components of executable in the MoDS input file. 
	 * 
	 * @throws IOException
	 * @throws AutoMechCalibAgentException
	 */
	@Override
	public void setUpMoDS() throws IOException, AutoMechCalibAgentException {
		// set up algorithms
		String active_subtype = new String();
		for (String i : activeParameters.keySet()) {
			active_subtype = active_subtype.concat(" subtype_"+"rxn_"+i);
		}
		LinkedHashMap<String, LinkedHashMap<String, String>> algorithms = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		LinkedHashMap<String, String> algoSampling = new LinkedHashMap<String, String>();
		algoSampling.put("optimisable_param_subtypes", active_subtype.substring(1));
		algoSampling.put("response_param_subtypes", "subtype_".concat(outputResponses.get(0)));
		algoSampling.put("algorithm_type", "Sobol");
//		algoSampling.put("model_name", "exe");
		algoSampling.put("objective_function", "SumOfSquares");
		algoSampling.put("output_by_case", "false");
		algoSampling.put("output_values", "true");
		algoSampling.put("n_points", "1000");
		algoSampling.put("seed", "1");
		algoSampling.put("output_interval", "10");
		algoSampling.put("previous_algorithm", "Initial");
		LinkedHashMap<String, String> algoCalibration = new LinkedHashMap<String, String>();
		algoCalibration.put("optimisable_param_subtypes", active_subtype.substring(1));
		algoCalibration.put("response_param_subtypes", "subtype_".concat(outputResponses.get(0)));
		algoCalibration.put("algorithm_type", "Hooke_Jeeves");
//		algoCalibration.put("model_name", "exe");
		algoCalibration.put("objective_function", "SumOfSquares");
		algoCalibration.put("output_by_case", "false");
		algoCalibration.put("output_values", "true");
		algoCalibration.put("n_iters", "400");
		algoCalibration.put("n_initial_points", "10");
		algoCalibration.put("constrained", "true");
		algoCalibration.put("rho", "0.2");
		algoCalibration.put("rho_factor", "0.5");
		algoCalibration.put("epsilon", "0.001");
		algoCalibration.put("previous_algorithm", "SamplingAlg");
		algorithms.put("SamplingAlg", algoSampling);
		algorithms.put("CalibrationAlg", algoCalibration);
		collectAlgorithms(algorithms);
		
		// set up model
		LinkedHashMap<String, LinkedHashMap<String, String>> models = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		LinkedHashMap<String, String> model = new LinkedHashMap<String, String>();
		model.put("executable_name", Property.MODEL_KINETICS_EXE.getPropertyName());
		model.put("working_directory", "");
		models.put(modelName, model);
		collectModels(models);
		
		// set up cases
		LinkedHashMap<String, List<String>> cases = new LinkedHashMap<String, List<String>>();
		List<String> caseModel = new ArrayList<>();
		caseModel.add(modelName);
		for (String caseName : caseNames) {
			cases.put(caseName, caseModel);
		}
		collectCases(cases);
		
		// set up files
		LinkedHashMap<String, LinkedHashMap<String, String>> files = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		for (String modelFile : modelFiles) {
			LinkedHashMap<String, String> file = new LinkedHashMap<String, String>();
			if (modelFile.endsWith(".xml")) {
				file.put("file_type", "XML");
				if (modelFile.contains(FILE_KINETICS_INPUTPARAMS)) {
					file.put("XML_namespace", "http://como.cheng.cam.ac.uk/srm");
				}
			} else if (modelFile.endsWith(".csv")) {
				file.put("file_type", "DSV");
				file.put("delimiter", ",");
			}
			files.put(modelFile, file);
		}
		collectFiles(files);
		
		// set up parameters
		List<Parameter> parameters = new ArrayList<>();
		// constructing row, lbFactor, and ubFactor
		String row = "";
		String lbFactor = "";
		String ubFactor = "";
		double sqrtN = Math.sqrt(caseNames.size());
		double errFrac = 0.20;
		double lb_fac = (1-errFrac)*sqrtN;
		double ub_fac = (1+errFrac)*sqrtN;
		for (int j = 0; j < caseNames.size(); j++) {
			row = row.concat(";"+j);
			lbFactor = lbFactor.concat(";"+lb_fac);
			ubFactor = ubFactor.concat(";"+ub_fac);
		}
		row = row.substring(1);
		lbFactor = lbFactor.substring(1);
		ubFactor = ubFactor.substring(1);
		// active parameters
		for (String i : activeParameters.keySet()) {
			Parameter param = new Parameter();
			param.setType("active_input");
			param.setName("rxn_"+i);
			param.setSubtype("subtype_"+"rxn_"+i);
			param.setPreserveWhiteSpace("true");
			param.setScaling("linear");
			param.setCaseNamesList(caseNames);
			param.setModelList(caseModel);
			
			LinkedHashMap<String, LinkedHashMap<String, String>> fileHash = new LinkedHashMap<String, LinkedHashMap<String, String>>();
			LinkedHashMap<String, String> initialRead = new LinkedHashMap<String, String>();
			initialRead.put("column", activeParameters.get(i));
			initialRead.put("row", "0");
			initialRead.put("read_function", "Get_DSV_double");
			initialRead.put("lb_abs", "1.0E-3");
			initialRead.put("ub_abs", "1000.0");
			
			LinkedHashMap<String, String> workingWrite = new LinkedHashMap<String, String>();
			workingWrite.put("path", "//srm_inputs/property_group[@ref='Chemistry']/property[@ref='ReactionRate_A_Modifiers']/value[@index='"+i+"']");
			workingWrite.put("write_function", "Set_XML_double");
			
			fileHash.put("initialRead "+FILE_MODS_PREFIX+UNDERSCORE+modelName+UNDERSCORE+FILE_MODS_ACTIVE_SUFFIX, initialRead);
			fileHash.put("workingWrite "+FILE_KINETICS_INPUTPARAMS, workingWrite);
			
			param.setFileHash(fileHash);
			parameters.add(param);
		}
		// passive parameters
		for (String i : passiveParameters) {
			Parameter param = new Parameter();
			param.setType("passive_input");
			param.setName(i);
			param.setSubtype("subtype_"+i);
			param.setCaseDetailSep(";");
			param.setPreserveWhiteSpace("true");
			param.setCaseNamesList(caseNames);
			param.setModelList(caseModel);
			
			String path = new String();
			if (i.contains("Temp")) {
				path = "//srm_inputs/property_group[@ref='Reactor']/property[@ref='IniTemp']/value";
			} else if (i.contains("Pres")) {
				path = "//srm_inputs/property_group[@ref='Reactor']/property[@ref='IniPres']/value";
			} else {
				path = "//srm_inputs/mixtures[@type='composition']/composition[@name='"+NAME_OXIDISER+"']/value[@species='"+i+"']";
			}
			
			LinkedHashMap<String, LinkedHashMap<String, String>> fileHash = new LinkedHashMap<String, LinkedHashMap<String, String>>();
			LinkedHashMap<String, String> initialRead = new LinkedHashMap<String, String>();
			initialRead.put("column", i);
			initialRead.put("row", row);
			initialRead.put("read_function", "Get_DSV_double");
			
			LinkedHashMap<String, String> workingWrite = new LinkedHashMap<String, String>();
			workingWrite.put("path", path);
			workingWrite.put("write_function", "Set_XML_double");
			
			fileHash.put("initialRead "+FILE_MODS_PREFIX+UNDERSCORE+modelName+UNDERSCORE+FILE_MODS_PASSIVE_SUFFIX, initialRead);
			fileHash.put("workingWrite "+FILE_KINETICS_INPUTPARAMS, workingWrite);
			
			param.setFileHash(fileHash);
			parameters.add(param);
		}
		// output response
		for (String i : outputResponses) {
			Parameter param = new Parameter();
			param.setType("active_output");
			param.setSubtype("subtype_"+i);
			param.setName(i);
			param.setCaseDetailSep(";");
			param.setNParamsPerCase("1");
			param.setPreserveWhiteSpace("true");
			param.setScaling("linear");
			param.setCaseNamesList(caseNames);
			param.setModelList(caseModel);
			
			String column = new String();
			if (i.toLowerCase().contains("igni") && i.toLowerCase().contains("delay")) {
				column = "Ignition time [ms]";
			} else if (i.toLowerCase().contains("flame") && i.toLowerCase().contains("speed")) {
				column = "Laminar flame speed [cm/s]";
			} // TODO further parameterise this
			
			LinkedHashMap<String, LinkedHashMap<String, String>> fileHash = new LinkedHashMap<String, LinkedHashMap<String, String>>();
			LinkedHashMap<String, String> initialRead = new LinkedHashMap<String, String>();
			initialRead.put("column", i);
			initialRead.put("row", row);
			initialRead.put("read_function", "Get_DSV_double");
			initialRead.put("lb_factor", lbFactor);
			initialRead.put("ub_factor", ubFactor);
			
			LinkedHashMap<String, String> workingRead = new LinkedHashMap<String, String>();
			workingRead.put("column", column);
			workingRead.put("row", "0");
			workingRead.put("read_function", "Get_DSV_double");
			
			fileHash.put("initialRead "+FILE_MODS_PREFIX+UNDERSCORE+modelName+UNDERSCORE+FILE_MODS_PASSIVE_SUFFIX, initialRead);
			fileHash.put("workingRead "+Property.MODEL_KINETICS_OUTPUT.getPropertyName(), workingRead);
			
			param.setFileHash(fileHash);
			parameters.add(param);
		}
		collectParameters(parameters);
		
		logger.info("Information related to "+modelName+" in MoDS_inputs XML file is collected. ");
	}
	
	/**
	 * Set up the simulation script required for the model to execute. 
	 * 
	 * @throws IOException
	 * @throws AutoMechCalibAgentException
	 */
	@Override
	public void placeScript() throws IOException, AutoMechCalibAgentException {
		File srcScript = new File(getClass().getClassLoader().getResource(Property.MODEL_KINETICS_SCRIPT.getPropertyName()).getFile());
		File jobScript = new File(jobFolderPath.concat(FRONTSLASH+FILE_KINETICSSRM_SCRIPT));
		
		// create the BufferedReader and BufferedWriter to read and write files
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		// copy the runKineticsSRM.sh script
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(srcScript)));
	        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jobScript)));
	        String line = new String();
	        while ((line = br.readLine()) != null) {
	        	bw.write(line.concat("\n"));
	        }
	        bw.close();
	        br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Create case file that contains the active parameters used by executable kineticsSRM. 
	 * 
	 * @param activeParameterFilePath
	 * @param processedActiveParam
	 * @param expData
	 * @param caseNames
	 * @return
	 * @throws IOException
	 * @throws AutoMechCalibAgentException
	 */
	private String createActiveParametersFile(File activeParameterFilePath, List<String> processedActiveParam, File expData, 
			List<String> caseNames) throws IOException, AutoMechCalibAgentException {
		
		
		// construct the title of columns
		List<String> columnTitle = new ArrayList<>();
		columnTitle.add("Case name");
		columnTitle.addAll(processedActiveParam);
		
		// construct the reactionMultipliers, serving the initial read of active parameters
		List<String> reactionMultipliers = new ArrayList<>();
		for (int i = 0; i < processedActiveParam.size(); i++) {
			reactionMultipliers.add("1");
		}
		
		// create the BufferedReader and BufferedWriter to read and write files
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		// create the active parameters csv file
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(expData)));
	        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(activeParameterFilePath), "UTF-8"));
	        // add the column title
	        br.readLine();
	        bw.write(convertToCSV(columnTitle.toArray(new String[0])).concat("\n"));
	        // add the cases
	        int i = 0;
	        while ((br.readLine()) != null) {
	        	bw.write(caseNames.get(i).concat(",").concat(convertToCSV(reactionMultipliers.toArray(new String[0]))).concat("\n"));
	        	i += 1;
	        }
	        // additional check if all cases are added
	        if (i != caseNames.size()) {
	        	System.out.println("The number of cases does NOT match the number of experimental observations.");
	        }
	        // close files
	        bw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return activeParameterFilePath.getName();
	}
	
	/**
	 * Create case file that contains the passive parameters used by executable kineticsSRM, 
	 * also the output response of executable kineticsSRM. 
	 * 
	 * @param passiveParametersAndOutputsFilePath
	 * @param expData
	 * @param caseNames
	 * @return
	 * @throws IOException
	 * @throws AutoMechCalibAgentException
	 */
	private String createPassiveParametersAndOutputsFile(File passiveParametersAndOutputsFilePath, File expData, 
			List<String> caseNames) throws IOException, AutoMechCalibAgentException {
		// create the BufferedReader and BufferedWriter to read and write files
		// add the caseNames to the start of each line of experimental data file
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		// create the passive parameters and output response csv file
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(expData)));
	        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(passiveParametersAndOutputsFilePath), "UTF-8"));
	        // add the column title
	        String line = br.readLine();
	        bw.write("Case name".concat(",").concat(line).concat("\n"));
	        // add the data part
	        int i = 0;
	        while ((line = br.readLine()) != null) {
	        	bw.write(caseNames.get(i).concat(",").concat(line).concat("\n"));
	        	i += 1;
	        }
	        // additional check if all cases are added
	        if (i != caseNames.size()) {
	        	logger.error("The number of cases does NOT match the number of experimental observations.");
	        }
	        // close files
	        bw.close();
	        br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return passiveParametersAndOutputsFilePath.getName();
	}
	
	/**
	 * Create the mechanism file used by the executable kineticsSRM. 
	 * 
	 * @param mechanism
	 * @param copyOfMechanism
	 * @return
	 * @throws IOException
	 * @throws AutoMechCalibAgentException
	 */
	private String copyMechanismFile(File copyOfMechanismFilePath, File mechanism) throws IOException, AutoMechCalibAgentException {
		// create the BufferedReader and BufferedWriter to read and write files
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		// copy mechanism file
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(mechanism)));
	        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(copyOfMechanismFilePath), "UTF-8"));
	        String line = new String();
	        while ((line = br.readLine()) != null) {
	        	bw.write(line+"\n");
	        }
	        bw.close();
	        br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return copyOfMechanismFilePath.getName();
	}
	
	/**
	 * Create the InputParams.xml file required by the executable kineticsSRM. 
	 * 
	 * @param inputParamsFilePath
	 * @param expData
	 * @param mechName
	 * @return
	 * @throws IOException
	 * @throws AutoMechCalibAgentException
	 */
	private String createInputParamsFile(File inputParamsFilePath, File expData) throws IOException, AutoMechCalibAgentException {
		// read the first case of experiment
		String[] headerLine = null;
		String[] firstData = null;
		if (expData.isFile()) {
			BufferedReader expReader = new BufferedReader(new FileReader(expData));
			headerLine = expReader.readLine().split(",");
			firstData = expReader.readLine().split(",");
			expReader.close();
		}
		
		// prepare the variables to be passed to InputParams.xml file
		String iniTemp = null;
		String iniTempUnit = null;
		String iniPres = null;
		String iniPresUnit = null;
		ArrayList<String> species = new ArrayList<String>();
		
		
		// ignition delay, uncomment corresponding method below
		String ignDelayDeltaT = "400";
		String ignDelayShowAll = "1";
		String ignDelayModel = getIgnDelayMethod();
		String ignDelaySpeciesIndex = getIgnDelaySpecies();
		
		
		// -Method 0. Searching for the maximum rate of temperature increase.
//		String ignDelayModel = "0";
//		String ignDelaySpeciesIndex = "AR";
		
		// -Method 1. Searching for the maximum rate of pressure increase.
//		String ignDelayModel = "1";
//		String ignDelaySpeciesIndex = "AR";
		
		// -Method 2. Searching for the point at which temperature increase exceeds 400 K.
//		String ignDelayModel = "2";
//		String ignDelaySpeciesIndex = "AR";
		
		// -Method 3. Searching for the maximum mole fraction of species "x".
//		String ignDelayModel = "3";
//		String ignDelaySpeciesIndex = "OH";
		
		// -Method 4. Searching for the maximum rate of increase of the mole fraction of species "x".
//		String ignDelayModel = "4";
//		String ignDelaySpeciesIndex = "CO";
		
		
		String oxidiser = NAME_OXIDISER; // this name is to be further parameterised, also to be connected to MoDS_Inputs.xml
		for (int i = 0; i < headerLine.length; i++) {
			if (headerLine[i].contains("Temp")) {
				if (headerLine[i].contains("Unit")) {
					iniTempUnit = firstData[i];
				} else {
					iniTemp = firstData[i];
				}
			} else if (headerLine[i].contains("Pres")) {
				if (headerLine[i].contains("Unit")) {
					iniPresUnit = firstData[i];
				} else {
					iniPres = firstData[i];
				}
			} else if (headerLine[i].contains("DMM3") 
					|| headerLine[i].contains("O2") 
					|| headerLine[i].contains("N2")) {
				species.add(headerLine[i]);
				species.add(firstData[i]);
			}
		}
		
		String jsonString = new JSONObject()
				.put("kinetics", 
						new JSONObject()
						.put("filePath", inputParamsFilePath.getAbsolutePath())
						.put("reactor", new JSONObject()
								.put("iniTemp", new JSONObject()
										.put("value", iniTemp)
										.put("unit", iniTempUnit))
								.put("iniPres", new JSONObject()
										.put("value", iniPres)
										.put("unit", iniPresUnit)))
						.put("chemistry", new JSONObject()
								.put("mechFile", FILE_MECHANISM)
								.put("numOfReactions", numOfReactions))
						.put("oxidiser", oxidiser)
						.put("ignDelayPostProcessor", new JSONObject()
								.put("ignDelayModel", ignDelayModel)
								.put("ignDelayDeltaT", ignDelayDeltaT)
								.put("ignDelaySpeciesIndex", ignDelaySpeciesIndex)
								.put("ignDelayShowAll", ignDelayShowAll))
						.put("mixtures", new JSONObject()
								.put("composition", species))).toString();
		
		InputParamsBuilder inputParamsBuilder = new InputParamsBuilder();
		File inputParams = inputParamsBuilder.formInputParamsXML(jsonString);
		
		return inputParams.getName();
	}
	
}
