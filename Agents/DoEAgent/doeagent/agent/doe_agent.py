from dataclasses import asdict
from pathlib import Path
from typing import List
import json
import os

from pyasyncagent import AsyncAgent

from doeagent.kg_operations import *
from doeagent.data_model import *
from doeagent.doe_algo import *
from doeagent.conf import *

class DoEAgent(AsyncAgent):
    def setupJob(self, agentInputs) -> List[str]:
        """
            This function sets up the job given the URL-decoded input JSON string.

            Arguments:
                agentInputs - URL-decoded input JSON string
                                an example is:
                                {
                                    "agent_input": {
                                    "https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#Strategy": "https://www.example.com/triplestore/ontodoe/DoE_1/Strategy_1",
                                    "https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#Domain": "https://www.example.com/triplestore/ontodoe/DoE_1/Domain_1",
                                    "https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#SystemResponse": 
                                        ["https://www.example.com/triplestore/ontodoe/DoE_1/SystemResponse_1", "https://www.example.com/triplestore/ontodoe/DoE_1/SystemResponse_2"],
                                    "https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#HistoricalData": "https://www.example.com/triplestore/ontodoe/DoE_1/HistoricalData_1"
                                    }
                                }
        """
        # Load string to JSON object (python dict)
        input_json = json.loads(agentInputs) if not isinstance(agentInputs, dict) else agentInputs

        # Create sparql_client
        self.sparql_client = ChemistryAndRobotsSparqlClient(
            self.kgUrl, self.kgUrl, self.kgUser, self.kgPassword
        )
        # Check if the input is in correct format, and return OntoDoE.DesignOfExperiment instance
        doe_instance = self.collectInputsInformation(input_json)
        self.logger.info("Collected inputs from the knowledge graph: ")
        self.logger.info(json.dumps(doe_instance.dict()))

        # Call function to suggest the new experiment and return an instance of dataclass OntoDoE.NewExperiment
        new_rxn_exp = suggest(doe_instance)

        # Upload the created OntoRxn:ReactionVariation triples to KG
        # Also update the triple between OntoDoE:DesignOfExperiment and OntoRxn:ReactionVariation
        self.sparql_client.updateNewExperimentInKG(doe_instance, new_rxn_exp)

        # NOTE here a list is created by looping through the list of new_rxn_exp, but in fact its length should be 1 in current design
        # NOTE the loop is added for future development
        list_new_rxn_exp_iri = [exp.instance_iri for exp in new_rxn_exp]
        self.logger.info(f"The proposed new experiment is: {' '.join(list_new_rxn_exp_iri)}.")
        return list_new_rxn_exp_iri

    def collectInputsInformation(self, agent_inputs) -> DesignOfExperiment:
        """
            This function checks the input parameters of the HTTP request against the I/O signiture as declared in the DoE Agent OntoAgent instance and collects information.
        """
        self.logger.info("Checking arguments...")
        exception_string = """Inputs are not provided in correct form. An example is: 
                                {
                                    "https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#Strategy": "https://www.example.com/triplestore/ontodoe/DoE_1/Strategy_1",
                                    "https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#Domain": "https://www.example.com/triplestore/ontodoe/DoE_1/Domain_1",
                                    "https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#SystemResponse": 
                                        ["https://www.example.com/triplestore/ontodoe/DoE_1/SystemResponse_1", "https://www.example.com/triplestore/ontodoe/DoE_1/SystemResponse_2"],
                                    "https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#HistoricalData": "https://www.example.com/triplestore/ontodoe/DoE_1/HistoricalData_1"
                                }"""
        # If the input JSON string is missing ontology concept keys, raise error with "exception_string"
        if ONTODOE_STRATEGY in agent_inputs:
            try:
                # Get the information from OntoDoE:Strategy instance
                strategy_instance = self.sparql_client.getDoEStrategy(agent_inputs[ONTODOE_STRATEGY])
            except ValueError:
                self.logger.error("Unable to interpret strategy ('%s') as an IRI." % agent_inputs[ONTODOE_STRATEGY])
                raise Exception("Unable to interpret strategy ('%s') as an IRI." % agent_inputs[ONTODOE_STRATEGY])
        else:
            self.logger.error('OntoDoE:Strategy instance might be missing. Received inputs: ' + agent_inputs + exception_string)
            raise Exception('OntoDoE:Strategy instance might be missing. Received inputs: ' + agent_inputs + exception_string)

        if ONTODOE_DOMAIN in agent_inputs:
            try:
                domain_instance = self.sparql_client.getDoEDomain(agent_inputs[ONTODOE_DOMAIN])
            except ValueError:
                self.logger.error("Unable to interpret domain ('%s') as an IRI." % agent_inputs[ONTODOE_DOMAIN])
                raise Exception("Unable to interpret domain ('%s') as an IRI." % agent_inputs[ONTODOE_DOMAIN])
        else:
            self.logger.error('OntoDoE:Domain instance might be missing. Received inputs: ' + agent_inputs + exception_string)
            raise Exception('OntoDoE:Domain instance might be missing. Received inputs: ' + agent_inputs + exception_string)

        if ONTODOE_SYSTEMRESPONSE in agent_inputs:
            try:
                system_response_instance = self.sparql_client.getSystemResponses(agent_inputs[ONTODOE_SYSTEMRESPONSE])
            except ValueError:
                self.logger.error("Unable to interpret systemResponse ('%s') as an IRI." % agent_inputs[ONTODOE_SYSTEMRESPONSE])
                raise Exception("Unable to interpret systemResponse ('%s') as an IRI." % agent_inputs[ONTODOE_SYSTEMRESPONSE])
        else:
            self.logger.error('OntoDoE:SystemResponse instances might be missing. Received inputs: ' + agent_inputs + exception_string)
            raise Exception('OntoDoE:SystemResponse instances might be missing. Received inputs: ' + agent_inputs + exception_string)

        if ONTODOE_HISTORICALDATA in agent_inputs:
            try:
                historical_data_instance = self.sparql_client.getDoEHistoricalData(agent_inputs[ONTODOE_HISTORICALDATA])
            except ValueError:
                self.logger.error("Unable to interpret historicalData ('%s') as an IRI." % agent_inputs[ONTODOE_HISTORICALDATA])
                raise Exception("Unable to interpret historicalData ('%s') as an IRI." % agent_inputs[ONTODOE_HISTORICALDATA])
        else:
            self.logger.error('OntoDoE:HistoricalData instance might be missing. Received inputs: ' + agent_inputs + exception_string)
            raise Exception('OntoDoE:HistoricalData instance might be missing. Received inputs: ' + agent_inputs + exception_string)

        doe_instance = DesignOfExperiment(
            instance_iri=None,
            usesStrategy=strategy_instance,
            hasDomain=domain_instance,
            hasSystemResponse=system_response_instance,
            utilisesHistoricalData=historical_data_instance,
            proposesNewExperiment=None) # TODO initialisation of ReactionExperiment is omitted here

        # Get the OntoDoE:DesignOfExperiment instances given the inputs, i.e. all the inputs should belong to the same OntoDoE:DesignOfExperiment instance
        doe_instance = self.sparql_client.getDoEInstanceIRI(doe_instance)
        return doe_instance

def suggest(doe_instance: DesignOfExperiment) -> List[ReactionExperiment]:
    """
        This method suggests the new experiment given information provided for design of experiment exercise.

        Arguments:
            doe_instance - instance of dataclass OntoDoE.DesignOfExperiment
    """

    # TODO this method calls summit doe, can be expanded in the future
    new_exp = proposeNewExperiment(doe_instance)

    return new_exp

# Show an instructional message at the DoEAgent servlet root
def default():
    """
        Instructional message at the app root.
    """
    msg  = "This is an asynchronous agent that capable of conducting Design Of Experiment (DoE).<BR>"
    msg += "For more information, please visit https://github.com/cambridge-cares/TheWorldAvatar/tree/133-dev-design-of-experiment/Agents/DoEAgent#readme<BR>"
    # TODO change above line to https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/Agents/DoEAgent#readme, before merging back to develop branch
    return msg
