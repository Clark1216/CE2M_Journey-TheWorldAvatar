package uk.ac.cam.cares.jps.agent.file_management.marshallr.sensana;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import uk.ac.cam.cares.jps.agent.file_management.marshallr.IMoDSMarshaller;
import uk.ac.cam.cares.jps.agent.file_management.marshallr.MoDSFileManagement;
import uk.ac.cam.cares.jps.agent.json.parser.JSonRequestParser;
import uk.ac.cam.cares.jps.agent.mechanism.sensana.MoDSSensAnaAgentException;
import uk.ac.cam.cares.jps.agent.mechanism.sensana.Utils;

public class MoDSFileMagtSensAna extends MoDSFileManagement {
	public static void main(String[] args) throws IOException, MoDSSensAnaAgentException {
		MoDSFileMagtSensAna fileMagt = new MoDSFileMagtSensAna();
		
		String jsonString = "{\"json\":{\"ontochemexpIRI\":{\"ignitionDelay\":[\"https://como.ceb.cam.ac.uk/kb/ontochemexp/x00001700.owl#Experiment_404313416274000\",\"https://como.ceb.cam.ac.uk/kb/ontochemexp/x00001701.owl#Experiment_404313804188800\",\"https://como.ceb.cam.ac.uk/kb/ontochemexp/x00001702.owl#Experiment_404313946760600\"]},\"ontokinIRI\":{\"mechanism\":\"http://www.theworldavatar.com/kb/ontokin/pode_mechanism_original.owl#ReactionMechanism_73656018231261\"}}}";
		String jobFolderName = "login-cpu.hpc.cam.ac.uk_"+Utils.getTimeStamp()+"_SensAna_MaxDtDt";
		
		fileMagt.createMoDSJob(jsonString, jobFolderName);
	}
	
	@Override
	public String createMoDSJob(String jsonString, String jobFolderName) throws IOException, MoDSSensAnaAgentException {
		List<String> ignitionDelayExpIRI = JSonRequestParser.getOntoChemExpIgnitionDelayIRI(jsonString);
		String mechanismIRI = JSonRequestParser.getOntoKinMechanismIRI(jsonString);
		List<String> reactionIRIList = new ArrayList<>(); // not in use, just for the arg place
		LinkedHashMap<String, String> ignDelayOption = new LinkedHashMap<String, String>();
		ignDelayOption.put("method", JSonRequestParser.getIgnDelayMethod(jsonString));
		ignDelayOption.put("species", JSonRequestParser.getIgnDelaySpecies(jsonString));
		
		IMoDSMarshaller iMoDSMarshaller = new MoDSMarshallerSensAna();
		iMoDSMarshaller.initialise(jobFolderName);
		iMoDSMarshaller.plugInKinetics(ignitionDelayExpIRI, mechanismIRI, reactionIRIList, ignDelayOption);
		String jobFolderPath = iMoDSMarshaller.marshall();
		
		return jobFolderPath;
	}
	
}
