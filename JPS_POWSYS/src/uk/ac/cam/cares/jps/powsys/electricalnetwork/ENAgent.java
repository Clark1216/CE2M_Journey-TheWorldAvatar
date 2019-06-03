package uk.ac.cam.cares.jps.powsys.electricalnetwork;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.JenaHelper;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.base.util.MiscUtil;
import uk.ac.cam.cares.jps.powsys.envisualization.ENVisualization;
import uk.ac.cam.cares.jps.powsys.envisualization.ENVisualization.StaticobjectgenClass;
import uk.ac.cam.cares.jps.powsys.envisualization.MapPoint;
import uk.ac.cam.cares.jps.powsys.nuclear.IriMapper;
import uk.ac.cam.cares.jps.powsys.nuclear.IriMapper.IriMapping;
import uk.ac.cam.cares.jps.powsys.util.Util;

@WebServlet(urlPatterns = { "/ENAgent/startsimulationPF", "/ENAgent/startsimulationOPF" })
public class ENAgent extends JPSHttpServlet {
	
	private static final long serialVersionUID = -4199209974912271432L;
	private Logger logger = LoggerFactory.getLogger(ENAgent.class);

	public DatatypeProperty getNumericalValueProperty(OntModel jenaOwlModel) {
		return jenaOwlModel.getDatatypeProperty(
				"http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#numericalValue");
	}

	protected void doGetJPS(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		JSONObject joforEN = AgentCaller.readJsonParameter(request);
		String iriofnetwork = joforEN.getString("electricalnetwork");
		String modeltype = null;

		String path = request.getServletPath();

		if ("/ENAgent/startsimulationPF".equals(path)) {
			modeltype = "PF";// PF or OPF
		} else if ("/ENAgent/startsimulationOPF".equals(path)) {
			modeltype = "OPF";
		}

		String baseUrl = QueryBroker.getLocalDataPath() + "/JPS_POWSYS_EN";
		
		startSimulation(iriofnetwork, baseUrl, modeltype);
	}
	
	public String createfinalKML(OntModel model) throws TransformerException {
		ENVisualization a = new ENVisualization();
		

		// ------------FOR GENERATORS-----------------
		List<String[]> generators = a.queryElementCoordinate(model, "PowerGenerator");
		ArrayList<ENVisualization.StaticobjectgenClass> gensmerged = new ArrayList<ENVisualization.StaticobjectgenClass>();
		ArrayList<String> coorddata = new ArrayList<String>();
		for (int e = 0; e < generators.size(); e++) {
			StaticobjectgenClass gh = a.new StaticobjectgenClass();
			gh.setnamegen("/" + generators.get(e)[0].split("#")[1] + ".owl");
			gh.setx(generators.get(e)[1]);
			gh.sety(generators.get(e)[2]);
			//System.out.println("/" + generators.get(e)[0].split("#")[1] + ".owl");

			if (coorddata.contains(gh.getx()) && coorddata.contains(gh.gety())) {
				int index = coorddata.indexOf(gh.getx()) / 2;
				gensmerged.get(index).setnamegen(gensmerged.get(index).getnamegen() + gh.getnamegen());
			} else {
				gensmerged.add(gh);
				coorddata.add(generators.get(e)[1]);
				coorddata.add(generators.get(e)[2]);
			}

		}

		for (int g = 0; g < gensmerged.size(); g++) {
			MapPoint c = new MapPoint(Double.valueOf(gensmerged.get(g).gety()),
					Double.valueOf(gensmerged.get(g).getx()), 0.0, gensmerged.get(g).getnamegen());
			a.addMark(c, "generator");
		}

		// --------------------------------
		
	
		// ------------FOR BUS-----------------
		List<String[]> bus = a.queryElementCoordinate(model, "BusNode");
		ArrayList<ENVisualization.StaticobjectgenClass> bussesmerged = new ArrayList<ENVisualization.StaticobjectgenClass>();
		ArrayList<String> coorddatabus = new ArrayList<String>();
		for (int e = 0; e < bus.size(); e++) {
			StaticobjectgenClass gh = a.new StaticobjectgenClass();
			gh.setnamegen("/" + bus.get(e)[0].split("#")[1] + ".owl");
			gh.setx(bus.get(e)[1]);
			gh.sety(bus.get(e)[2]);
			//System.out.println("/" + bus.get(e)[0].split("#")[1] + ".owl");

			if (coorddatabus.contains(gh.getx()) && coorddatabus.contains(gh.gety())) {
				int index = coorddatabus.indexOf(gh.getx()) / 2;
				bussesmerged.get(index).setnamegen(bussesmerged.get(index).getnamegen() + gh.getnamegen());
			} else {
				bussesmerged.add(gh);
				coorddatabus.add(bus.get(e)[1]);
				coorddatabus.add(bus.get(e)[2]);
			}

		}

		for (int g = 0; g < bussesmerged.size(); g++) {
			MapPoint c = new MapPoint(Double.valueOf(bussesmerged.get(g).gety()),
					Double.valueOf(bussesmerged.get(g).getx()), 0.0, bussesmerged.get(g).getnamegen());
			a.addMark(c, "bus");
		}

		// --------------------------------

		
//		int size2 = bus.size();
//		for (int g = 0; g < size2; g++) {
//			MapPoint c = new MapPoint(Double.valueOf(bus.get(g)[2]), Double.valueOf(bus.get(g)[1]), 0.0,
//					"/" + bus.get(g)[0].split("#")[1] + ".owl");
//			a.addMark(c, "bus");
//		}

		return a.writeFiletoString();
	}

