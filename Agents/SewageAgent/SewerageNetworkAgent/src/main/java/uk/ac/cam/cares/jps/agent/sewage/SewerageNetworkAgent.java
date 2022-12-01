package uk.ac.cam.cares.jps.agent.sewage;

import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.AccessAgentCaller;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.graph.NodeFactory;
import javax.servlet.annotation.WebServlet;
import org.springframework.stereotype.Controller;
import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Controller
@WebServlet(urlPatterns = {"/performsewageupdate"})


public class SewerageNetworkAgent extends JPSAgent {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LogManager.getLogger(SewerageNetworkAgent.class);
	private static final String DATAINSTANTIATION = "Could not update data";

	// Common Base URLs
	public static String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	public static String XSD = "http://www.w3.org/2001/XMLSchema#";
	public static String OM = "http://www.ontology-of-units-of-measure.org/resource/om-2/";
	public static String OS = "https://www.theworldavatar.com/kg/ontosewage/";
	public static String KB = "https://www.theworldavatar.com/kb/ontosewage/";

	// IRIs for OntoSewage and others
	public static String BMO = "https://w3id.org/digitalconstruction/0.3/BuildingMaterials#";
	public static String s4watr = "https://saref.etsi.org/saref4watr/";
	public static String schema = "https://schema.org/";
	public static String dul = "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl";
	public static String ogc = "http://www.opengis.net/ont/geosparql#";
	public static String sio = "http://semanticscience.org/resource/#";
	public static String juso = "http://rdfs.co/juso/#";

	// For units of measure
	public static String OM_QUANTITY = OM + "Quantity";
	public static String OM_MEASURE = OM + "Measure";
	public static String OM_UNIT = OM + "Unit";
	public static String OM_HAS_VALUE = OM + "hasValue";
	public static String OM_HAS_UNIT = OM + "hasUnit";
	public static String OM_SYMBOL = OM + "symbol";
	public static String OM_Has_NUMERICAL_VALUE = OM + "hasNumericalValue";

	public static String OM_LENGTH = OM + "Length";
	public static String OM_THICKNESS = OM + "Thickness";
	public static String OM_HEIGHT = OM + "Height";
	public static String OM_WIDTH = OM + "Width";
	public static String OM_DISTANCE = OM + "Distance";
	public static String OM_DEPTH = OM + "Depth";

	// Data types
	public static String RDF_TYPE = RDF + "type";
	public static String RDFS_COMMENT = RDFS + "comment";
	public static String RDFS_LABEL = RDFS + "label";
	public static String XSD_STRING = XSD + "string";
	public static String XSD_FLOAT = XSD + "float";
	public static String XSD_DATE = XSD + "date";
	public static String XSD_BOOLEAN = XSD + "Boolean";
	public static String XSD_INTEGER = XSD + "integer";


	@Override
	public JSONObject processRequestParameters(JSONObject requestParams) {	

		JSONObject jsonMessage = new JSONObject();
		try {
			dataInstantiation();
			jsonMessage.accumulate("Result", "Data has been instantiated.");
		} catch (JPSRuntimeException e) {
			LOGGER.error(DATAINSTANTIATION, e);
			throw new JPSRuntimeException(DATAINSTANTIATION, e);
		}

		return jsonMessage;
	}

