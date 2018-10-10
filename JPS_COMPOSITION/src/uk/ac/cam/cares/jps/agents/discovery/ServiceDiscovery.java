package uk.ac.cam.cares.jps.agents.discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.cam.cares.jps.agents.ontology.ServiceReader;
import uk.ac.cam.cares.jps.composition.servicemodel.MessagePart;
import uk.ac.cam.cares.jps.composition.servicemodel.Service;

public class ServiceDiscovery {

	public ArrayList<Service> services;
	public Map<String,Service> httpToServiceMap;
	
	public ServiceDiscovery() throws Exception {
		
		this.services = new ArrayList<Service>();
		this.httpToServiceMap = new HashMap<String,Service>();
		this.loadServices();
		this.generateHttpToServiceMap();
	}	
	
	public ArrayList<Service> getAllServiceCandidates(List<MessagePart> inputs, ArrayList<Service> servicePool){
		
		  
		ArrayList<Service> result = new ArrayList<Service>();
		ArrayList<URI> inputTypesList = new ArrayList<URI>();
		for (MessagePart messagePart_inputs : inputs) {
			inputTypesList.add(messagePart_inputs.getType());
		}

		for (Service currentService : this.services) {
			boolean flag = true;
			for (MessagePart messagePart : currentService.getAllInputs()) {
				URI type = messagePart.getType();
				if (!inputTypesList.contains(type)) {
					flag = false;
				}
			}
			if (flag && !(servicePool.contains(currentService))) {
				result.add(currentService);
			}
		}
		return result;
	}
	
	public void loadServices() throws Exception {
		 this.services = readTheServicePool();
 		 
	}
	
	public void generateHttpToServiceMap() {
		for(Service s : this.services) {this.httpToServiceMap.put(s.getOperations().get(0).getHttpUrl(),s);}
	}
	
	public Service getServiceFromHttpUrl(String url) {
		return this.httpToServiceMap.get(url);
	}
	
	public static ArrayList<Service> readTheServicePool() throws Exception {

		ServiceReader reader = new ServiceReader();
 		String directory = "C:\\Users\\nasac\\Documents\\TMP\\newAgentsMSM";
 		ArrayList<Service> servicesLoaded = new ArrayList<Service>();
 		File[] files = new File(directory).listFiles();
 		
 		for(File file : files) {
 			
 			if(file.getName().endsWith("owl")) {
 	 	 		String wholeContent = "";
 	 			try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsoluteFile()))) {
 	 				String sCurrentLine;
 	 				while ((sCurrentLine = br.readLine()) != null) {
 	 					wholeContent = wholeContent + sCurrentLine;
 	 				}
 	 			} catch (IOException e) {
 	 				e.printStackTrace();
 	 			}
 	 			List<Service> services = reader.parse(wholeContent, "http://www.theworldavatar.com");
 	 			servicesLoaded.addAll(services);
 			}
 		}
		return servicesLoaded;
 		 
	}
	
}