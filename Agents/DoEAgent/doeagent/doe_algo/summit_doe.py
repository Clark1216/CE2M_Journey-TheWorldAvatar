from summit.utils.dataset import DataSet as DataSet_summit
from summit.domain import ContinuousVariable as ContinuousVariable_summit
from summit.domain import Domain as Domain_summit
from summit.strategies import TSEMO as TSEMO_summit

from functools import reduce
import pandas as pd

from doeagent.data_model import *

import logging
logger = logging.getLogger('summit_doe')

def proposeNewExperiment(doe: DesignOfExperiment) -> List[ReactionExperiment]:
    """
        This method is a wrapper around the TSEMO algorithm as provided by python package `summit`. It suggests the new experiment given information about DesignOfExperiment.
        
        Arguments:
            doe - instance of dataclass OntoDoE.DesignOfExperiment
                    Stores information about design of experiment exercise retrieved from the OntoDoE:DesignOfExperiment instance in the knowledge graph
    """
    # Create domain for design of experiment
    domain = Domain_summit()

    # Add all optimisation variables to domain
    for var in doe.hasDomain.hasDesignVariable:
        domain += ContinuousVariable_summit(
            name=var.name, description=var.instance_iri,
            bounds=[var.lowerLimit, var.upperLimit]
        )
    # Add all system responses to domain
    for var in doe.hasSystemResponse:
        domain += ContinuousVariable_summit(
            name=var.name, description=var.instance_iri, \
            bounds=[-1000000000000000000000, 100000000000000000000], is_objective=True, maximize=var.maximise
            )

    # Create strategy (only supporting TSEMO at the moment)
    # TODO support LHS and other algorithms
    if isinstance(doe.usesStrategy, TSEMO):
        strategy = TSEMO_summit(
            domain,
            n_spectral_points=doe.usesStrategy.nSpectralPoints,
            generations=doe.usesStrategy.nGenerations,
            n_retries=doe.usesStrategy.nRetries,
            pop_size=doe.usesStrategy.populationSize
            )
    else:
        raise Exception('Currently only TSEMO is supported as DoE algorithm.')

    # Construct table of historical data "previous_results"
    # The "previous_results" is a dataframe looks like below:
    #    | ContinuousVariable_1 | ContinuousVariable_2 | ContinuousVariable_3 | ContinuousVariable_4 | SystemResponse_1 | SystemResponse_2
    # 0  |                5.19  |                0.10  |                14.7  |                42.0  |            47.9  |            7.44
    # 1  |                1.59  |                0.07  |                13.3  |                35.0  |             8.7  |            7.74
    # 2  |                8.44  |                0.16  |                 7.9  |                62.0  |            54.1  |            6.96
    # 3  |                8.83  |                0.04  |                11.8  |                67.0  |            40.0  |            8.10
    # 4  |                5.01  |                0.17  |                 8.1  |                56.0  |            47.7  |            6.83
    previous_results = constructPreviousResultsTable(doe) 

    # Suggest the next experiment, the output "next_exp" is a DataSet contains the suggested values for the optimisation variables in the next runs
    next_exp = strategy.suggest_experiments(doe.utilisesHistoricalData.numOfNewExp, prev_res=previous_results)

    # Extract the suggestion from Summit DataSet and populate that to dataclass ontorxn.ReactionExperiment/ReactionVariation
    new_rxn_exp = formNewExperiment(doe, next_exp)
    return new_rxn_exp

