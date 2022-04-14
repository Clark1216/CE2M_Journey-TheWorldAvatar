package uk.ac.cam.cares.jps.base.derivation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * 
 * @author Kok Foong Lee
 * @author Jiaru Bai
 *
 */
public class DerivationClient {
	// input and output of agents need to be a JSONArray consisting a list of IRIs
	// with the do
	public static final String AGENT_INPUT_KEY = "agent_input";
	public static final String AGENT_OUTPUT_KEY = "agent_output";
	public static final String BELONGSTO_KEY = "belongsTo";
	// defines the endpoint DerivedQuantityClient should act on
	StoreClientInterface kbClient;
	DerivationSparql sparqlClient;
	boolean upstreamDerivationRequested;

	/**
	 * Logger for error output.
	 */
	private static final Logger LOGGER = LogManager.getLogger(DerivationClient.class);

	/**
	 * This constructor is tagged as @Deprecated as ideally user should provide
	 * based URL when creating derivation instances.
	 * 
	 * @param kbClient
	 */
	@Deprecated
	public DerivationClient(StoreClientInterface kbClient) {
		this.kbClient = kbClient;
		this.sparqlClient = new DerivationSparql(kbClient);
	}

	/**
	 * This constructor should be used to enable customised derivation instance base
	 * URL.
	 * 
	 * @param kbClient
	 * @param derivationInstanceBaseURL
	 */
	public DerivationClient(StoreClientInterface kbClient, String derivationInstanceBaseURL) {
		this.kbClient = kbClient;
		this.sparqlClient = new DerivationSparql(kbClient, derivationInstanceBaseURL);
	}

	/**
	 * This creates a new derived instance and adds the following statements
	 * <entity> <belongsTo> <derived>, <derived> <isDerivedUsing> <agentIRI>,
	 * <agentIRI> <hasHttpUrl> <agentURL>, <derived> <isDerivedFrom> <inputsIRI>
	 * Use this for instances that get replaced by agents
	 * 
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

	public List<String> bulkCreateDerivations(List<List<String>> entitiesList, List<String> agentIRIList,
			List<String> agentURLList, List<List<String>> inputsList) {
		List<String> derivations = this.sparqlClient.bulkCreateDerivations(entitiesList, agentIRIList, agentURLList,
				inputsList);
		LOGGER.info("Instantiated derivations " + derivations);

		// add timestamp to each derivation
		this.sparqlClient.addTimeInstance(derivations);

		return derivations;
	}

	/**
	 * This method creates a new derived instance and adds the following statements
	 * <entity> <belongsTo> <derived>, <derived> <isDerivedUsing> <agentIRI>,
	 * <derived> <isDerivedFrom> <inputsIRI>
	 * Use this for instances that get replaced by agents, also when the information
	 * about agent exists already
	 * 
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
	 * use this if all the agent does to the instance is appending time series data,
	 * entity do not get replaced
	 * 
	 * @param entity
	 * @param agentIRI
	 * @param agentURL
	 * @param inputsIRI
	 */
	public String createDerivationWithTimeSeries(List<String> entities, String agentIRI, String agentURL,
			List<String> inputsIRI) {
		String createdDerivation = this.sparqlClient.createDerivationWithTimeSeries(entities, agentIRI, agentURL,
				inputsIRI);
		this.sparqlClient.addTimeInstance(createdDerivation);
		LOGGER.info("Instantiated derivation with time series <" + createdDerivation + ">");
		LOGGER.debug("<" + entities + "> belongsTo <" + createdDerivation + ">");
		LOGGER.debug("<" + createdDerivation + "> isDerivedFrom <" + inputsIRI + ">");
		LOGGER.debug("<" + createdDerivation + "> isDerivedUsing <" + agentIRI + "> located at " + agentURL);
		return createdDerivation;
	}

	public List<String> bulkCreateDerivationsWithTimeSeries(List<List<String>> entitiesList, List<String> agentIRIList,
			List<String> agentURLList, List<List<String>> inputsList) {
		List<String> derivations = this.sparqlClient.bulkCreateDerivationsWithTimeSeries(entitiesList, agentIRIList,
				agentURLList, inputsList);
		LOGGER.info("Instantiated derivations with time series " + derivations);

		// add timestamp to each derivation
		this.sparqlClient.addTimeInstance(derivations);

		return derivations;
	}

	/**
	 * This method creates a new asynchronous derived instance and adds the
	 * following statements
	 * <entity> <belongsTo> <derived>, <derived> <isDerivedUsing> <agentIRI>,
	 * <derived> <isDerivedFrom> <inputsIRI>
	 * Use this for asynchronous instances that get replaced by agents, also when
	 * the information about agent exists already
	 * 
	 * @param entities
	 * @param agentIRI
	 * @param inputsIRI
	 * @return
	 */
	public String createAsyncDerivation(List<String> entities, String agentIRI, List<String> inputsIRI,
			boolean forUpdate) {
		String createdDerivation = this.sparqlClient.createDerivationAsync(entities, agentIRI, inputsIRI, forUpdate);
		this.sparqlClient.addTimeInstance(createdDerivation);
		// mark up the derivation with current timestamp
		if (!forUpdate) {
			this.sparqlClient.updateTimeStamp(createdDerivation);
		}
		LOGGER.info("Instantiated asynchronous derivation <" + createdDerivation + ">");
		LOGGER.debug("<" + entities + "> belongsTo <" + createdDerivation + ">");
		LOGGER.debug("<" + createdDerivation + "> isDerivedFrom <" + inputsIRI + ">");
		LOGGER.debug("<" + createdDerivation + "> isDerivedUsing <" + agentIRI + ">");
		return createdDerivation;
	}

