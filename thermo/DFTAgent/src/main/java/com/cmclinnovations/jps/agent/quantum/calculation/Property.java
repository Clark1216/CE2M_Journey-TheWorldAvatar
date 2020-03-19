package com.cmclinnovations.jps.agent.quantum.calculation;

import java.io.File;

/**
 * This enumerated list defines the name of important properties of</br>
 * DFT Agent. Some example properties are:</br>
 * - the name of the agent class</br>
 * - the name of the workspace folder on the machine where the agent runs</br>
 * - the name of the workspace folder on HPC where DFT calculations run</br>
 *  
 * @author Feroz Farazi(msff2@cam.ac.uk)
 *
 */
public enum Property {

	AGENT_CLASS("DFTAgent"),
	AGENT_JOB_SPACE(AGENT_CLASS.getPropertyName()),
	AGENT_WORKSPACE_DIR(System.getProperty("user.home")),
	HPC_CAMBRIDGE_ADDRESS("login-skylake.hpc.cam.ac.uk"),
	HOST_VIENNA_ADDRESS("vienna.cheng.cam.ac.uk"),
	INPUT_FILE_EXTENSION(".com"),
	MAX_NUMBER_OF_JOBS(10),
	CHK_POINT_FILE_EXTENSION(".chk"),
	STATUS_FILE_EXTENSION(".txt"),
	JSON_FILE_EXTENSION(".json"),
	STATUS_FILE_NAME("status"),
	JSON_INPUT_FILE_NAME("input"),
	SLURM_SCRIPT_FILE_NAME("Slurm.sh"),
	JOB_NO_OF_CORES_PREFIX("%nprocshared="),
	JOB_MEMORY_PREFIX("%mem="),
	JOB_MEMORY_UNITS("GB"),
	JOB_CHK_POINT_FILE_ADDRESS_PART("%Chk=".concat(HPC_CAMBRIDGE_ADDRESS.getPropertyName())),
	SPECIES_CHARGE_ZERO("0"),
	SPECIES_MULTIPLICITY("1"),
	JOB_PRINT_DIRECTIVE("#n"),
	RDF4J_SERVER_URL_FOR_LOCALHOST("http://localhost:8080/rdf4j-server/"),
	FUSAKI_URL_FOR_WORLD_AVATAR("http://www.theworldavatar.com/damecoolquestion/agents/query?query="),
	RDF4J_ONTOSPECIES_REPOSITORY_ID("ontospecies"),
	PREFIX_BINDING_ONTOSPECIES("PREFIX OntoSpecies: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#> \n"),
	PREFIX_BINDING_RDFS("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"),
	PREFIX_BINDING_RDF("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"),
	PREFIX_BINDING_MSM("PREFIX msm: <http://www.theworldavatar.com/ontology/ontoagent/MSM.owl#>"),
	PREFIX_BINDING_RAM("PREFIX ram: <http://cookingbigdata.com/linkeddata/ccinstances#>"),
	PREFIX_MSM("msm"),
	PREFIX_RAM("ram"),
	DFT_AGENT_IRI("<http://www.theworldavatar.com/kb/agents/Service__DFT.owl#Service>");
	
	private String propertyName;
	private int value;
	private Property(String propertyName){
		this.propertyName = propertyName;
	}
	
	public String getPropertyName(){
		return propertyName;
	}
	
	private Property(final int newValue){
		value = newValue;
	}
	
	public int getValue(){
		return value;
	}
}