def formNewExperiment(doe: DesignOfExperiment, new_exp_ds: DataSet_summit) -> List[ReactionExperiment]:
    """
        This method converts the Summit suggested experiment from DataSet format to an instance of dataclass ontodoe.NewExperiment.
    """
    # Initialise a list to host the new suggested ReactionExperiment/ReactionVariation instances
    list_rxnvar = []
    
    # Get the first ReactionExperiment in the historical data 
    # The new created ReactionVariation instances <isVariationOf> this ReactionExperiment
    # Most of the information from this ReactionExperiment will be copied to the new created ReactionVariation instance
    # NOTE the ReactionVariation MUST and ONLY <isVariationOf> instance of ReactionExperiment, otherwise it will create huge overhead in recursive querying function getReactionExperiment
    first_rxn_exp = [rxn_exp for rxn_exp in doe.utilisesHistoricalData.refersTo if rxn_exp.clz == ONTOREACTION_REACTIONEXPERIMENT][0]

    # Iterate over the new suggested experiments to create each of them
    # NOTE below design works for multiple (>1) experiments
    # NOTE however, for the time being, the DoE Agent will be used to generate 1 experiment to fit the derivation framework
    # NOTE i.e. len(new_exp_ds) == 1
    # NOTE here we iterate through index (instead of range(len(new_exp_ds))) to make it robust against the situation where index doesn't start from 0
    for i in new_exp_ds.index:
        # Prepare a list of ReactionCondition
        list_con = []
        # Iterate over ReactionCondition in parent ReactionExperiment to populate the new suggested ReactionCondition in ReactionVariation
        # The ReactionCondition kept unchanged will be preserved (new instance of ReactionCondition will be generated)
        for first_rxn_exp_con in first_rxn_exp.hasReactionCondition:
            var_loc = []
            for design_var in doe.hasDomain.hasDesignVariable:
                if tuple((design_var.refersTo, design_var.positionalID)) == tuple((first_rxn_exp_con.clz, first_rxn_exp_con.positionalID)):
                    var_loc.append(design_var.name)
            if len(var_loc) > 1:
                raise Exception(
                        """Only one appearance should be allowed for a ReactionCondition to be a DesignVariable within one ReactionExperiment/ReactionVariation. \
                        If you intend to use ReactionCondition that instantiated from same class for different variables, please consider use positionalID to distinguish.
                        """)
            if len(var_loc) < 0:
                # it means this ReactionCondition is not adjusted in the DoE, so should be kept as the same value in the parent ReactionExperiment
                # TODO if we don't want to add duplicated data to KG, we may 'continue' here, and add support in querying ReactionVariation to retrieve data from its parent ReactionExperiment as well
                # TODO in that case, line 103 "hasNumericalValue=first_rxn_exp_con.hasValue.hasNumericalValue if len(var_loc) < 1 else new_exp_ds[var_loc[0]][i]"
                # TODO should be updated to "hasNumericalValue=new_exp_ds[var_loc[0]][i]" as "len(var_loc) > 0" is guaranteed (as we 'continue' if true)
                pass

            logger.debug("-------------------------------------------------------------------------------------")
            logger.debug("New suggested experiment summit DataSet:")
            logger.debug(new_exp_ds)
            logger.debug("-------------------------------------------------------------------------------------")

            # Prepare numerical value for the OM_Measure
            # NOTE TODO here we took a short-cut wrt decimal places, in the future, this should be connected to KG
            _raw_numerical_value_ = first_rxn_exp_con.hasValue.hasNumericalValue if len(var_loc) < 1 else new_exp_ds[var_loc[0]][i] # an example: df['ContinuousVariable_1'][0]
            if first_rxn_exp_con.clz == ONTOREACTION_REACTIONTEMPERATURE:
                _demical_numerical_val = round(_raw_numerical_value_)
            elif first_rxn_exp_con.clz == ONTOREACTION_RESIDENCETIME:
                _demical_numerical_val = round(_raw_numerical_value_, 2)
            elif first_rxn_exp_con.clz == ONTOREACTION_STOICHIOMETRYRATIO:
                _demical_numerical_val = round(_raw_numerical_value_, 2)
            elif first_rxn_exp_con.clz == ONTOREACTION_REACTIONSCALE:
                _demical_numerical_val = round(_raw_numerical_value_, 2)
            else:
                _demical_numerical_val = round(_raw_numerical_value_, 2)

            # Create instance for OM_Measure
            om_measure = OM_Measure(
                instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
                namespace_for_init=getNameSpace(first_rxn_exp_con.hasValue.instance_iri),
                hasUnit=first_rxn_exp_con.hasValue.hasUnit,
                # TODO for the moment, a new om:Measure instance is always created
                hasNumericalValue=_demical_numerical_val
            )

            # Create instance for ReactionCondition
            _objPropWithExp = first_rxn_exp_con.objPropWithExp
            if ONTOREACTION_HASREACTIONCONDITION not in _objPropWithExp:
                _objPropWithExp.append(ONTOREACTION_HASREACTIONCONDITION)
            con = ReactionCondition(
                instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
                namespace_for_init=getNameSpace(first_rxn_exp_con.instance_iri),
                clz=first_rxn_exp_con.clz,
                objPropWithExp=_objPropWithExp,
                hasValue=om_measure,
                positionalID=first_rxn_exp_con.positionalID,
                indicatesMultiplicityOf=first_rxn_exp_con.indicatesMultiplicityOf,
                indicateUsageOf=first_rxn_exp_con.indicateUsageOf
            )

            # Add created instance to list
            list_con.append(con)

        # # Prepare a list of empty PerformanceIndicator, by empty here means the OM_Measure is initialised as None
        # list_perf = []
        # # Iterate over PerformanceIndicator in parent ReactionExperiment to populate the empty PerformanceIndicator in ReactionVariation that to be computed
        # for first_rxn_exp_perf in first_rxn_exp.hasPerformanceIndicator:
        #     # Create instance for PerformanceIndicator
        #     perf = PerformanceIndicator(
        #         instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
        #         namespace_for_init=getNameSpace(first_rxn_exp_perf.instance_iri),
        #         clz=first_rxn_exp_perf.clz,
        #         objPropWithExp=first_rxn_exp_perf.objPropWithExp,
        #         hasValue=None,
        #         positionalID=first_rxn_exp_perf.positionalID
        #     )

        #     # Add created instance to list
        #     list_perf.append(perf)

        # # TODO revisit this design when closing the loop
        # # Generate all the rest PerformanceIndicator placeholders that not presented in the first_rxn_exp
        # lst_other_perf = [p for p in AVAILABLE_PERFORMANCE_INDICATOR_LIST if p not in [pi.clz for pi in first_rxn_exp.hasPerformanceIndicator]]
        # for o_p in lst_other_perf:
        #     perf = PerformanceIndicator(
        #         instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
        #         namespace_for_init=getNameSpace(first_rxn_exp.instance_iri),
        #         clz=o_p,
        #         objPropWithExp=OBJECT_RELATIONSHIP_PERFORMANCE_INDICATOR_RXN_EXP_DICT[o_p],
        #         hasValue=None,
        #         positionalID=None
        #     )
        #     # Add created instance to list
        #     list_perf.append(perf)

        # Populate the information to create instance of ReactionVariation
        # TODO add support for creating instance of ReactionExperiment (given no prior experiment data/history)
        rxnvar = ReactionVariation(
            instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
            namespace_for_init=getNameSpace(first_rxn_exp.instance_iri),
            hasReactionCondition=list_con,
            # hasPerformanceIndicator=list_perf,
            hasPerformanceIndicator=None,
            hasInputChemical=first_rxn_exp.hasInputChemical, # TODO revisit this design when testing
            # NOTE here the OutputChemical is set to be None as the OutputChemical will need to be generated after the physical experimentation
            # TODO revisit this design when testing
            hasOutputChemical=None,
            isVariationOf=first_rxn_exp
        )

        # Add created instance to list of ReactionVariation
        list_rxnvar.append(rxnvar)
    
    return list_rxnvar

