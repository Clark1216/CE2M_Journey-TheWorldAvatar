package plantOwl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.InputStreamReader;
import java.io.Reader;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;


import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;


//import com.ibm.icu.text.Transliterator; //ICU4J library import

//import com.hp.hpl.jena.ontology.Individual;
//import com.hp.hpl.jena.ontology.ObjectProperty;
//import com.hp.hpl.jena.ontology.OntClass;
//import com.hp.hpl.jena.ontology.OntModel;
//import com.hp.hpl.jena.rdf.model.ModelFactory;
//import com.hp.hpl.jena.rdf.model.Resource;


//All need to be changed :
// baseURL2, topnodeinstance,filepath2, folder, irilist

public class TopNodeCreator {
	
	//public static String baseURL2 = "D:\\KBDev-git/irp3-JPS-KBDev-git/Server Ontology Configuration Root/kb/powerplants/"; //the targeted IRI without the file name
	public static String baseURL2 = "D:\\KBDev-git/irp3-JPS-KBDev-git/Server Ontology Configuration Root/kb/sgp/jurongisland/nuclearpowerplants/";
	
	public static String baseURL1 = "D:\\KBDev-git/irp3-JPS-KBDev-git/Server Ontology Configuration Root/kb/temporary/"; //the template for the empty top node
	


	
	private OntClass compositeclass = null;
	private ObjectProperty hassubsystem = null;
	ArrayList <String> irilist = new ArrayList <String>();
	private Individual topnodeinstance;
	
	
	public void savefile(OntModel jenaOwlModel, String filePath2) throws URISyntaxException, FileNotFoundException {

		FileOutputStream out = new FileOutputStream(filePath2);
		
		Collection errors = new ArrayList();
		jenaOwlModel.write(out, "RDF/XML-ABBREV");

		
		System.out.println("File saved with " + errors.size() + " errors.");
	}
	
	public void initOWLClasses(OntModel jenaOwlModel) {
		compositeclass = jenaOwlModel.getOntClass("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#CompositeSystem");
		hassubsystem = jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#hasSubsystem");
		  //topnodeinstance = compositeclass.createIndividual("http://www.theworldavatar.com/kb/powerplants/WorldPowerPlants.owl#WorldPowerPlants");
		topnodeinstance = compositeclass.createIndividual("http://www.jparksimulator.com/kb/sgp/jurongisland/nuclearpowerplants/NuclearPowerPlants.owl#JurongIslandNuclearPlants");
	}
	
	public void doConversion(OntModel jenaOwlModel, String plantname){
		
		

		Resource plantiri = jenaOwlModel.createResource(plantname);		
		topnodeinstance.addProperty(hassubsystem, plantiri);
		
	}
	
	
	@SuppressWarnings("deprecation")
	public void startConversion() throws Exception {


	        String filePath = baseURL1 + "planttemplatekb2.owl"; // the empty owl file
	        //String filePath2 = baseURL2 +"WorldPowerPlants.owl"; // the result of written owl file
	        String filePath2 = baseURL2 +"NuclearPowerPlants.owl";
	        	            
	        	FileInputStream inFile = new FileInputStream(filePath);
    			Reader in = new InputStreamReader(inFile, "UTF-8");
    				    			
    			OntModel jenaOwlModel = ModelFactory.createOntologyModel();
    			
    			jenaOwlModel.read(in, null);

    			initOWLClasses(jenaOwlModel);
    			
    			//File folder = new File("D:/KBDev-git/irp3-JPS-KBDev-git/Server Ontology Configuration Root/kb/powerplants"); //all the content that is become the subnode of it
    			File folder = new File("D:/KBDev-git/irp3-JPS-KBDev-git/Server Ontology Configuration Root/kb/sgp/jurongisland/nuclearpowerplants");
    			
    			//please exclude the templateplant file
    			File[] listOfFiles = folder.listFiles();
    			
    			
    			
    			
    			for (int i = 0; i < listOfFiles.length; i++) {
    			  if (listOfFiles[i].isFile()) {
    			   // changing all non-ascii character
    			   String content= listOfFiles[i].getName();
    			   content = content.replaceAll("\\(","");
	                content = content.replaceAll("\\)","");
	                content = content.replaceAll("&apos;", "");
	                content = content.replaceAll("&amp;", "&");
	                content = content.replaceAll("�", "");
	                content = content.replaceAll("�", "A");
	                content = content.replaceAll("�-", "A");
	                content = content.replaceAll("�", "");
	                content = content.replaceAll("�", "");
	               // content= Normalizer.normalize(content, Normalizer.Form.NFD);
	                System.out.println("File " + content);
	              
	                
    			  
	                //irilist.add("http://www.theworldavatar.com/kb/powerplants/" + content + "#"+content.replace(".owl", ""));
	                irilist.add("http://www.jparksimulator.com/kb/sgp/jurongisland/nuclearpowerplants/" + content + "#"+content.replace(".owl", ""));
    			    
    			  } else if (listOfFiles[i].isDirectory()) {
    			    System.out.println("Directory " + listOfFiles[i].getName());
    			  }
    			}
    			
    			
	        	
//    			while ((line = br.readLine()) != null) {
//	            	String[] data = line.split(cvsSplitBy);
//	   				doConversion(jenaOwlModel, data[0]); //plant,country,owner,fuel,tech,x,y,emission,cost,anngen,capa,age	
//	            }
    			 System.out.println("Filenumber " + irilist.size());
    			 
    		
    			 
    			for (int i=0;i<irilist.size();i++)
    			{
    				doConversion(jenaOwlModel, irilist.get(i));
	    			
    				
    				
    			}

    			/** save the updated model file */
				savefile(jenaOwlModel, filePath2);	
	            }
	        
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("Starting Process");
		TopNodeCreator converter = new TopNodeCreator();
		converter.startConversion();

	}
}
