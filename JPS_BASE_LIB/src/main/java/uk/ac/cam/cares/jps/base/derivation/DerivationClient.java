package uk.ac.cam.cares.jps.base.derivation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.interfaces.StoreClientInterface;

/**
 * this class acts as an interface to create and deal with derived quantities
 * @author Kok Foong Lee
 * @author Jiaru Bai
 *
 */
public class DerivationClient {
	// input and output of agents need to be a JSONArray consisting a list of IRIs with the do
	public static final String AGENT_INPUT_KEY = "agent_input";
	public static final String AGENT_OUTPUT_KEY = "agent_output";
	public static final String DERIVATION_KEY = "derivation";
	// defines the endpoint DerivedQuantityClient should act on
	StoreClientInterface kbClient;
	DerivationSparql sparqlClient;
	boolean upstreamDerivationPendingUpdate;
	
     /**
     * Logger for error output.
     */
    private static final Logger LOGGER = LogManager.getLogger(DerivationClient.class);
    
    public DerivationClient(StoreClientInterface kbClient) {
    	this.kbClient = kbClient;
    	this.sparqlClient = new DerivationSparql(kbClient);
    	
    }
    
    /**
     * This creates a new derived instance and adds the following statements
     * <entity> <belongsTo> <derived>, <derived> <isDerivedUsing> <agentIRI>, <agentIRI> <hasHttpUrl> <agentURL>, <derived> <isDerivedFrom> <inputsIRI>
     * Use this for instances that get replaced by agents
     * @param derivedQuantityIRI
     * @param inputsIRI
     * @param agentIRI
     */
    public String createDerivation(List<String> entities, String agentIRI, String agentURL, List<String> inputsIRI) {
    	String createdDerivation = this.sparqlClient.createDerivation(entities, agentIRI, agentURL, inputsIRI);
    	this.sparqlClient.addTimeInstance(createdDerivation);
    	LOGGER.info("Instantiated derivation <" + createdDerivation + ">");
    	LOGGER.debug("<" + entities + "> belongsTo <" + createdDerivation + ">");
    	LOGGER.debug("<" + createdDerivation + "> isDerivedFrom <" + inputsIRI + ">");
    	LOGGER.debug("<" + createdDerivation + "> isDerivedUsing <" + agentIRI + "> located at " + agentURL);
    	return createdDerivation;
    }
    
    public List<String> bulkCreateDerivations(List<List<String>> entitiesList, List<String> agentIRIList, List<String> agentURLList, List<List<String>> inputsList) {
    	List<String> derivations = this.sparqlClient.bulkCreateDerivations(entitiesList, agentIRIList, agentURLList, inputsList);
    	LOGGER.info("Instantiated derivations " + derivations);
    	
    	// add timestamp to each derivation
    	this.sparqlClient.addTimeInstance(derivations);
    	
    	return derivations;
    }
    
	/**
	 * This method creates a new derived instance and adds the following statements
	 * <entity> <belongsTo> <derived>, <derived> <isDerivedUsing> <agentIRI>, <derived> <isDerivedFrom> <inputsIRI>
     * Use this for instances that get replaced by agents, also when the information about agent exists already
	 * @param entities
	 * @param agentIRI
	 * @param inputsIRI
	 * @return
	 */
	public String createDerivation(List<String> entities, String agentIRI, List<String> inputsIRI) {
		String createdDerivation = this.sparqlClient.createDerivation(entities, agentIRI, inputsIRI);
		this.sparqlClient.addTimeInstance(createdDerivation);
		LOGGER.info("Instantiated derivation for asynchronous operation <" + createdDerivation + ">");
		LOGGER.debug("<" + entities + "> belongsTo <" + createdDerivation + ">");
		LOGGER.debug("<" + createdDerivation + "> isDerivedFrom <" + inputsIRI + ">");
		LOGGER.debug("<" + createdDerivation + "> isDerivedUsing <" + agentIRI + ">");
		return createdDerivation;
	}