	/**
	 * This method creates a new asynchronous derived instance given an existing
	 * derivation and adds the following statements
	 * <entity> <belongsTo> <derived>, <derived> <isDerivedUsing> <agentIRI>,
	 * <derived> <isDerivedFrom> <inputsIRI>
	 * Note that the <inputsIRI> to be used are actually derivation outputs
	 * retrieved from the given derivation.
	 * Use this for asynchronous instances that get replaced by agents, also when
	 * the information about agent exists already
	 * 
	 * @param entities
	 * @param agentIRI
	 * @param derivation
	 * @param forUpdate
	 * @return
	 */
	public String createAsyncDerivation(List<String> entities, String agentIRI, String derivation, boolean forUpdate) {
		// first retrieve a list of inputs
		List<String> inputsIRI = this.sparqlClient.retrieveMatchingInstances(derivation, agentIRI);
		// then create asynchronous derivation as usual
		return createAsyncDerivation(entities, agentIRI, inputsIRI, forUpdate);
	}

	public String createAsyncDerivationForNewInfo(String agentIRI, List<String> inputsAndDerivations) {
		return createAsyncDerivation(new ArrayList<>(), agentIRI, inputsAndDerivations, true);
	}

	/**
	 * adds a timestamp to your input following the w3c standard for unix timestamp
	 * https://www.w3.org/TR/owl-time/
	 * <entity> <hasTime> <time>, <time> <numericPosition> 123
	 * 
	 * @param entity
	 */
	public void addTimeInstance(String entity) {
		this.sparqlClient.addTimeInstance(entity);
		LOGGER.info("Added timestamp to <" + entity + ">");
	}

	/**
	 * same method as above but in bulk
	 * 
	 * @param entities
	 */
	public void addTimeInstance(List<String> entities) {
		this.sparqlClient.addTimeInstance(entities);
		LOGGER.info("Added timestamps to <" + entities + ">");
	}

	/**
	 * manually update the timestamps of pure inputs or derivations
	 * entity can be a derivation or a pure input
	 * 
	 * @param entities
	 */
	public void updateTimestamps(List<String> entities) {
		// if the given entity is part of a derivation, update the derivation instead
		Map<String, String> entityDerivationMap = this.sparqlClient.getDerivationsOf(entities);
		Map<String, Long> timestamp_map = new HashMap<>();
		long currentTime = Instant.now().getEpochSecond();
		for (String entity : entities) {
			if (entityDerivationMap.containsKey(entity)) {
				// belongs to a derivation, update timestamp of derivation
				timestamp_map.put(entityDerivationMap.get(entity), currentTime);
			} else {
				// assume this is a pure input, if this does not exist
				// nothing should happen
				timestamp_map.put(entity, currentTime);
			}
		}
		this.sparqlClient.updateTimestamps(timestamp_map);
	}

	public void updateTimestamp(String entity) {
		updateTimestamps(Arrays.asList(entity));
	}

	/**
	 * This method checks and makes sure the derived instance is up-to-date by
	 * comparing the timestamp
	 * of the derivation to all of its inputs.
	 * 
	 * @param derivationIRI
	 */
	@Deprecated
	public void updateDerivationAsyn(String derivationIRI) {
		// the graph object makes sure that there is no circular dependency
		DirectedAcyclicGraph<String, DefaultEdge> graph = new DirectedAcyclicGraph<String, DefaultEdge>(
				DefaultEdge.class);
		try {
			// the flag upstreamDerivationRequested is set as false by default
			upstreamDerivationRequested = false;
			updateDerivationAsyn(derivationIRI, graph);
		} catch (Exception e) {
			LOGGER.fatal(e.getMessage());
			throw new JPSRuntimeException(e);
		}
	}

	/**
	 * This method updates a DAG of pure asynchronous derivations or asynchronous
	 * derivations depending on synchronous derivations.
	 * 
	 * @param derivationIRI
	 */
	public void updateMixedAsyncDerivation(String derivationIRI) {
		// the graph object makes sure that there is no circular dependency
		DirectedAcyclicGraph<String, DefaultEdge> graph = new DirectedAcyclicGraph<String, DefaultEdge>(
				DefaultEdge.class);
		Derivation derivation = this.sparqlClient.getDerivation(derivationIRI);
		try {
			updateMixedAsyncDerivation(derivation, graph);
		} catch (Exception e) {
			LOGGER.fatal(e.getMessage());
			throw new JPSRuntimeException(e);
		}
	}

	/**
	 * This method updates the given and its upstream pure synchronous derivations.
	 * 
	 * @param derivationIRI
	 */
	public void updatePureSyncDerivation(String derivationIRI) {
		updatePureSyncDerivations(Arrays.asList(derivationIRI));
	}

	/**
	 * This method updates the list of given and their upstream pure synchronous
	 * derivations.
	 * 
	 * @param derivationIRI
	 */
	public void updatePureSyncDerivations(List<String> derivationIRIs) {
		// the graph object makes sure that there is no circular dependency
		DirectedAcyclicGraph<String, DefaultEdge> graph = new DirectedAcyclicGraph<String, DefaultEdge>(
				DefaultEdge.class);
		List<Derivation> derivations = this.sparqlClient.getAllDerivationsInKG();
		try {
			for (String derivationIRI : derivationIRIs) {
				Derivation derivation = derivations.stream().filter(d -> d.getIri().equals(derivationIRI)).findFirst()
						.get();
				updatePureSyncDerivation(derivation, graph);
			}
		} catch (Exception e) {
			LOGGER.fatal(e.getMessage());
			throw new JPSRuntimeException(e);
		}
	}

	/**
	 * This method updates all synchronous derivations in the knowledge graph.
	 */
	public void updateAllSyncDerivations() {
		List<Derivation> derivations = this.sparqlClient.getAllDerivationsInKG();

		// find derivations with entities that are not input of anything (the top nodes)
		List<Derivation> topNodes = new ArrayList<>();
		for (Derivation derivation : derivations) {
			// all entities need to match the condition
			if (derivation.getEntities().stream().allMatch(e -> !e.isInputToDerivation())) {
				topNodes.add(derivation);
			}
		}

		// the graph object makes sure that there is no circular dependency
		DirectedAcyclicGraph<String, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
		try {
			for (Derivation derivation : topNodes) {
				updatePureSyncDerivation(derivation, graph);
			}
		} catch (Exception e) {
			LOGGER.fatal(e.getMessage());
			throw new JPSRuntimeException(e);
		}
	}