	public void startSimulation(String iriofnetwork, String baseUrl, String modeltype) throws IOException {
		
		logger.info("starting simulation for electrical network = " + iriofnetwork + ", modeltype = " + modeltype + ", local data path=" + baseUrl);
		
		OntModel model = readModelGreedy(iriofnetwork);
		
		//create line javascript & kml for visualization
		ENVisualization a=new ENVisualization();
		QueryBroker broker = new QueryBroker();
		broker.put(baseUrl + "/line.js", a.createLineJS(model));
		try {
			broker.put(baseUrl + "/test2.kml",createfinalKML(model));
		} catch (TransformerException e1) {
			logger.error(e1.getMessage(),e1);
			e1.printStackTrace();
		}
		
		
		List<String[]> buslist = generateInput(model, iriofnetwork, baseUrl, modeltype);
		
		logger.info("running PyPower simulation");
		runModel(baseUrl);

		try {
			logger.info("converting PyPower results to OWL files");
			doConversion(model, iriofnetwork, baseUrl, modeltype, buslist);
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
			throw new JPSRuntimeException(e.getMessage(), e);
		}
		
		logger.info("finished simulation for electrical network = " + iriofnetwork + ", modeltype = " + modeltype + ", local data path=" + baseUrl);
	}

	public List<String[]> generateInput(OntModel model, String iriofnetwork, String baseUrl, String modeltype) throws IOException {
		
		String genInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
				+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
				+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
				+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
				+ "SELECT ?entity ?BusNumbervalue ?activepowervalue ?reactivepowervalue ?Qmaxvalue ?Qminvalue ?Vgvalue ?mBasevalue ?Statusvalue ?Pmaxvalue ?Pminvalue ?Pc1value ?Pc2value ?Qc1minvalue ?Qc1maxvalue ?Qc2minvalue ?Qc2maxvalue ?Rampagcvalue ?Ramp10value ?Ramp30value ?Rampqvalue ?apfvalue "

				+ "WHERE {?entity  a  j1:PowerGenerator  ."
				+ "?entity   j2:isModeledBy ?model ."

				+ "?model   j5:hasModelVariable ?num ." 
				+ "?num  a  j3:BusNumber  ." 
				+ "?num  j2:hasValue ?vnum ."
				+ "?vnum   j2:numericalValue ?BusNumbervalue ." // number

				+ "?model   j5:hasModelVariable ?Pg ." 
				+ "?Pg  a  j3:Pg  ." 
				+ "?Pg  j2:hasValue ?vpg ."
				+ "?vpg   j2:numericalValue ?activepowervalue ." // pg

				+ "?model   j5:hasModelVariable ?Qg ." 
				+ "?Qg  a  j3:Qg  ." 
				+ "?Qg  j2:hasValue ?vqg ."
				+ "?vqg   j2:numericalValue ?reactivepowervalue ." // qg

				+ "?model   j5:hasModelVariable ?qmax ." 
				+ "?qmax  a  j3:QMax  ." 
				+ "?qmax  j2:hasValue ?vqmax ."
				+ "?vqmax   j2:numericalValue ?Qmaxvalue ." // qmax

				+ "?model   j5:hasModelVariable ?qmin ." 
				+ "?qmin  a  j3:QMin  ." 
				+ "?qmin  j2:hasValue ?vqmin ."
				+ "?vqmin   j2:numericalValue ?Qminvalue ." // qmin

				+ "?model   j5:hasModelVariable ?Vg ." 
				+ "?Vg  a  j3:Vg  ." 
				+ "?Vg  j2:hasValue ?vVg ."
				+ "?vVg   j2:numericalValue ?Vgvalue ." // vg

				+ "?model   j5:hasModelVariable ?mbase ." 
				+ "?mbase  a  j3:mBase  ." 
				+ "?mbase  j2:hasValue ?vmbase ."
				+ "?vmbase   j2:numericalValue ?mBasevalue ." // mbase

				+ "?model   j5:hasModelVariable ?stat ." 
				+ "?stat  a  j3:Status ." 
				+ "?stat  j2:hasValue ?vstat ."
				+ "?vstat   j2:numericalValue ?Statusvalue ." // status

				+ "?model   j5:hasModelVariable ?pmax ." 
				+ "?pmax  a  j3:PMax  ." 
				+ "?pmax  j2:hasValue ?vpmax ."
				+ "?vpmax   j2:numericalValue ?Pmaxvalue ." // pmax

				+ "?model   j5:hasModelVariable ?pmin ." 
				+ "?pmin  a  j3:PMin  ." 
				+ "?pmin  j2:hasValue ?vpmin ."
				+ "?vpmin   j2:numericalValue ?Pminvalue ." // pmin

				+ "?model   j5:hasModelVariable ?pc1 ." 
				+ "?pc1  a  j3:Pc1  ." 
				+ "?pc1  j2:hasValue ?vpc1 ."
				+ "?vpc1   j2:numericalValue ?Pc1value ." // pc1

				+ "?model   j5:hasModelVariable ?pc2 ." 
				+ "?pc2  a  j3:Pc2  ." 
				+ "?pc2  j2:hasValue ?vpc2 ."
				+ "?vpc2   j2:numericalValue ?Pc2value ." // pc2

				+ "?model   j5:hasModelVariable ?qc1min ." 
				+ "?qc1min  a  j3:QC1Min  ."
				+ "?qc1min  j2:hasValue ?vqc1min ." 
				+ "?vqc1min   j2:numericalValue ?Qc1minvalue ." // qc1min

				+ "?model   j5:hasModelVariable ?Qc1max ." 
				+ "?Qc1max  a  j3:QC1Max  ."
				+ "?Qc1max  j2:hasValue ?vQc1max ." 
				+ "?vQc1max   j2:numericalValue ?Qc1maxvalue ." // qc1max

				+ "?model   j5:hasModelVariable ?qc2min ." 
				+ "?qc2min  a  j3:QC2Min  ."
				+ "?qc2min  j2:hasValue ?vqc2min ."
				+ "?vqc2min   j2:numericalValue ?Qc2minvalue ." // qc2min

				+ "?model   j5:hasModelVariable ?Qc2max ."
				+ "?Qc2max  a  j3:QC2Max  ."
				+ "?Qc2max  j2:hasValue ?vQc2max ." 
				+ "?vQc2max   j2:numericalValue ?Qc2maxvalue ." // qc2max

				+ "?model   j5:hasModelVariable ?rampagc ." 
				+ "?rampagc  a  j3:Rampagc  ."
				+ "?rampagc  j2:hasValue ?vrampagc ." 
				+ "?vrampagc   j2:numericalValue ?Rampagcvalue ." // rampagc

				+ "?model   j5:hasModelVariable ?ramp10 ." 
				+ "?ramp10  a  j3:Ramp10  ."
				+ "?ramp10  j2:hasValue ?vramp10 ."
				+ "?vramp10   j2:numericalValue ?Ramp10value ." // ramp10

				+ "?model   j5:hasModelVariable ?ramp30 ." 
				+ "?ramp30  a  j3:Ramp30  ."
				+ "?ramp30  j2:hasValue ?vramp30 ." 
				+ "?vramp30   j2:numericalValue ?Ramp30value ." // ramp30

				+ "?model   j5:hasModelVariable ?rampq ." 
				+ "?rampq  a  j3:Rampq  ." 
				+ "?rampq  j2:hasValue ?vrampq ."
				+ "?vrampq   j2:numericalValue ?Rampqvalue ." // rampq

				+ "?model   j5:hasModelVariable ?apf ."
				+ "?apf  a  j3:APF  ." 
				+ "?apf  j2:hasValue ?vapf ."
				+ "?vapf   j2:numericalValue ?apfvalue ." // apf

				+ "}";

		String genInfocost = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
				+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
				+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
				+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
				+ "SELECT ?entity ?formatvalue ?startupcostvalue ?shutdowncostvalue ?gencostnvalue ?gencostn1value ?gencostn2value ?gencostcvalue "
				+ "WHERE {?entity  a  j1:PowerGenerator  ." 
				+ "?entity   j2:isModeledBy ?model ."

				+ "?model   j5:hasModelVariable ?format ." 
				+ "?format  a  j3:CostModel  ."
				+ "?format  j2:hasValue ?vformat ." 
				+ "?vformat  j2:numericalValue ?formatvalue ."

				+ "?model   j5:hasModelVariable ?startup ." 
				+ "?startup  a  j3:StartCost  ."
				+ "?startup  j2:hasValue ?vstartup ." 
				+ "?vstartup   j2:numericalValue ?startupcostvalue ."

				+ "?model   j5:hasModelVariable ?shutdown ." 
				+ "?shutdown  a  j3:StopCost  ."
				+ "?shutdown  j2:hasValue ?vshutdown ." 
				+ "?vshutdown   j2:numericalValue ?shutdowncostvalue ."

				+ "?model   j5:hasModelVariable ?gencostn ." 
				+ "?gencostn  a  j3:genCostn  ."
				+ "?gencostn  j2:hasValue ?vgencostn ." 
				+ "?vgencostn   j2:numericalValue ?gencostnvalue ."

				+ "?model   j5:hasModelVariable ?gencostn1 ." 
				+ "?gencostn1  a  j3:genCostcn-1  ."
				+ "?gencostn1  j2:hasValue ?vgencostn1 ." 
				+ "?vgencostn1   j2:numericalValue ?gencostn1value ."

				+ "?model   j5:hasModelVariable ?gencostn2 ." 
				+ "?gencostn2  a  j3:genCostcn-2  ."
				+ "?gencostn2  j2:hasValue ?vgencostn2 ." 
				+ "?vgencostn2   j2:numericalValue ?gencostn2value ."

				+ "?model   j5:hasModelVariable ?gencostc ." 
				+ "?gencostc  a  j3:genCostc0  ."
				+ "?gencostc  j2:hasValue ?vgencostc ." 
				+ "?vgencostc   j2:numericalValue ?gencostcvalue ." 
				+ "}";

		String branchInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
				+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
				+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
				+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
				+ "SELECT ?entity ?BusNumber1value ?BusNumber2value ?resistancevalue ?reactancevalue ?susceptancevalue ?rateAvalue ?rateBvalue ?rateCvalue ?ratiovalue ?anglevalue ?statusvalue ?angleminvalue ?anglemaxvalue "

				+ "WHERE {?entity  a  j1:UndergroundCable  ." 
				+ "?entity   j2:isModeledBy ?model ."
				+ "?model   j5:hasModelVariable ?num1 ." 
				+ "?num1  a  j3:BusFrom  ." 
				+ "?num1  j2:hasValue ?vnum1 ."
				+ "?vnum1   j2:numericalValue ?BusNumber1value ." // number 1

				+ "?model   j5:hasModelVariable ?num2 ." 
				+ "?num2  a  j3:BusTo  ." 
				+ "?num2  j2:hasValue ?vnum2 ."
				+ "?vnum2   j2:numericalValue ?BusNumber2value ." // number 2

				+ "?model   j5:hasModelVariable ?res ." 
				+ "?res  a  j3:R  ." 
				+ "?res  j2:hasValue ?vres ."
				+ "?vres   j2:numericalValue ?resistancevalue ." // resistance

				+ "?model   j5:hasModelVariable ?rea ." 
				+ "?rea  a  j3:X  ." 
				+ "?rea  j2:hasValue ?vrea ."
				+ "?vrea   j2:numericalValue ?reactancevalue ." // reactance

				+ "?model   j5:hasModelVariable ?sus ." 
				+ "?sus  a  j3:B  ." 
				+ "?sus  j2:hasValue ?vsus ."
				+ "?vsus   j2:numericalValue ?susceptancevalue ." // susceptance

				+ "?model   j5:hasModelVariable ?ratea ." 
				+ "?ratea  a  j3:RateA  ." 
				+ "?ratea  j2:hasValue ?vratea ."
				+ "?vratea   j2:numericalValue ?rateAvalue ." // rateA

				+ "?model   j5:hasModelVariable ?rateb ." 
				+ "?rateb  a  j3:RateB  ." 
				+ "?rateb  j2:hasValue ?vrateb ."
				+ "?vrateb   j2:numericalValue ?rateBvalue ." // rateB

				+ "?model   j5:hasModelVariable ?ratec ." 
				+ "?ratec  a  j3:RateC  ." 
				+ "?ratec  j2:hasValue ?vratec ."
				+ "?vratec   j2:numericalValue ?rateCvalue ." // rateC

				+ "?model   j5:hasModelVariable ?ratio ." 
				+ "?ratio  a  j3:RatioCoefficient  ."
				+ "?ratio  j2:hasValue ?vratio ." 
				+ "?vratio   j2:numericalValue ?ratiovalue ." // ratio

				+ "?model   j5:hasModelVariable ?ang ." 
				+ "?ang  a  j3:Angle  ." 
				+ "?ang  j2:hasValue ?vang ."
				+ "?vang   j2:numericalValue ?anglevalue ." // angle

				+ "?model   j5:hasModelVariable ?stat ." 
				+ "?stat  a  j3:BranchStatus ." 
				+ "?stat  j2:hasValue ?vstat ."
				+ "?vstat   j2:numericalValue ?statusvalue ." // status

				+ "?model   j5:hasModelVariable ?angmin ." 
				+ "?angmin  a  j3:AngleMin  ."
				+ "?angmin  j2:hasValue ?vangmin ." 
				+ "?vangmin   j2:numericalValue ?angleminvalue ." // anglemin

				+ "?model   j5:hasModelVariable ?angmax ." 
				+ "?angmax  a  j3:AngleMax  ."
				+ "?angmax  j2:hasValue ?vangmax ." 
				+ "?vangmax   j2:numericalValue ?anglemaxvalue ." // anglemax

				+ "}";

		String busInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
				+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
				+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
				+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
				+ "SELECT ?BusNumbervalue ?typevalue ?activepowervalue ?reactivepowervalue ?Gsvalue ?Bsvalue ?areavalue ?VoltMagvalue ?VoltAnglevalue ?BaseKVvalue ?Zonevalue ?VMaxvalue ?VMinvalue "

				+ "WHERE {?entity  a  j1:BusNode  ." 
				+ "?entity   j2:isModeledBy ?model ."
				+ "?model   j5:hasModelVariable ?num ." 
				+ "?num  a  j3:BusNumber  ." 
				+ "?num  j2:hasValue ?vnum ."
				+ "?vnum   j2:numericalValue ?BusNumbervalue ." // number

				+ "?model   j5:hasModelVariable ?type ." 
				+ "?type  a  j3:BusType  ." 
				+ "?type  j2:hasValue ?vtype ."
				+ "?vtype   j2:numericalValue ?typevalue ." // type

				+ "?model   j5:hasModelVariable ?Pd ." 
				+ "?Pd  a  j3:PdBus  ." 
				+ "?Pd  j2:hasValue ?vpd ."
				+ "?vpd   j2:numericalValue ?activepowervalue ." // pd

				+ "?model   j5:hasModelVariable ?Gd ." 
				+ "?Gd  a  j3:GdBus  ." 
				+ "?Gd  j2:hasValue ?vgd ."
				+ "?vgd   j2:numericalValue ?reactivepowervalue ." // Gd

				+ "?model   j5:hasModelVariable ?Gsvar ." 
				+ "?Gsvar  a  j3:Gs  ." 
				+ "?Gsvar  j2:hasValue ?vGsvar ."
				+ "?vGsvar   j2:numericalValue ?Gsvalue ." // Gs

				+ "?model   j5:hasModelVariable ?Bsvar ." 
				+ "?Bsvar  a  j3:Bs  ." 
				+ "?Bsvar  j2:hasValue ?vBsvar ."
				+ "?vBsvar   j2:numericalValue ?Bsvalue ." // Bs

				+ "?model   j5:hasModelVariable ?areavar ." 
				+ "?areavar  a  j3:Area  ."
				+ "?areavar  j2:hasValue ?vareavar ." 
				+ "?vareavar   j2:numericalValue ?areavalue ." // area

				+ "?model   j5:hasModelVariable ?VM ." 
				+ "?VM  a  j3:Vm  ." 
				+ "?VM  j2:hasValue ?vVM ."
				+ "?vVM   j2:numericalValue ?VoltMagvalue ." // Vm

				+ "?model   j5:hasModelVariable ?VA ." 
				+ "?VA  a  j3:Va  ." 
				+ "?VA  j2:hasValue ?vVA ."
				+ "?vVA   j2:numericalValue ?VoltAnglevalue ." // Va

				+ "?model   j5:hasModelVariable ?BKV ." 
				+ "?BKV  a  j3:baseKV  ." 
				+ "?BKV  j2:hasValue ?vBKV ."
				+ "?vBKV   j2:numericalValue ?BaseKVvalue ." // Base KV

				+ "?model   j5:hasModelVariable ?zvar ." 
				+ "?zvar  a  j3:Zone  ." 
				+ "?zvar  j2:hasValue ?vzvar ."
				+ "?vzvar   j2:numericalValue ?Zonevalue ." // Zone

				+ "?model   j5:hasModelVariable ?vmaxvar ." 
				+ "?vmaxvar  a  j3:VmMax  ."
				+ "?vmaxvar  j2:hasValue ?vvmaxvar ." 
				+ "?vvmaxvar   j2:numericalValue ?VMaxvalue ." // Vmax

				+ "?model   j5:hasModelVariable ?vminvar ." 
				+ "?vminvar  a  j3:VmMin  ."
				+ "?vminvar  j2:hasValue ?vvminvar ." 
				+ "?vvminvar   j2:numericalValue ?VMinvalue ." // Vmin

				+ "}";
		
		QueryBroker broker = new QueryBroker();

		List<String[]> buslist = extractOWLinArray(model, iriofnetwork, busInfo, "bus", baseUrl);
		String content = createNewTSV(buslist, baseUrl + "/mappingforbus.csv", baseUrl + "/mappingforbus.csv");
		broker.put(baseUrl + "/bus.txt", content);

		List<String[]> genlist = extractOWLinArray(model, iriofnetwork, genInfo, "generator", baseUrl);
		content = createNewTSV(genlist, baseUrl + "/mappingforgenerator.csv", baseUrl + "/mappingforbus.csv");
		broker.put(baseUrl + "/gen.txt", content);
		
		List<String[]> gencostlist = extractOWLinArray(model, iriofnetwork, genInfocost, "generatorcost", baseUrl);
		content = createNewTSV(gencostlist, baseUrl + "/mappingforgeneratorcost.csv", baseUrl + "/mappingforbus.csv");
		broker.put(baseUrl + "/genCost.txt", content);

		List<String[]> branchlist = extractOWLinArray(model, iriofnetwork, branchInfo, "branch", baseUrl);
		content = createNewTSV(branchlist, baseUrl + "/mappingforbranch.csv", baseUrl + "/mappingforbus.csv");
		broker.put(baseUrl + "/branch.txt", content);

		String resourceDir = Util.getResourceDir(this);
		File file = new File(resourceDir + "/baseMVA.txt");
		broker.put(baseUrl + "/baseMVA.txt", file);

		File file2 = new File(AgentLocator.getNewPathToPythonScript("model", this) + "/PyPower-PF-OPF-JA-8.py");
		broker.put(baseUrl + "/PyPower-PF-OPF-JA-8.py", file2);
		
		File file3 = new File(AgentLocator.getNewPathToPythonScript("model", this) + "/runpy.bat");
		broker.put(baseUrl + "/runpy.bat", file3);
		
		
		return buslist;
	}