    /**
     * use this if all the agent does to the instance is appending time series data, entity do not get replaced
     * @param entity
     * @param agentIRI
     * @param agentURL
     * @param inputsIRI
     */
    public String createDerivationWithTimeSeries(List<String> entities, String agentIRI, String agentURL, List<String> inputsIRI) {
    	String createdDerivation = this.sparqlClient.createDerivationWithTimeSeries(entities, agentIRI, agentURL, inputsIRI);
    	this.sparqlClient.addTimeInstance(createdDerivation);
    	LOGGER.info("Instantiated derivation with time series <" + createdDerivation + ">");
    	LOGGER.debug("<" + entities + "> belongsTo <" + createdDerivation + ">");
    	LOGGER.debug("<" + createdDerivation + "> isDerivedFrom <" + inputsIRI + ">");
    	LOGGER.debug("<" + createdDerivation + "> isDerivedUsing <" + agentIRI + "> located at " + agentURL);
    	return createdDerivation;
    }
    
    public List<String> bulkCreateDerivationsWithTimeSeries(List<List<String>> entitiesList, List<String> agentIRIList, List<String> agentURLList, List<List<String>> inputsList) {
    	List<String> derivations = this.sparqlClient.bulkCreateDerivationsWithTimeSeries(entitiesList, agentIRIList, agentURLList, inputsList);
    	LOGGER.info("Instantiated derivations with time series " + derivations);
    	
    	// add timestamp to each derivation
    	this.sparqlClient.addTimeInstance(derivations);
    	
    	return derivations;
    }
    
    /**
     * This method creates a new asynchronous derived instance and adds the following statements
	 * <entity> <belongsTo> <derived>, <derived> <isDerivedUsing> <agentIRI>, <derived> <isDerivedFrom> <inputsIRI>
     * Use this for asynchronous instances that get replaced by agents, also when the information about agent exists already
     * @param entities
     * @param agentIRI
     * @param inputsIRI
     * @return
     */
    public String createAsynDerivation(List<String> entities, String agentIRI, List<String> inputsIRI) {
    	String createdDerivation = this.sparqlClient.createDerivationAsyn(entities, agentIRI, inputsIRI);
    	this.sparqlClient.addTimeInstance(createdDerivation);
    	LOGGER.info("Instantiated asynchronous derivation <" + createdDerivation + ">");
    	LOGGER.debug("<" + entities + "> belongsTo <" + createdDerivation + ">");
    	LOGGER.debug("<" + createdDerivation + "> isDerivedFrom <" + inputsIRI + ">");
    	LOGGER.debug("<" + createdDerivation + "> isDerivedUsing <" + agentIRI + ">");
    	return createdDerivation;
    }
    
    /**
     * adds a timestamp to your input following the w3c standard for unix timestamp https://www.w3.org/TR/owl-time/
     * <entity> <hasTime> <time>, <time> <numericPosition> 123
     * @param entity
     */
    public void addTimeInstance(String entity) {
    	this.sparqlClient.addTimeInstance(entity);
    	LOGGER.info("Added timestamp to <" + entity + ">");
    }
    
    /**
     * same method as above but in bulk
     * @param entities
     */
    public void addTimeInstance(List<String> entities) {
    	this.sparqlClient.addTimeInstance(entities);
    	LOGGER.info("Added timestamps to <" + entities + ">");
    }
    
    /**
     * removes time instance added using addTimeInstance
     * @param entity
     */
    public void removeTimeInstance(String entity) {
    	this.sparqlClient.removeTimeInstance(entity);
    	LOGGER.info("Removed timestamp for <" + entity + ">");
    }
    
    /**
     * you may want to use this to update an input's timestamp, the DerivationClient does not deal with inputs directly
     */
    public void updateTimestamp(String entity) {
		if (this.sparqlClient.hasBelongsTo(entity)) {
			String derivation = getDerivationOf(entity);
			LOGGER.info("<" + entity + "> has a derivation instance attached, timestamp of the derivation will get updated");
			this.sparqlClient.updateTimeStamp(derivation);
			LOGGER.info("Updated timestamp of <" + derivation + ">");
		} else {
			this.sparqlClient.updateTimeStamp(entity);
			LOGGER.info("Updated timestamp of <" + entity + ">");
		}
    }
    
    /**
	 * makes sure the given instance is up-to-date by comparing its timestamp to all of its inputs
	 * the input, derivedIRI, should have an rdf:type DerivedQuantity or DerivedQuantityWithTimeSeries
	 * @param kbClient
	 * @param derivedIRI
	 */
	public void updateDerivation(String derivedIRI) {
		// the graph object makes sure that there is no circular dependency
		DirectedAcyclicGraph<String,DefaultEdge> graph = new DirectedAcyclicGraph<String,DefaultEdge>(DefaultEdge.class);
		try {
			updateDerivation(derivedIRI, graph);
		} catch (Exception e) {
			LOGGER.fatal(e.getMessage());
			throw new JPSRuntimeException(e);
		}
	}
	