	/**
	 * This method is a wrapper method of updateMixedAsyncDerivation(String
	 * derivationIRI) and updatePureSyncDerivation(String derivationIRI) that
	 * updates the given derivationIRI regardless its rdf:type.
	 * 
	 * @param derivationIRI
	 */
	public void unifiedUpdateDerivation(String derivationIRI) {
		// depend on the rdf:type of the root derivationIRI, different method is
		// triggered
		try {
			if (isDerivedAsynchronous(derivationIRI)) {
				// update pure async or async depend on sync derivations
				updateMixedAsyncDerivation(derivationIRI);
			} else {
				// update pure sync derivations
				updatePureSyncDerivations(Arrays.asList(derivationIRI));
			}
		} catch (Exception e) {
			LOGGER.fatal(e.getMessage());
			throw new JPSRuntimeException(e);
		}
	}

	/**
	 * makes sure the given instances are up-to-date by comparing their timestamps
	 * to all of their inputs. The input, derivedIRIs, should have rdf:type
	 * DerivedQuantity or DerivedQuantityWithTimeSeries
	 * 
	 * @param kbClient
	 * @param derivedIRI
	 */
	public void updateDerivations(List<String> derivedIRIs) {
		// the graph object makes sure that there is no circular dependency
		DirectedAcyclicGraph<String, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
		List<Derivation> derivations = this.sparqlClient.getDerivations();
		try {
			for (String derivedIRI : derivedIRIs) {
				Derivation derivation = derivations.stream().filter(d -> d.getIri().equals(derivedIRI)).findFirst()
						.get();
				updateDerivation(derivation, graph);
			}

			// update timestamps in KG
			Map<String, Long> derivationTime_map = new HashMap<>();
			for (Derivation derivation : derivations) {
				if (derivation.getUpdateStatus()) {
					derivationTime_map.put(derivation.getIri(), derivation.getTimestamp());
				}
			}
			this.sparqlClient.updateTimestamps(derivationTime_map);
		} catch (Exception e) {
			LOGGER.fatal(e.getMessage());
			throw new JPSRuntimeException(e);
		}
	}

	/**
	 * updates all derivations in the triple-store
	 */
	public void updateDerivations() {
		List<Derivation> derivations = this.sparqlClient.getDerivations();

		// find derivations with entities that are not input of anything (the top nodes)
		List<Derivation> topNodes = new ArrayList<>();
		for (Derivation derivation : derivations) {
			// all entities need to match the condition
			if (derivation.getEntities().stream().allMatch(e -> !e.isInputToDerivation())) {
				topNodes.add(derivation);
			}
		}

		// the graph object makes sure that there is no circular dependency
		DirectedAcyclicGraph<String, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
		try {
			for (Derivation derivation : topNodes) {
				updateDerivation(derivation, graph);
			}

			// update timestamps in kg
			Map<String, Long> derivationTime_map = new HashMap<>();
			for (Derivation derivation : derivations) {
				if (derivation.getUpdateStatus()) {
					derivationTime_map.put(derivation.getIri(), derivation.getTimestamp());
				}
			}
			this.sparqlClient.updateTimestamps(derivationTime_map);

		} catch (Exception e) {
			LOGGER.fatal(e.getMessage());
			throw new JPSRuntimeException(e);
		}
	}

	/**
	 * This checks for any circular dependency and ensures that all the linked
	 * inputs have a suitable timestamp attached. This does not check for
	 * everything, e.g. instances having appropriate rdf:types, and the agent design
	 * 
	 * @param derived
	 * @return
	 */
	public boolean validateDerivations() {
		// check if any instances that should be pure inputs but part of a derivation
		if (!this.sparqlClient.validatePureInputs()) {
			throw new JPSRuntimeException("Entities belonging to a derivation should not have timestamps attached");
		}
		List<Derivation> derivations = this.sparqlClient.getAllDerivationsInKG();

		// find derivations (the top nodes) if it meet below criteria
		// (1) with entities that are not input of anything
		// (2) don't have any outputs (entities), also no directedDownstream derivations
		List<Derivation> topNodes = new ArrayList<>();
		for (Derivation derivation : derivations) {
			// if derivation has entities, then all entities need to match the condition
			if (!derivation.getEntities().isEmpty()) {
				if (derivation.getEntities().stream().allMatch(e -> !e.isInputToDerivation())) {
					topNodes.add(derivation);
				}
			} else {
				// if the derivation doesn't have entities (outputs), then it MUST not
				// have any directedDownstream derivations
				if (derivation.getDirectedDownstreams().isEmpty()) {
					topNodes.add(derivation);
				}
			}
		}

		// the graph object makes sure that there is no circular dependency
		DirectedAcyclicGraph<String, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
		try {
			for (Derivation derivation : topNodes) {
				validateDerivation(derivation, graph);
			}
		} catch (Exception e) {
			LOGGER.fatal(e.getMessage());
			throw new JPSRuntimeException(e);
		}

		return true;
	}

	/**
	 * This method retrieves the agent inputs that mapped against the OntoAgent I/O
	 * signature.
	 * 
	 * @param derivation
	 * @param agentIRI
	 * @return
	 */
	public JSONObject retrieveAgentInputIRIs(String derivation, String agentIRI) {
		JSONObject agentInputs = new JSONObject();
		agentInputs.put(AGENT_INPUT_KEY, this.sparqlClient.getInputsMapToAgent(derivation, agentIRI));

		// mark derivation status as InProgress
		// record timestamp at the point the derivation status is marked as InProgress
		this.sparqlClient.updateStatusBeforeSetupJob(derivation);

		return agentInputs;
	}

	/**
	 * drops absolutely everything
	 */
	public void dropAllDerivationsAndTimestamps() {
		dropAllDerivations();
		dropAllTimestamps();
	}

	/**
	 * drops absolutely everything except for triples with OntoAgent
	 */
	public void dropAllDerivationsAndTimestampsNotOntoAgent() {
		dropAllDerivationsNotOntoAgent();
		dropAllTimestamps();
	}