	public static OntModel readModelGreedy(String iriofnetwork) {
		String electricalnodeInfo = "PREFIX j1:<http://www.jparksimulator.com/ontology/ontoland/OntoLand.owl#> "
				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "SELECT ?component "
				+ "WHERE {?entity  a  j2:CompositeSystem  ." + "?entity   j2:hasSubsystem ?component ." + "}";

		QueryBroker broker = new QueryBroker();
		return broker.readModelGreedy(iriofnetwork, electricalnodeInfo);
	}
	
	public List<String[]> extractOWLinArray(OntModel model, String iriofnetwork, String busInfo, String context, String baseUrl)
			throws IOException {

		ResultSet resultSet = JenaHelper.query(model, busInfo);
		String result = JenaResultSetFormatter.convertToJSONW3CStandard(resultSet);
		String[] keys = JenaResultSetFormatter.getKeys(result);
		List<String[]> resultList = JenaResultSetFormatter.convertToListofStringArrays(result, keys);
		
		String keysConcat = MiscUtil.concat(keys, ", ");
		StringBuffer b = new StringBuffer("context = ").append(context)
				.append(", keys = ").append(keys.length).append(", ").append(keysConcat);
		logger.info(b.toString());
		
//		if ("generator".equals(context)) {
//			for (String[] current : resultList) {
//				System.out.println(MiscUtil.concat(current, ", "));
//			}
//		}
		
		if (!context.toLowerCase().contains("output")) {
			/*
			 * special case 1. for bus, the mapper contains bus number instead of iri case
			 * 2. for gencost no need mapper as it just read from the gen mapper
			 */
			IriMapper mapper = new IriMapper();
			for (int i = 0; i < resultList.size(); i++) {
				String[] current = resultList.get(i);
				String id = "" + (i + 1);
				mapper.add(current[0], id, context);
				// current[0]=id;// ??? no need to be there because it is not written in the tsv
			}

			String csv = mapper.serialize();
			QueryBroker broker = new QueryBroker();
			broker.put(baseUrl + "/mappingfor" + context + ".csv", csv);
		}

		return resultList;
	}

