package uk.ac.cam.cares.jps.agent.gPROMS;

import java.io.File;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter; 

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import org.apache.commons.io.FileUtils;

import org.json.JSONObject;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import uk.ac.cam.cares.jps.agent.configuration.gPROMSAgentConfiguration;
import uk.ac.cam.cares.jps.agent.configuration.gPROMSAgentProperty;
//import uk.ac.cam.cares.jps.agent.gPROMS.gPROMSAgent;
//import uk.ac.cam.cares.jps.agent.gPROMS.gPROMSAgentException;
import uk.ac.cam.cares.jps.agent.utils.ZipUtility;
import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.slurm.job.JobSubmission;
import uk.ac.cam.cares.jps.base.slurm.job.PostProcessing;
import uk.ac.cam.cares.jps.base.slurm.job.SlurmJobException;
import uk.ac.cam.cares.jps.base.slurm.job.Status;
import uk.ac.cam.cares.jps.base.slurm.job.Utils;
import uk.ac.cam.cares.jps.base.slurm.job.SlurmJob;
import uk.ac.cam.cares.jps.base.util.FileUtil;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;


/**
 * gPROMS Agent developed for setting-up and running gPROMS chemical network on
 * HPC.
 *The input files for gPROMS execution should be placed in user.home//input folder
 * @author Aravind Devanand (aravind@u.nus.edu)
 *
 */
@Controller
@WebServlet(urlPatterns = { gPROMSAgent.JOB_REQUEST_PATH, gPROMSAgent.JOB_STATISTICS_PATH,
		/* gPROMSAgent.JOB_OUTPUT_REQUEST_PATH */ })
public class gPROMSAgent extends JPSAgent {

	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(gPROMSAgent.class);
	private File workspace;
	static JobSubmission jobSubmission;
	static SlurmJob slurmJob;
	public static ApplicationContext applicationContextgPROMSAgent;
	public static gPROMSAgentProperty gpROMSAgentProperty;

	public static final String BAD_REQUEST_MESSAGE_KEY = "message";
	public static final String UNKNOWN_REQUEST = "The request is unknown to gPROMS Agent";

	public static final String JOB_REQUEST_PATH = "/job/request";
	public static final String JOB_OUTPUT_REQUEST_PATH = "/job/output/request";
	public static final String JOB_STATISTICS_PATH = "/job/statistics";
	public static final String JOB_SHOW_STATISTICS_PATH = "/job/show/statistics";
	
	// Create a temporary folder in the user's home location
	private Path temporaryDirectory = null;

	public JSONObject produceStatistics(String input) throws IOException, gPROMSAgentException {
		System.out.println("Received a request to send statistics.\n");
		logger.info("Received a request to send statistics.\n");
		// Initialises all properties required for this agent to set-up<br>
		// and run jobs. It will also initialise the unique instance of<br>
		// Job Submission class.
		initAgentProperty();
		return jobSubmission.getStatistics(input);
	}

