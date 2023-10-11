################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) #    
# Date: 25 Sep 2023                            #
################################################

# The purpose of this module is to provide the DHOptimisationAgent class based on 
# the pyderivationagent.DerivationAgent class, i.e., implementing the district 
# heating generation optimisation agent as derivation agent using synchronous 
# derivation with time series

from rdflib import Graph, URIRef

from pyderivationagent import DerivationAgent
from pyderivationagent import DerivationInputs
from pyderivationagent import DerivationOutputs

from dhoptimisation.datamodel.iris import *
from dhoptimisation.agent.config import *
from dhoptimisation.kgutils.kgclient import KGClient
from dhoptimisation.kgutils.tsclient import TSClient
from dhoptimisation.agent.optimisation_tasks import *
from dhoptimisation.utils.env_configs import DB_USER, DB_PASSWORD


class DHOptimisationAgent(DerivationAgent):

    def __init__(self, **kwargs):
        # Initialise DerivationAgent parent instance
        super().__init__(**kwargs)

        # Initialise the Sparql_client (with defaults specified in environment variables)
        self.sparql_client = self.get_sparql_client(KGClient)
        

    def agent_input_concepts(self) -> list:
        # Please note: Declared inputs/outputs need proper instantiation incl. 
        #              RDF TYPE declarations in the KG for the derivation to work
        return [TS_FORECAST, TIME_INTERVAL]


    def agent_output_concepts(self) -> list:
        # Output concepts (i.e., results) of the Derivation
        return [OHN_PROVIDED_HEAT_AMOUNT, OHN_CONSUMED_GAS_AMOUNT,
                OHN_GENERATED_HEAT_AMOUNT]


    def validate_inputs(self, http_request) -> bool:
        # Validate completeness of received HTTP request (i.e. non-empty HTTP request, 
        # contains derivationIRI, etc.) -> only relevant for synchronous derivation
        return super().validate_inputs(http_request)


    def validate_input_values(self, inputs, derivationIRI=None):
        """
        Check whether received input instances are suitable to optimise heat generation.
        Throw exception if data is not suitable.
                # Extract required optimisation inputs from derivation markup (i.e., map
        # retrieved derivation inputs to corresponding model input parameters)

        Arguments:
            inputs {dict} -- Dictionary of inputs with input concepts as keys and values as list
            derivationIRI {str} -- IRI of the derivation instance (optional)

        Returns:
            dictionary of ...
        """
        
        # Initialise dict of return values
        input_iris = {}
        
        # 1) Verify that exactly one (optimisation) time:Interval instance is provided
            # Check whether input is available
        if not inputs.get(TIME_INTERVAL):
            raise_error(TypeError, f"Derivation {derivationIRI}: No 'time:Interval' IRI provided.")
        else:
            inp = inputs.get(TIME_INTERVAL)
            # Check whether only one input has been provided
            if len(inp) == 1:
                input_iris['interval'] = inp
            else:
                raise_error(TypeError, f"Derivation {derivationIRI}: More than one 'time:Interval' IRI provided.")
       
        # 2) Verify that exactly one forecast for heat demand and each grid temperature
        #    is provided, i.e., map forecast instances to corresponding input parameters
        if not inputs.get(TS_FORECAST):
            raise_error(TypeError, f"Derivation {derivationIRI}: No 'ts:Forecast' IRI provided.")
        else:
            inp = self.sparql_client.get_input_types_from_forecast_iris(inputs[TS_FORECAST])
            # Throw exception in case any could not be retrieved (i.e., is None)
            for key, value in inp.items():
                if value is None:
                    raise_error(ValueError, f"Derivation {derivationIRI}: No forecast for '{key}' provided.")

        #return input_iris
    
        # Create dict between input concepts and return values
        input_iris = {
        }

        # 2) Verify that exactly one forecast for heat demand and each grid temperature
        #    is provided
        
        # 3) Verify that forecast time series cover required optimisation interval


        # 2) Verify that either 1 ProvidedHeat (heat sourced from energy from waste plant) 
        #    or at least 1 ConsumedGas instance (heat generated from gas combustion) is provided
        # NOTE: a list of consumed gas instances can be provided to account for multiple 
        #       gas boilers and gas turbine emitting through the same chimney
            
        # Extract lists of consumed gas and provided heat instances
        provided_heat = inputs.get(OHN_PROVIDED_HEAT_AMOUNT)
        consumed_gas = inputs.get(OHN_CONSUMED_GAS_AMOUNT)
        # Create empty lists in case no instances have been marked up
        provided_heat = [] if provided_heat is None else provided_heat
        consumed_gas = [] if consumed_gas is None else consumed_gas

        if provided_heat and consumed_gas:
            msg = f"Derivation {derivationIRI}: Both 'ProvidedHeatAmount' and 'ConsumedGasAmount' instances provided."
            self.logger.error(msg)
            raise TypeError(msg)
        if not provided_heat and not consumed_gas:
            msg = f"Derivation {derivationIRI}: Neither 'ProvidedHeatAmount' nor 'ConsumedGasAmount' instances provided."
            self.logger.error(msg)
            raise TypeError(msg)
        if provided_heat:                
            if len(provided_heat) > 1:
                msg = f"Derivation {derivationIRI}: More than one 'ProvidedHeatAmount' instance provided."
                self.logger.error(msg)
                raise TypeError(msg)
            else:
                input_iris[OHN_PROVIDED_HEAT_AMOUNT] = provided_heat

        if consumed_gas:
                input_iris[OHN_CONSUMED_GAS_AMOUNT] = consumed_gas

        return input_iris


    def process_request_parameters(self, derivation_inputs: DerivationInputs, 
                                   derivation_outputs: DerivationOutputs):
        """
        This method takes 
            multiple ts:Forecast instances, representing 1 forecasted heat demand
              and 4 grid temperatures (i.e, flow and return temperature at municipal
              utility and energy from waste plant)
            1 time:Interval instance, representing the optimisation horizon
        and generates
            1 ohn:ProvidedHeatAmount instance representing the amount of heat provided
              by the energy from waste plant
            4 ohn:GeneratedHeatAmount instances representing the amount of heat generated
              by three conventional gas boilers and the CHP gas turbine
            4 ohn:ConsumedGasAmount instances representing the amount of gas consumed
              by three conventional gas boilers and the CHP gas turbine

        NOTE: This is a minimal design in the sense than many more input parameters 
              required for the optimisation are queried from the KG (and instantiated
              back into the KG); however, not all are marked as inputs/outputs of the
              derivation and only the required subset to create the target derivation
              chain for the use case (forecast -> otpimise -> emission estimation -> aermod)
              are actually considered
        """

        #TODO: Decide on whether to round or not

        # Get input IRIs from the agent inputs (derivation_inputs)
        # (returns dict of inputs with input concepts as keys and values as list)
        inputs = derivation_inputs.getInputs()
        derivIRI = derivation_inputs.getDerivationIRI()

        # Get validated optimisation model inputs
        input_iris = self.validate_input_values(inputs=inputs, derivationIRI=derivIRI)

        # Optimise heat generation
        #TODO: mocked for now; to be properly implemented
        # 1) Get optimisation interval bounds
        interval = self.sparql_client.get_interval_details(input_iris[TIME_INTERVAL][0])
        # 2) Get relevant time series settings from KG
        fc_details = self.sparql_client.get_input_forecast_details(input_iris[TS_FORECAST][0])
        # 3) Get potentially already instantiated optimisation output instances, i.e.,
        #    ProvidedHeat and ConsumedGas Amounts, which ts would just get updated
        #    (checks for actual forecast instances)
        outputs = self.sparql_client.get_optimisation_outputs(input_iris[TS_FORECAST][0])

        # Create optimisation
        rdb_url, time_format = get_rdb_endpoint(fc_details)
        ts_client = TSClient(kg_client=self.sparql_client, rdb_url=rdb_url, 
                             rdb_user=DB_USER, rdb_password=DB_PASSWORD)
        
        # Mock optimisation data
        # 1) retrieve 1 input time series
        times, values = ts_client.retrieve_timeseries(input_iris[TS_FORECAST][0])
        # 2) initialise output forecast for "random" values
        import random
        provided_heat = [round(random.uniform(2.0, 11.0),1) for _ in times]
        consumed_gas = [round(random.uniform(1.0, 6.0),1) for _ in times]

        # Instantiate new optimisation outputs in KG and RDB (if not yet existing)
        if not outputs:
            # Initialise return Graph
            g = Graph()
            providers = self.sparql_client.get_heat_providers()
            # efw plant
            efw_outputs = self.sparql_client.get_efw_output_iris(providers['efw_plant'][0])
            g, efw_ts = self.sparql_client.instantiate_new_outputs(g, efw_outputs)
            # gas boiler
            boiler_outputs = self.sparql_client.get_heatgenerator_output_iris(providers['boilers'][0])
            g, boiler_ts = self.sparql_client.instantiate_new_outputs(g, boiler_outputs)
            
            # Initialise time series
            ts_client.init_timeseries(dataIRI=efw_ts[OHN_PROVIDED_HEAT_AMOUNT], 
                                      times=times, values=provided_heat,
                                      ts_type=DOUBLE,
                                      time_format=time_format)
            ts_client.init_timeseries(dataIRI=boiler_ts[OHN_CONSUMED_GAS_AMOUNT], 
                                      times=times, values=consumed_gas, 
                                      ts_type=DOUBLE,
                                      time_format=time_format)

            # Add output graph to ensure complete derivation markup
            # --> this part of the code is only relevant when called via 
            # 'createSyncDerivationForNewInfo' and its only purpose is to ensure
            #  that forecast instance is marked up as "belongsTo" the derivation
            derivation_outputs.addGraph(g)

        else:
            # Only update optimisation time series data in RDB
            # NOTE: Entire previous optimisation data is replaced, i.e., NOT just 
            #       appending new data and potentially overwriting existing data
            # efw plant
            data_IRI, _ = self.sparql_client.get_associated_dataIRI(instance_iri=outputs[OHN_PROVIDED_HEAT_AMOUNT][0],
                                                                    unit=None, forecast=True)
            ts_client.replace_ts_data(dataIRI=data_IRI, 
                                      times=times, values=provided_heat)
            # gas boiler
            data_IRI, _ = self.sparql_client.get_associated_dataIRI(instance_iri=outputs[OHN_CONSUMED_GAS_AMOUNT][0],
                                                                    unit=None, forecast=True)
            ts_client.replace_ts_data(dataIRI=data_IRI, 
                                      times=times, values=consumed_gas)
        
        created_at = pd.to_datetime('now', utc=True)
        logger.info(f'Created generation optimisation at: {created_at}')

        # NOTE: DerivationWithTimeSeries does not return any output triples, 
        #       as all updates to the time series are expected to be conducted
        #       within the agent logic 
        

def default():
    """
    Instructional message at the agent root.
    """

    msg = '<B>District Heating Optimisation Agent</B>:<BR><BR>'
    msg += 'This district heating optimisation agent is used to optimise the total heat generation cost '
    msg += 'for the Pirmasens municipal utility company by solving the economic dispatch problem including '
    msg += 'an energy-from-waste plant, a combined heat and power gas turbine, and a set of conventional '
    msg += 'gas boilers in an MPC-like fashion. <BR>'
    msg += "The agent is implemented as derivation agent using ontoderivation:DerivationWithTimeSeries"
    msg += "<BR><BR>"
    msg += 'For further details please see the <a href="https://github.com/cambridge-cares/TheWorldAvatar/tree/main/Agents/DistrictHeatingOptimisationAgent/">District Heating Optimisation Agent README</a>.'
    return msg