	public String createNewTSV(List<String[]> componentlist, String mapdir, String mapdirbus) throws IOException {
		StringWriter writer = new StringWriter();
//			try (BufferedWriter bw = new BufferedWriter(new FileWriter(tsvFileout))) {
		try (BufferedWriter bw = new BufferedWriter(writer)) {
			int line = componentlist.size();
			IriMapper map2 = new IriMapper();
			List<IriMapping> original = map2.deserialize2(mapdir);
			List<IriMapping> originalforbus = map2.deserialize2(mapdirbus);// because the gen and branch input also
																			// depends on the bus number
																			// =baseUrl+"/mappingforbus.csv"
			for (int x = 0; x < line; x++) {
				int element = componentlist.get(x).length;
				for (int e = 0; e < element; e++) {

					if (mapdir.contains("bus") && e == 0) { // first condition is when the map is bus, it takes the 1st
															// element of busnumber and use the mapped id to be written
															// in tsv
						String content = componentlist.get(x)[e];
						String content2 =map2.getIDFromMap(original, content)+"\t";
						bw.write(content2);
					} else if (!mapdir.contains("bus") && e == 0) {
						// condition when the map is not bus, it ignores the 1st element ( entities real iri is not to be
						// written in tsv)
					}

					else if (e == element - 1) {// condition for every type in the last property, it will add the \n to
												// move to new row of the tsv
						String content = componentlist.get(x)[e] + "\n";
						bw.write(content);
					} else {// the rest element index (not the first and not the last

						if (e == 1 && mapdir.contains("mappingforgenerator.csv")&& !mapdir.contains("cost.csv")) { // condition that original bus
																					// number extracted from gen owl
																					// file need to be mapped to the
																					// mapped bus number

							String ori = componentlist.get(x)[e].replace(".", "x").split("x")[0]; // remove the decimal
																									// values queried
																									// from the kb in
																									// gen in the form
																									// of string
							String content2 =map2.getIDFromMap(originalforbus, ori)+"\t";
							bw.write(content2);
						}

						else if ((mapdir.contains("branch") && e == 1) || (mapdir.contains("branch") && e == 2)) {// condition that original 2 bus numbers
																												//	 extracted from branch owl file need to 
																												//	 be mapped to the mapped bus number

							String content = componentlist.get(x)[e];
							String content2 =map2.getIDFromMap(originalforbus, content)+"\t";
							bw.write(content2);

						} else if (mapdir.contains("bus") && e == 7) {
							double pu = Double.valueOf(componentlist.get(x)[e]);

							if (pu > 1.20000) {
								String basekv = componentlist.get(x)[9];
								pu = Double.valueOf(componentlist.get(x)[e]) / Double.valueOf(basekv);
							}
							String content = pu + "\t";
							bw.write(content);

						} else { // the rest normal condition
							String content = componentlist.get(x)[e] + "\t";
							bw.write(content);
						}
					}
				}
			}

			System.out.println("Done");

		} catch (IOException e) {

			logger.error(e.getMessage(), e);
			throw new JPSRuntimeException(e.getMessage(), e);
		}

		return writer.toString();

	}
	