	public static void dataInstantiation() {
		
		String HG_Path = System.getenv("HGDATA");
		String KG_Path = System.getenv("KGDATA");

		UpdateBuilder SewerageNetwork_ub = 
				new UpdateBuilder()
				.addInsert(NodeFactory.createURI(KB + "MainNetwork"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "SewerageNetwork"))
				.addInsert(NodeFactory.createURI(KB + "SubNetwork"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "SewerageNetwork"));
		UpdateRequest SewerageNetwork_ur = SewerageNetwork_ub.buildRequest();
		AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", SewerageNetwork_ur.toString());

		
		int HG_column_length = 0;
		try {
			HG_column_length = ColNum(HG_Path, ",");
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}


		for (int i = 1; i < 10; i++) { //HG_column_length; i++) {
			String[] HG_Instance = ReadCol(i, HG_Path, ","); //System.out.println(i);

			// Instantiation HG data	
			String HG_Instance_Name = HG_Instance[0];
			String HG001 = HG_Instance[1];
			String HG003 = HG_Instance[2];
			String HG004 = HG_Instance[3];
			String HG005 = HG_Instance[4];
			String HG006 = HG_Instance[5];
			String HG007 = HG_Instance[6];
			String HG008 = HG_Instance[7];
			String HG009 = HG_Instance[8];
			String HG010 = HG_Instance[9];
			String HG011 = HG_Instance[10];
			String HG101 = HG_Instance[11];
			String HG102 = HG_Instance[12];
			String HG103 = HG_Instance[13];
			String HG104 = HG_Instance[14];
			String HG107 = HG_Instance[15];
			String HG108 = HG_Instance[16];
			String HG301 = HG_Instance[17];
			String HG302 = HG_Instance[18];
			String HG303 = HG_Instance[19];
			String HG304 = HG_Instance[20];
			String HG305 = HG_Instance[21];
			String HG306 = HG_Instance[22];
			String HG307 = HG_Instance[23];
			String HG310 = HG_Instance[24];
			String HG311 = HG_Instance[25];
			String HG313 = HG_Instance[26];
			String HG401 = HG_Instance[27];
			String HG402 = HG_Instance[28];
			String HG403 = HG_Instance[29];
			String HG404 = HG_Instance[30];
			String HG406 = HG_Instance[31];
			String HG410 = HG_Instance[32];
			String HG500 = HG_Instance[33];
			String HG_GP001_SchachtMP = HG_Instance[34];
			String HG_GP001_HaltungsMP = HG_Instance[35];
			String HG_GP002 = HG_Instance[36]; 
			String HG_GP003_SchachtMP = HG_Instance[37];
			String HG_GP003_HaltungsMP = HG_Instance[38];
			String HG_GP004_SchachtMP = HG_Instance[39];
			String HG_GP004_HaltungsMP = HG_Instance[40]; 
			String HG_GP007_SchachtMP = HG_Instance[41];
			String HG_GP007_HaltungsMP = HG_Instance[42];
			String HG_GP008 = HG_Instance[43]; //String HG_GP008 = HG_Instance[43];
			String HG_GP009 = HG_Instance[45]; //String HG_GP009 = HG_Instance[44];
			String HG_GP010 = HG_Instance[47]; //String HG_GP010 = HG_Instance[45];


			UpdateBuilder SewerageComponentHG_ub = 
					new UpdateBuilder()
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(s4watr + "Pipe"))
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(RDFS_LABEL), HG001)		
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(OS + "hasUsage"), NodeFactory.createURI(KB + "SewerageUsage" + HG302))
					.addInsert(NodeFactory.createURI(KB + "SewerageUsage" + HG302), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "SewerageUsage"))		
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(OS + "hasSewerageRecords"), NodeFactory.createURI(KB + "SewerageRecords" + HG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "SewerageRecords" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "SewerageRecords"))
					.addInsert(NodeFactory.createURI(KB + "SewerageRecords" + HG_Instance_Name), NodeFactory.createURI(OS + "hasSewagePlantID"), HG108)
					.addInsert(NodeFactory.createURI(KB + "SewerageRecords" + HG_Instance_Name), NodeFactory.createURI(OS + "hasOwnershipType"), NodeFactory.createURI(KB + "OwnershipType" + HG402))
					.addInsert(NodeFactory.createURI(KB + "SewerageRecords" + HG_Instance_Name), NodeFactory.createURI(OS + "hasFunctionalState"), NodeFactory.createURI(KB + "FunctionalState" + HG401))
					.addInsert(NodeFactory.createURI(KB + "OwnershipType" + HG402), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "OwnershipType"))
					.addInsert(NodeFactory.createURI(KB + "FunctionalState" + HG401), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "FunctionalState"));
			UpdateRequest SewerageComponentHG_ur = SewerageComponentHG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", SewerageComponentHG_ur.toString());


			UpdateBuilder ConstructionPropertiesHG_ub = 
					new UpdateBuilder()
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(OS + "hasConstructionProperties"), NodeFactory.createURI(KB + "ConstructionProperties" + HG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "ConstructionProperties" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "ConstructionProperties"))
					.addInsert(NodeFactory.createURI(KB + "ConstructionProperties" + HG_Instance_Name), NodeFactory.createURI(OS + "constructionYear"), HG303)
					.addInsert(NodeFactory.createURI(KB + "ConstructionProperties" + HG_Instance_Name), NodeFactory.createURI(OS + "hasMaterial"), NodeFactory.createURI(KB + "Material" + HG304))
					.addInsert(NodeFactory.createURI(KB + "ConstructionProperties" + HG_Instance_Name), NodeFactory.createURI(OS + "hasChannelType"), NodeFactory.createURI(KB + "ChannelType" + HG301))
					.addInsert(NodeFactory.createURI(KB + "Material" + HG304), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(BMO + "Material"))
					.addInsert(NodeFactory.createURI(KB + "ChannelType" + HG301), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "ChannelType"));
			UpdateRequest ConstructionPropertiesHG_ur = ConstructionPropertiesHG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", ConstructionPropertiesHG_ur.toString());


			UpdateBuilder BranchConnectionHG_ub = 
					new UpdateBuilder()
					.addInsert(NodeFactory.createURI(KB + "BranchConnection" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "BranchConnection"))	
					.addInsert(NodeFactory.createURI(KB + "BranchConnection" + HG_Instance_Name), NodeFactory.createURI(OS + "isInFlowDirection"), HG008)			
					.addInsert(NodeFactory.createURI(KB + "BranchConnection" + HG_Instance_Name), NodeFactory.createURI(OS + "clockPositionOfBranchPipe"), HG009)			
					.addInsert(NodeFactory.createURI(KB + "BranchConnection" + HG_Instance_Name), NodeFactory.createURI(OS + "relativeDistanceOnMainPipe"), NodeFactory.createURI(KB + "DistanceOnMainPipe" + "BranchConnection" + HG_Instance_Name))			
					.addInsert(NodeFactory.createURI(KB + "DistanceOnMainPipe" + "BranchConnection" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_DISTANCE));
			UpdateRequest BranchConnectionHG_ur = BranchConnectionHG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", BranchConnectionHG_ur.toString());
			omHasValue("hasDistanceFromMainPipe" + "BranchConnection" + HG_Instance_Name, "meter", HG007);	


			UpdateBuilder CrossSectionHG_ub = 
					new UpdateBuilder()
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(OS + "hasCrossSection"), NodeFactory.createURI(KB + "CrossSection" + HG_Instance_Name))		
					.addInsert(NodeFactory.createURI(KB + "CrossSection" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "CrossSection"))			
					.addInsert(NodeFactory.createURI(KB + "CrossSection" + HG_Instance_Name), NodeFactory.createURI(OS + "hasShape"), NodeFactory.createURI(KB + "Shape" + HG305))
					.addInsert(NodeFactory.createURI(KB + "Shape" + HG305), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "Shape"))
					.addInsert(NodeFactory.createURI(KB + "CrossSection" + HG_Instance_Name), NodeFactory.createURI(OS + "hasLength"), NodeFactory.createURI(KB + "Length" + "Shaft" + HG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Length" + "Shaft" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_LENGTH))
					.addInsert(NodeFactory.createURI(KB + "CrossSection" + HG_Instance_Name), NodeFactory.createURI(OS + "hasWidth"), NodeFactory.createURI(KB + "Width" + "Shaft" + HG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Width" + "Shaft" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_WIDTH));		
			UpdateRequest CrossSectionHG_ur = CrossSectionHG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", CrossSectionHG_ur.toString());
			omHasValue("Length" + "Shaft" + HG_Instance_Name, "millimeter", HG307);		
			omHasValue("Width" + "Shaft" + HG_Instance_Name, "millimeter", HG306);	


			UpdateBuilder SewerageFluidHG_ub = 
					new UpdateBuilder()	
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(OS + "usedFor"), NodeFactory.createURI(KB + "SewerageFluid" + HG500))
					.addInsert(NodeFactory.createURI(KB + "SewerageFluid" + HG500), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "SewerageFluid"))
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(OS + "hasConnectionID"), HG011)
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(OS + "hasSourceID"), HG005)
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(OS + "hasPipeType"), NodeFactory.createURI(KB + "PipeType" + HG313))
					.addInsert(NodeFactory.createURI(KB + "PipeType" + HG313), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "PipeType"))
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(OS + "hasInclination"), NodeFactory.createURI(KB + "Inclination" + HG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Inclination" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "Inclination"))
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(OS + "hasLength"), NodeFactory.createURI(KB + "Length" + HG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Length" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_LENGTH))
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(OS + "hasWallThickness"), NodeFactory.createURI(KB + "WallThickness" + HG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "WallThickness" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_LENGTH));
			UpdateRequest SewerageFluidHG_ur = SewerageFluidHG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", SewerageFluidHG_ur.toString());
			omHasValue("Inclination" + HG_Instance_Name, "percentage", HG311);		
			omHasValue("Length" + HG_Instance_Name, "meter", HG310);	
			omHasValue("Thickness" + HG_Instance_Name, "meter", HG410);	


			UpdateBuilder ConnectionPropertiesHG_ub = 
					new UpdateBuilder()	
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(OS + "hasConnectionProperties"), NodeFactory.createURI(KB + "ConnectionProperties" + HG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "ConnectionProperties" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "ConnectionProperties"))
					.addInsert(NodeFactory.createURI(KB + "ConnectionProperties" + HG_Instance_Name), NodeFactory.createURI(OS + "hasUpstreamConnector"), HG003)
					.addInsert(NodeFactory.createURI(KB + "ConnectionProperties" + HG_Instance_Name), NodeFactory.createURI(OS + "hasDownstreamConnector"), HG004)
					.addInsert(NodeFactory.createURI(KB + "ConnectionProperties" + HG_Instance_Name), NodeFactory.createURI(OS + "hasEndpointObject"), HG006)
					.addInsert(NodeFactory.createURI(KB + "ConnectionProperties" + HG_Instance_Name), NodeFactory.createURI(OS + "hasEndpointType"), NodeFactory.createURI(KB + "hasEndpointType" + HG010))
					.addInsert(NodeFactory.createURI(KB + "hasEndpointType" + HG010), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "EndpointType"));
			UpdateRequest ConnectionPropertiesHG_ur = ConnectionPropertiesHG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", ConnectionPropertiesHG_ur.toString());


			UpdateBuilder LocationHG_ub = 
					new UpdateBuilder()
					.addInsert(NodeFactory.createURI(KB + HG_Instance_Name), NodeFactory.createURI(dul + "hasLocation"), NodeFactory.createURI(KB + "Location" + HG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Location" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(schema + "Location"))
					.addInsert(NodeFactory.createURI(KB + "Location" + HG_Instance_Name), NodeFactory.createURI(OS + "isInWaterProtectionZone"), HG403)
					.addInsert(NodeFactory.createURI(KB + "Location" + HG_Instance_Name), NodeFactory.createURI(OS + "isInFloodplane"), HG406)
					.addInsert(NodeFactory.createURI(KB + "Location" + HG_Instance_Name), NodeFactory.createURI(OS + "isAssociatedWith"), NodeFactory.createURI(KB + "AssociatedInfrastructure" + HG404))
					.addInsert(NodeFactory.createURI(KB + "AssociatedInfrastructure" + HG404), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "AssociatedInfrastructure"))  
					.addInsert(NodeFactory.createURI(KB + "Location" + HG_Instance_Name), NodeFactory.createURI(OS + "hasCoordinates"), NodeFactory.createURI(KB + "GeoCoordinate" + HG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "GeoCoordinate" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(schema + "GeoCoordinate"))    
					.addInsert(NodeFactory.createURI(KB + "GeoCoordinate" + HG_Instance_Name), NodeFactory.createURI(OS + "hasPositionAccuracy"), HG_GP008)
					.addInsert(NodeFactory.createURI(KB + "GeoCoordinate" + HG_Instance_Name), NodeFactory.createURI(OS + "hasCoordinateReference"), HG_GP002)
					.addInsert(NodeFactory.createURI(KB + "GeoCoordinate" + HG_Instance_Name), NodeFactory.createURI(ogc + "asWKT"), HG_GP003_SchachtMP + HG_GP004_SchachtMP)
					.addInsert(NodeFactory.createURI(KB + "GeoCoordinate" + HG_Instance_Name), NodeFactory.createURI(ogc + "asWKT"), HG_GP003_HaltungsMP + HG_GP004_HaltungsMP)  
					.addInsert(NodeFactory.createURI(KB + "Location" + HG_Instance_Name), NodeFactory.createURI(OS + "hasPointDesignation"), HG_GP001_SchachtMP)
					.addInsert(NodeFactory.createURI(KB + "Location" + HG_Instance_Name), NodeFactory.createURI(OS + "hasPointDesignation"), HG_GP001_HaltungsMP)
					.addInsert(NodeFactory.createURI(KB + "Location" + HG_Instance_Name), NodeFactory.createURI(OS + "hasElevation"), NodeFactory.createURI(KB + "Elevation" + HG_Instance_Name))	
					.addInsert(NodeFactory.createURI(KB + "Elevation" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(schema + "Elevation"))   
					.addInsert(NodeFactory.createURI(KB + "Elevation" + HG_Instance_Name), NodeFactory.createURI(OS + "hasElevationReference"), HG_GP010)
					.addInsert(NodeFactory.createURI(KB + "Elevation" + HG_Instance_Name), NodeFactory.createURI(OS + "hasElevationAccuracy"), HG_GP009)
					.addInsert(NodeFactory.createURI(KB + "Elevation" + HG_Instance_Name), NodeFactory.createURI(OS + "hasElevation"), HG_GP007_HaltungsMP)
					.addInsert(NodeFactory.createURI(KB + "Elevation" + HG_Instance_Name), NodeFactory.createURI(OS + "hasElevation"), HG_GP007_SchachtMP)
					.addInsert(NodeFactory.createURI(KB + "Location" + HG_Instance_Name), NodeFactory.createURI(OS + "hasCatchmentAreaKey"), NodeFactory.createURI(HG107))
					.addInsert(NodeFactory.createURI(KB + "Location" + HG_Instance_Name), NodeFactory.createURI(sio + "SIO_000061"), NodeFactory.createURI(KB + "Distrcit" + HG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Distrcit" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(juso + "District"))
					.addInsert(NodeFactory.createURI(KB + "Distrcit" + HG_Instance_Name), NodeFactory.createURI(OS + "hasDistrictReference"), HG103)
					.addInsert(NodeFactory.createURI(KB + "Distrcit" + HG_Instance_Name), NodeFactory.createURI(OS + "hasDistrictName"), HG104)   
					.addInsert(NodeFactory.createURI(KB + "Location" + HG_Instance_Name), NodeFactory.createURI(sio + "SIO_000061"), NodeFactory.createURI(KB + "Street" + HG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Street" + HG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(juso + "Street"))
					.addInsert(NodeFactory.createURI(KB + "Street" + HG_Instance_Name), NodeFactory.createURI(OS + "hasStreetReference"), HG101)
					.addInsert(NodeFactory.createURI(KB + "Street" + HG_Instance_Name), NodeFactory.createURI(OS + "hasStreetName"), HG102);
			UpdateRequest LocationHG_ur = LocationHG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", LocationHG_ur.toString());   
		}


		int KG_column_length = 0;
		try {
			KG_column_length = ColNum(KG_Path, ",");
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}

		for (int i = 1; i < 10; i++) { //KG_column_length; i++) {
			String[] KG_Instance = ReadCol(i, KG_Path, ","); //System.out.println(i);

			// Instantiation KG data
			String KG_Instance_Name = KG_Instance[0];	
			String KG001 = KG_Instance[1];
			String KG108 = KG_Instance[2];
			String KG211 = KG_Instance[3];
			String KG301 = KG_Instance[4];
			String KG302 = KG_Instance[5];
			String KG303 = KG_Instance[6];
			String KG304 = KG_Instance[7];
			String KG305 = KG_Instance[8];
			String KG306 = KG_Instance[9];
			String KG307 = KG_Instance[10];
			String KG308 = KG_Instance[11];
			String KG309 = KG_Instance[12];
			String KG310 = KG_Instance[13];
			String KG311 = KG_Instance[14];
			String KG312 = KG_Instance[15];
			String KG316 = KG_Instance[16];
			String KG318 = KG_Instance[17];
			String KG319 = KG_Instance[18];
			String KG401 = KG_Instance[19];
			String KG402 = KG_Instance[20];
			String KG403 = KG_Instance[21];
			String KG404 = KG_Instance[22];
			String KG406 = KG_Instance[23];
			String KG_GP001_SchachtMP = KG_Instance[24];; 
			String KG_GP001_HaltungsMP = KG_Instance[24];
			String KG_GP002 = KG_Instance[25];
			String KG_GP010 = KG_Instance[26];
			String KG_GP003_SchachtMP = KG_Instance[27];
			String KG_GP003_HaltungsMP = KG_Instance[28];
			String KG_GP004_SchachtMP = KG_Instance[29];
			String KG_GP004_HaltungsMP = KG_Instance[30];
			String KG_GP007_SchachtMP = KG_Instance[31]; 
			String KG_GP007_HaltungsMP = KG_Instance[32];
			String KG_GP008 = KG_Instance[33];
			String KG_GP009 = KG_Instance[35];


			UpdateBuilder SewerageComponentKG_ub = 
					new UpdateBuilder()
					.addInsert(NodeFactory.createURI(KB + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(s4watr + "Manhole"))
					.addInsert(NodeFactory.createURI(KB + KG_Instance_Name), NodeFactory.createURI(RDFS_LABEL), KG001)		
					.addInsert(NodeFactory.createURI(KB + KG_Instance_Name), NodeFactory.createURI(OS + "hasUsage"), NodeFactory.createURI(KB + "SewerageUsage" + KG302))
					.addInsert(NodeFactory.createURI(KB + "SewerageUsage" + KG302), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "SewerageUsage"))		
					.addInsert(NodeFactory.createURI(KB + KG_Instance_Name), NodeFactory.createURI(OS + "hasSewerageRecords"), NodeFactory.createURI(KB + "SewerageRecords" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "SewerageRecords" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "SewerageRecords"))
					.addInsert(NodeFactory.createURI(KB + "SewerageRecords" + KG_Instance_Name), NodeFactory.createURI(OS + "hasSewagePlantID"), KG108)
					.addInsert(NodeFactory.createURI(KB + "SewerageRecords" + KG_Instance_Name), NodeFactory.createURI(OS + "hasOwnershipType"), NodeFactory.createURI(KB + "OwnershipType" + KG402))
					.addInsert(NodeFactory.createURI(KB + "SewerageRecords" + KG_Instance_Name), NodeFactory.createURI(OS + "hasFunctionalState"), NodeFactory.createURI(KB + "FunctionalState" + KG401))
					.addInsert(NodeFactory.createURI(KB + "OwnershipType" + KG402), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "OwnershipType"))
					.addInsert(NodeFactory.createURI(KB + "FunctionalState" + KG401), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "FunctionalState"));
			UpdateRequest SewerageComponentKG_ur = SewerageComponentKG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", SewerageComponentKG_ur.toString());


			UpdateBuilder ConstructionPropertiesKG_ub = 
					new UpdateBuilder()
					.addInsert(NodeFactory.createURI(KB + KG_Instance_Name), NodeFactory.createURI(OS + "hasConstructionProperties"), NodeFactory.createURI(KB + "ConstructionProperties" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "ConstructionProperties" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "ConstructionProperties"))
					.addInsert(NodeFactory.createURI(KB + "ConstructionProperties" + KG_Instance_Name), NodeFactory.createURI(OS + "constructionYear"), KG303)
					.addInsert(NodeFactory.createURI(KB + "ConstructionProperties" + KG_Instance_Name), NodeFactory.createURI(OS + "hasMaterial"), NodeFactory.createURI(KB + "Material" + KG304))
					.addInsert(NodeFactory.createURI(KB + "ConstructionProperties" + KG_Instance_Name), NodeFactory.createURI(OS + "hasChannelType"), NodeFactory.createURI(KB + "ChannelType" + KG301))
					.addInsert(NodeFactory.createURI(KB + "Material" + KG304), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(BMO + "Material"))
					.addInsert(NodeFactory.createURI(KB + "ChannelType" + KG301), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "ChannelType"));
			UpdateRequest ConstructionPropertiesKG_ur = ConstructionPropertiesKG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", ConstructionPropertiesKG_ur.toString());


			UpdateBuilder FlumeKG_ub = 
					new UpdateBuilder()
					.addInsert(NodeFactory.createURI(KB + KG_Instance_Name), NodeFactory.createURI(OS + "hasNodeType"), NodeFactory.createURI(KB + "NodeType" + KG305))
					.addInsert(NodeFactory.createURI(KB + "NodeType" + KG305), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "NodeType"))	
					.addInsert(NodeFactory.createURI(KB + KG_Instance_Name), NodeFactory.createURI(OS + "hasConnectionType"), NodeFactory.createURI(KB + "ConnectionType" + KG306))
					.addInsert(NodeFactory.createURI(KB + "NodeType" + KG306), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "ConnectionType"))
					.addInsert(NodeFactory.createURI(KB + KG_Instance_Name), NodeFactory.createURI(OS + "hasFlume"), NodeFactory.createURI(KB + "Flume" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Flume" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "Flume"))
					.addInsert(NodeFactory.createURI(KB + "Flume" + KG_Instance_Name), NodeFactory.createURI(OS + "hasShape"), NodeFactory.createURI(KB + "Shape" + KG316))
					.addInsert(NodeFactory.createURI(KB + "Shape" + KG316), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "Shape"))
					.addInsert(NodeFactory.createURI(KB + "Flume" + KG_Instance_Name), NodeFactory.createURI(OS + "hasLength"), NodeFactory.createURI(KB + "Length" + "Flume" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Length" + "Flume" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_LENGTH))
					.addInsert(NodeFactory.createURI(KB + "Flume" + KG_Instance_Name), NodeFactory.createURI(OS + "hasWidth"), NodeFactory.createURI(KB + "Width" + "Flume" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Width" + "Flume" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_WIDTH));
			UpdateRequest FlumeKG_ur = FlumeKG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", FlumeKG_ur.toString());
			omHasValue("Length" + "Flume" + KG_Instance_Name, "millimeter", KG319);		
			omHasValue("Width" + "Flume" + KG_Instance_Name, "millimeter", KG318);	


			UpdateBuilder ShaftKG_ub = 
					new UpdateBuilder()
					.addInsert(NodeFactory.createURI(KB + KG_Instance_Name), NodeFactory.createURI(OS + "hasShaft"), NodeFactory.createURI(KB + "Shaft" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Shaft" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "Shaft"))	
					.addInsert(NodeFactory.createURI(KB + "Shaft" + KG_Instance_Name), NodeFactory.createURI(OS + "hasShape"), NodeFactory.createURI(KB + "Shape" + KG307))
					.addInsert(NodeFactory.createURI(KB + "Shape" + KG307), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "Shape"))
					.addInsert(NodeFactory.createURI(KB + "Shaft" + KG_Instance_Name), NodeFactory.createURI(OS + "hasLength"), NodeFactory.createURI(KB + "Length" + "Shaft" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Length" + "Shaft" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_LENGTH))
					.addInsert(NodeFactory.createURI(KB + "Shaft" + KG_Instance_Name), NodeFactory.createURI(OS + "hasWidth"), NodeFactory.createURI(KB + "Width" + "Shaft" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Width" + "Shaft" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_WIDTH))
					.addInsert(NodeFactory.createURI(KB + "Shaft" + KG_Instance_Name), NodeFactory.createURI(OS + "hasDepth"), NodeFactory.createURI(KB + "Depth" + "Shaft" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Depth" + "Shaft" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_LENGTH));
			UpdateRequest ShaftKG_ur = ShaftKG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", ShaftKG_ur.toString());
			omHasValue("Length" + "Shaft" + KG_Instance_Name, "millimeter", KG308);		
			omHasValue("Width" + "Shaft" + KG_Instance_Name, "millimeter", KG309);	
			omHasValue("Depth" + "Shaft" + KG_Instance_Name, "meter", KG211);	


			UpdateBuilder CoverKG_ub = 
					new UpdateBuilder()
					.addInsert(NodeFactory.createURI(KB + KG_Instance_Name), NodeFactory.createURI(OS + "hasCover"), NodeFactory.createURI(KB + "Cover" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Cover" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "Cover"))
					.addInsert(NodeFactory.createURI(KB + "Cover" + KG_Instance_Name), NodeFactory.createURI(BMO + "hasMaterial"), NodeFactory.createURI(KB + "Material" + KG311))
					.addInsert(NodeFactory.createURI(KB + "Material" + KG311), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(BMO + "Material"))
					.addInsert(NodeFactory.createURI(KB + "Cover" + KG_Instance_Name), NodeFactory.createURI(BMO + "hasClass"), NodeFactory.createURI(KB + "Class" + KG312))
					.addInsert(NodeFactory.createURI(KB + "Class" + KG312), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "CoverClass"))
					.addInsert(NodeFactory.createURI(KB + "Cover" + KG_Instance_Name), NodeFactory.createURI(BMO + "hasShape"), NodeFactory.createURI(KB + "Shape" + KG310))
					.addInsert(NodeFactory.createURI(KB + "Shape" + KG310), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "Shape"));
			UpdateRequest CoverKG_ur = CoverKG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", CoverKG_ur.toString());


			UpdateBuilder LocationKG_ub = 
					new UpdateBuilder()
					.addInsert(NodeFactory.createURI(KB + KG_Instance_Name), NodeFactory.createURI(dul + "hasLocation"), NodeFactory.createURI(KB + "Location" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "Location" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(schema + "Location"))
					.addInsert(NodeFactory.createURI(KB + "Location" + KG_Instance_Name), NodeFactory.createURI(OS + "isInWaterProtectionZone"), KG403)
					.addInsert(NodeFactory.createURI(KB + "Location" + KG_Instance_Name), NodeFactory.createURI(OS + "isInFloodplane"), KG406)
					.addInsert(NodeFactory.createURI(KB + "Location" + KG_Instance_Name), NodeFactory.createURI(OS + "isAssociatedWith"), NodeFactory.createURI(KB + "AssociatedInfrastructure" + KG404))
					.addInsert(NodeFactory.createURI(KB + "AssociatedInfrastructure" + KG404), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OS + "AssociatedInfrastructure"))  
					.addInsert(NodeFactory.createURI(KB + "Location" + KG_Instance_Name), NodeFactory.createURI(OS + "hasCoordinates"), NodeFactory.createURI(KB + "GeoCoordinate" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "GeoCoordinate" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(schema + "GeoCoordinate"))    
					.addInsert(NodeFactory.createURI(KB + "GeoCoordinate" + KG_Instance_Name), NodeFactory.createURI(OS + "hasPositionAccuracy"), KG_GP008)
					.addInsert(NodeFactory.createURI(KB + "GeoCoordinate" + KG_Instance_Name), NodeFactory.createURI(OS + "hasCoordinateReference"), KG_GP002)
					.addInsert(NodeFactory.createURI(KB + "GeoCoordinate" + KG_Instance_Name), NodeFactory.createURI(ogc + "asWKT"), KG_GP003_SchachtMP + KG_GP004_SchachtMP)
					.addInsert(NodeFactory.createURI(KB + "GeoCoordinate" + KG_Instance_Name), NodeFactory.createURI(ogc + "asWKT"), KG_GP003_HaltungsMP + KG_GP004_HaltungsMP)  
					.addInsert(NodeFactory.createURI(KB + "Location" + KG_Instance_Name), NodeFactory.createURI(OS + "hasPointDesignation"), KG_GP001_SchachtMP)
					.addInsert(NodeFactory.createURI(KB + "Location" + KG_Instance_Name), NodeFactory.createURI(OS + "hasPointDesignation"), KG_GP001_HaltungsMP)
					.addInsert(NodeFactory.createURI(KB + "Location" + KG_Instance_Name), NodeFactory.createURI(OS + "hasElevation"), NodeFactory.createURI(KB + "Elevation" + KG_Instance_Name))	
					.addInsert(NodeFactory.createURI(KB + "Elevation" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(schema + "Elevation"))   
					.addInsert(NodeFactory.createURI(KB + "Elevation" + KG_Instance_Name), NodeFactory.createURI(OS + "hasElevationReference"), KG_GP010)
					.addInsert(NodeFactory.createURI(KB + "Elevation" + KG_Instance_Name), NodeFactory.createURI(OS + "hasElevationAccuracy"), KG_GP009)		        
					.addInsert(NodeFactory.createURI(KB + "Elevation" + KG_Instance_Name), NodeFactory.createURI(OS + "hasElevationAboveSeaLevel"), NodeFactory.createURI(KB + "ElevationAboveSeaLevel" + KG_Instance_Name))
					.addInsert(NodeFactory.createURI(KB + "ElevationAboveSeaLevel" + KG_Instance_Name), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_HEIGHT));
			UpdateRequest LocationKG_ur = LocationKG_ub.buildRequest();
			AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", LocationKG_ur.toString()); 	
			omHasValue("ElevationAboveSeaLevel" + KG_Instance_Name, "meter", KG_GP007_HaltungsMP);	
			omHasValue("ElevationAboveSeaLevel" + KG_Instance_Name, "meter", KG_GP007_SchachtMP);	
		}

		AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontosewage", "delete {?x ?y \"None\"} where {?x ?y \"None\"}"); 

	}

	public static void omHasValue(String Instance, String Unit, String NumericalValue) {
		UpdateBuilder omHasValue_ub =
				new UpdateBuilder()
				.addInsert(NodeFactory.createURI(KB + Instance), NodeFactory.createURI(OM_HAS_VALUE), NodeFactory.createURI(KB + "Measure" + Instance))
				.addInsert(NodeFactory.createURI(KB + "Measure" + Instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_MEASURE))
				.addInsert(NodeFactory.createURI(KB + "Measure" + Instance), NodeFactory.createURI(OM_HAS_UNIT), NodeFactory.createURI(KB + Unit))
				.addInsert(NodeFactory.createURI(KB + Unit), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_UNIT))
				.addInsert(NodeFactory.createURI(KB + Unit), NodeFactory.createURI(OM_SYMBOL), Unit)
				.addInsert(NodeFactory.createURI(KB + "Measure" + Instance), NodeFactory.createURI("OM_Has_NUMERICAL_VALUE"), NumericalValue);
		UpdateRequest omHasValue_ur = omHasValue_ub.buildRequest();
		AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", omHasValue_ur.toString());
	}

	public static int ColNum(String filepath, String delimiter) throws java.io.IOException {
		FileReader fr_col;
		String[] currentLine;
		fr_col = new FileReader(filepath);
		BufferedReader br_col = new BufferedReader(fr_col);
		currentLine = br_col.readLine().split(",");
		int col_length = currentLine.length;
		return col_length;
	}

	public static String[] ReadCol(int col, String filepath, String delimiter) {
		String currentLine;
		String[] data;
		ArrayList<String> colData = new ArrayList<String>();

		try {
			FileReader fr = new FileReader(filepath);
			BufferedReader br = new BufferedReader(fr);
			while ((currentLine = br.readLine()) != null) {
				data = currentLine.split(delimiter);
				colData.add(data[col]);
			}
		} catch (Exception e) {
			throw new JPSRuntimeException(e);
		}
		return colData.toArray(new String[0]);
	}

}
