package uk.ac.cam.ceb.como.paper.enthalpy.data.preprocessing;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.solver.reactiontype.ISDReactionType;
import uk.ac.cam.ceb.como.paper.enthalpy.cross_validation.LeaveOneOutCrossValidationAlgorithm;
import uk.ac.cam.ceb.como.paper.enthalpy.utils.FolderUtils;

public class HCOISDReaction5RadicalTest {
	String folderName;
	String validTestResults;
	
	@Before
	public void generateTestResults() throws Exception{
		validTestResults = Folder.VALID_TEST_RESULT_ISD_HCO_115.getFolderName();
		folderName = new FolderUtils().generateUniqueFolderName(Folder.ISD_HCO_115.getFolderName());
		LeaveOneOutCrossValidationAlgorithm leaveOneOutCrossValidationAlgorithm = new LeaveOneOutCrossValidationAlgorithm();
		ISDReactionType isdReactionTypePreProcessing = new ISDReactionType();
		Files.createDirectories(Paths.get(Folder.REACTIONS_HCO_ISD.getFolderName()+folderName));
		leaveOneOutCrossValidationAlgorithm.preProcessingAndInitialDataAnalysis(true, Folder.COMPOUNDS_REF_HCO.getFolderName(), Folder.REF_POOL_HCO.getFolderName(), Folder.REACTIONS_HCO_ISD.getFolderName()+folderName, Utils.ctrRuns, Utils.ctrRes, Utils.ctrRadicals_5, isdReactionTypePreProcessing);
	}
	
	@Test
	public void comparisonTest() {
		HashMap<String, String> resultFileMap = (new Utils()).generatePairedFileList(folderName, Folder.REACTIONS_HCO_ISD.getFolderName(), validTestResults);
		for(String srcFilePath:resultFileMap.keySet()){
			File sourceFile = new File(srcFilePath);
			File targetFile = new File (resultFileMap.get(srcFilePath));
			(new Utils()).compareFiles(srcFilePath, sourceFile, targetFile, resultFileMap);
		}
	}
}