	/**
	 * clears all derivations from the kg, only removes timestamps directly attached
	 * to derivations, does not remove timestamps of pure inputs
	 */
	public void dropAllDerivations() {
		this.sparqlClient.dropAllDerivations();
		LOGGER.info("Dropped all derivations");
	}

	/**
	 * clears all derivations from the kg, only removes timestamps directly attached
	 * to derivations, does not remove timestamps of pure inputs, does not remove
	 * triples that can be part of OntoAgent
	 */
	public void dropAllDerivationsNotOntoAgent() {
		this.sparqlClient.dropAllDerivationsNotOntoAgent();
		LOGGER.info("Dropped all derivations but not OntoAgent triples");
	}

	/**
	 * optional, removes timestamps of inputs that you added manually with
	 * addTimeInstance
	 */
	public void dropAllTimestamps() {
		this.sparqlClient.dropAllTimestamps();
		LOGGER.info("Dropped all timestamps");
	}

	/**
	 * This method updates the status of the Derivation at job completion: the
	 * status of the derivation will be marked as "Finished" and the newDerivedIRI
	 * will be attached to the status.
	 * 
	 * @param derivation
	 * @param newDerivedIRI
	 */
	public void updateStatusAtJobCompletion(String derivation, List<String> newDerivedIRI) {
		// mark as Finished and add newDerivedIRI to Finished status
		this.sparqlClient.updateStatusAtJobCompletion(derivation, newDerivedIRI);
	}

	/**
	 * This method checks at the status "Requested" to see if any immediate upstream
	 * derivations are yet to be updated.
	 * 
	 * @param derivation
	 */
	public Map<String, List<String>> checkImmediateUpstreamDerivation(String derivation) {
		// get a list of immediate upstream derivations that need an update
		// (IMMEDIATE upstream derivations in the chain - <derivation>
		// <isDerivedFrom>/<belongsTo> <upstreamDerivation>)
		// if all IMMEDIATE upstream derivations are up-to-date,
		// or if the derivation is the first one in the chain, this function returns
		// empty list
		// TODO when the list is not empty, it is possible to add more operations as now
		// we know exactly which IMMEDIATE upstream derivation(s) need an update
		// TODO additional support to be added when detecting any upstream derivation
		// needs an update is synchronous derivation
		Map<String, List<String>> upstreamDerivationsNeedUpdate = this.sparqlClient
				.getUpstreamDerivationsNeedUpdate(derivation);
		return upstreamDerivationsNeedUpdate;
	}

	public List<String> groupSyncDerivationsToUpdate(Map<String, List<String>> derivationsToUpdate) {
		List<String> syncDerivations = new ArrayList<>();
		if (!derivationsToUpdate.isEmpty()) {
			for (String rdfType : Arrays.asList(DerivationSparql.ONTODERIVATION_DERIVATION,
					DerivationSparql.ONTODERIVATION_DERIVATIONWITHTIMESERIES)) {
				if (derivationsToUpdate.containsKey(rdfType)) {
					syncDerivations.addAll(derivationsToUpdate.get(rdfType));
				}
			}
		}
		return syncDerivations;
	}

	/**
	 * This method cleans up the "Finished" derivation by reconnecting the new
	 * generated derived IRI with derivations and deleting all status.
	 * 
	 * @param derivation
	 */
	public void cleanUpFinishedDerivationUpdate(String derivation) {
		// this method largely follows the part of code after obtaining the response
		// from Agent in method updateDerivation(String instance,
		// DirectedAcyclicGraph<String,DefaultEdge> graph)
		// the additional part in this method (compared to the above mentioned method)
		// is: (1) how we get newDerivedIRI; (2) we connect the newDerivedIRI with
		// downstream derivaitons if the current derivation was created for new info;
		// (3) we delete all triples connected to the status of the derivation
		// in the future development, there's a potential these two methods can be
		// merged into one

		// (1) get newDerivedIRI as the new instances created by agent
		List<String> newEntitiesString = this.sparqlClient.getNewDerivedIRI(derivation);

		// get those old entities that are inputs as other derivations
		// query for ?x <belongsTo> <derivation>; ?downstreamDerivation <isDerivedFrom>
		// ?x.
		// also ?x <rdf:type> ?xType; ?downstreamDerivation <rdf:type> ?preDevType.
		List<Entity> oldEntitiesAsInput = this.sparqlClient.getDerivedEntitiesAndDownstreamDerivation(derivation);

		// delete old instances
		// IT SHOULD BE NOTED THAT DELETION OF OLD ENTITIES SHOULD ONLY BE DONE AFTER
		// YOU STORED THE OLD ENTITIES INTO LOCAL VARIABLE "oldEntitiesAsInput"
		this.sparqlClient.deleteBelongsTo(derivation);
		LOGGER.debug("Deleted old instances of derivation: " + derivation);

		// link new entities to derived instance, adding ?x <belongsTo> <instance>
		this.sparqlClient.addNewEntitiesToDerived(derivation, newEntitiesString);
		LOGGER.debug("Added new instances <" + newEntitiesString + "> to the derivation <" + derivation + ">");

		// create local variable for the new entities for reconnecting purpose
		List<Entity> newEntities = this.sparqlClient.initialiseNewEntities(newEntitiesString);

		// if none of the outputs of derivaiton is input of other derivations, or if the derivation was created
		// for new info, the code will NOT enter the next if block
		if (oldEntitiesAsInput.size() > 0) {
			LOGGER.debug("This derivation contains at least one entity which is an input to another derivation");
			LOGGER.debug("Relinking new instance(s) to the derivation by matching their rdf:type");
			// after deleting the old entity, we need to make sure that it remains linked to
			// the appropriate derived instance
			List<String> newInputs = new ArrayList<>();
			List<String> derivationsToReconnect = new ArrayList<>();
			for (Entity oldInput : oldEntitiesAsInput) {
				// find within new Entities with the same rdf:type
				List<Entity> matchingEntity = newEntities.stream()
						.filter(e -> e.getRdfType().equals(oldInput.getRdfType())).collect(Collectors.toList());

				if (matchingEntity.size() != 1) {
					String errmsg = "When the agent writes new instances, make sure that there is 1 instance with matching rdf:type over the old set";
					LOGGER.error(errmsg);
					LOGGER.error("Number of matching entities = " + matchingEntity.size());
					throw new JPSRuntimeException(errmsg);
				}

				// add IRI of the matched instance and the derivation it should connect to
				oldInput.getInputOf().forEach(d -> {
					newInputs.add(matchingEntity.get(0).getIri());
					derivationsToReconnect.add(d.getIri());
				});
			}
			// reconnect within the triple store
			this.sparqlClient.reconnectInputToDerived(newInputs, derivationsToReconnect);
		}

		// (2) we need to check if any of the downstream derivations are directly connected
		// to this derivation, i.e. the current derivation was created for new information,
		// and other derivation instances further depend on the current one
		Map<String, String> downstreamDerivations = this.sparqlClient.getDownstreamDerivationForNewInfo(derivation);
		if (!downstreamDerivations.isEmpty()) {
			List<String> newInfoAsInputs = new ArrayList<>();
			List<String> downstreamDerivationsToReconnect = new ArrayList<>();
			Map<String, String> derivationPairMap = new HashMap<>();
			downstreamDerivations.forEach((downstream_derivation, agent_iri) -> {
				List<String> asInputs = this.sparqlClient.retrieveMatchingInstances(derivation, agent_iri);
				asInputs.forEach(inp -> {
					newInfoAsInputs.add(inp);
					downstreamDerivationsToReconnect.add(downstream_derivation);
					derivationPairMap.put(downstream_derivation, derivation);
				});
			});
			// delete <downstreamDerivation> <isDerivedFrom> <derivation>
			this.sparqlClient.deleteDirectConnectionBetweenDerivations(derivationPairMap);
			// reconnect within the triple store
			this.sparqlClient.reconnectInputToDerived(newInfoAsInputs, downstreamDerivationsToReconnect);
		}

		// (3) delete all triples connected to status of the derivation
		this.sparqlClient.deleteStatus(derivation);

		// if there are no errors, assume update is successful
		// retrieve the recorded value in {<derivation> <retrievedInputsAt> timestamp}
		// also delete it after value retrieved
		Map<String, Long> derivationTime_map = this.sparqlClient.retrieveInputReadTimestamp(derivation);
		// update timestamp with the retrieved value
		this.sparqlClient.updateTimestamps(derivationTime_map);
		LOGGER.info("Updated timestamp of <" + derivation + ">");
	}

