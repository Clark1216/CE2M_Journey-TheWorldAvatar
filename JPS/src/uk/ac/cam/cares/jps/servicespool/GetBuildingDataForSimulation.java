package uk.ac.cam.cares.jps.servicespool;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.building.BuildingQueryPerformer;
import uk.ac.cam.cares.jps.building.CRSTransformer;
import uk.ac.cam.cares.jps.building.SimpleBuildingData;
import uk.ac.cam.cares.jps.semantic.JSONFlattenTool;


@WebServlet("/GetBuildingDataForSimulation")
public class GetBuildingDataForSimulation extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String BUILDING_IRI_THE_HAGUE_PREFIX = "http://www.theworldavatar.com/kb/nld/thehague/buildings/";
 
	// This agent takes selected region, plant IRI and CityIRI
	// returns BuildingDataForSimulation (Semantic)
	
	 
	
	public GetBuildingDataForSimulation() {
        super();
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 
		String value = request.getParameter("value");
		
		System.out.println(value);
		
		JSONObject input = null;
		try {
			input = new JSONObject(value);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONObject region = null;
		try {
			region = JSONFlattenTool.flattenRegion(input);
			System.out.println("====================== Region ========================");
			System.out.println(region.toString());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
		 
			double[] coords;
			coords = queryPlantXY(input.getString("plant"));
			double plantX = coords[0];
			double plantY = coords[1];
			String city = input.getString("city");			
			
			double upperx = Double.parseDouble(region.getString("upperx"));
			double uppery = Double.parseDouble(region.getString("uppery"));
			double lowerx = Double.parseDouble(region.getString("lowerx"));
			double lowery = Double.parseDouble(region.getString("lowery"));
			
			
			double plantx = 79831;
			double planty = 454766;
			
			String data = "";
			
			city = city.replace("page", "resource");
			
			if(city.equalsIgnoreCase("http://dbpedia.org/resource/The_Hague")) {
				//response.getWriter().write("This is HAGUE -- " + "|" + city + "|" + String.valueOf(upperx) + "|" +String.valueOf(uppery) + "|" + String.valueOf(lowerx) + "|" + String.valueOf(lowery) + "|" + String.valueOf(plantx) +"|" +  String.valueOf(planty));
				data = retrieveBuildingDataInJSON(BuildingQueryPerformer.THE_HAGUE_IRI, plantx, planty, 25, lowerx, lowery, upperx, uppery);			
			}
			else {
				String sourceCRS = CRSTransformer.EPSG_25833; // Berlin
				double[] sourceCenter = new double[]{392825, 5819122};
				String targetCRS = CRSTransformer.EPSG_28992; // The Hague
				double[] targetCenter = CRSTransformer.transform(sourceCRS, targetCRS, sourceCenter);
				plantx = targetCenter[0];
				planty = targetCenter[1];
				
				double[] upperPointOld = new double[] {Double.parseDouble(region.getString("upperx")),Double.parseDouble(region.getString("uppery"))};
				double[] lowerPointOld = new double[] {Double.parseDouble(region.getString("lowerx")),Double.parseDouble(region.getString("lowery"))};
				
				double[] upperPointNew = CRSTransformer.transform(sourceCRS, targetCRS, upperPointOld);
				double[] lowerPointNew = CRSTransformer.transform(sourceCRS, targetCRS, lowerPointOld);
				
				upperx= upperPointNew[0];
				uppery= upperPointNew[1];
				lowerx= lowerPointNew[0];
				lowery= lowerPointNew[1];
				
				data = retrieveBuildingDataInJSON(BuildingQueryPerformer.BERLIN_IRI, plantx, planty, 25, lowerx, lowery, upperx, uppery);
			}
			
			response.getWriter().write(data);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public String convertBuildingDataToSemantic(String buildingData) {
	
		Model BuildingDataModel = ModelFactory.createDefaultModel();
		Property hasBuildingType = BuildingDataModel.createProperty("http://test.com/Property/hasBuildingType");
		Property hasBuildingX = BuildingDataModel.createProperty("http://test.com/Property/hasBuildingX");
		Property hasBuildingY = BuildingDataModel.createProperty("http://test.com/Property/hasBuildingY");
		Property hasBuildingName = BuildingDataModel.createProperty("http://test.com/Property/hasBuildingName");
		Property hasBuildingHeight = BuildingDataModel.createProperty("http://test.com/Property/hasBuildingHeigth");
		Property hasBuildingLength = BuildingDataModel.createProperty("http://test.com/Property/hasBuildingLength");
		Property hasBuildingWidth = BuildingDataModel.createProperty("http://test.com/Property/hasBuildingWidth");
		Property hasBuildingAngle = BuildingDataModel.createProperty("http://test.com/Property/hasBuildingAngel");

		
		try {
			JSONObject buildingDataInJSON = new JSONObject(buildingData);
			int length = buildingDataInJSON.getJSONArray("BldName").length();
			for(int i = 0; i < length; i++) {
				String BuildingIRI =      buildingDataInJSON.getJSONArray("BldIRI").getString(i);
				String BuildingName =     buildingDataInJSON.getJSONArray("BldName").getString(i);
				int BuildingType =        buildingDataInJSON.getJSONArray("BldType").getInt(i);
				double BuildingX =   	  buildingDataInJSON.getJSONArray("BldX").getDouble(i);
				double BuildingY =        buildingDataInJSON.getJSONArray("BldY").getDouble(i);
				double BuildingHeight =   buildingDataInJSON.getJSONArray("BldHeight").getDouble(i);
				double BuildingLength =   buildingDataInJSON.getJSONArray("BldLength").getDouble(i);
				double BuildingWidth =    buildingDataInJSON.getJSONArray("BldWidth").getDouble(i);
				double BuildingAngle =    buildingDataInJSON.getJSONArray("BldAngle").getDouble(i);
				Resource building = BuildingDataModel.createResource(BuildingIRI);
				building.addProperty(RDF.type, "http://test.com/Ontology/Building");
				building.addLiteral(hasBuildingType, BuildingType);
				building.addLiteral(hasBuildingX, BuildingX);
				building.addLiteral(hasBuildingY, BuildingY);
				building.addLiteral(hasBuildingName, BuildingName);
				building.addLiteral(hasBuildingHeight, BuildingHeight);
				building.addLiteral(hasBuildingLength, BuildingLength);
				building.addLiteral(hasBuildingWidth, BuildingWidth);
				building.addLiteral(hasBuildingAngle, BuildingAngle);
			}
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		StringWriter out = new StringWriter();
		RDFDataMgr.write(out, BuildingDataModel, RDFFormat.RDFJSON);


		return out.toString();
	}
	
	
	
	public String retrieveBuildingDataInJSON(String cityIRI, double plantx, double planty, int buildingLimit, double lowerx, double lowery, double upperx, double uppery) {
		List<String> buildingIRIs = createQueryPerformerForTheHague().performQueryClosestBuildingsFromRegion(cityIRI, plantx, planty, buildingLimit, lowerx, lowery, upperx, uppery);
		SimpleBuildingData result = createQueryPerformerForTheHague().performQuerySimpleBuildingData(cityIRI, buildingIRIs);
		String argument = new Gson().toJson(result);
		return argument;
	}
	
	public static BuildingQueryPerformer createQueryPerformerForTheHague() {
		// TODO-AE URGENT remove this as soon as we don't need the old KB for The Hague anymore
		if (BUILDING_IRI_THE_HAGUE_PREFIX.equals("http://www.theworldavatar.com/Building/")) {
			return new BuildingQueryPerformer("www.theworldavatar.com", 80, "/damecoolquestion/buildingsLite/query");
		}
		return new BuildingQueryPerformer();
	}
	
	
	public double[] queryPlantXY(String plantIRI) throws JSONException {

		 
		
		String myHost = "www.theworldavatar.com" ;
		int myPort = 80;
		String myPath = "/damecoolquestion/composition/query";
		
		//space_and_time_extended:hasGISCoordinateSystem
		String plantXYQuery =
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+   "PREFIX space_and_time_extended: <http://www.theworldavatar.com/OntoCAPE/OntoCAPE/supporting_concepts/space_and_time/space_and_time_extended.owl#>\n"
				+ 	"PREFIX system: <http://www.theworldavatar.com/OntoCAPE/OntoCAPE/upper_level/system.owl#>" + 
				"SELECT ?plantX ?plantY WHERE \n"
				+ "{"
				+ "		<%s>      space_and_time_extended:hasGISCoordinateSystem  ?coor . \n"
				+ "		?coor     space_and_time_extended:hasProjectedCoordinate_x ?x .  \n"
				+ "		?coor     space_and_time_extended:hasProjectedCoordinate_y ?y .  \n"
				+ "		?x        system:hasValue ?vx .  \n"
				+ "     ?vx   	  system:numericalValue ?plantX . \n"
				+ "     ?y		  system:hasValue ?vy .  \n"
				+ "     ?vy       system:numericalValue ?plantY . \n"
				+ "}";
		
		String finalQuery = String.format(plantXYQuery, plantIRI);
		URIBuilder builder;
		builder = new URIBuilder().setScheme("http").setHost(myHost).setPort(myPort)
				.setPath(myPath)
				.setParameter("query", finalQuery)
				.setParameter("output", "json");
		String result = executeGet(builder);
		JSONObject resultInJSON = new JSONObject(result);
		JSONArray bindings = resultInJSON.getJSONObject("results").getJSONArray("bindings");
		Double X = 0.0,Y=0.0; 
		for(int i = 0; i < bindings.length();i++) {
			JSONObject binding = bindings.getJSONObject(i);
			Y = (binding.getJSONObject("plantY").getDouble("value"));
			X = (binding.getJSONObject("plantX").getDouble("value"));
		}
	
		return new double[]{X,Y};	
	}

	
	public String executeGet(URIBuilder builder) {
		try {
			URI uri = builder.build();
			HttpGet request = new HttpGet(uri);
			request.setHeader(HttpHeaders.ACCEPT, "application/json");
			HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				throw new JPSRuntimeException("HTTP response with error = " + httpResponse.getStatusLine());
			}
			return EntityUtils.toString(httpResponse.getEntity());
		} catch (Exception e) {
			throw new JPSRuntimeException(e.getMessage(), e);
		} 
	}
	
	
}