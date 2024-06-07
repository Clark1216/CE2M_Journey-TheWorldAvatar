from functools import cache
from typing import Annotated

from fastapi import Depends

from services.kg import KgClient, get_ontokin_bgClient
from services.processs_response.augment_node import NodeDataRetriever
from services.processs_response.ontokin.mechanism import get_mechanism_data
from services.processs_response.ontokin.rxn import get_kinetic_model_data, get_rxn_data
from services.processs_response.ontokin.species import (
    get_thermo_model_data,
    get_transport_model_data,
)


@cache
def get_ontokin_nodeDataRetriever(
    bg_client: Annotated[KgClient, Depends(get_ontokin_bgClient)]
):
    return NodeDataRetriever(
        kg_client=bg_client,
        type2getter={
            "ocape:ChemicalReaction": get_rxn_data,
            "okin:ThermoModel": get_thermo_model_data,
            "okin:TransportModel": get_transport_model_data,
            "okin:KineticModel": get_kinetic_model_data,
            "okin:ReactionMechanism": get_mechanism_data,
        },
    )
