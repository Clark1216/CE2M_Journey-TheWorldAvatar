from functools import cache
import logging
from typing import Annotated, Callable, List

from fastapi import Depends
from services.connector.ontospecies import (
    OntoSpeciesAgentConnector,
    get_ontospecies_agent_connector_getter,
)
from services.connector.agent_connector import IAgentConnector
from services.func_call import IFuncCaller, get_func_caller


logger = logging.getLogger(__name__)


class AgentConnectorMediator:
    def __init__(self, func_call_predictor: IFuncCaller, agents: List[IAgentConnector]):
        self.func_call_predictor = func_call_predictor
        self.funcs = [x for agent in agents for x in agent.get_funcs()]
        self.funcname2agent = {
            method_name: agent
            for agent in agents
            for method_name in agent.get_name2method().keys()
        }

    def query(self, query: str):
        logger.info("Predicting function to call...")
        func_name, func_args = self.func_call_predictor.predict(
            funcs=self.funcs, query=query
        )
        logger.info(
            "Predicted function: {name}({args})".format(name=func_name, args=func_args)
        )
        return self.funcname2agent[func_name].exec(
            method_name=func_name, args=func_args
        )


@cache
def get_agents(
    ontospecies_agent_connector_getter: Annotated[
        Callable[[], OntoSpeciesAgentConnector],
        Depends(get_ontospecies_agent_connector_getter),
    ]
):
    return [ontospecies_agent_connector_getter()]


def get_agent_connector_mediator(
    func_call_predictor: Annotated[IFuncCaller, Depends(get_func_caller)],
    agents: Annotated[List[IAgentConnector], Depends(get_agents)],
):
    return AgentConnectorMediator(
        func_call_predictor=func_call_predictor, agents=agents
    )