	/**
	 * This method checks and makes sure the derived instance is up-to-date by comparing the timestamp
	 * of the derivation to all of its inputs.
	 * @param derivationIRI
	 */
	public void updateDerivationAsyn(String derivationIRI) {
		// the graph object makes sure that there is no circular dependency
		DirectedAcyclicGraph<String,DefaultEdge> graph = new DirectedAcyclicGraph<String,DefaultEdge>(DefaultEdge.class);
		try {
			// the flag upstreamDerivationRequested is set as false by default
			upstreamDerivationPendingUpdate = false;
			updateDerivationAsyn(derivationIRI, graph);
		} catch (Exception e) {
			LOGGER.fatal(e.getMessage());
			throw new JPSRuntimeException(e);
		}
	}
	
	/**
	 * This checks for any circular dependency and ensures that all the linked inputs have a suitable timestamp attached
	 * This does not check for everything, e.g. instances having appropriate rdf:types, and the agent design
	 * @param derived
	 * @return
	 */
	public boolean validateDerivation(String derived) {
		// keep track of quantities to avoid circular dependencies
		DirectedAcyclicGraph<String,DefaultEdge> graph = new DirectedAcyclicGraph<String,DefaultEdge>(DefaultEdge.class);
        
		try {
			validateDerivation(derived, graph);
			return true;
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
		    throw new JPSRuntimeException(e);
		}
	}
	
	/**
	 * This method retrieves the agent inputs that mapped against the OntoAgent I/O signature.
	 * @param derivation
	 * @param agentIRI
	 * @return
	 */
	public JSONObject retrieveAgentInputs(String derivation, String agentIRI) {
		JSONObject agentInputs = new JSONObject();
		agentInputs.put(AGENT_INPUT_KEY, this.sparqlClient.getInputsMapToAgent(derivation, agentIRI));
		return agentInputs;
	}
	
	/**
	 * clears all derivations from the kg, including timestamps of inputs
	 */
	public void dropAllDerivationsAndTimestamps() {
		// get all inputs with a time stamp
		List<String> inputs = this.sparqlClient.getInputsWithTimestamps();
		for (String input : inputs) {
			this.sparqlClient.removeTimeInstance(input);
		}
		this.sparqlClient.dropAllDerivations();
	}
	
	/**
	 * This method updates the status of the Derivation at job completion: the status of the derivation will be marked as "Finished" and the newDerivedIRI will be attached to the status. 
	 * @param derivation
	 * @param newDerivedIRI
	 */
	public void updateStatusAtJobCompletion(String derivation, List<String> newDerivedIRI) {
		// mark as Finished
		String statusIRI = this.sparqlClient.markAsFinished(derivation);
		// add newDerivedIRI to Finished status
		this.sparqlClient.addNewDerivedIRIToFinishedStatus(statusIRI, newDerivedIRI);
	}
	
	/**
	 * This method checks at the status "PendingUpdate" to decide whether change it to "Requested".
	 * @param derivation
	 */
	public void checkAtPendingUpdate(String derivation) {
		// assume this derivation can be updated now
		boolean toRequest = true;
		
		// get a list of previous derivations
		List<String> previousDerivations = this.sparqlClient.getPreviousDerivations(derivation);
		
		// for each of the derivation, check if they are up-to-date, and no status associated
		for (String dev : previousDerivations) {
			List<String> inputs = this.sparqlClient.getInputs(dev);
			if (isOutOfDate(dev, inputs) || hasStatus(dev)) {
				toRequest = false;
				break;
			}
		}
		
		// only when flag toRequest is not changed during checking, mark as Requested
		if (toRequest) {
			this.sparqlClient.markAsRequested(derivation);			
		}
	}
	
