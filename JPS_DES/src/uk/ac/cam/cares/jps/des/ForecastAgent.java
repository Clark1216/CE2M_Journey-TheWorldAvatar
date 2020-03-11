package uk.ac.cam.cares.jps.des;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.base.util.MatrixConverter;
@WebServlet(urlPatterns = {"/GetForecastData" })
public class ForecastAgent extends JPSHttpServlet{
	private static final long serialVersionUID = 1L;
	private static String SolCastURL= "https://api.solcast.com.au/weather_sites/0ff4-0cb4-c270-5389/forecasts?format=json&api_key=IxJaiBo4-jICEIZSFPuRYVvJ2OqiFBqN";
	private static String AccuWeatherURL = "http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/300565?apikey=%20%09NP6DUl1mQkBlOAn7CE5j3MGPAAR9xbpg&details=true&metric=true";
	private Logger logger = LoggerFactory.getLogger(WeatherIrradiationRetriever.class);
	protected void doGetJPS(HttpServletRequest request, HttpServletResponse res) {
		JSONObject jo = AgentCaller.readJsonParameter(request);

		String baseUrl = jo.optString("baseUrl",  QueryBroker.getLocalDataPath()+"/JPS_DES");
		
		JSONObject result=new JSONObject();
		try {
			forecastNextDay();
//			result = forecastNextDay(baseUrl);
//			readandwriteToFile(baseUrl);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AgentCaller.printToResponse(jo, res);
 
		logger.info("return the result from forecast agent");		
	}
	/** Runs python script and reads to JSON file. Rewriting in Java afterwards
	 * 
	 * @param folder in which python files are stored. 
	 * @return
	 * @throws InterruptedException
	 * @throws FileNotFoundException
	 */
	private JSONObject forecastNextDayOld(String folder) throws InterruptedException, FileNotFoundException{
		new DistributedEnergySystem().copyFromPython(folder, "runpyforecast.bat");
		new DistributedEnergySystem().copyFromPython(folder,"scrapy.py");
		String startbatCommand =folder+"/runpyforecast.bat";
		System.out.println(startbatCommand);
		String resultpy= new DistributedEnergySystem().executeSingleCommand(folder,startbatCommand);
		String jsonres=new QueryBroker().readFileLocal(folder+"/WeatherForecast.json");
		//WeatherForecast.json may not be created if we run more than 10 times a day. 
		JSONObject current= new JSONObject(jsonres);
		return current;
	}
	/** read from csv and transform to List<String> to be converted to OWL file through WeatherTImeStampKB
	 * 
	 * @param folder file in which csv is stored. 
	 * @throws Exception
	 */
	private static void readandwriteToFileOld(String folder) throws Exception { //initializing the forecast sensors. 
		String csv=new QueryBroker().readFileLocal(folder+"/WeatherForecast.csv");
		List<String[]> readingFromCSV = MatrixConverter.fromCsvToArray(csv);
		//String baseURL2 = AgentLocator.getCurrentJpsAppDirectory(this) + "/workingdir/";
		WeatherTimeStampKB converter = new WeatherTimeStampKB();
		converter.startConversionForecast(readingFromCSV,"temperature", 4);
		converter.startConversionForecast(readingFromCSV,"irradiation", 6);
		converter.startConversionForecast(readingFromCSV,"windspeed", 8);
	}
	/**
	 * 
	 * @param url String of API
	 * @return
	 * @throws IOException
	 */
	public static String GETReq(String url) throws IOException{
	    URL urlForGetRequest = new URL(url);
	    HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
	    conection.setRequestMethod("GET");
	    String readLine = null;
	    int responseCode = conection.getResponseCode();
	    if (responseCode == HttpURLConnection.HTTP_OK) {
	    	 BufferedReader in = new BufferedReader(new InputStreamReader(conection.getInputStream()));
	    	 StringBuffer response = new StringBuffer();
	    	 while ((readLine = in.readLine()) != null) {
	    		 response.append(readLine);
	    	 	}
	    	 in.close();

		 return response.toString();
	    }else {
	    	throw new ConnectException("Request to "+ url + "failed; try again later. ");
	    }
	}
	
	/** Using Java to read API call from AccuWeather. Taken from 
	 * https://dzone.com/articles/how-to-implement-get-and-post-request-through-simp
	 * change this api if we ever manage to get a free 24 hourly prediction of temperature and windspeed
	 * @param args
	 * @throws Exception
	 */
	public static ArrayList<ArrayList<String>> AccuRequest() throws IOException {
		JSONArray arr = new JSONArray(GETReq(AccuWeatherURL));
	    DecimalFormat doubSF = new DecimalFormat("##.#");
	    ArrayList<ArrayList<String>> arraarray = new ArrayList<ArrayList<String>>();
        for (int i = 0; i< arr.length(); i++) {
        	JSONObject object = arr.getJSONObject(i);
        	JSONObject temp= (JSONObject) object.get("Temperature");
        	double temper = temp.getFloat("Value");
        	String tempera = doubSF.format(temper);
        	
        	JSONObject wind = (JSONObject) object.get("Wind");
        	JSONObject winds = (JSONObject) wind.get("Speed");
        	double windsp = winds.getFloat("Value");
        	String windspe = doubSF.format(windsp/3.6);//because windspeed is in km/h and not m/s
        	ArrayList<String> lstInit = new ArrayList<>();
        	lstInit.add(tempera);
        	lstInit.add(windspe);
        	arraarray.add(lstInit);
        }        
        return arraarray;
	}
	/** feed in solcast api, and since it's in 30 minute invervals, the step iteration
	 * for the for loop is two
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<ArrayList<String>> SolCastRequest() throws IOException{
		JSONObject jo =  new JSONObject(GETReq(SolCastURL));
		JSONArray arr = jo.getJSONArray("forecasts");
	    DecimalFormat doubSF = new DecimalFormat("##.#");
		ArrayList<ArrayList<String>> arraarray =  new ArrayList<ArrayList<String>>();
        for (int i = 0; i< 48; i+= 2) {
        	JSONObject object = arr.getJSONObject(i);
        	int temp= object.getInt("air_temp");
        	String tempera = doubSF.format(temp);
        	int ghi = object.getInt("ghi");
        	String irrad = doubSF.format(ghi);//because windspeed is in km/h and not m/s
        	ArrayList<String> lstInit = new ArrayList<>();
        	lstInit.add(tempera);
        	lstInit.add(irrad);
        	arraarray.add(lstInit);
        }   
        return arraarray;     
	}
	/** Increments the date. 
	 * 
	 * @param date
	 * @return
	 */
	 static public String addOneDay(LocalDate date) {
		    return date.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		  }
	 /** creates a list of times to be saved to OWL file
	  * 
	  * @return
	  * @throws Exception
	  */
	public static ArrayList<String[]> createTimer() throws Exception {
		Date date = new Date();
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);
		ArrayList<String[]> sj1 = new ArrayList<String[]>();
		LocalDate today = LocalDate.now();
		int n = LocalTime.now().getHour();
		String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		for (int i = 0; i < 24; i++){
			if (n == 24){
		        n = 0;
		        formattedDate = addOneDay(today);
		      }
		      String[] a = {String.format("%02d", Integer.valueOf(n)) + ":00:00", formattedDate};
		      sj1.add(a);
		      n++; 
		      

		    }
		return sj1;
	}
	/** Calls API and writes to OWL file the temperature, irradiation and windspeed. 
	 * 
	 * @throws Exception
	 */
	public void forecastNextDay() throws Exception {
		ArrayList<ArrayList<String>> accuArray = AccuRequest();
		ArrayList<ArrayList<String>> solArray = SolCastRequest(); 
		ArrayList<String[]> readingFromCSV = new ArrayList<String[]>(); 
		//mash the two together
		ArrayList<String[]> datetime = createTimer();
		for (int i = 0; i<accuArray.size(); i++) {
			ArrayList<String> ji =  solArray.get(i);
			ji.set(0, accuArray.get(i).get(0));//set temperature
			ji.add(accuArray.get(i).get(1));//set windspeed
			ji.add(datetime.get(i)[0]);
			ji.add(datetime.get(i)[1]);
			String[] stringArray = ji.toArray(new String[0]);
			readingFromCSV.add(stringArray);
		}
		for (int i = 12; i < solArray.size(); i++) { //no windspeed, place in zero.
			ArrayList<String> ji =  solArray.get(i); 
			ji.add("0.0");
			ji.add(datetime.get(i)[0]);
			ji.add(datetime.get(i)[1]);
			String[] stringArray = ji.toArray(new String[0]);
			readingFromCSV.add(stringArray);
		}
		WeatherTimeStampKB converter = new WeatherTimeStampKB();
		converter.startConversionForecast(readingFromCSV,"temperature", 0);
		converter.startConversionForecast(readingFromCSV,"irradiation", 1);
		converter.startConversionForecast(readingFromCSV,"windspeed", 2);
	}
	
	
	
}