	public String executeSingleCommand(String targetFolder , String command) 
	{  
	 
		logger.info("In folder: " + targetFolder + " Excuted: " + command);
		Runtime rt = Runtime.getRuntime();
		Process pr = null;
		try {
			pr = rt.exec(command, null, new File(targetFolder)); // IMPORTANT: By specifying targetFolder, all the cmds will be executed within such folder.

		} catch (IOException e) {
			throw new JPSRuntimeException(e.getMessage(), e);
		}
		
				 
		BufferedReader bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = "";
		String resultString = "";
		try {
			
			while((line = bfr.readLine()) != null) {
				resultString += line;

			}
		} catch (IOException e) {
			throw new JPSRuntimeException(e.getMessage(), e);
		}
		
		return resultString; 
	}

	public void runModel(String baseUrl) throws IOException {

		// String result = PythonHelper.callPython("PyPower-PF-OPF-JA-8.py",null, this);
		// directory need to be changed soon
		// String targetFolder = AgentLocator.getNewPathToPythonScript("model", this);

		ArrayList<String> args = new ArrayList<String>();
		args.add("python");
		args.add("PyPower-PF-OPF-JA-8.py");

		//String result = CommandHelper.executeCommands(baseUrl, args);
		String startbatCommand =baseUrl+"/runpy.bat";
		String result= executeSingleCommand(baseUrl,startbatCommand);
		logger.info("final after calling: "+result);
	}

