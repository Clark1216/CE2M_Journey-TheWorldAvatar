package uk.ac.cam.cares.jps.agent.rfid;

import org.json.JSONObject;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;
import uk.ac.cam.cares.jps.base.agent.JPSAgent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Properties;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class with a main method that is the entry point of the compiled war and puts all components together to retrieve
 * data from the API and write it into the database.
 * @author 
 */
@WebServlet(urlPatterns = {"/retrieve"})
public class RFIDUpdateAgentLauncher extends JPSAgent {
	
	public static final String KEY_AGENTPROPERTIES = "agentProperties";
	public static final String KEY_APIPROPERTIES = "apiProperties";
	public static final String KEY_CLIENTPROPERTIES = "clientProperties";
	public static final String KEY_LAUNCHERPROPERTIES = "launcherProperties";
	
	
	 String agentProperties;
	 String apiProperties;
	 String clientProperties;
	 String launcherProperties;
	
    /**
     * Logger for reporting info/errors.
     */
    private static final Logger LOGGER = LogManager.getLogger(RFIDUpdateAgentLauncher.class);
    /**
     * Logging / error messages
     */
    private static final String ARGUMENT_MISMATCH_MSG = "Need four properties files in the following order: 1) input agent 2) time series client 3) API connector 4) launcher properties.";
    private static final String AGENT_ERROR_MSG = "The RFID Update agent could not be constructed!";
    private static final String TSCLIENT_ERROR_MSG = "Could not construct the time series client needed by the input agent!";
    private static final String INITIALIZE_ERROR_MSG = "Could not initialize time series.";
    private static final String CONNECTOR_ERROR_MSG = "Could not construct the RFID API connector needed to interact with the API!";
    private static final String GET_READINGS_ERROR_MSG = "Some readings could not be retrieved.";