	/**
	 * Checks if the derivation is an instance of DerivationAsyn.
	 * 
	 * @param derivation
	 * @return
	 */
	public boolean isDerivedAsynchronous(String derivation) {
		return this.sparqlClient.isDerivedAsynchronous(derivation);
	}

	/**
	 * This method retrieves the status rdf:type in the format of an enum of a given
	 * derivation instance IRI.
	 * 
	 * @param derivation
	 * @return
	 */
	public StatusType getStatusType(String derivation) {
		return this.sparqlClient.getStatusType(derivation);
	}

	/**
	 * Gets the new derived IRI at derivation update (job) completion.
	 * 
	 * @param derivation
	 * @return
	 */
	public List<String> getNewDerivedIRI(String derivation) {
		return this.sparqlClient.getNewDerivedIRI(derivation);
	}

	/**
	 * Gets the agent IRI that is used to update the derivation.
	 * 
	 * @param derivedQuantity
	 * @return
	 */
	public String getAgentUrl(String derivedQuantity) {
		return this.sparqlClient.getAgentUrl(derivedQuantity);
	}

	/**
	 * Gets a list of derivations that is derived using a given agent IRI.
	 * 
	 * @param agentIRI
	 * @return
	 */
	public List<String> getDerivations(String agentIRI) {
		return this.sparqlClient.getDerivations(agentIRI);
	}

	/**
	 * Gets a list of paired derivations and their status type (if applicable) that
	 * are derived using a given agent IRI.
	 * 
	 * @param agentIRI
	 * @return
	 */
	public Map<String, StatusType> getDerivationsAndStatusType(String agentIRI) {
		return this.sparqlClient.getDerivationsAndStatusType(agentIRI);
	}

	/**
	 * This method retrieves a list of derivation instance IRI given a list of
	 * derived quantities.
	 * 
	 * @param entities
	 * @return
	 */
	public Map<String, String> getDerivationsOf(List<String> entities) {
		return this.sparqlClient.getDerivationsOf(entities);
	}

	/**
	 * All private functions below
	 */