	/**
	 * This method cleans up the "Finished" derivation by reconnecting the new generated derived IRI with derivations and deleting all status. 
	 * @param derivation
	 */
	public void cleanUpFinishedDerivationUpdate(String derivation) {
		// this method largely follows the part of code after obtaining the response from Agent in method updateDerivation(String instance, DirectedAcyclicGraph<String,DefaultEdge> graph)
		// the additional part in this method (compared to the above mentioned method) is: (1) how we get newDerivedIRI; (2) we delete all triples connected to the status of the derivation
		// in the future development, there's a potential these two methods can be merged into one
		
		// (1) get newDerivedIRI
		List<String> newEntities = this.sparqlClient.getNewDerivedIRI(derivation);
		
		// get all the other entities linked to the derived quantity, to be deleted and replaced with new entities
		// query for ?x <belongsTo> <instance>
		List<String> entities = this.sparqlClient.getDerivedEntities(derivation);
		
		// check if any of the old entities is an input for another derived quantity
		// query ?x <isDerivedFrom> <entity>, <entity> a ?y
		// where ?x = a derived instance, ?y = class of entity
		// index 0 = derivedIRIs list, index 1 = type IRI list
		List<List<String>> derivedAndType = this.sparqlClient.getIsDerivedFromEntities(entities);
		
		// delete old instances
		this.sparqlClient.deleteInstances(entities);
		LOGGER.debug("Deleted old instances: " + Arrays.asList(entities));
		
		// link new entities to derived instance, adding ?x <belongsTo> <instance>
		this.sparqlClient.addNewEntitiesToDerived(derivation, newEntities);
		LOGGER.debug("Added new instances <" + newEntities + "> to the derivation <" + derivation + ">");
		
		if (derivedAndType.get(0).size() > 0) {
			LOGGER.debug("This derivation contains at least one entity which is an input to another derivation");
			LOGGER.debug("Relinking new instance(s) to the derivation by matching their rdf:type");
			// after deleting the old entity, we need to make sure that it remains linked to the appropriate derived instance
			List<String> classOfNewEntities = this.sparqlClient.getInstanceClass(newEntities);
			
			// look for the entity with the same rdf:type that we need to reconnect
			List<String> oldDerivedList = derivedAndType.get(0);
			List<String> oldTypeList = derivedAndType.get(1);
	
			// for each instance in the old derived instance that is connected to another derived instance, reconnect it
			for (int i = 0; i < oldDerivedList.size(); i++) {
				LOGGER.debug("Searching within <" + newEntities + "> with rdf:type <" + oldTypeList.get(i) + ">");
				// index in the new array with the matching type
				Integer matchingIndex = null;
				for (int j = 0; j < classOfNewEntities.size(); j++) {
					if (classOfNewEntities.get(j).contentEquals(oldTypeList.get(i))) {
						if (matchingIndex != null) {
							throw new JPSRuntimeException("Duplicate rdf:type found within output, the DerivationClient does not support this");
						}
						matchingIndex = j;
					}
				}
				if (matchingIndex == null) {
					String reconnectError = "Unable to find an instance with the same rdf:type to reconnect to " + oldDerivedList.get(i);
					throw new JPSRuntimeException(reconnectError);
				}
			    // reconnect
				this.sparqlClient.reconnectInputToDerived(newEntities.get(matchingIndex), oldDerivedList.get(i));
			}
		}
		
		// (2) delete all triples connected to status of the derivation
		this.sparqlClient.deleteStatus(derivation);
		
		// if there are no errors, assume update is successful
		this.sparqlClient.updateTimeStamp(derivation);
		LOGGER.info("Updated timestamp of <" + derivation + ">");
	}
	
	/**
	 * Checks if the derivation is an instance of DerivationAsyn.
	 * @param derivation
	 * @return
	 */
	public boolean isDerivedAsynchronous(String derivation) {
		return this.sparqlClient.isDerivedAsynchronous(derivation);
	}
	/**
	 * returns the derivation instance linked to this entity
	 * @param entity
	 * @return
	 */
	public String getDerivationOf(String entity) {
		return this.sparqlClient.getDerivedIRI(entity);
	}
	
	/**
	 * Checks if the derivation status is "PendingUpdate".
	 * @param derivation
	 * @return
	 */
	public boolean isPendingUpdate(String derivation) {
		return this.sparqlClient.isPendingUpdate(derivation);
	}

	/**
	 * Checks if the derivation status is "Requested".
	 * @param derivation
	 * @return
	 */
	public boolean isRequested(String derivation) {
		return this.sparqlClient.isRequested(derivation);
	}

