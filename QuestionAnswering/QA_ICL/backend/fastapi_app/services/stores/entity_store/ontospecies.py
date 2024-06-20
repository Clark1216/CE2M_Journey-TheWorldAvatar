from functools import cache
import logging
from typing import Annotated

from fastapi import Depends
from model.entity_linking.ontospecies import ElementLinkingArgs, SpeciesLinkingArgs
from services.stores.entity_store.base import LinkerManager
from services.sparql import SparqlClient, get_ontospecies_endpoint


logger = logging.getLogger(__name__)


class OntospeciesLinkerManager(LinkerManager):
    SPECIES_IDENTIFIER_KEY_TO_PREDICATE_KEY = {
        "inchi": "InChI",
        "smiles": "SMILES",
        "iupac_name": "IUPACName",
    }

    def __init__(self, ontospecies_endpoint: str):
        self.sparql_client = SparqlClient(ontospecies_endpoint)

    @property
    def cls2linker(self):
        return {"os:Species": self.linkSpecies, "pt:Element": self.linkElement}

    def linkSpecies(self, text: str | None, **kwargs):
        args = SpeciesLinkingArgs.model_validate(kwargs)
        kvs = [
            ("InChI", args.inchi),
            ("SMILES", args.smiles),
            ("IUPACName", args.iupac_name),
        ]
        try:
            key, val = next((k, v) for k, v in kvs if v)
            logger.info(f"Linking species with args: {key}={val}")
            query = f"""PREFIX os: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#>

SELECT ?Species
WHERE {{
    ?Species a os:Species .
    ?Species os:has{key}/os:value "{val}"
}}"""
            _, bindings = self.sparql_client.querySelectThenFlatten(query)
            iris = [row["Species"] for row in bindings]

            if iris:
                logger.info(f"Linked IRIs: {iris}")
                return iris
        except StopIteration:
            pass

        texts = list(kwargs.values())
        texts.append(text)
        if not texts:
            lst: list[str] = []
            return lst

        logger.info(f"Linking species by matching with all identifiers: {texts}")

        query = """PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX os: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#>

SELECT ?Species WHERE {{
    ?Species a os:Species .
    VALUES ?Text {{ {texts} }}
    ?Species (((os:hasIUPACName|os:hasMolecularFormula|os:hasSMILES)/os:value)|rdfs:label|skos:altLabel) ?Text .
}}""".format(
            texts=" ".join('"{val}"'.format(val=text) for text in texts)
        )
        _, bindings = self.sparql_client.querySelectThenFlatten(query)
        iris = [row["Species"] for row in bindings]

        logger.info(f"Linked IRIs: {iris}")

        return iris

    def linkElement(self, text: str | None, **kwargs):
        try:
            args = ElementLinkingArgs.model_validate(kwargs)
        except:
            lst: list[str] = []
            return lst

        logger.info(f"Linking elements with args: {args.model_dump_json()}")

        query = f"""PREFIX os: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#>
        
SELECT DISTINCT *
WHERE {{
    ?Element os:hasElementSymbol/os:value "{args.symbol}"
}}"""
        _, bindings = self.sparql_client.querySelectThenFlatten(query)
        iris = [binding["Element"] for binding in bindings]

        logger.info(f"Linked IRIs: {iris}")

        return iris


@cache
def get_ontospecies_linkerManager(
    ontospecies_endpoint: Annotated[str, Depends(get_ontospecies_endpoint)]
):
    return OntospeciesLinkerManager(ontospecies_endpoint=ontospecies_endpoint)