	/**
	 * This method marks the derivation as "Requested" when it detects a derivation
	 * is outdated.
	 * 
	 * @param instance
	 * @param graph
	 */
	@Deprecated
	private void updateDerivationAsyn(String instance, DirectedAcyclicGraph<String, DefaultEdge> graph) {
		// this method follows the first a few steps of method updateDerivation(String
		// instance, DirectedAcyclicGraph<String, DefaultEdge> graph)
		// TODO in future development, ideally these two method should be merged into
		// the same method?
		List<String> inputsAndDerived = this.sparqlClient.getInputsAndDerived(instance);

		if (!graph.containsVertex(instance)) {
			graph.addVertex(instance);
		}

		for (String input : inputsAndDerived) {
			if (graph.addVertex(input) && (null != graph.addEdge(instance, input))) {
				// (1) graph.addVertex(input) will try to add input as vertex if not already
				// exist in the graph
				// (2) (null != graph.addEdge(instance, input)) will throw an error here if
				// there is circular dependency
				// continuing... (2) addEdge will return 'null' if the edge has already been
				// added as DAGs can't
				// continuing... (2) have duplicated edges so we can stop traversing this
				// branch.
				// only when both (1) and (2) are true, we can update input
				// otherwise, node <D1> will be traversed multiple times if we have below chain
				// of derivations
				// and we run updateDerivationAsyn(<D3>, graph):
				// <I3> <belongsTo> <D3> .
				// <D3> <isDerivedFrom> <I2.1> .
				// <D3> <isDerivedFrom> <I2.2> .
				// <I2.1> <belongsTo> <D2.1> .
				// <I2.2> <belongsTo> <D2.2> .
				// <D2.1> <isDerivedFrom> <I1> .
				// <D2.2> <isDerivedFrom> <I1> .
				// <I1> <belongsTo> <D1> .
				// <D1> <isDerivedFrom> <I0.1> .
				// <D1> <isDerivedFrom> <I0.2> .
				// <D1> <isDerivedFrom> <I0.3> .
				updateDerivationAsyn(input, graph);
			}
		}

		List<String> inputs = this.sparqlClient.getInputs(instance);
		if (inputs.size() > 0) {
			// here only derivation instance will enter, first we check if it is an
			// asynchronous derivation
			if (isDerivedAsynchronous(instance)) {
				// we start with checking if this derivation is OutOfDate
				if (isOutOfDate(instance, inputs)) {
					// if it is OutOfDate and no status, just mark it as Requested
					// from Requested to other status will be handled from AsynAgent side
					if (!this.sparqlClient.hasStatus(instance)) {
						this.sparqlClient.markAsRequested(instance);
					}
					// set the flag to true so that other derivations will know there is one
					// derivation upstream already Requested
					// thus they can be marked as Requested as well
					upstreamDerivationRequested = true;
				} else {
					// if the Derivation is not OutOfDate, then only consider mark it as Requested
					// if meet all below situations
					// (1) there is upstream derivation being marked as Requested;
					// (2) this Derivation does NOT have any status, otherwise just leave it with
					// its existing status
					if (upstreamDerivationRequested && !this.sparqlClient.hasStatus(instance)) {
						this.sparqlClient.markAsRequested(instance);
					}
				}
			}
		}
	}

	/**
	 * This method marks the derivation as "Requested" if the derivation is
	 * outdated. This applies to both a-/sync derivations in a DAG of pure
	 * async derivation or mixed types of derivations (async depends on sync).
	 * 
	 * @param derivation
	 * @param graph
	 */
	private void updateMixedAsyncDerivation(Derivation derivation, DirectedAcyclicGraph<String, DefaultEdge> graph) {
		// inputs that are part of another derivation (for recursive call)
		// don't need direct inputs here
		List<Derivation> immediateUpstreamDerivations = this.sparqlClient
				.getAllImmediateUpstreamDerivations(derivation.getIri());

		if (!graph.containsVertex(derivation.getIri())) {
			graph.addVertex(derivation.getIri());
		}

		for (Derivation upstream : immediateUpstreamDerivations) {
			if (graph.addVertex(upstream.getIri()) & (null != graph.addEdge(derivation.getIri(), upstream.getIri()))) {
				// (1) graph.addVertex(input) will try to add input as vertex if not already
				// exist in the graph
				// (2) (null != graph.addEdge(instance, input)) will throw an error here if
				// there is circular dependency; addEdge will return 'null' if the edge has
				// already been added as DAGs can't have duplicated edges so we can stop
				// traversing this branch.
				// NOTE both (1) and (2) will execute as here we are using Non-short-circuit
				// Operator "&", instead of short-circuit operator "&&"
				// Only when both (1) and (2) are true, we can update input, otherwise, node
				// <D1> will be traversed multiple times if we have below DAG of derivations
				// and we run updateMixedAsyncDerivation(<D3>, graph):
				// <I3> <belongsTo> <D3> .
				// <D3> <isDerivedFrom> <I2.1> .
				// <D3> <isDerivedFrom> <I2.2> .
				// <I2.1> <belongsTo> <D2.1> .
				// <I2.2> <belongsTo> <D2.2> .
				// <D2.1> <isDerivedFrom> <I1> .
				// <D2.2> <isDerivedFrom> <I1> .
				// <I1> <belongsTo> <D1> .
				// <D1> <isDerivedFrom> <I0.1> .
				// <D1> <isDerivedFrom> <I0.2> .
				// <D1> <isDerivedFrom> <I0.3> .
				updateMixedAsyncDerivation(upstream, graph);
			}
		}

		if (this.sparqlClient.isOutdated(derivation.getIri())) {
			// mark it as Requested if the current derivation does NOT have status already
			if (!this.sparqlClient.hasStatus(derivation.getIri())) {
				this.sparqlClient.markAsRequested(derivation.getIri());
			}
		}
	}