	/**
	 * Checks if the derivation status is "InProgress".
	 * @param derivation
	 * @return
	 */
	public boolean isInProgress(String derivation) {
		return this.sparqlClient.isInProgress(derivation);
	}

	/**
	 * Checks if the derivation status is "Finished".
	 * @param derivation
	 * @return
	 */
	public boolean isFinished(String derivation) {
		return this.sparqlClient.isFinished(derivation);
	}

	/**
	 * Marks the derivation status as "PendingUpdate".
	 * @param derivation
	 */
	public void markAsPendingUpdate(String derivation) {
		this.sparqlClient.markAsPendingUpdate(derivation);
	}
	
	/**
	 * Marks the derivation status as "Requested".
	 * @param derivation
	 */
	public void markAsRequested(String derivation) {
		this.sparqlClient.markAsRequested(derivation);
	}

	/**
	 * Marks the derivation status as "InProgress".
	 * @param derivation
	 */
	public void markAsInProgress(String derivation) {
		this.sparqlClient.markAsInProgress(derivation);
	}

	/**
	 * Marks the derivation status as "Finished".
	 * @param derivation
	 */
	public void markAsFinished(String derivation) {
		this.sparqlClient.markAsFinished(derivation);
	}

	/**
	 * Checks if a derivation has status.
	 * @param derivation
	 * @return
	 */
	public boolean hasStatus(String derivation) {
		return this.sparqlClient.hasStatus(derivation);
	}

	/**
	 * Gets the status of a derivation.
	 * @param derivation
	 * @return
	 */
	public String getStatus(String derivation) {
		return this.sparqlClient.getStatus(derivation);
	}

	/**
	 * Gets the new derived IRI at derivation update (job) completion.
	 * @param derivation
	 * @return
	 */
	public List<String> getNewDerivedIRI(String derivation) {
		return this.sparqlClient.getNewDerivedIRI(derivation);
	}

	/**
	 * Gets the agent IRI that is used to update the derivation.
	 * @param derivedQuantity
	 * @return
	 */
	public String getAgentUrl(String derivedQuantity) {
		return this.sparqlClient.getAgentUrl(derivedQuantity);
	}

	/**
	 * Gets a list of derivations that is derived using a given agent IRI.
	 * @param agentIRI
	 * @return
	 */
	public List<String> getDerivations(String agentIRI) {
		return this.sparqlClient.getDerivations(agentIRI);
	}
	
	/**
	 * All private functions below
	 */
	
