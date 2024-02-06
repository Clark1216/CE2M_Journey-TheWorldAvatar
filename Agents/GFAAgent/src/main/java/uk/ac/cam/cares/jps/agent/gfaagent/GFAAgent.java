package uk.ac.cam.cares.jps.agent.gfaagent;

import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(urlPatterns = {"/gfa", "/floors"})
public class GFAAgent extends JPSAgent{
    private static final String PROPERTIES_PATH = "/resources/config.properties";
    
    private EndpointConfig endpointConfig = new EndpointConfig();

    private String dbName;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String kgurl;
    private String kgNameSpace;
    public String floorsCsv;

    public synchronized void init() {
        readConfig();
        this.dbUrl = endpointConfig.getDbUrl(dbName);
        this.dbUser = endpointConfig.getDbUser();
        this.dbPassword = endpointConfig.getDbPassword();
        this.kgurl = endpointConfig.getKGUrl(kgNameSpace);
    }

    public void readConfig() {
        try (InputStream input = FileReader.getStream(PROPERTIES_PATH)) {
            Properties prop = new Properties();
            prop.load(input);
            this.dbName = prop.getProperty("db.name");
            this.floorsCsv = prop.getProperty("floors.csv");
            this.kgNameSpace = prop.getProperty("kg.namespace");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new JPSRuntimeException("config.properties file not found");
        } catch (IOException e) {
            e.printStackTrace();
            throw new JPSRuntimeException(e);
        }
    }

    @Override
    public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
        return processRequestParameters(requestParams);
    }

    @Override
    public JSONObject processRequestParameters(JSONObject requestParams) {

        System.out.println("debug mode");
        try {
            GFACalculation gfaCalculation = new GFACalculation(dbUrl, dbUser, dbPassword);
            GFAkg gfAkg = new GFAkg(kgurl);
            boolean test = requestParams.has("gfa");
            if(requestParams.getString("requestUrl").contains("gfa")){            
                //calculate GFA 1. query footpring 2. query height (if no height, estimate 3.2m/floor) 3. calculate 4. store
                JSONArray gfaResult = gfaCalculation.calculationGFA();
            }else if(requestParams.getString("requestUrl").contains("floors")){
               //integrate floors data
                IntegrateFloors integrateFloors = new IntegrateFloors(floorsCsv, dbUrl, dbUser, dbPassword);
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new JPSRuntimeException(e);
        }

        return requestParams;
    }
}