	/**
	 * This method updates the DAG of pure synchronous derivations. The differences
	 * between this method and method updateDerivation(Derivation derivation,
	 * DirectedAcyclicGraph<String, DefaultEdge> graph) please see NOTE in comments.
	 * 
	 * @param derivation
	 * @param graph
	 */
	private void updatePureSyncDerivation(Derivation derivation, DirectedAcyclicGraph<String, DefaultEdge> graph) {
		// inputs that are part of another derivation (for recursive call)
		// don't need direct inputs here
		List<Derivation> upstreamDerivations = derivation.getInputsWithBelongsTo();

		if (!graph.containsVertex(derivation.getIri())) {
			graph.addVertex(derivation.getIri());
		}

		for (Derivation upstream : upstreamDerivations) {
			if (graph.addVertex(upstream.getIri()) & (null != graph.addEdge(derivation.getIri(), upstream.getIri()))) {
				// NOTE difference 1 - resolve multiple traverse problem
				// the above line is different from the checking in method
				// updateDerivation(Derivation derivation, DirectedAcyclicGraph<String,
				// DefaultEdge> graph) - this is to prevent the multiple traverse of the
				// upstream derivation in a DAG structure as demonstrated in the comments of
				// method updateMixedAsyncDerivation(Derivation derivation,
				// DirectedAcyclicGraph<String, DefaultEdge> graph)
				updatePureSyncDerivation(upstream, graph);
			}
		}

		// inputs required by the agent
		List<String> inputs = derivation.getAgentInputs();
		if (inputs.size() > 0) {
			// at this point, "instance" is a derived instance for sure, any other instances
			// will not go through this code
			// getInputs queries for <instance> <isDerivedFrom> ?x
			if (derivation.isOutOfDate()) {
				LOGGER.info("Updating <" + derivation.getIri() + ">");
				// calling agent to create a new instance
				String agentURL = derivation.getAgentURL();
				JSONObject requestParams = new JSONObject();
				// NOTE difference 6 - pass in the derivation agent inputs
				JSONObject inputsMapJson = derivation.getAgentInputsMap();
				requestParams.put(AGENT_INPUT_KEY, inputsMapJson);
				requestParams.put(BELONGSTO_KEY, derivation.getEntitiesIri()); // IRIs of belongsTo

				LOGGER.debug("Updating <" + derivation.getIri() + "> using agent at <" + agentURL
						+ "> with http request " + requestParams);
				// NOTE difference 2 - timestamp is now recorded at the agent side when it
				// receives request
				String response = AgentCaller.executeGetWithURLAndJSON(agentURL, requestParams.toString());

				LOGGER.debug("Obtained http response from agent: " + response);

				// NOTE difference 7 - the response is processed as Derivation outputs for both
				// Derivation and DerivationWithTimeSeries
				JSONObject agentResponse = new JSONObject(response);
				DerivationOutputs derivationOutputs = new DerivationOutputs(
					agentResponse.getJSONObject(AGENT_OUTPUT_KEY));
				// NOTE difference 3 - the timestamp is read from the agent response
				// set the timestamp
				derivation.setTimestamp(agentResponse.getLong(DerivationOutputs.RETRIEVED_INPUTS_TIMESTAMP_KEY));

				// if it is a derived quantity with time series, there will be no changes to the
				// instances
				if (!derivation.isDerivationWithTimeSeries()) {
					// collect new instances created by agent
					List<String> newEntitiesString = derivationOutputs.getNewDerivedIRI();

					// delete old instances
					this.sparqlClient.deleteBelongsTo(derivation.getIri());
					LOGGER.debug("Deleted old instances of: " + derivation.getIri());

					// link new entities to derived instance, adding ?x <belongsTo> <instance>
					this.sparqlClient.addNewEntitiesToDerived(derivation.getIri(), newEntitiesString);
					LOGGER.debug("Added new instances <" + newEntitiesString + "> to the derivation <"
							+ derivation.getIri() + ">");

					// entities that are input to another derivation
					List<Entity> inputToAnotherDerivation = derivation.getEntities()
							.stream().filter(e -> e.isInputToDerivation()).collect(Collectors.toList());

					List<Entity> newEntities = this.sparqlClient.initialiseNewEntities(newEntitiesString);

					if (inputToAnotherDerivation.size() > 0) {
						LOGGER.debug(
								"This derivation contains at least one entity which is an input to another derivation");
						LOGGER.debug("Relinking new instance(s) to the derivation by matching their rdf:type");
						// after deleting the old entity, we need to make sure that it remains linked to
						// the appropriate derived instance
						List<String> newInputs = new ArrayList<>();
						List<String> derivationsToReconnect = new ArrayList<>();
						for (Entity oldInput : inputToAnotherDerivation) {
							// find within new Entities with the same rdf:type
							List<Entity> matchingEntity = newEntities.stream()
									.filter(e -> e.getRdfType().equals(oldInput.getRdfType()))
									.collect(Collectors.toList());

							if (matchingEntity.size() != 1) {
								String errmsg = "When the agent writes new instances, make sure that there is 1 instance with matching rdf:type over the old set";
								LOGGER.error(errmsg);
								LOGGER.error("Number of matching entities = " + matchingEntity.size());
								throw new JPSRuntimeException(errmsg);
							}

							// update cached data
							oldInput.getInputOf().forEach(d -> {
								Derivation derivationToReconnect = d;
								derivationToReconnect.addInput(matchingEntity.get(0));
								derivationToReconnect.removeInput(oldInput);

								newInputs.add(matchingEntity.get(0).getIri());
								derivationsToReconnect.add(derivationToReconnect.getIri());
							});
						}
						// update triple-store and cached data
						this.sparqlClient.reconnectInputToDerived(newInputs, derivationsToReconnect);
						derivation.replaceEntities(newEntities);
					}
				}
				// NOTE difference 4 - update timestamp after the update of every derivation
				Map<String, Long> derivationTime_map = new HashMap<>();
				derivationTime_map.put(derivation.getIri(), derivation.getTimestamp());
				this.sparqlClient.updateTimestamps(derivationTime_map);
				// NOTE difference 5 - delete status if there's any (for sync in mixed)
				this.sparqlClient.deleteStatus(derivation.getIri());
				// if there are no errors, assume update is successful
				derivation.setUpdateStatus(true);
			}
		}
	}