def constructPreviousResultsTable(doe: DesignOfExperiment) -> DataSet_summit:
    """
        This method constructs a summit.utils.dataset.DataSet instance from instance of dataclass OntoDoE.DesignOfExperiment 
        
        Arguments:
            doe - instance of dataclass OntoDoE.DesignOfExperiment
                    Stores information about design of experiment exercise retrieved from the OntoDoE:DesignOfExperiment instance in the knowledge graph \n
                    
                    The output "previous_results" is a dataframe that looks like below:
                       | ContinuousVariable_1 | ContinuousVariable_2 | ContinuousVariable_3 | ContinuousVariable_4 | SystemResponse_1 | SystemResponse_2 \n
                    0  |                5.19  |                0.10  |                14.7  |                42.0  |            47.9  |            7.44  \n
                    1  |                1.59  |                0.07  |                13.3  |                35.0  |             8.7  |            7.74  \n
                    2  |                8.44  |                0.16  |                 7.9  |                62.0  |            54.1  |            6.96  \n
                    3  |                8.83  |                0.04  |                11.8  |                67.0  |            40.0  |            8.10  \n
                    4  |                5.01  |                0.17  |                 8.1  |                56.0  |            47.7  |            6.83  \n
    """

    # Initialise the list of dict for historical data that will be turned into pandas.DataFrame
    list_of_prev_result_df = []

    # get all data for DesignVariable
    for var in doe.hasDomain.hasDesignVariable:
        # prepare data for the previous results table
        data = []
        for exp in doe.utilisesHistoricalData.refersTo:
            # locate the value of the DesignVariable in each historical experiment
            var_val = []
            for con in exp.hasReactionCondition:
                if (con.clz == var.refersTo) and (con.positionalID == var.positionalID):
                    var_val.append(con.hasValue.hasNumericalValue)
            
            # raise Exception if there's more than one appearance of ReactionCondition that matches the DesignVariable
            if len(var_val) > 1:
                raise Exception(
                        """Only one appearance should be allowed for a ReactionCondition to be a DesignVariable within one ReactionExperiment/ReactionVariation. \
                        If you intend to use ReactionCondition that instantiated from same class for different variables, please consider use positionalID to distinguish.
                        """)
            
            # append the collected value in the experiment
            data.append({'rxnexp': exp.instance_iri, var.name: var_val[0]})
        # the prepared data will be converted from a dict to a pandas.DataFrame and added to a list
        _to_df = {}
        for k in data[0]:
            _to_df[k] = tuple(d[k] for d in data)
        list_of_prev_result_df.append(pd.DataFrame.from_dict(_to_df))

    # get all data for SystemResponse
    for var in doe.hasSystemResponse:
        # prepare data for the previous results table
        data = []
        for exp in doe.utilisesHistoricalData.refersTo:
            # locate the value of the SystemResponse in each historical experiment
            var_val = []
            for indi in exp.hasPerformanceIndicator:
                if (indi.clz == var.refersTo) and (indi.positionalID == var.positionalID):
                    var_val.append(indi.hasValue.hasNumericalValue)
            
            # raise Exception if there's more than one appearance of PerformanceIndicator that matches the SystemResponse
            if len(var_val) > 1:
                raise Exception(
                        """Only one appearance should be allowed for a PerformanceIndicator to be a SystemResponse within one ReactionExperiment/ReactionVariation. \
                        If you intend to use PerformanceIndicator that instantiated from same class for different variables, please consider use positionalID to distinguish.
                        """)
            
            # append the collected value in the experiment
            data.append({'rxnexp': exp.instance_iri, var.name: var_val[0]})
        # the prepared data will be converted from a dict to a pandas.DataFrame and added to a list
        _to_df = {}
        for k in data[0]:
            _to_df[k] = tuple(d[k] for d in data)
        list_of_prev_result_df.append(pd.DataFrame.from_dict(_to_df))

    # Merge the list of pandas.DataFrame to one DataFrame, using the IRI of OntoRxn:ReactionExperiment as unique identifier
    previousResults_df = reduce(lambda df1, df2: pd.merge(df1, df2, on='rxnexp'), list_of_prev_result_df)

    previous_results = DataSet_summit.from_df(previousResults_df.drop(columns="rxnexp").astype(float))

    return previous_results
