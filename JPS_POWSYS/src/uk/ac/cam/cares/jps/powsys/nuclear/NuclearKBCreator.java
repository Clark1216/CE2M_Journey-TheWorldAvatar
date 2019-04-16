package uk.ac.cam.cares.jps.powsys.nuclear;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

public class NuclearKBCreator {
//	public static String baseURL2 = "D:\\KBDev-git/irp3-JPS-KBDev-git/Server Ontology Configuration Root/kb/powerplants/";
	public static String baseURL = "D:\\JPS/JParkSimulator-git/JPS_POWSYS/testres/";
	public static String baseURL2 = "C:\\JPS_DATA/workingdir/JPS_POWSYS/";
	String plantname=null;
	
	static Individual nuclear;

	static Individual m;
	static Individual degree;
	static Individual MW;
	static Individual length;
	static Individual xaxis;
	static Individual yaxis;
		
	private OntClass nuclearpowerplantclass = null;
	private OntClass organizationclass = null;
	private OntClass coordinateclass = null;
	private OntClass coordinatesystemclass = null;
	private OntClass valueclass = null;
	private OntClass scalarvalueclass = null;

	private OntClass designcapacityclass = null;
	private OntClass generatedactivepowerclass=null;
	private OntClass nucleargeneratorclass = null;


	private ObjectProperty hasdimension = null;
	private ObjectProperty referto = null;
	private ObjectProperty hascoordinatesystem = null;
	private ObjectProperty hasx = null;
	private ObjectProperty hasy = null;
	private ObjectProperty hasvalue = null;
	private ObjectProperty hasunit = null;
	private ObjectProperty hasaddress = null;
	private ObjectProperty isownedby = null;
	private ObjectProperty designcapacity = null;
	private ObjectProperty hasyearofbuilt = null;
	private ObjectProperty realizes = null;
	
	private ObjectProperty consumesprimaryfuel = null;
	private ObjectProperty hasemission = null;
	private ObjectProperty hascosts = null;
	private ObjectProperty hasannualgeneration = null;
	private ObjectProperty usesgenerationtechnology = null;
	
	private ObjectProperty hasSubsystem = null;
	private ObjectProperty hasActivepowergenerated=null;

	private DatatypeProperty numval = null;
	private DatatypeProperty hasname = null;
	private BufferedReader br;
	
	public void initOWLClasses(OntModel jenaOwlModel) {
		nuclearpowerplantclass = jenaOwlModel.getOntClass("http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#NuclearPlant");
		nucleargeneratorclass = jenaOwlModel.getOntClass("http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#NuclearGenerator");
		coordinateclass = jenaOwlModel.getOntClass("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time.owl#StraightCoordinate");
		coordinatesystemclass = jenaOwlModel.getOntClass("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#ProjectedCoordinateSystem");
		valueclass = jenaOwlModel.getOntClass("http://www.theworldavatar.com/ontology/ontocape/upper_level/coordinate_system.owl#CoordinateValue");
		scalarvalueclass = jenaOwlModel.getOntClass("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#ScalarValue");
		designcapacityclass = jenaOwlModel.getOntClass("http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_realization.owl#DesignCapacity");
		generatedactivepowerclass=jenaOwlModel.getOntClass("http://www.theworldavatar.com/ontology/ontopowsys/PowSysBehavior.owl#GeneratedActivePower");
		
		consumesprimaryfuel = jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#consumesPrimaryFuel");
		hasdimension = jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#hasDimension");
		referto = jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontocape/upper_level/coordinate_system.owl#refersToAxis");
		hascoordinatesystem = jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#hasGISCoordinateSystem");
		hasx = jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#hasProjectedCoordinate_x");
		hasy = jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#hasProjectedCoordinate_y");
		hasvalue = jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#hasValue");
		hasunit = jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#hasUnitOfMeasure");
		designcapacity = jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_realization.owl#designCapacity");
		hasActivepowergenerated=jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontopowsys/PowSysBehavior.owl#hasActivePowerGenerated");
		realizes=jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#realizes");
		hasSubsystem=jenaOwlModel.getObjectProperty("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#hasSubsystem");
		
		numval = jenaOwlModel.getDatatypeProperty("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#numericalValue");
		nuclear = jenaOwlModel.getIndividual("http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#Nuclear");
		MW=jenaOwlModel.getIndividual("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/SI_unit/derived_SI_units.owl#MW");
		degree=jenaOwlModel.getIndividual("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/SI_unit/derived_SI_units.owl#degree");
		length=jenaOwlModel.getIndividual("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/physical_dimension/physical_dimension.owl#length");
		xaxis=jenaOwlModel.getIndividual("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time.owl#x-axis");
		yaxis=jenaOwlModel.getIndividual("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time.owl#y-axis");
	}
	
