package uk.ac.cam.cares.jps.ontomatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.cam.cares.jps.base.annotate.MetaDataAnnotator;
import uk.ac.cam.cares.jps.base.query.JenaHelper;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
/***
 * 

Agent that processes all alignment files produced by single metric matching to a combination result
Include functions to choose: weighted sum, filtering ,cardinality filtering, class type penalizing
Must start with weighted sum and end with filtering
Input: IRI of alignments
Output: IRI of new alignmnent
 * @author shaocong
 *
 */
import uk.ac.cam.cares.jps.base.util.AsyncPythonHelper;


/***
 * Aggregates element level alignment result(tmp alignment owl files).
 * Contains several function to be composed by choice.
 * Input: alignment owl IRIs.
 * Output: result alignment owl IRI.
 */
@WebServlet(urlPatterns = { "/matchAggregator" })

public class MatchAggregator extends JPSHttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1142445270131640156L;
	protected String srcOnto, tgtOnto;
	protected String thisAlignmentIRI;
	protected List<List> matchScoreLists = new ArrayList<List>();
	protected List<Map> finalScoreList = new ArrayList<Map>();
	protected List<Double> weights= new ArrayList<Double>(); 
	protected double threshold;
	protected List<AGGREGATE_CHOICE> choices = new ArrayList<AGGREGATE_CHOICE>();
	protected String classAlignmentIRI;
	protected double pFactor,sameClassThreshold;
	

    public enum AGGREGATE_CHOICE {
        PENALIZING, CARDINALITY
	}
	protected JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
		System.out.println("Match Aggregator agent");

		JSONObject jo = requestParams;
		
		try {
			threshold = jo.getFloat("threshold");
			srcOnto = jo.getString("srcOnto");
			tgtOnto = jo.getString("tgtOnto");
			thisAlignmentIRI = jo.getString("addr");

			//get weights
			JSONArray jweight = jo.getJSONArray("weights");
		for (int i=0; i<jweight.length(); i++) {
			weights.add(jweight.getDouble(i) );
		}

		JSONArray jalignments = jo.getJSONArray("alignments");
	    List<String> alignmentIRIs = new ArrayList<String>(); 
		for (int i=0; i<jalignments.length(); i++) {
			String aIRI = jalignments.getString(i);
			getAlignmentList(aIRI);
		}
		if(jo.has("choices")){
		JSONArray functionChoice = jo.getJSONArray("choices");
		for(int i = 0; i < functionChoice.length(); i++){
			choices.add(AGGREGATE_CHOICE.valueOf(functionChoice.getString(i)));
		}
		}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		JSONObject resultObj = new JSONObject();
		
		//reading choice params 
		try {
			classAlignmentIRI = jo.getString("classAlign");
			pFactor = jo.getInt("pFactor");
			sameClassThreshold = jo.getInt("sameClassThreshold");
		}catch(JSONException e) {
			//do nothing as these are optional params
		};
		try {
			handleChoice();
			AlignmentIOHelper.writeAlignment2File(finalScoreList, srcOnto, tgtOnto, thisAlignmentIRI);
			//TODO:this should render a new alignment file,add this function to AlignmentHelper
			resultObj.put("success", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultObj;
	}
	
	    public void handleChoice() throws Exception {
	    	weighting();
	    	if(choices !=null &&choices.contains(AGGREGATE_CHOICE.CARDINALITY)){
	    		one2oneCardinalityFiltering();
	    	}
	    	if(choices !=null &&choices.contains(AGGREGATE_CHOICE.PENALIZING)){
	    		penalizing(classAlignmentIRI, sameClassThreshold, pFactor);
	    	}
	    	filtering(threshold);
	    }
	
		public void getAlignmentList(String iriOfAlignmentFile) {
			String queryStr = "PREFIX alignment: <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#> "
					+ "SELECT ?entity1 ?entity2 ?measure " 
					+ "WHERE {?cell alignment:entity1 ?entity1."
					+ "?cell  alignment:entity2 ?entity2 ."
					+"?cell alignment:measure ?measure."
					//+ "FILTER (?measure >= "+threshold +" ) " //filtering gen 001 as it is slackbus
					+ "}";
			System.out.println(queryStr);
			List<String[]> resultListfromquery = null;
			try {
				OntModel model = JenaHelper.createModel(iriOfAlignmentFile);
				ResultSet resultSet = JenaHelper.query(model, queryStr);
				String result = JenaResultSetFormatter.convertToJSONW3CStandard(resultSet);
				String[] keys = JenaResultSetFormatter.getKeys(result);
				resultListfromquery = JenaResultSetFormatter.convertToListofStringArrays(result, keys);
		        System.out.println("reading alignment:");
				System.out.println(resultListfromquery.toString());
				
			}
			catch(Exception e) {
	            StringWriter sw = new StringWriter();
	            e.printStackTrace(new PrintWriter(sw));
	            String exceptionAsString = sw.toString();
				logger.error(exceptionAsString);
			}
			 matchScoreLists.add(resultListfromquery);
		}
		
	protected void weighting() {		
		int matcherNum = matchScoreLists.size();
		for(int idxMatcher = 0 ; idxMatcher<matcherNum;idxMatcher++) {
			List aScoreList = matchScoreLists.get(idxMatcher);
			double myWeight  = weights.get(idxMatcher);
			int elementNum = matchScoreLists.get(0).size();
			for(int idxElement = 0; idxElement < elementNum; idxElement++) {
				String[] myscore = (String[]) aScoreList.get(idxElement);
				if(idxElement >= finalScoreList.size()){//index not exists, initiates
					 Map<String, Object> acell = new HashMap<>();
					 acell.put("entity1", myscore[0]);
					 acell.put("entity2", myscore[1]);
					 acell.put("measure", Double.parseDouble(myscore[2])*myWeight);
					 finalScoreList.add(acell);
					}else{// index exists, update measure
                        Map mcell = finalScoreList.get(idxElement);                       
						 mcell.put("measure",(double)mcell.get("measure")+Double.parseDouble(myscore[2])*myWeight);
					}	
			 }			
		}
	}
	
	
	protected void filtering(double threshold2) {//remove cellmaps with measure<threshold
	//loop thru cells to filter out based on measurement 
		int elementNum = finalScoreList.size();
		for(int idxElement = 0; idxElement < elementNum; idxElement++) {
            Map mcell = finalScoreList.get(idxElement);             
            if((double)mcell.get("measure") - threshold2 <0) {
            mcell.remove(mcell);
            }
		}		
	}
	
	
	protected void penalizing(String classAlignmentIRI, double sameClassThreshold, double pFactor) throws Exception {
		//need to call the python for now
		//JSONArray scoreListNow = AlignmentIOHelper.scoreList2Json(finalScoreList);
		//JSONArray classAlignment = AlignmentIOHelper.readAlignmentFileAsJSONArray(classAlignmentIRI, 0.0);
		//String[] paras = {scoreListNow.toString(), classAlignmentIRI};
		//String[] results = AsyncPythonHelper.callPython("PenalizerCaller.py",paras,LexicalProcessor.class);
		//parse the return back to list
		//JSONArray scoreListNew = new JSONArray(results[0]);
		//finalScoreList =  AlignmentIOHelper.Json2ScoreList(scoreListNew);
		//read 
	    OntModel srcModel = ModelFactory.createOntologyModel();
	    srcModel.read(srcOnto);
	    OntModel tgtModel = ModelFactory.createOntologyModel();
	    tgtModel.read(tgtOnto);
	    Map ICMap1 = constructICMap(srcModel,srcOnto);
	    Map ICMap2 = constructICMap(tgtModel, tgtOnto);
		int elementNum = finalScoreList.size();
		for(int idxElement = 0; idxElement < elementNum; idxElement++) {
            Map mcell = finalScoreList.get(idxElement);             
            String indi1 = (String) mcell.get("entity1");
            String indi2 = (String) mcell.get("entity2");
            JSONArray joca = AlignmentIOHelper.readAlignmentFileAsJSONArray(classAlignmentIRI, sameClassThreshold); 
            List classAlign = AlignmentIOHelper.Json2ScoreList(joca);
            if(!sameClass(classAlign, ICMap1, ICMap2, indi1, indi2)) {//does not belong to same class
				 mcell.put("measure",(double)mcell.get("measure")*pFactor);//penalize
            }
		}
        
	}
	

    protected boolean sameClass(List<Map> classAlign, Map icmap1, Map icmap2, String indiIri1, String indiIri2) {
    	String class1 = (String) icmap1.get(indiIri1);
    	String class2 = (String) icmap1.get(indiIri2);
    	if (sameClass(classAlign, class1, class2)) {
    		return true;
    	} else {
    		return false;
    	}
 
    }
    
    protected boolean sameClass(List<Map> classAlign, String classIRI1, String classIRI2) {
    	for(Map map:classAlign) {
    		String mapped1 = (String) map.get("entity1");
    		String mapped2 = (String) map.get("entity2");
    		System.out.println("in class align");
    		System.out.println(mapped1);
    		System.out.println(mapped2);

    		if(mapped1.equals(classIRI1) && mapped2.equals(classIRI2) ||(mapped1.equals(classIRI2)&&mapped2.equals(classIRI1))) {
    		return true;	
    		}
    	}
    	return false;
    }
	
	protected Map constructICMap(OntModel model, String ontologyIRI) {
	    Map ICMap = new HashMap();
	     ExtendedIterator classes = model.listClasses();
	     while (classes.hasNext())
	     {
	       OntClass thisClass = (OntClass) classes.next();
	       ExtendedIterator instances = thisClass.listInstances();
	       while (instances.hasNext())
	       {
	         Individual thisInstance = (Individual) instances.next();
	         ICMap.put(thisInstance.toString(), thisClass.toString());
	       }
	     }
	     return ICMap;
	}
	
	protected void one2oneCardinalityFiltering() throws IOException {
		//need to call the python for now
		//input: map rendered as json
		JSONArray scoreListNow = AlignmentIOHelper.scoreList2Json(finalScoreList);
		String[] paras = {scoreListNow.toString()};
		String[] results = AsyncPythonHelper.callPython("matchers/onetooneCardi.py",paras,MatchAggregator.class);	
		JSONArray scoreListNew = new JSONArray(results[0]);
		finalScoreList =  AlignmentIOHelper.Json2ScoreList(scoreListNew);
	}
}

