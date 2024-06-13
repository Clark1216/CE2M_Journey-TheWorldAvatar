from typing import Annotated, Optional

from fastapi import Depends
from services.stores.entity_store.base import IEntityLinker
from services.sparql import SparqlClient, get_ontozeolite_endpoint


class ZeoliteFrameworkLinker(IEntityLinker):
    def __init__(self, ontozeolite_endpoint: str):
        self.sparql_client = SparqlClient(ontozeolite_endpoint)

    def link(self, text: Optional[str], **kwargs):
        if "framework_code" not in kwargs:
            return []

        query = """PREFIX zeo: <http://www.theworldavatar.com/kg/ontozeolite/>
SELECT ?Framework
WHERE {{
    ?Framework zeo:hasFrameworkCode "{framework_code}" .
}}""".format(
            framework_code=kwargs["framework_code"]
        )

        _, bindings = self.sparql_client.querySelectThenFlatten(query)
        return [row["Framework"] for row in bindings]


def get_zeoliteFramework_linker(
    endpoint: Annotated[str, Depends(get_ontozeolite_endpoint)]
):
    return ZeoliteFrameworkLinker(ontozeolite_endpoint=endpoint)


class ZeoliticMaterialLinker(IEntityLinker):
    def __init__(self, ontozeolite_endpoint: str):
        self.sparql_client = SparqlClient(ontozeolite_endpoint)

    def link(self, text: Optional[str], **kwargs):
        if "formula" not in kwargs:
            return []

        query = """PREFIX zeo: <http://www.theworldavatar.com/kg/ontozeolite/>
SELECT ?Material
WHERE {{
    ?Material zeo:hasChemicalFormula "{formula}" .
}}""".format(
            formula=kwargs["formula"]
        )

        _, bindings = self.sparql_client.querySelectThenFlatten(query)
        return [row["Material"] for row in bindings]


def get_zeoliticMaterial_linker(
    endpoint: Annotated[str, Depends(get_ontozeolite_endpoint)]
):
    return ZeoliticMaterialLinker(ontozeolite_endpoint=endpoint)