    @Override
    public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
        return processRequestParameters(requestParams);
    } 
    
    
    @Override
    public JSONObject processRequestParameters(JSONObject requestParams) {
    	JSONObject jsonMessage = new JSONObject();
      if (validateInput(requestParams)) {
        	LOGGER.info("Passing request to RFID Update Agent..");
            String agentProperties = System.getenv(requestParams.getString(KEY_AGENTPROPERTIES));
            String clientProperties = System.getenv(requestParams.getString(KEY_CLIENTPROPERTIES));
            String apiProperties = System.getenv(requestParams.getString(KEY_APIPROPERTIES));
            String launcherProperties = System.getenv(requestParams.getString(KEY_LAUNCHERPROPERTIES));
            String[] args = new String[] {agentProperties,clientProperties,apiProperties, launcherProperties};
            
				jsonMessage = initializeAgent(args);
			
            jsonMessage.accumulate("Result", "Timeseries Data has been updated.");
            requestParams = jsonMessage;
            }
      else {
    	  jsonMessage.put("Result", "Request parameters are not defined correctly.");
    	  requestParams = jsonMessage;
      }
	return requestParams;
}
    
    @Override
    public boolean validateInput(JSONObject requestParams) throws BadRequestException {
      boolean validate = true;
      String agentProperties;
      String apiProperties;
      String clientProperties;
      String launcherProperties;
      if (requestParams.isEmpty()) {
    	  validate = false;
      }
      else {
 		 validate = requestParams.has(KEY_AGENTPROPERTIES);
 		 if (validate == true) {
 		 validate = requestParams.has(KEY_CLIENTPROPERTIES);
 		 }
 		 if (validate == true) {
 		 validate = requestParams.has(KEY_APIPROPERTIES);
 		 }
 		if (validate == true) {
 	 		 validate = requestParams.has(KEY_LAUNCHERPROPERTIES);
 	 		 }
 		 if (validate == true) {
 		 agentProperties = (requestParams.getString(KEY_AGENTPROPERTIES));
 		 clientProperties =  (requestParams.getString(KEY_CLIENTPROPERTIES));
 		 apiProperties = (requestParams.getString(KEY_APIPROPERTIES));
 		 launcherProperties = (requestParams.getString(KEY_LAUNCHERPROPERTIES));
 		 
 		if (System.getenv(agentProperties) == null) {
 			validate = false;
 		 
 		 }
 		if (System.getenv(apiProperties) == null) {
 			validate = false;
 		 
 		 }
 		if (System.getenv(clientProperties) == null) {
 			validate = false;
 		
 		 }
 		if (System.getenv(launcherProperties) == null) {
 			validate = false;
 		}
 		}
 		}
	return validate;
      
      }
    
 // TODO: Use proper argument parsing
    /**
     * Main method that runs through all steps to update the data received from the RFID API.
     * defined in the provided properties file.
     * @param args The command line arguments. Four properties files should be passed here in order: 1) input agent
     *             2) time series client 3) API connector 4)Launcher Properties .
     * @throws FileNotFoundException 
     */
    
    public static JSONObject initializeAgent(String[] args) {

        // Ensure that there are three properties files
        if (args.length != 4) {
            LOGGER.error(ARGUMENT_MISMATCH_MSG);
            throw new JPSRuntimeException(ARGUMENT_MISMATCH_MSG);
        }
        LOGGER.debug("Launcher called with the following files: " + String.join(" ", args));

        // Create the agent
        RFIDUpdateAgent agent;
        try {
            agent = new RFIDUpdateAgent(args[0]);
        } catch (IOException e) {
            LOGGER.error(AGENT_ERROR_MSG, e);
            throw new JPSRuntimeException(AGENT_ERROR_MSG, e);
        }
        LOGGER.info("Input agent object initialized.");
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.accumulate("Result", "Input agent object initialized.");

        // Create and set the time series client
        try {
            TimeSeriesClient<OffsetDateTime> tsClient = new TimeSeriesClient<>(OffsetDateTime.class, args[1]);
            agent.setTsClient(tsClient);
        } catch (IOException | JPSRuntimeException e) {
            LOGGER.error(TSCLIENT_ERROR_MSG, e);
            throw new JPSRuntimeException(TSCLIENT_ERROR_MSG, e);
        }
        LOGGER.info("Time series client object initialized.");
        jsonMessage.accumulate("Result", "Time series client object initialized.");
        // Initialize time series'
        try {
            agent.initializeTimeSeriesIfNotExist();
        }
        catch (JPSRuntimeException e) {
            LOGGER.error(INITIALIZE_ERROR_MSG,e);
            throw new JPSRuntimeException(INITIALIZE_ERROR_MSG, e);
        }

        // Create the connector to interact with the RFID API
        RFIDUpdateAPIConnector connector;
        try {
            connector = new RFIDUpdateAPIConnector(args[2]);
        } catch (IOException e) {
            LOGGER.error(CONNECTOR_ERROR_MSG, e);
            throw new JPSRuntimeException(CONNECTOR_ERROR_MSG, e);
        }
        LOGGER.info("API connector object initialized.");
        jsonMessage.accumulate("Result", "API connector object initialized.");

        // Retrieve readings
        JSONObject RFIDVariableReadings;
        
        try {
            RFIDVariableReadings = connector.getAllReadings();
        }
        catch (Exception e) {
            LOGGER.error(GET_READINGS_ERROR_MSG, e);
            throw new JPSRuntimeException(GET_READINGS_ERROR_MSG, e);
        }
        LOGGER.info(String.format("Retrieved %d RFID readings.",
                RFIDVariableReadings.length()));
        jsonMessage.accumulate("Result", "Retrieved " + RFIDVariableReadings.length() +  
        		" RFID readings.");
        
        File file = new File(args[3]);
        if (!file.exists()) {
            throw new JPSRuntimeException("No properties file found at specified filepath: " + args[3]);
        }
        // Read username and password for ThingsBoard API from properties file
        // Try-with-resource to ensure closure of input stream
        String keys = null;
        try (InputStream input = new FileInputStream(file)) {

            // Load properties file from specified path
            Properties prop = new Properties();
            prop.load(input);
            if (prop.containsKey("keys")) {
            	 keys = prop.getProperty("keys");
            } else {
                throw new IOException("Properties file is missing \"keys=<keys>\"");
            }
        } catch (IOException e) {
			e.printStackTrace();
		}
        
        // If readings are not empty there is new data
        if(!RFIDVariableReadings.isEmpty()) {
        	
            // Update the data
        	for (int i =0; i < keys.split(",").length; i++) {
            agent.updateData(RFIDVariableReadings, keys.split(",")[i]);
            LOGGER.info("Data updated with new readings from API.");
            jsonMessage.accumulate("Result", "Data updated with new readings from API.");
        	}
        	}
        // If all are empty no new readings are available
        else if(RFIDVariableReadings.isEmpty()) {
            LOGGER.info("No new readings are available.");
            jsonMessage.accumulate("Result", "No new readings are available.");
        }
        
		return jsonMessage;
        
    }

}