	/**
	 * This method marks the derivation as "Requested" when it detects a derivation is outdated. 
	 * @param instance
	 * @param graph
	 */
	private void updateDerivationAsyn(String instance, DirectedAcyclicGraph<String, DefaultEdge> graph) {
		// this method follows the first a few steps of method updateDerivation(String instance, DirectedAcyclicGraph<String, DefaultEdge> graph)
		// TODO in future development, ideally these two method should be merged into the same method?
		List<String> inputsAndDerived = this.sparqlClient.getInputsAndDerived(instance);
		
		if (!graph.containsVertex(instance)) {
			graph.addVertex(instance);
		}
		
		for (String input : inputsAndDerived) {
			if (!graph.containsVertex(input)) {
				graph.addVertex(input);
			}
			graph.addEdge(instance, input);
			updateDerivationAsyn(input, graph);
		}
		
		List<String> inputs = this.sparqlClient.getInputs(instance);
		if (inputs.size() > 0) {
			// here only derivation instance will enter, first we check if it is an asynchronous derivation
			if (isDerivedAsynchronous(instance)) {
				if (isOutOfDate(instance, inputs)) {
					if (!this.sparqlClient.hasStatus(instance)) {
						this.sparqlClient.markAsPendingUpdate(instance);
					}
					upstreamDerivationPendingUpdate = true;
				} else {
					if (upstreamDerivationPendingUpdate && !this.sparqlClient.hasStatus(instance) && !this.sparqlClient.hasUpstreamDerivation(instance)) {
						this.sparqlClient.markAsPendingUpdate(instance);
					}
				}
//				// then we check if this derivation is part of a chain and if any upstream one is already requested
//				// as the upstreamDerivationRequested flag is false by default, the code will directly go to the else part
//				if (upstreamDerivationRequested) {
//					// it is likely the flag is up but we are at the start of the chain
//					// this will happen if one derivation has two downstream branches
//					if (this.sparqlClient.isFirstDerivation(instance)) {
//						if (isOutOfDate(instance, inputs) && !this.sparqlClient.hasStatus(instance)) {
//							this.sparqlClient.markAsRequested(instance);
//						}
//					} else {
//						// here it means one of the upstream derivation is already requested
//						// so we check if the current derivation has status already
//						// if there is status already, the derivation framework just pass
//						// otherwise, we mark it as PendingUpdate
//						if (!this.sparqlClient.hasStatus(instance)) {
//							LOGGER.info("Pending to update <" + instance + ">, marked as PendingUpdate");
//							this.sparqlClient.markAsPendingUpdate(instance);
//						}						
//					}
//				} else {
//					// only if all upstream ones are up-to-date, we check if the current derivation is out-of-date
//					// this applies to the first derivation in the chain by default
//					if (isOutOfDate(instance,inputs)) {
//						// if the Derivation is out of date, the first thing we do is checking its status
//						if (!this.sparqlClient.hasStatus(instance)) {
//							// if there's no status, then mark as requested - a job need to be started to update the derivation
//							LOGGER.info("Updating <" + instance + ">, marked as Requested");
//							LOGGER.debug("<" + instance + "> is out-of-date when compared to <" + inputs + ">");
//							this.sparqlClient.markAsRequested(instance);
//						} else {
//							// for now, if there's any status, the derivation framework just pass
//						}
//						// as at this point, this should be the first derivation in the chain to be updated
//						// we set the flag to remind all downstream derivations
//						upstreamDerivationRequested = true;
//					} else {
//						// if the derivation is up to date, then delete <hasStatus> <Status> if applies
//						// as in theory, here flag upstreamDerivationRequested is false, so this derivation remains up-to-date
//						if (this.sparqlClient.hasStatus(instance)) {
//							this.sparqlClient.deleteStatus(instance);
//						}
//					}
//				}
			}
		}
	}
	