	/**
	 * called by the public function updateInstance
	 * 
	 * @param instance
	 * @param derivedList
	 */
	@Deprecated
	private void updateDerivation(Derivation derivation, DirectedAcyclicGraph<String, DefaultEdge> graph) {
		// inputs that are part of another derivation (for recursive call)
		// don't need direct inputs here
		List<Derivation> inputsWithBelongsTo = derivation.getInputsWithBelongsTo();

		if (!graph.containsVertex(derivation.getIri())) {
			graph.addVertex(derivation.getIri());
		}

		for (Derivation input : inputsWithBelongsTo) {
			if (!graph.containsVertex(input.getIri())) {
				graph.addVertex(input.getIri());
			}
			if (null != graph.addEdge(derivation.getIri(), input.getIri())) { // will throw an error here if there is
																				// circular dependency
				// addEdge will return 'null' if the edge has already been added as DAGs can't
				// have duplicated edges so we can stop traversing this branch.
				updateDerivation(input, graph);
			}
		}

		// inputs required by the agent
		List<String> inputs = derivation.getAgentInputs();
		if (inputs.size() > 0) {
			// at this point, "instance" is a derived instance for sure, any other instances
			// will not go through this code
			// getInputs queries for <instance> <isDerivedFrom> ?x
			if (derivation.isOutOfDate()) {
				LOGGER.info("Updating <" + derivation.getIri() + ">");
				// calling agent to create a new instance
				String agentURL = derivation.getAgentURL();
				JSONObject requestParams = new JSONObject();
				JSONArray iris = new JSONArray(inputs);
				requestParams.put(AGENT_INPUT_KEY, iris);
				requestParams.put(BELONGSTO_KEY, derivation.getEntitiesIri()); // IRIs of belongsTo

				LOGGER.debug("Updating <" + derivation.getIri() + "> using agent at <" + agentURL
						+ "> with http request " + requestParams);
				// record timestamp at the point the request is sent to the agent
				long newTimestamp = Instant.now().getEpochSecond();
				String response = AgentCaller.executeGetWithURLAndJSON(agentURL, requestParams.toString());

				LOGGER.debug("Obtained http response from agent: " + response);

				// if it is a derived quantity with time series, there will be no changes to the
				// instances
				if (!derivation.isDerivationWithTimeSeries()) {
					// collect new instances created by agent
					List<String> newEntitiesString = new JSONObject(response).getJSONArray(AGENT_OUTPUT_KEY).toList()
							.stream().map(iri -> (String) iri).collect(Collectors.toList());

					// delete old instances
					this.sparqlClient.deleteBelongsTo(derivation.getIri());
					LOGGER.debug("Deleted old instances of: " + derivation.getIri());

					// link new entities to derived instance, adding ?x <belongsTo> <instance>
					this.sparqlClient.addNewEntitiesToDerived(derivation.getIri(), newEntitiesString);
					LOGGER.debug("Added new instances <" + newEntitiesString + "> to the derivation <"
							+ derivation.getIri() + ">");

					// entities that are input to another derivation
					List<Entity> inputToAnotherDerivation = derivation.getEntities()
							.stream().filter(e -> e.isInputToDerivation()).collect(Collectors.toList());

					List<Entity> newEntities = this.sparqlClient.initialiseNewEntities(newEntitiesString);

					if (inputToAnotherDerivation.size() > 0) {
						LOGGER.debug(
								"This derivation contains at least one entity which is an input to another derivation");
						LOGGER.debug("Relinking new instance(s) to the derivation by matching their rdf:type");
						// after deleting the old entity, we need to make sure that it remains linked to
						// the appropriate derived instance
						List<String> newInputs = new ArrayList<>();
						List<String> derivationsToReconnect = new ArrayList<>();
						for (Entity oldInput : inputToAnotherDerivation) {
							// find within new Entities with the same rdf:type
							List<Entity> matchingEntity = newEntities.stream()
									.filter(e -> e.getRdfType().equals(oldInput.getRdfType()))
									.collect(Collectors.toList());

							if (matchingEntity.size() != 1) {
								String errmsg = "When the agent writes new instances, make sure that there is 1 instance with matching rdf:type over the old set";
								LOGGER.error(errmsg);
								LOGGER.error("Number of matching entities = " + matchingEntity.size());
								throw new JPSRuntimeException(errmsg);
							}

							// update cached data
							// TODO below lines are only changed to make the code compile
							// TODO its functions are NOT tested due to marked as Deprecated
							oldInput.getInputOf().forEach(d -> {
								Derivation derivationToReconnect = d;
								derivationToReconnect.addInput(matchingEntity.get(0));
								derivationToReconnect.removeInput(oldInput);

								newInputs.add(matchingEntity.get(0).getIri());
								derivationsToReconnect.add(derivationToReconnect.getIri());
							});
						}
						// update triple-store and cached data
						this.sparqlClient.reconnectInputToDerived(newInputs, derivationsToReconnect);
						derivation.replaceEntities(newEntities);
					}
				}
				// if there are no errors, assume update is successful
				derivation.setTimestamp(newTimestamp);
				derivation.setUpdateStatus(true);
			}
		}
	}

	private void validateDerivation(Derivation derivation, DirectedAcyclicGraph<String, DefaultEdge> graph) {
		// we also need to consider the derivations that are created for new information
		// here we assume that the directedUpstream derivation doesn't have outptus yet
		List<Derivation> allUpstreamDerivations = Stream
				.concat(derivation.getInputsWithBelongsTo().stream(), derivation.getDirectedUpstreams().stream())
				.distinct().collect(Collectors.toList());

		if (!graph.containsVertex(derivation.getIri())) {
			graph.addVertex(derivation.getIri());
		}

		for (Derivation upstream : allUpstreamDerivations) {
			if (!derivation.isDerivationAsyn() && upstream.isDerivationAsyn()) {
				// this checking is added to raise an error when a sync derivation is depending
				// on an async derivation
				throw new JPSRuntimeException("Synchronous derivation <" + derivation.getIri()
						+ "> depends on asynchronous derivation <" + upstream.getIri() + ">.");
			}

			if (graph.addVertex(upstream.getIri()) & (null != graph.addEdge(derivation.getIri(), upstream.getIri()))) {
				// NOTE the changes made here combined the two condition check into one, this
				// is to prevent the multiple traverse of the upstream derivation in a DAG
				// structure as demonstrated in the comments of method
				// updateMixedAsyncDerivation(Derivation derivation,
				// DirectedAcyclicGraph<String, DefaultEdge> graph)

				// NOTE Non-short-circuit Operator "&", instead of short-circuit operator "&&"
				// was used here so that the second condition will ALWAYS be checked, so will
				// throw an error here if there is circular dependency
				// addEdge will return 'null' if the edge has already been added as DAGs
				// can't have duplicated edges so we can stop traversing this branch.
				validateDerivation(upstream, graph);
			}
		}

		// this mainly checks for the presence of timestamp in pure inputs
		List<Entity> inputs = derivation.getInputs();
		for (Entity input : inputs) {
			if (!input.hasBelongsTo()) {
				if (input.getTimestamp() == null) {
					throw new JPSRuntimeException(input.getIri() + " does not have a timestamp");
				}
			}
		}
	}

	/**
	 * compares the timestamps of quantities used to derived this instance
	 * returns true if any of its input is newer
	 * 
	 * @param instance
	 * @return
	 */
	@Deprecated
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
