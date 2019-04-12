package uk.ac.cam.cares.jps.thermo.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManager {

	public static void main(String[] args) {

	}

	/**
	 * 
	 * @author NK510
	 * @param inputStream
	 * @return Properties (key, value) given in .properties file of JPS_THERMO project
	 * 
	 */
	public static Properties loadProperties(InputStream inputStream) {

		Properties properties = new Properties();

		try {

			
			if (inputStream == null) {

				return properties;
			}

			// load a properties file from class path.
			properties.load(inputStream);

		} catch (IOException ex) {
			
			ex.printStackTrace();
		
		} finally {
		
			if (inputStream != null) {
				try {
					
					inputStream.close();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
			}
		}

		return properties;

	}

}