	public ArrayList<String[]> readResult(String outputfiledir, int colnum) throws IOException {
		ArrayList<String[]> entryinstance = new ArrayList<String[]>();
		
		logger.info("reading result from " + outputfiledir);
		String content = new QueryBroker().readFile(outputfiledir);
		StringReader stringreader = new StringReader(content);
		CSVReader reader = null;
		try {
			reader = new CSVReader(stringreader, '\t');
			//CSVReader reader = new CSVReader(new FileReader(outputfiledir), '\t');
			String[] record;
			while ((record = reader.readNext()) != null) {
				int element = 0;
				String[] entityline = new String[colnum];
				for (String value : record) {
	
					entityline[element] = value;
					element++;
				}
				entryinstance.add(entityline);
	
			}
		} finally {
			reader.close();
		}
		return entryinstance;
	}

	public void doConversion(OntModel model, String iriofnetwork, String baseUrl, String modeltype, List<String[]> buslist)
			throws URISyntaxException, IOException {

		String genoutputInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
				+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
				+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
				+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
				+ "SELECT ?entity ?vpg ?vqg "

				+ "WHERE {?entity  a  j1:PowerGenerator  ." 
				+ "?entity   j2:isModeledBy ?model ."
				+ "?model   j5:hasModelVariable ?Pg ." 
				+ "?Pg  a  j3:Pg  ." + "?Pg  j2:hasValue ?vpg ."// pg

				+ "?model   j5:hasModelVariable ?Qg ." 
				+ "?Qg  a  j3:Qg  ." 
				+ "?Qg  j2:hasValue ?vqg ."// qg

				+ "}";

		String busoutputInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
				+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
				+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
				+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
				+ "SELECT ?BusNumbervalue ?vpdbus ?vgdbus ?vpdgen ?vgdgen ?vVM ?vVA  "

				+ "WHERE {?entity  a  j1:BusNode  ." 
				+ "?entity   j2:isModeledBy ?model ."
				+ "?model   j5:hasModelVariable ?num ." 
				+ "?num  a  j3:BusNumber  ." + "?num  j2:hasValue ?vnum ."
				+ "?vnum   j2:numericalValue ?BusNumbervalue ." // number

				+ "?model   j5:hasModelVariable ?Pd ." 
				+ "?Pd  a  j3:PdBus  ." 
				+ "?Pd  j2:hasValue ?vpdbus ." // pd
				+ "?vpdbus  a  j5:ModelVariableSpecification  ."

				+ "?model   j5:hasModelVariable ?Gd ." 
				+ "?Gd  a  j3:GdBus  ." 
				+ "?Gd  j2:hasValue ?vgdbus ." // Gd
				+ "?vgdbus  a  j5:ModelVariableSpecification  ."

				+ "?model   j5:hasModelVariable ?Pdgen ." 
				+ "?Pdgen  a  j3:PdGen  ." 
				+ "?Pdgen  j2:hasValue ?vpdgen ." // pdgen
				+ "?vpdgen  a  j5:ModelVariableSpecification  ."

				+ "?model   j5:hasModelVariable ?Gdgen ." 
				+ "?Gdgen  a  j3:GdGen  ." 
				+ "?Gdgen  j2:hasValue ?vgdgen ." // Gd
				+ "?vgdgen  a  j5:ModelVariableSpecification  ."

				+ "?model   j5:hasModelVariable ?VM ." 
				+ "?VM  a  j3:Vm  ." 
				+ "?VM  j2:hasValue ?vVM ."// Vm
				+ "?vVM  a  j5:ModelVariableSpecification  ."

				+ "?model   j5:hasModelVariable ?VA ." 
				+ "?VA  a  j3:Va  ." 
				+ "?VA  j2:hasValue ?vVA ."// Va
				+ "?vVA  a  j5:ModelVariableSpecification  ." 
				+ "}";

		String branchoutputInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
				+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
				+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
				+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
				+ "SELECT ?entity ?vploss ?vqloss ?vpave ?vqave ?vsave "

				+ "WHERE {?entity  a  j1:UndergroundCable  ." 
				+ "?entity   j2:isModeledBy ?model ."
				+ "?model   j5:hasModelVariable ?ploss ." 
				+ "?ploss  a  j3:PLoss  ." 
				+ "?ploss  j2:hasValue ?vploss ." // ploss

				+ "?model   j5:hasModelVariable ?qloss ." 
				+ "?qloss  a  j3:QLoss  ." 
				+ "?qloss  j2:hasValue ?vqloss ." // qloss

				+ "?model   j5:hasModelVariable ?pave ." 
				+ "?pave  a  j3:PAverage  ." 
				+ "?pave  j2:hasValue ?vpave ." // pave

				+ "?model   j5:hasModelVariable ?qave ." 
				+ "?qave  a  j3:QAverage  ." 
				+ "?qave  j2:hasValue ?vqave ." // qave

				+ "?model   j5:hasModelVariable ?save ." 
				+ "?save  a  j3:SAverage  ." 
				+ "?save  j2:hasValue ?vsave ." // save

				+ "}";


		
		QueryBroker broker = new QueryBroker();

		logger.info("extractOWLinArray for bus entities");
		List<String[]> busoutputlist = extractOWLinArray(model, iriofnetwork, busoutputInfo, "output", baseUrl);
		ArrayList<String[]> resultfrommodelbus = readResult(baseUrl + "/outputBus" + modeltype.toUpperCase() + ".txt",
				7);

		int amountofbus = busoutputlist.size();
		IriMapper map2 = new IriMapper();
		List<IriMapping> originalforbus = map2.deserialize2(baseUrl + "/mappingforbus.csv");
		for (int a = 0; a < amountofbus; a++) {
			
			String currentIri = busoutputlist.get(a)[1];
			OntModel jenaOwlModel = JenaHelper.createModel(currentIri);
			DatatypeProperty numval = getNumericalValueProperty(jenaOwlModel);

			// mapping from output tab to correct owl file
			String keymapper = busoutputlist.get(a)[0];
			int amod = Integer.valueOf(map2.getIDFromMap(originalforbus, keymapper));
			Individual vpdbusout = jenaOwlModel.getIndividual(busoutputlist.get(a)[1]);
			vpdbusout.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelbus.get(amod - 1)[5]));

