package uk.ac.cam.cares.jps.agent.nusDavisWeatherStation;


import com.weatherlink.api.v2.signature.SignatureCalculator;
import com.weatherlink.api.v2.signature.SignatureException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.weatherlink;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

import java.io.*;
import java.time.Instant;
import java.util.Properties;

class NUSDavisWeatherStationAPIConnector{
    private String api_key;
    private String api_secret;
    private String api_url="https://api.weatherlink.com/v2/historic/";
    private int stationId;
    private String api_Signature=" ";
    private long end_timestamp= Instant.now().getEpochSecond();
    private long start_timestamp= end_timestamp-1800;//assuming starting time is half an hour before the end time.


    private static final String ERR_MSG="Weather data could not be retrieved";
    private static final Logger LOGGER = LogManager.getLogger(NUSDavisWeatherStationInputAgentLauncher.class);

    public NUSDavisWeatherStationAPIConnector(String my_key, String my_secret, String my_url, int my_stationId){
        this.api_key=my_key;
        this.api_secret=my_secret;
        this.api_url=my_url;
        this.stationId=my_stationId;
    }

    public NUSDavisWeatherStationAPIConnector(String my_key, String my_secret, String my_url, int my_stationId,long start,long end){
        this.api_key=my_key;
        this.api_secret=my_secret;
        this.api_url=my_url;
        this.stationId=my_stationId;
        this.start_timestamp=start;
        this.end_timestamp=end;
    }

    public NUSDavisWeatherStationAPIConnector(String filepath)throws IOException {
        loadAPIconfigs(filepath);
    }

    /*
    * Method for setting the API signature. It uses the SDK given by the API provider: weatherlink.
    * The HMAC SHA-256 algorithm is used to generate the API signature.
    * */
    private void setAPISignature(long requestTimestamp) throws SignatureException {
        SignatureCalculator sc=new SignatureCalculator();
        String signature= sc.calculateHistoricSignature(api_key,api_secret,requestTimestamp,stationId,start_timestamp,end_timestamp);
        this.api_Signature=signature;
    }

    /**
     * Retrieves the latest weather readings from the weather station API
     * @return a JSON Object containing key-value pairs
     */

    public JSONObject getWeatherReadings(){
        try{
            return retrieveWeatherReadings();
        }
        catch(IOException | JSONException | SignatureException e){
            LOGGER.error(ERR_MSG,e);
            throw new JPSRuntimeException(ERR_MSG,e);
        }
    }

    /**
     * Retrieves the latest readings from the weather station API
     * @return Readings in a JSON Object with multiple key-value pairs
     */
    private JSONObject retrieveWeatherReadings() throws SignatureException, IOException {
        long unix_timestamp = Instant.now().getEpochSecond();
        if (api_Signature == " ")
            setAPISignature(unix_timestamp);
        //Sample path taken from the documentation using mock values
        //https://api.weatherlink.com/v2/historic/96230?api-key=987654321&t=1558729481&start-timestamp=1561964400&end-timestamp=1562050800&api-signature=fbe025018d78d7b13bb09eb36c6c2d7b1461b33253bf3d291b1ed37826599e8e
        String path = api_url + stationId + "?api-key=" + api_key + "&t=" + unix_timestamp + "&start-timestamp=" + start_timestamp + "&end-timestamp=" + end_timestamp + "api-signature=" + api_Signature;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet readingRequest = new HttpGet(path);
            try (CloseableHttpResponse response = httpclient.execute(readingRequest)) {
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    return new JSONObject(EntityUtils.toString(response.getEntity()));
                } else
                    throw new HttpResponseException(status, "Data could not be retrieved due to a server error");
            }
        }
    }

    /**
     * Reads the api_key, stationId and api_url needed to connect to the API from a properties file and saves it in fields.
     * @param filepath Path to the properties file from which to read the api_key, api_secret, api_url and stationId
     */
    private void loadAPIconfigs(String filepath) throws IOException{
        File file=new File(filepath);
        if(!file.exists()){
            throw new FileNotFoundException("There was no properties file found in the specified path: "+filepath);
        }
        try(InputStream input= new FileInputStream(file)) {
            Properties prop = new Properties();
            prop.load(input);
            if (prop.containsKey("weather.api_key")){
                this.api_key=prop.getProperty("weather.api_key");
            }else{
                throw new IOException("The properties file is missing \"weather.api_key=<api_key>\"");
            }
            if (prop.containsKey("weather.api_secret")){
                this.api_url=prop.getProperty("weather.api_secret");
            }else{
                throw new IOException("The properties file is missing \"weather.api_secret=<api_secret>\"");
            }
            if (prop.containsKey("weather.api_url")){
                this.api_url=prop.getProperty("weather.api_url");
            }else{
                throw new IOException("The properties file is missing \"weather.api_url=<api_url>\"");
            }
            if (prop.containsKey("weather.stationId")){
                this.stationId=Integer.parseInt(prop.getProperty("weather.stationId"));
            }else{
                throw new IOException("The properties file is missing \"weather.stationId=<stationId>\"");
            }
        }
    }

}