	/**
	 * called by the public function updateInstance
	 * @param instance
	 * @param derivedList
	 */
	private void updateDerivation(String instance, DirectedAcyclicGraph<String,DefaultEdge> graph) {
		// this will query the direct inputs, as well as the derived instance of any of the inputs if the input is part of a derived instance
		List<String> inputsAndDerived = this.sparqlClient.getInputsAndDerived(instance);

		if (!graph.containsVertex(instance)) {
			graph.addVertex(instance);
		}
		
		for (String input : inputsAndDerived) {
			if (!graph.containsVertex(input)) {
				graph.addVertex(input);
			}
			graph.addEdge(instance, input); // will throw an error here if there is circular dependency
			updateDerivation(input, graph);
		}

		// inputs required by the agent
		List<String> inputs = this.sparqlClient.getInputs(instance);
		if (inputs.size() > 0) {
			// at this point, "instance" is a derived instance for sure, any other instances will not go through this code
			// getInputs queries for <instance> <isDerivedFrom> ?x
			if (isOutOfDate(instance,inputs)) {
				LOGGER.info("Updating <" + instance + ">");
				LOGGER.debug("<" + instance + "> is out-of-date when compared to <" + inputs + ">");
				// calling agent to create a new instance
				String agentURL = this.sparqlClient.getAgentUrl(instance);
				JSONObject requestParams = new JSONObject();
				JSONArray iris = new JSONArray(inputs);
				requestParams.put(AGENT_INPUT_KEY, iris);
				requestParams.put(DERIVATION_KEY, instance);
				
				LOGGER.debug("Updating <" + instance + "> using agent at <" + agentURL + "> with http request " + requestParams);
				String response = AgentCaller.executeGetWithURLAndJSON(agentURL, requestParams.toString());
				
				LOGGER.debug("Obtained http response from agent: " + response);
				
				// if it is a derived quantity with time series, there will be no changes to the instances
				if (!this.sparqlClient.isDerivedWithTimeSeries(instance)) {
					// collect new instances created by agent
					List<String> newEntities = new JSONObject(response).getJSONArray(AGENT_OUTPUT_KEY).toList()
							.stream().map(iri -> (String) iri).collect(Collectors.toList());

					// get all the other entities linked to the derived quantity, to be deleted and replaced with new entities
					// query for ?x <belongsTo> <instance>
					List<String> entities = this.sparqlClient.getDerivedEntities(instance);
					
					// check if any of the old entities is an input for another derived quantity
					// query ?x <isDerivedFrom> <entity>, <entity> a ?y
					// where ?x = a derived instance, ?y = class of entity
					// index 0 = derivedIRIs list, index 1 = type IRI list
					List<List<String>> derivedAndType = this.sparqlClient.getIsDerivedFromEntities(entities);
					
					// delete old instances
					this.sparqlClient.deleteInstances(entities);
					LOGGER.debug("Deleted old instances: " + Arrays.asList(entities));
					
					// link new entities to derived instance, adding ?x <belongsTo> <instance>
					this.sparqlClient.addNewEntitiesToDerived(instance, newEntities);
					LOGGER.debug("Added new instances <" + newEntities + "> to the derivation <" + instance + ">");
					
					if (derivedAndType.get(0).size() > 0) {
						LOGGER.debug("This derivation contains at least one entity which is an input to another derivation");
						LOGGER.debug("Relinking new instance(s) to the derivation by matching their rdf:type");
						// after deleting the old entity, we need to make sure that it remains linked to the appropriate derived instance
						List<String> classOfNewEntities = this.sparqlClient.getInstanceClass(newEntities);
						
						// look for the entity with the same rdf:type that we need to reconnect
						List<String> oldDerivedList = derivedAndType.get(0);
						List<String> oldTypeList = derivedAndType.get(1);
				
						// for each instance in the old derived instance that is connected to another derived instance, reconnect it
						for (int i = 0; i < oldDerivedList.size(); i++) {
							LOGGER.debug("Searching within <" + newEntities + "> with rdf:type <" + oldTypeList.get(i) + ">");
							// index in the new array with the matching type
							Integer matchingIndex = null;
							for (int j = 0; j < classOfNewEntities.size(); j++) {
								if (classOfNewEntities.get(j).contentEquals(oldTypeList.get(i))) {
									if (matchingIndex != null) {
										throw new JPSRuntimeException("Duplicate rdf:type found within output, the DerivationClient does not support this");
									}
									matchingIndex = j;
								}
							}
							if (matchingIndex == null) {
								String reconnectError = "Unable to find an instance with the same rdf:type to reconnect to " + oldDerivedList.get(i);
								throw new JPSRuntimeException(reconnectError);
							}
						    // reconnect
							this.sparqlClient.reconnectInputToDerived(newEntities.get(matchingIndex), oldDerivedList.get(i));
						}
					}
				}
				// if there are no errors, assume update is successful
				this.sparqlClient.updateTimeStamp(instance);
				LOGGER.info("Updated timestamp of <" + instance + ">");
			}
		}
	}
	
	/**
	 * called by the public function validateDerived
	 * @param instance
	 * @param derivedList
	 */
	private void validateDerivation(String instance, DirectedAcyclicGraph<String,DefaultEdge> graph) {
		List<String> inputsAndDerived = this.sparqlClient.getInputsAndDerived(instance);
		if (!graph.containsVertex(instance)) {
			graph.addVertex(instance);
		}
		
		for (String input : inputsAndDerived) {
			if (!graph.containsVertex(input)) {
				graph.addVertex(input);
			}
			graph.addEdge(instance, input); // will throw an error here if there is circular dependency
			validateDerivation(input, graph);
		}
		
		// check that for each derived quantity, there is a timestamp to compare to
		List<String> inputs = this.sparqlClient.getInputs(instance);
		if (inputs.size() > 0) {
			// getTimestamp will throw an exception if there is no timestamp
			this.sparqlClient.getTimestamp(instance);
			for (String input : inputs) {
				this.sparqlClient.getTimestamp(input);
			}
		}
	}
	
	/**
	 * compares the timestamps of quantities used to derived this instance
	 * returns true if any of its input is newer
	 * @param instance
	 * @return
	 */
	private boolean isOutOfDate(String instance, List<String> inputs) {
	    boolean outOfDate = false;
	    long instanceTimestamp = this.sparqlClient.getTimestamp(instance);
	    
	    for (String input : inputs) {
	    	long inputTimestamp = this.sparqlClient.getTimestamp(input);
	    	if (inputTimestamp > instanceTimestamp) {
	    		outOfDate = true;
	    		return outOfDate;
	    	}
	    }
	    return outOfDate;
	}
}
