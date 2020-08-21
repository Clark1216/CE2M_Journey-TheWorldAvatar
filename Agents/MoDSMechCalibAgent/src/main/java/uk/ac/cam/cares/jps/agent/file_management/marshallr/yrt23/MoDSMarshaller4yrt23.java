package uk.ac.cam.cares.jps.agent.file_management.marshallr.yrt23;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import uk.ac.cam.cares.jps.agent.file_management.marshallr.ExecutableModel;
import uk.ac.cam.cares.jps.agent.file_management.marshallr.MoDSMarshaller;
import uk.ac.cam.cares.jps.agent.file_management.marshallr.ModelCanteraLFS;
import uk.ac.cam.cares.jps.agent.file_management.marshallr.ModelKineticsSRM;
import uk.ac.cam.cares.jps.agent.mechanism.calibration.MoDSMechCalibAgentException;

public class MoDSMarshaller4yrt23 extends MoDSMarshaller {
	@Override
	public void plugInKinetics(List<String> experimentIRI, String mechanismIRI, List<String> reactionIRIList, String otherOptions) throws IOException, MoDSMechCalibAgentException {
		// TODO Auto-generated method stub
		ModelKineticsSRM4yrt23 kineticsSRM = new ModelKineticsSRM4yrt23();
		ExecutableModel exeModel = kineticsSRM.formExecutableModel(experimentIRI, mechanismIRI, reactionIRIList);
		kineticsSRM.formFiles(exeModel, otherOptions);
		kineticsSRM.setUpMoDS();
		kineticsSRM.placeScript();
	}

	@Override
	public void plugInCantera(List<String> experimentIRI, String mechanismIRI, List<String> reactionIRIList, String otherOptions) throws IOException, MoDSMechCalibAgentException {
		// TODO Auto-generated method stub
		ModelCanteraLFS4yrt23 canteraLFS = new ModelCanteraLFS4yrt23();
		ExecutableModel exeModel = canteraLFS.formExecutableModel(experimentIRI, mechanismIRI, reactionIRIList);
		canteraLFS.formFiles(exeModel, otherOptions);
		canteraLFS.setUpMoDS();
		canteraLFS.placeScript();
	}
}
