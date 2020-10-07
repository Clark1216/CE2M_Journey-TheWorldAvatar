package uk.ac.cam.cares.jps.ontomatch.alignment;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;

import uk.ac.cam.cares.jps.base.query.JenaHelper;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.query.ResourcePathConverter;

/**
 * Helper class that contains: Input and output methods to render
 * alignment(List<Map>) to knowledge graph and back
 *
 * @author shaocong zhang
 * @version 1.0
 * @since 2020-09-08
 */
public class AlignmentIOHelper {
	
	public static final String VAR_CELL = "cell";
	public static final String VAR_E1 = "entity1";
	public static final String VAR_E2 = "entity2";
	public static final String VAR_M = "measure";


	/**
	 * return the sparql var format with given varname
	 * @param varname
	 * @return
	 */
	public static String sparqlVar(String varname) {
		return "?"+varname;
	}
	/**
	 * get the list of alignment as string array given alignmentfile iri
	 * @param iriOfAlignmentFile
	 * @return
	 * @throws ParseException
	 */
	public static List<String[]> readAlignmentFileAsList(String iriOfAlignmentFile) throws ParseException {
		return readAlignmentFileAsList(iriOfAlignmentFile,0.0);
	}

	/**
	 * get the list of alignment as string array given alignmentfile iri and a filter threshold on measure score field
	 * @param iriOfAlignmentFile
	 * @param threshold
	 * @return
	 * @throws ParseException
	 */
	public static List<String[]> readAlignmentFileAsList(String iriOfAlignmentFile, double threshold) throws ParseException {
		SelectBuilder q = new SelectBuilder();
		q.addPrefix(AlignmentNamespace.PREFIX, AlignmentNamespace.IRI)
		.addVar(sparqlVar(VAR_E1)).addVar(sparqlVar(VAR_E2)).addVar(sparqlVar(VAR_M))
		.addWhere(sparqlVar(VAR_CELL),AlignmentNamespace.ENTITY1,sparqlVar(VAR_E1))
		.addWhere(sparqlVar(VAR_CELL),AlignmentNamespace.ENTITY2,sparqlVar(VAR_E2))
		.addWhere(sparqlVar(VAR_CELL),AlignmentNamespace.MEASURE,sparqlVar(VAR_M));
		if(Math.abs(threshold - 0)>Math.ulp(0.1)) {//threshold is not 0
			q.addFilter(" ("+sparqlVar(VAR_M)+" - "+Double.toString(threshold) + ">=0 ) ");
		} else {
		}
			OntModel model = JenaHelper.createModel(iriOfAlignmentFile);
			ResultSet resultSet = JenaHelper.query(model, q.buildString());
			Iterator<String[]> iter = new ResultSetStringArrayIterator(resultSet, new QuerySolutionToStringArrayAdapter() {
			    @Override
			    public Iterator<String[]> adapt(QuerySolution qs) {
			        List<String[]> list = new ArrayList<String[]>();
						String sIRI = qs.get(VAR_E1).toString();
						String p = qs.get(VAR_E2).toString();
						String o = qs.getLiteral(VAR_M).getValue().toString();
						String[] values = {sIRI, p,o};
			            list.add(values);
			        
			        return list.iterator();
			    }
			});

			List<String[]> resultListfromquery = new ArrayList<String[]>();
			iter.forEachRemaining(resultListfromquery::add);
			 
			//String[] keys = JenaResultSetFormatter.getKeys(result);
			//resultListfromquery = JenaResultSetFormatter.convertToListofStringArrays(result, keys);

		return resultListfromquery;
	}
	
	/**
	 * Alignment knowledge graph(RDF) to JSONArray
	 * 
	 * @param iriOfAlignmentFile
	 * @param threshold
	 * @return JSONArray
	 * @throws Exception
	 */
	public static JSONArray readAlignmentFileAsJSONArray(String iriOfAlignmentFile, double threshold) throws Exception {

		List<String[]> resultListfromquery = readAlignmentFileAsList(iriOfAlignmentFile);
		String[] keys = {VAR_E1,VAR_E2,VAR_M};
		JSONArray resArr = new JSONArray();
		for (String[] paras : resultListfromquery) {
			JSONObject resObj = new JSONObject();
			for (int idx = 0; idx < keys.length; idx++) {
				resObj.put(keys[idx], paras[idx]);
			}
			resArr.put(resObj);
		}
		return resArr;
	}

	/***
	 *  Alignment knowledge graph(RDF) to JSONArray, with threshold 0
	 * @param iriOfAlignmentFile
	 * @return JSONArray
	 * @throws Exception
	 */
	public static JSONArray readAlignmentFileAsJSONArray(String iriOfAlignmentFile) throws Exception {
		return readAlignmentFileAsJSONArray(iriOfAlignmentFile, 0.0);
	}