	@RequestMapping(value = gPROMSAgent.JOB_SHOW_STATISTICS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public String showStatistics() throws IOException, gPROMSAgentException {
		System.out.println("Received a request to show statistics.\n");
		logger.info("Received a request to show statistics.\n");
		initAgentProperty();
		return jobSubmission.getStatistics();
	}

	/**
	 * Starts the asynchronous scheduler to monitor quantum jobs.
	 *
	 * @throws gPROMSAgentException
	 */

	public void init() throws ServletException {
		logger.info("---------- gPROMS Simulation Agent has started ----------");
		System.out.println("---------- gPROMS Simulation Agent has started ----------");
		System.out.println(System.getProperty("user.dir"));
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		gPROMSAgent gPROMSAgent = new gPROMSAgent();
		// initialising classes to read properties from the gPROMS-agent.properites
		// file
		initAgentProperty();
		// In the following method call, the parameter getAgentInitialDelay-<br>
		// ToStartJobMonitoring refers to the delay (in seconds) before<br>
		// the job scheduler starts and getAgentPeriodicActionInterval<br>
		// refers to the interval between two consecutive executions of<br>
		// the scheduler.
		executorService.scheduleAtFixedRate(() -> {
			try {
				gPROMSAgent.monitorJobs();
			} catch (SlurmJobException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, gpROMSAgentProperty.getAgentInitialDelayToStartJobMonitoring(),
				gpROMSAgentProperty.getAgentPeriodicActionInterval(), TimeUnit.SECONDS);
		logger.info("---------- gPROMS Simulation jobs are being monitored  ----------");
		System.out.println("---------- gPROMS Simulation jobs are being monitored  ----------");

	}


	/**
	 * Initialises the unique instance of the gpROMSAgentProperty class that<br>
	 * reads all properties of gPROMSAgent from the kinetics-agent property
	 * file.<br>
	 *
	 * Initialises the unique instance of the SlurmJobProperty class and<br>
	 * sets all properties by reading them from the kinetics-agent property file<br>
	 * through the gPROMSAgent class.
	 */
	public void initAgentProperty() {
		// initialising classes to read properties from the kinetics-agent.properites
		// file
		if (applicationContextgPROMSAgent == null) {
			applicationContextgPROMSAgent = new AnnotationConfigApplicationContext(gPROMSAgentConfiguration.class);
		}
		if (gpROMSAgentProperty == null) {
			gpROMSAgentProperty = applicationContextgPROMSAgent.getBean(gPROMSAgentProperty.class);
		}
		if (jobSubmission == null) {
			jobSubmission = new JobSubmission(gpROMSAgentProperty.getAgentClass(), gpROMSAgentProperty.getHpcAddress());
			jobSubmission.slurmJobProperty.setHpcServerLoginUserName(gpROMSAgentProperty.getHpcServerLoginUserName());
			jobSubmission.slurmJobProperty
					.setHpcServerLoginUserPassword(gpROMSAgentProperty.getHpcServerLoginUserPassword());
			jobSubmission.slurmJobProperty.setAgentClass(gpROMSAgentProperty.getAgentClass());
			jobSubmission.slurmJobProperty
					.setAgentCompletedJobsSpacePrefix(gpROMSAgentProperty.getAgentCompletedJobsSpacePrefix());
			jobSubmission.slurmJobProperty
					.setAgentFailedJobsSpacePrefix(gpROMSAgentProperty.getAgentFailedJobsSpacePrefix());
			jobSubmission.slurmJobProperty.setHpcAddress(gpROMSAgentProperty.getHpcAddress());
			jobSubmission.slurmJobProperty.setInputFileName(gpROMSAgentProperty.getInputFileName());
			jobSubmission.slurmJobProperty.setInputFileExtension(gpROMSAgentProperty.getInputFileExtension());
			jobSubmission.slurmJobProperty.setOutputFileName(gpROMSAgentProperty.getOutputFileName());
			jobSubmission.slurmJobProperty.setOutputFileExtension(gpROMSAgentProperty.getOutputFileExtension());
			jobSubmission.slurmJobProperty.setJsonInputFileName(gpROMSAgentProperty.getJsonInputFileName());
			jobSubmission.slurmJobProperty.setJsonFileExtension(gpROMSAgentProperty.getJsonFileExtension());
			jobSubmission.slurmJobProperty.setJsonFileExtension(gpROMSAgentProperty.getJsonFileExtension());
			jobSubmission.slurmJobProperty.setSlurmScriptFileName(gpROMSAgentProperty.getSlurmScriptFileName());
			jobSubmission.slurmJobProperty.setMaxNumberOfHPCJobs(gpROMSAgentProperty.getMaxNumberOfHPCJobs());
			jobSubmission.slurmJobProperty.setAgentInitialDelayToStartJobMonitoring(
					gpROMSAgentProperty.getAgentInitialDelayToStartJobMonitoring());
			jobSubmission.slurmJobProperty
					.setAgentPeriodicActionInterval(gpROMSAgentProperty.getAgentPeriodicActionInterval());
		}
	}

	/**
	 * Receives and processes HTTP requests that match with the URL patterns<br>
	 * listed in the annotations of this class.
	 *
	 */
	@Override
	public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
		String path = request.getServletPath();
		System.out.println("A request has been received..............................");
		if (path.equals(gPROMSAgent.JOB_REQUEST_PATH)) {
			try {
				return setUpJob(requestParams.toString());
			} catch (SlurmJobException | IOException | gPROMSAgentException e) {
				throw new JPSRuntimeException(e.getMessage());
			}
			 } else if (path.equals(gPROMSAgent.JOB_OUTPUT_REQUEST_PATH)) {
			 JSONObject result = getSimulationResults(requestParams);
			 return result;
		} else if (path.equals(gPROMSAgent.JOB_STATISTICS_PATH)) {
			try {
				return produceStatistics(requestParams.toString());
			} catch (IOException | gPROMSAgentException e) {
				throw new JPSRuntimeException(e.getMessage());
			}
		} else {
			System.out.println("Unknown request");
			throw new JPSRuntimeException(UNKNOWN_REQUEST);
		}
	}

	/**
	 * Validates input parameters specific to Kinetics Agent to decide whether<br>
	 * the job set up request can be served.
	 */
	@Override
	public boolean validateInput(JSONObject requestParams) throws BadRequestException {
		if (requestParams.isEmpty()) {
			throw new BadRequestException();
		}
		return true;
	}

	/**
	 * Checks the status of a job and returns results if it is finished and<br>
	 * post-processing is successfully completed. If the job has terminated<br>
	 * with an error or failed, then error termination message is sent.
	 *
	 * The JSON input for this request has the following format: {"jobId":
	 * "login-skylake.hpc.cam.ac.uk_117804308649998"}
	 *
	 * @param requestParams
	 * @return
	 */
	private JSONObject getSimulationResults(JSONObject requestParams) {
		JSONObject json = new JSONObject();
		String jobId = getJobId(requestParams);
		if (jobId == null) {
			return json.put("message", "jobId is not present in the request parameters.");
		}
		initAgentProperty();
		JSONObject message = checkJobInWorkspace(jobId);
		if (message != null) {
			return message;
		}
		JSONObject result = checkJobInCompletedJobs(jobId);
		if (result != null) {
			return result;
		}
		message = checkJobInFailedJobs(jobId);
		if (message != null) {
			return message;
		}
		return json.put("message", "The job is not available in the system.");
	}

	/**
	 * Checks the presence of the requested job in the workspace.<br>
	 * If the job is available, it returns that the job is currently running.
	 *
	 * @param json
	 * @return
	 */
	private JSONObject checkJobInWorkspace(String jobId) {
		JSONObject json = new JSONObject();
		// The path to the set-up and running jobs folder.
		workspace = jobSubmission.getWorkspaceDirectory();
		if (workspace.isDirectory()) {
			File[] jobFolders = workspace.listFiles();
			for (File jobFolder : jobFolders) {
				if (jobFolder.getName().equals(jobId)) {
					return json.put("message", "The job is being executed.");
				}
			}
		}
		return null;
	}

	/**
	 * Checks the presence of the requested job in the completed jobs.<br>
	 * If the job is available, it returns the result.
	 *
	 * @param json
	 * @return
	 */
	private JSONObject checkJobInCompletedJobs(String jobId) {
		JSONObject json = new JSONObject();
		// The path to the completed jobs folder.
		String completedJobsPath = workspace.getParent().concat(File.separator)
				.concat(gpROMSAgentProperty.getAgentCompletedJobsSpacePrefix()).concat(workspace.getName());
		File completedJobsFolder = new File(completedJobsPath);
		if (completedJobsFolder.isDirectory()) {
			File[] jobFolders = completedJobsFolder.listFiles();
			for (File jobFolder : jobFolders) {
				if (jobFolder.getName().equals(jobId)) {
					try {
						String inputJsonPath = completedJobsPath.concat(File.separator).concat(jobFolder.getName())
								.concat(File.separator).concat(gpROMSAgentProperty.getReferenceOutputJsonFile());
						InputStream inputStream = new FileInputStream(inputJsonPath);
						return new JSONObject(FileUtil.inputStreamToString(inputStream));
					} catch (FileNotFoundException e) {
						return json.put("message",
								"The job has been completed, but the file that contains results is not found.");
					}
				}
			}
		}
		return null;
	}

	/**
	 * Checks the presence of the requested job in the failed jobs.<br>
	 * If the job is available, it returns a message saying that<br>
	 * job has failed.
	 *
	 * @param json
	 * @param jobId
	 * @return
	 */
	private JSONObject checkJobInFailedJobs(String jobId) {
		JSONObject json = new JSONObject();
		// The path to the failed jobs folder.
		String failedJobsPath = workspace.getParent().concat(File.separator)
				.concat(gpROMSAgentProperty.getAgentFailedJobsSpacePrefix()).concat(workspace.getName());
		File failedJobsFolder = new File(failedJobsPath);
		if (failedJobsFolder.isDirectory()) {
			File[] jobFolders = failedJobsFolder.listFiles();
			for (File jobFolder : jobFolders) {
				if (jobFolder.getName().equals(jobId)) {
					return json.put("message",
							"The job terminated with an error. Please check the failed jobs folder.");
				}
			}
		}
		return null;
	}

	
	
	/**
	 * Monitors already set up jobs.
	 *
	 * @throws SlurmJobException
	 */
	private void monitorJobs() throws SlurmJobException {
		//Configures all properties required for setting-up and running a Slurm job. 
		jobSubmission.monitorJobs();
		processOutputs();
	}

	/**
	 * Monitors the currently running quantum jobs to allow new jobs to start.</br>
	 * In doing so, it checks if the number of running jobs is less than the</br>
	 * maximum number of jobs allowed to run at a time.
	 *
	 */
	public void processOutputs() {
		workspace = jobSubmission.getWorkspaceDirectory();
		try {
			if (workspace.isDirectory()) {
				File[] jobFolders = workspace.listFiles();
				for (File jobFolder : jobFolders) {

					if (Utils.isJobCompleted(jobFolder) && !Utils.isJobOutputProcessed(jobFolder)) {

						boolean outcome = postProcessing(Paths.get(jobFolder.getAbsolutePath()));

						if (outcome) {
							// Success
							PostProcessing.updateJobOutputStatus(jobFolder);
						} else {
							// Failure
							Utils.modifyStatus(
								Utils.getStatusFile(jobFolder).getAbsolutePath(),
								Status.JOB_LOG_MSG_ERROR_TERMINATION.getName()
							);
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("gPROMSsAgent: IOException.".concat(e.getMessage()));
			e.printStackTrace();
		}
	}

	
	
	/**
	 * Executes post-processing on the input job folder, returning true if the post-processing task returns
	 * successfully.
	 *
	 * @param jobFolder job folder
	 *
	 * @return true if post-processing is successful
	 */
	public boolean postProcessing(Path jobFolder) throws IOException {
		// Find the job results ZIP
		Path archive = Paths.get(
				jobFolder.toString(),
				gpROMSAgentProperty.getOutputFileName() + gpROMSAgentProperty.getOutputFileExtension()
			);
			if (!Files.exists(archive)) throw new IOException("Cannot find expected archive at: " + archive);

		if (!Files.exists(archive) || Files.readAllBytes(archive).length <= 0) {
			return false;
		}
		return true;

	}
	/**
	 * Sets up a quantum job by creating the job folder and the following files</br>
	 * under this folder:</br>
	 * - the input file.</br>
	 * - the Slurm script file.</br. - the Status file.</br>
	 * - the JSON input file, which comes from the user request.</br>
	 *
	 * @param jsonString
	 * @return
	 * @throws IOException
	 * @throws gPROMSAgentException
	 */
	public JSONObject setUpJob(String jsonString) throws IOException, gPROMSAgentException, SlurmJobException {
		String message = setUpJobOnAgentMachine(jsonString);
		JSONObject obj = new JSONObject();
		obj.put("jobId", message);
		return obj;
	}

	/**
	 * Sets up the quantum job for the current input.
	 *
	 * @param jsonInput
	 * @return
	 * @throws IOException
	 * @throws gPROMSAgentException
	 */
	private String setUpJobOnAgentMachine(String jsonInput)
			throws IOException, gPROMSAgentException, SlurmJobException {
		initAgentProperty();
		long timeStamp = Utils.getTimeStamp();
		String jobFolderName = getNewJobFolderName(gpROMSAgentProperty.getHpcAddress(), timeStamp);
		System.out.println("Jobfolder is"+ jobFolderName);
		//
		
		
		Path temporaryDirectory1 = Paths.get(System.getProperty("user.home"), "." + jobFolderName);
		System.out.println("tempdir is"+ temporaryDirectory1.toString());
		System.out.println("tempdir1 is"+ temporaryDirectory1.toString());
		System.out.println("userdir is"+ System.getProperty("user.dir"));
		System.out.println("scrptdir is"+gpROMSAgentProperty.getAgentScriptsLocation().toString());
		return jobSubmission.setUpJob(jsonInput,
				new File(URLDecoder.decode(getClass().getClassLoader().getResource(gpROMSAgentProperty.getSlurmScriptFileName())
						.getPath(), "utf-8")),
			/**	new File("C:/Users/caresadmin/JParkSimulator-git/JPS_DIGITAL_TWIN/src/main/resources/input.zip"),
			*	timeStamp);
			*/
				getInputFile(jsonInput, jobFolderName),timeStamp);
	}			
	

	/**
	 * Prepares input files, bundle them in a zip file and return the zip file to the calling method.
	 *
	 * @param jsonInput
	 * @param jobFolderName
	 * @return
	 * @throws IOException
	 * @throws gPROMSAgentException
	 */
	private File getInputFile(String jsonInput, String jobFolderName) throws IOException, gPROMSAgentException {
		
		//Preparation of settings.input file
		
		
		
		

//Extracting required variables from owl files
			System.out.println(System.getProperty("user.dir"));
		   String filePath = System.getProperty("user.home")+"\\input\\debutaniser_section.owl";

			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

			try {
			    File file = new File(filePath);
			    FileInputStream reader = new FileInputStream(file);
			    model.read(reader,null);     //load the ontology model
			} catch (Exception e) {
			    e.printStackTrace();
			}
//Sample Sparql query
//			String sparqlQuery =
//					"SELECT ?Temp\n"+
//					"WHERE {\n "+
//					"?x a <http://www.semanticweb.org/caresadmin1/ontologies/2020/5/untitled-ontology-396#Temperature> .\n"+	
//					"?x  <http://www.semanticweb.org/caresadmin1/ontologies/2020/5/untitled-ontology-396#hasValue>  ?Temp .\n"+
//					"}" ;
//			//System.err.println(sparqlQuery); //Prints the query
//			Query query = QueryFactory.create(sparqlQuery);
//
//			QueryExecution qe = QueryExecutionFactory.create(query, model);
//
//			ResultSet results = qe.execSelect();
//			//ResultSetFormatter.out(System.out, results, query);			
//			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();	
//			ResultSetFormatter.outputAsCSV(byteArrayOutputStream,results);
//			String s=byteArrayOutputStream.toString();
//			System.out.println(s);
//			//List se=Arrays.asList(s.split("\\s*,\\s*|http"));
//			//System.out.println(se);
//			String[] sa= s.split("\\r?\\n");
//			//System.out.println(Arrays.toString(sa[1]));
//			System.out.println(sa[1]);

//Trial one with the debutaniser file
			
			String TempQuery =
					"PREFIX process:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_function/process.owl#>\r\n" +
					"PREFIX system:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>\r\n" +
					"\r \n"+
					"SELECT ?Temp\n"+
					"WHERE {\n "+
					"?x a  system:ScalarValue  .\n" + 
					"?x system:value ?Temp .\n"+
					"}" ;
			//System.err.println(TempQuery); //Prints the query
			Query queryt = QueryFactory.create(TempQuery);

			QueryExecution qet = QueryExecutionFactory.create(queryt, model);

			ResultSet results = qet.execSelect();
			//ResultSetFormatter.out(System.out, results, query);			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();	
			ResultSetFormatter.outputAsCSV(byteArrayOutputStream,results);
			String s=byteArrayOutputStream.toString();
			//System.out.println(s);
			//List se=Arrays.asList(s.split("\\s*,\\s*|http"));
			//System.out.println(se);
			String[] sa= s.split("\\r?\\n");
			//System.out.println(Arrays.toString(sa[1]));
			//System.out.println(sa[1]);			
			
//Once the file is created, data has to be written to it
		  try {
			  String input = System.getProperty("user.home")+"\\input\\Settings.input";
		      FileWriter myWriter = new FileWriter(input);
		      myWriter.write("Feed__T \n");
		      myWriter.write(sa[1]);
		      myWriter.write("\n");
		      myWriter.write("Feed__P\n");
		      myWriter.write(sa[2]);
		      myWriter.write("\nFeed__molar_fraction(\"PROPANE\")\r\n" + 
		      		"0.4\r\n" + 
		      		"Feed__molar_fraction(\"ISOBUTANE\")\r\n" + 
		      		".6\r\n" + 
		      		"Feed_pump__Mechanical efficiency\r\n" + 
		      		"85\r\n" + 
		      		"Feed_heater__Outlet Temperature\r\n" + 
		      		"330\r\n" + 
		      		"Column__column_diameter\r\n" + 
		      		"5.91\r\n" + 
		      		"Column__plate_efficiency\r\n" + 
		      		"80\r\n" + 
		      		"Column__plate_spacing\r\n" + 
		      		".61\r\n" + 
		      		"Column__no_stages_actual\r\n" + 
		      		"32\r\n" + 
		      		"Pressure_controller__K_c\r\n" + 
		      		"20\r\n" + 
		      		"Pressure_controller__tau_I\r\n" + 
		      		"12\r\n" + 
		      		"Pressure_controller__target_value\r\n" + 
		      		"14.173193\r\n" + 
		      		"Temeprature_controller__K_c\r\n" + 
		      		"2\r\n" + 
		      		"Temeprature_controller__tau_I\r\n" + 
		      		"1\r\n" + 
		      		"Temeprature_controller__target_value\r\n" + 
		      		"332.2859\r\n" + 
		      		"Reboiler_level_controller__K_c\r\n" + 
		      		"1\r\n" + 
		      		"Reboiler_level_controller__tau_I\r\n" + 
		      		"20\r\n" + 
		      		"Reboiler_level_controller__target_value\r\n" + 
		      		"0.625\r\n" + 
		      		"Condenser_level_controller__K_c\r\n" + 
		      		"1\r\n" + 
		      		"Condenser_level_controller__tau_I\r\n" + 
		      		"20\r\n" + 
		      		"Condenser_level_controller__target_value\r\n" + 
		      		".245\r\n" + 
		      		"Distilate__Pressure\r\n" + 
		      		"12\r\n" + 
		      		"Bottoms__Pressure\r\n" + 
		      		"12\r\n" + 
		      		"step1__initial_value\r\n" + 
		      		"52\r\n" + 
		      		"step1__final_value\r\n" + 
		      		"54.5\r\n" + 
		      		"step1__step_time\r\n" + 
		      		"50\r\n" + 
		      		"step2__initial_value\r\n" + 
		      		"0\r\n" + 
		      		"step2__Final_value\r\n" + 
		      		"2.5\r\n" + 
		      		"step2__step_time\r\n" + 
		      		"550\r\n" + 
		      		"step3__initial_value\r\n" + 
		      		"0\r\n" + 
		      		"step3__final_value\r\n" + 
		      		"2.5\r\n" + 
		      		"step3__step_time\r\n" + 
		      		"650\r\n" + 
		      		"step4__initial_value\r\n" + 
		      		"0\r\n" + 
		      		"step4__final_value\r\n" + 
		      		"2.5\r\n" + 
		      		"step4__step_time\r\n" + 
		      		"750");
		      
		      myWriter.close();
		      System.out.println("Successfully wrote to the file.");
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
	
		
		// Compress all files in the temporary directory into a ZIP
		//Path zipFile = Paths.get(System.getProperty("user.home"), temporaryDirectory.getFileName().toString() + ".zip");
		Path zipFile = Paths.get(System.getProperty("user.home")+"\\input.zip");
		// Create a temporary folder in the user's home location
		//Path temporaryDirectory= Paths.get("C:\\Users\\caresadmin\\JParkSimulator-git\\JPS_DIGITAL_TWIN\\src\\main\\resources\\input");

		Path temporaryDirectory= Paths.get(System.getProperty("user.home")+"\\input");
		// Path temporaryDirectory= Paths.get("C:\\Users\\caresadmin\\JParkSimulator-git\\JPS_DIGITAL_TWIN\\src\\main\\resources\\input");
		List<File> zipContents = new ArrayList<>();

		Files.walk(temporaryDirectory)
			.map(Path::toFile)
			.forEach((File f) -> zipContents.add(f));
		zipContents.remove(temporaryDirectory.toFile());

		// Will throw an IOException if something goes wrong
		new ZipUtility().zip(zipContents, zipFile.toString());

		// Return the final ZIP file
		return new File(zipFile.toString());
	}


	/**
	 * Produces a job folder name by following the schema hpcAddress_timestamp.
	 *
	 * @param hpcAddress
	 * @param timeStamp
	 * @return
	 */
	public String getNewJobFolderName(String hpcAddress, long timeStamp) {
		return hpcAddress.concat("_").concat("" + timeStamp);
	}

	/**
	 * Returns the job id.
	 *
	 * @param jsonObject
	 * @return
	 */
	public String getJobId(JSONObject jsonObject) {
		if (jsonObject.has("jobId")) {
			return jsonObject.get("jobId").toString();
		} else {
			return null;
		}
	}
}
