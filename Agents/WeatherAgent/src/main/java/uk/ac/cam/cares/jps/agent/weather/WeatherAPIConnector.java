package uk.ac.cam.cares.jps.agent.weather;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

class WeatherAPIConnector {
	 static final Logger LOGGER = LogManager.getLogger(WeatherAPIConnector.class);
	/**
	 * obtain current weather data given the set of coordinates (EPSG:4326)
	 * @param lat
	 * @param lon
	 * @return
	 */
	static Map<String,Double> getWeatherDataFromOpenWeather(double lat, double lon) {
		URIBuilder builder = new URIBuilder().setScheme("http").setHost("api.openweathermap.org")
                .setPath("/data/2.5/weather");
		builder.setParameter("lat", String.valueOf(lat));
		builder.setParameter("lon", String.valueOf(lon));
		builder.setParameter("units", "metric");
		builder.setParameter("appid", Config.apikey);
		
		try {
            HttpGet request = new HttpGet(builder.build());
            CloseableHttpClient httpclient = HttpClients.createDefault();
            CloseableHttpResponse response = httpclient.execute(request);
            JSONObject apiresult = new JSONObject(EntityUtils.toString(response.getEntity()));
            
            // units are set according to mcwind file for Episode
            double precipitation = 0.0; // in mm/h
			if (apiresult.has("rain")) {
				JSONObject rain = apiresult.getJSONObject("rain");
				try {
					precipitation = rain.getDouble("1h");
				} catch (Exception e) {
					precipitation = rain.getDouble("3h")/3.0;
				}
			}

			// collect results into a Map			
			Map<String,Double> resultMap = new HashMap<>();
			resultMap.put(WeatherQueryClient.Rainfall, precipitation);
			resultMap.put(WeatherQueryClient.AtmosphericPressure, apiresult.getJSONObject("main").getDouble("pressure"));
			resultMap.put(WeatherQueryClient.CloudCover,apiresult.getJSONObject("clouds").getDouble("all")/100);
			resultMap.put(WeatherQueryClient.WindSpeed, apiresult.getJSONObject("wind").getDouble("speed"));
			if (apiresult.getJSONObject("wind").has("deg")) {
				resultMap.put(WeatherQueryClient.WindDirection, apiresult.getJSONObject("wind").getDouble("deg"));
			}
			resultMap.put(WeatherQueryClient.RelativeHumidity, apiresult.getJSONObject("main").getDouble("humidity"));
			resultMap.put(WeatherQueryClient.AirTemperature, apiresult.getJSONObject("main").getDouble("temp"));
			
            return resultMap;
        } catch (Exception e) {
        	LOGGER.error(e.getMessage());
            throw new JPSRuntimeException(e.getMessage(), e);
        }
	}
}