	/***
	 * alignment(List<Map>) to JsonArray
	 * @param mlist(alignment as java List)
	 * @return JSONArray
	 */
	public static JSONArray scoreList2Json(List<Map> mlist) {
		JSONArray jlist = new JSONArray();
		for (int i = 0; i < mlist.size(); i++) {
			Map mcell = mlist.get(i);
			JSONArray ja = new JSONArray();
			ja.put(mcell.get(VAR_E1));
			ja.put(mcell.get(VAR_E2));
			ja.put(mcell.get(VAR_M));
			jlist.put(ja);
		}
		return jlist;
	}

	/**
	 * JSONArray to alignment(List<Map>)
	 * 
	 * @param ja, JSONArray
	 * @return alignment(List<Map>)
	 */
	public static List<Map> Json2ScoreList(JSONArray ja) {
		List<Map> mlist = new ArrayList<Map>();
		for (int idx = 0; idx < ja.length(); idx++) {
			JSONObject jo = ja.getJSONObject(idx);
			Map<String, Object> mcell = new HashMap();
			mcell.put(VAR_E1, jo.getString(VAR_E1));
			mcell.put(VAR_E2, jo.getString(VAR_E2));
			mcell.put(VAR_M, jo.getInt(VAR_M));
			mlist.add(mcell);
		}
		return mlist;
	}

	/***
	 * Alignment(List<Map>) to knowledge graph(RDF)
	 * 
	 * @param alignment
	 * @param onto1
	 * @param onto2
	 * @param addr(local address)
	 * @throws IOException
	 */
	public static void writeAlignment2File(List<Map> alignment, String onto1, String onto2, String aIRI)
			throws IOException {
		String addr = ResourcePathConverter.convertToLocalPath(aIRI);
		try {
			Model model = ModelFactory.createDefaultModel();
			BufferedWriter writer = new BufferedWriter(new FileWriter(addr));
			model.write(writer);//create empty model
		} catch (Exception e) {
			e.printStackTrace();
		}
		QueryBroker b = new QueryBroker();
		// write header: onto1 address, onto2 address
		UpdateBuilder q = new UpdateBuilder();
		Resource node = ResourceFactory.createResource(aIRI);
        Resource nodeOnto1 = ResourceFactory.createResource(aIRI+"#onto1");//TODO
        Resource nodeOnto2 = ResourceFactory.createResource(aIRI+"#onto2");

		q.addPrefix(AlignmentNamespace.PREFIX, AlignmentNamespace.IRI)
		.addInsert(node,RDF.type,AlignmentNamespace.ALIGNMENT)
        .addInsert(node,AlignmentNamespace.ONTO1,nodeOnto1)
		.addInsert(node,AlignmentNamespace.ONTO2,nodeOnto2)
		.addInsert(nodeOnto1,RDF.type,AlignmentNamespace.ONTOLOGY)
		.addInsert(nodeOnto2,RDF.type,AlignmentNamespace.ONTOLOGY)
		.addInsert(nodeOnto1,AlignmentNamespace.LOCATION,onto1)
		.addInsert(nodeOnto2,AlignmentNamespace.LOCATION,onto2);
		b.updateFile(aIRI, q.buildRequest().toString());
		//write each pair of aligned entity: entity1 IRI, entity2 IRI, measure(score)
		for (int idx = 0; idx < alignment.size(); idx++) {
			Map matched = alignment.get(idx);
			String nodeName = aIRI + "#" +VAR_CELL+ Integer.toString(idx + 1);
			String updateStr = getCellUpdateStr(matched, nodeName);
			b.updateFile(aIRI, updateStr);
		}
	}

	/**
	 * construct one set of sparql string to insert one matched pair of alignment
	 * file
	 * 
	 * @param match
	 * @param nodeIRI
	 * @return String of update sparql
	 */
	private static String getCellUpdateStr(Map match, String nodeIRI) {
		String entity1 = (String) match.get(VAR_E1);
		String entity2 = (String) match.get(VAR_E2);
		double measure = (double) match.get(VAR_M);
		UpdateBuilder q = new UpdateBuilder();

		Resource node = ResourceFactory.createResource(nodeIRI);

		q.addPrefix(AlignmentNamespace.PREFIX, AlignmentNamespace.IRI)
		.addInsert(node,RDF.type,AlignmentNamespace.CELL)
		.addInsert(node,AlignmentNamespace.ENTITY1,entity1)
		.addInsert(node,AlignmentNamespace.ENTITY2,entity2)
		.addInsert(node,AlignmentNamespace.MEASURE, Double.toString(measure))
		.addInsert(node,AlignmentNamespace.RELATION,AlignmentNamespace.EQUAL_RELATION);
		return q.buildRequest().toString();
	}
}