	public ArrayList<String> startConversion(String csvfileoutput) throws URISyntaxException, NumberFormatException, IOException {
		String line = "";
	        String cvsSplitBy = ",";
	        int linereader=0;
	        
	        ArrayList<String>iriofplant= new ArrayList<String>();
	        String iriprefix="http://www.theworldavatar.com/kb/sgp/jurongisland/nuclearpowerplants/";
	    	ArrayList<NuclearGenType> generatortype=extractInformationForGen(csvfileoutput + "\\parameters_req.csv", "0","3");
	    	
//	    	IriMapper map2=new IriMapper();
//	    	List<IriMapping> original=map2.deserialize(csvfileoutput);
	    	
	    	
	        //reading from output file and put that to owl file
		try (BufferedReader br = new BufferedReader(new FileReader(csvfileoutput + "\\results.csv"))) {

			while ((line = br.readLine()) != null) {
				if(linereader==0) {
	        		System.out.println("skipped because it's header in reading csv output");
	        	}
				else {
					String[] data = line.split(cvsSplitBy);

					String filePath = baseURL + "plantgeneratortemplate.owl"; // the empty owl file
	
	
	
					//String filePath2 = baseURL2 + "NucPP_"+UUID.randomUUID() + ".owl"; // the result of written owl file
	
					 //System.out.println("filepath created= "+filePath2);
					FileInputStream inFile = new FileInputStream(filePath);
					Reader in = new InputStreamReader(inFile, "UTF-8");
	
					OntModel jenaOwlModel = ModelFactory.createOntologyModel();
					jenaOwlModel.read(in, null);
	
					initOWLClasses(jenaOwlModel);
					
					
					//assume 1 line is 1 nuclear power plant and 1 nuclear powerplant is a plant with uniform type of reactor in 1 area					
					if(data[1].contentEquals("t1")) {
						doConversion(jenaOwlModel,iriprefix, "NucPP_"+UUID.randomUUID(), Integer.valueOf(data[2]),0, data[5],data[4],generatortype); // plant,iriprefix,nreactora,nreactorb,x,y
					}
					else if(data[1].contentEquals("t2")) {
						doConversion(jenaOwlModel,iriprefix, "NucPP_"+UUID.randomUUID(), 0,Integer.valueOf(data[2]), data[5],data[4],generatortype); // plant,iriprefix,nreactora,nreactorb,x,y
					}					
					iriofplant.add(iriprefix+"NucPP_"+UUID.randomUUID()+".owl#"+"NucPP_"+UUID.randomUUID());
				
				}
				linereader++;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return iriofplant;
	}
	
	
	/*public ArrayList<LandlotObjectType> extractInformationForLots(String csvfileinputlandlot, String indexinput1,String indexinput2,String indexinput3) throws NumberFormatException, IOException {
		String line = "";
        String cvsSplitBy = ",";
        int linereader=0;
        ArrayList<LandlotObjectType> lotlisted= new ArrayList<LandlotObjectType>();
        br = new BufferedReader(new FileReader(csvfileinputlandlot));
			while ((line = br.readLine()) != null) {
				if(linereader==0) {
	        		System.out.println("skipped because it's header");
	        	}
				else {
				String[] data = line.split(cvsSplitBy);
				LandlotObjectType lots= new LandlotObjectType(data[Integer.valueOf(indexinput1)]); //should be 0
				lots.setx(Double.valueOf(data[Integer.valueOf(indexinput2)])); //should be 2
				lots.sety(Double.valueOf(data[Integer.valueOf(indexinput3)]));//should be 1
				lotlisted.add(lots);
				}
				linereader++;
				
			}
			System.out.println("lots info are captured");
			return lotlisted;
	}*/
	
	public ArrayList<NuclearGenType> extractInformationForGen(String csvfileinputparam, String indexinput1,String indexinput2) throws NumberFormatException, IOException {
		/**some documentation:
		*Co= capital cost of single unit reactor (million $)
		*UAo= minimum area of single unit reactor (m2)
		*Fo= capacity of single unit reactor (MW)
		*Q= cooling water needed of single unit reactor (m3/h)
		*---------------------------------------------------------------
		*L= project life span (yr)
		*Ds= discount rate
		*alpha= discount to loss factor (/mMW/yr)
		*ro= neighborhood radius for 1MW plant (m)
		*FP= probability of reactor failure
		*Hu= value of human life ($)
		*/
		String line = "";
        String cvsSplitBy = ",";
        int linereader=0;
        ArrayList<NuclearGenType> nucleargeneratorlisted= new ArrayList<NuclearGenType>();
        br = new BufferedReader(new FileReader(csvfileinputparam));
			while ((line = br.readLine()) != null) {
				if(linereader==0) {
	        		System.out.println("skipped because it's header");
	        	}
				else {
					String[] data = line.split(cvsSplitBy);
					NuclearGenType nuclear= new NuclearGenType(data[Integer.valueOf(indexinput1)]);//should be 0
					nuclear.setcapacity(Double.valueOf(data[Integer.valueOf(indexinput2)]));//should be 3
					nucleargeneratorlisted.add(nuclear);
				}
				linereader++;
				
			}
			System.out.println("generators info are captured");
			return nucleargeneratorlisted;
	}


	
	
	public void doConversionreactor(String iriprefix,String generatorname,String xnumval,String ynumval,double capacity) throws FileNotFoundException, UnsupportedEncodingException, URISyntaxException {
		String filePath = baseURL + "plantgeneratortemplate.owl"; // the empty owl file
		FileInputStream inFile = new FileInputStream(filePath);
		Reader in = new InputStreamReader(inFile, "UTF-8");

		OntModel jenaOwlModel2 = ModelFactory.createOntologyModel();
		jenaOwlModel2.read(in, null);

		initOWLClasses(jenaOwlModel2);
		
		
		String filePathname = baseURL2 + generatorname + ".owl"; // the result of written owl file
		Individual capagen = generatedactivepowerclass.createIndividual(iriprefix + generatorname + ".owl#GeneratedActivePower_"+generatorname);
		Individual capagenvalue = scalarvalueclass.createIndividual(iriprefix + generatorname + ".owl#V_GeneratedActivePower_"+generatorname);
		
		Individual generator=nucleargeneratorclass.createIndividual(iriprefix + generatorname + ".owl#"+generatorname);
		Individual gencoordinate = coordinatesystemclass.createIndividual(iriprefix + generatorname + ".owl#CoordinateSystem_of_"+generatorname);
		Individual xgencoordinate = coordinateclass.createIndividual(iriprefix + generatorname + ".owl#x_coordinate_of_"+generatorname);
		Individual ygencoordinate = coordinateclass.createIndividual(iriprefix + generatorname+ ".owl#y_coordinate_of_"+generatorname);
		Individual xgencoordinatevalue = valueclass.createIndividual(iriprefix + generatorname + ".owl#v_x_coordinate_of_"+generatorname);
		Individual ygencoordinatevalue = valueclass.createIndividual(iriprefix + generatorname + ".owl#v_y_coordinate_of_"+generatorname);
		
		generator.addProperty(hascoordinatesystem, gencoordinate);
		
		gencoordinate.addProperty(hasx, xgencoordinate);
		xgencoordinate.addProperty(hasvalue, xgencoordinatevalue);
		xgencoordinate.addProperty(referto, xaxis);
		xgencoordinate.addProperty(hasdimension, length);
		xgencoordinatevalue.addProperty(numval, jenaOwlModel2.createTypedLiteral(xnumval));
		xgencoordinatevalue.addProperty(hasunit, degree);
		
		gencoordinate.addProperty(hasy, ygencoordinate);
		ygencoordinate.addProperty(hasvalue, ygencoordinatevalue);
		ygencoordinate.addProperty(referto, yaxis);
		ygencoordinate.addProperty(hasdimension, length);
		ygencoordinatevalue.addProperty(numval, jenaOwlModel2.createTypedLiteral(ynumval));
		ygencoordinatevalue.addProperty(hasunit, degree);
		
		generator.addProperty(hasActivepowergenerated, capagen);
		capagen.addProperty(hasvalue, capagenvalue);
		capagenvalue.setPropertyValue(numval, jenaOwlModel2.createTypedLiteral(new Double(capacity)));
		capagenvalue.addProperty(hasunit, MW);

		/** save the updated model file*/ 
		LandlotsKB ins2 = new LandlotsKB();
		ins2.savefile(jenaOwlModel2, filePathname);
		
	}
	
	public void doConversion(OntModel jenaOwlModel,String iriprefix, String plantname ,int numberofreactorA,int numberofreactorB,String xnumval,String ynumval,ArrayList<NuclearGenType> generatortype) throws NumberFormatException, IOException, URISyntaxException{
		
		
		double capacityA=0.0;
		double capacityB=0.0;
		int difftype=generatortype.size();
		for(int b=0;b<difftype;b++) {
			if(numberofreactorA>0||numberofreactorB>0){
				if(generatortype.get(b).getnucleargen().contentEquals("t1")) {
					capacityA=generatortype.get(b).getcapacity();	
				}
				else if(generatortype.get(b).getnucleargen().contentEquals("t2")){
					capacityB=generatortype.get(b).getcapacity();
				}
			}

		}
		
		double totalcapacityA=numberofreactorA*capacityA;
		double totalcapacityB=numberofreactorB*capacityB;
		double totalcapacity=totalcapacityA+totalcapacityB;
		
		
		
		String filePath2 = baseURL2 + plantname + ".owl"; // the result of written owl file
		
		Individual plant = nuclearpowerplantclass.createIndividual(iriprefix + plantname + ".owl#"+plantname);
		
		Individual plantcoordinate = coordinatesystemclass.createIndividual(iriprefix + plantname + ".owl#CoordinateSystem_of_"+plantname);
		Individual xcoordinate = coordinateclass.createIndividual(iriprefix + plantname + ".owl#x_coordinate_of_"+plantname);
		Individual ycoordinate = coordinateclass.createIndividual(iriprefix + plantname + ".owl#y_coordinate_of_"+plantname);
		Individual xcoordinatevalue = valueclass.createIndividual(iriprefix + plantname + ".owl#v_x_coordinate_of_"+plantname);
		Individual ycoordinatevalue = valueclass.createIndividual(iriprefix + plantname + ".owl#v_y_coordinate_of_"+plantname);
		
		Individual capa = designcapacityclass.createIndividual(iriprefix + plantname + ".owl#capa_of_"+plantname);
		Individual capavalue = scalarvalueclass.createIndividual(iriprefix + plantname + ".owl#v_capa_of_"+plantname);
		
		Individual powergeneration = jenaOwlModel.getIndividual("http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#NuclearGeneration");
		
		plant.addProperty(hascoordinatesystem, plantcoordinate);
		
		plantcoordinate.addProperty(hasx, xcoordinate);
		xcoordinate.addProperty(hasvalue, xcoordinatevalue);
		xcoordinate.addProperty(referto, xaxis);
		xcoordinate.addProperty(hasdimension, length);
		xcoordinatevalue.addProperty(numval, jenaOwlModel.createTypedLiteral(xnumval));
		xcoordinatevalue.addProperty(hasunit, degree);
		
		plantcoordinate.addProperty(hasy, ycoordinate);
		ycoordinate.addProperty(hasvalue, ycoordinatevalue);
		ycoordinate.addProperty(referto, yaxis);
		ycoordinate.addProperty(hasdimension, length);
		ycoordinatevalue.addProperty(numval, jenaOwlModel.createTypedLiteral(ynumval));
		ycoordinatevalue.addProperty(hasunit, degree);
		
		plant.addProperty(designcapacity, capa);
		capa.addProperty(hasvalue, capavalue);
		capavalue.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(new Double (totalcapacity)));
		capavalue.addProperty(hasunit, MW);
		
		plant.addProperty(realizes, powergeneration);
		powergeneration.setPropertyValue(consumesprimaryfuel, nuclear);
		
		//numberofreactor=5; //temporary
		

		
		for(int f=0; f<numberofreactorA;f++){
			String generatorname="NucGenerator_"+UUID.randomUUID();
			
			Individual generator=nucleargeneratorclass.createIndividual(iriprefix + generatorname + ".owl#"+generatorname);
			plant.addProperty(hasSubsystem, generator);
			
			doConversionreactor(iriprefix, generatorname, xnumval, ynumval, capacityA);

		}
		
		for(int f=0; f<numberofreactorB;f++){
			String generatorname="NucGenerator_"+UUID.randomUUID();
			Individual generator=nucleargeneratorclass.createIndividual(iriprefix + generatorname + ".owl#"+generatorname);
			plant.addProperty(hasSubsystem, generator);

			doConversionreactor(iriprefix, generatorname, xnumval, ynumval, capacityB);
			

		}

		/** save the updated model file */
		LandlotsKB ins2 = new LandlotsKB();
		ins2.savefile(jenaOwlModel, filePath2);
		
		
	}

}