			Individual vgdbusout = jenaOwlModel.getIndividual(busoutputlist.get(a)[2]);
			vgdbusout.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelbus.get(amod - 1)[6]));

			Individual vpdgenout = jenaOwlModel.getIndividual(busoutputlist.get(a)[3]);
			vpdgenout.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelbus.get(amod - 1)[3]));

			Individual vgdgenout = jenaOwlModel.getIndividual(busoutputlist.get(a)[4]);
			vgdgenout.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelbus.get(amod - 1)[4]));

			Individual vVmout = jenaOwlModel.getIndividual(busoutputlist.get(a)[5]);
			double basekv = Double.valueOf(buslist.get(amod - 1)[9]);
			//System.out.println("basekv= " + basekv);
			//System.out.println("pukv= " + resultfrommodelbus.get(amod - 1)[1]);
			double originalv = basekv * Double.valueOf(resultfrommodelbus.get(amod - 1)[1]);
			
			//vVmout.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(originalv)); //if vm is in kv
			vVmout.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelbus.get(amod - 1)[1])); //if vm is in pu
			

			Individual vVaout = jenaOwlModel.getIndividual(busoutputlist.get(a)[6]);
			vVaout.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelbus.get(amod - 1)[2]));
			
			String content = JenaHelper.writeToString(jenaOwlModel);
			broker.put(currentIri, content);
		}
		
		
		
		logger.info("extractOWLinArray for generator entities");
		List<String[]> genoutputlist = extractOWLinArray(model, iriofnetwork, genoutputInfo, "output", baseUrl);
		ArrayList<String[]> resultfrommodelgen = readResult(baseUrl + "/outputGen" + modeltype.toUpperCase() + ".txt",
				3);

		IriMapper map3 = new IriMapper();
		List<IriMapping> originalforgen = map3.deserialize2(baseUrl + "/mappingforgenerator.csv");
		int amountofgen = genoutputlist.size();
		for (int a = 0; a < amountofgen; a++) {
			
			String currentIri = genoutputlist.get(a)[1];
			OntModel jenaOwlModel = JenaHelper.createModel(currentIri);
			DatatypeProperty numval = getNumericalValueProperty(jenaOwlModel);

			// mapping from output tab to correct owl file
			String keymapper = genoutputlist.get(a)[0];

			int amod = Integer.valueOf(map3.getIDFromMap(originalforgen, keymapper));

			Individual vpout = jenaOwlModel.getIndividual(genoutputlist.get(a)[1]);
			vpout.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelgen.get(amod - 1)[1]));

			Individual vqout = jenaOwlModel.getIndividual(genoutputlist.get(a)[2]);
			vqout.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelgen.get(amod - 1)[2]));

			String content = JenaHelper.writeToString(jenaOwlModel);
			broker.put(currentIri, content);
		}
		
		
		logger.info("extractOWLinArray for branch entities");
		List<String[]> branchoutputlist = extractOWLinArray(model, iriofnetwork, branchoutputInfo, "output", baseUrl);
		ArrayList<String[]> resultfrommodelbranch = readResult(
				baseUrl + "/outputBranch" + modeltype.toUpperCase() + ".txt", 6);

		IriMapper map = new IriMapper();
		List<IriMapping> originalforbranch = map.deserialize2(baseUrl + "/mappingforbranch.csv");
		int amountofbranch = branchoutputlist.size();
		for (int a = 0; a < amountofbranch; a++) {
			
			String currentIri = branchoutputlist.get(a)[1];
			OntModel jenaOwlModel = JenaHelper.createModel(currentIri);
			DatatypeProperty numval = getNumericalValueProperty(jenaOwlModel);

			// mapping from output tab to correct owl file
			String keymapper = branchoutputlist.get(a)[0];
			int amod = Integer.valueOf(map.getIDFromMap(originalforbranch, keymapper));

			Individual vploss = jenaOwlModel.getIndividual(branchoutputlist.get(a)[1]);
			vploss.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelbranch.get(amod - 1)[1]));

			Individual vqloss = jenaOwlModel.getIndividual(branchoutputlist.get(a)[2]);
			vqloss.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelbranch.get(amod - 1)[2]));

			Individual vpave = jenaOwlModel.getIndividual(branchoutputlist.get(a)[3]);
			vpave.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelbranch.get(amod - 1)[3]));

			Individual vqave = jenaOwlModel.getIndividual(branchoutputlist.get(a)[4]);
			vqave.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelbranch.get(amod - 1)[4]));

			Individual vsave = jenaOwlModel.getIndividual(branchoutputlist.get(a)[5]);
			vsave.setPropertyValue(numval, jenaOwlModel.createTypedLiteral(resultfrommodelbranch.get(amod - 1)[5]));
			
			String content = JenaHelper.writeToString(jenaOwlModel);
			broker.put(currentIri, content);
		}
	}
}
