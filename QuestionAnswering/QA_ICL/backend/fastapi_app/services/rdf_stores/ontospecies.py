from functools import cache
from typing import Annotated

from fastapi import Depends

from model.ontospecies import (
    GcAtom,
    OntospeciesHasLabel,
    OntospeciesIdentifier,
    OntospeciesProperty,
    OntospeciesSpecies,
    PeriodictableElement,
)
from services.rdf_orm import RDFStore
from services.rdf_stores.base import Cls2GetterRDFStore
from services.sparql import get_ontospecies_endpoint


class OntospeciesRDFStore(Cls2GetterRDFStore):
    def __init__(self, ontospecies_endpoint: str):
        self.rdf_store = RDFStore(ontospecies_endpoint)

    @property
    def cls2getter(self):
        return {
            "pt:Element": self.get_elements,
            "gc:Atom": self.get_atoms,
            "os:Species": self.get_species,
            "os:Property": self.get_properties,
            "os:Identifier": self.get_identifiers,
            "os:ChemicalClass": self.get_has_label_models,
            "os:Use": self.get_has_label_models,
        }

    def get_elements(self, iris: list[str] | tuple[str]):
        return self.rdf_store.getMany(PeriodictableElement, iris)

    def get_atoms(self, iris: list[str] | tuple[str]):
        return self.rdf_store.getMany(GcAtom, iris)

    def get_species(self, iris: list[str] | tuple[str]):
        return self.rdf_store.getMany(OntospeciesSpecies, iris)

    def get_properties(self, iris: list[str] | tuple[str]):
        return self.rdf_store.getMany(OntospeciesProperty, iris)

    def get_identifiers(self, iris: list[str] | tuple[str]):
        return self.rdf_store.getMany(OntospeciesIdentifier, iris)

    def get_has_label_models(self, iris: list[str] | tuple[str]):
        return self.rdf_store.getMany(OntospeciesHasLabel, iris)


@cache
def get_ontospecies_rdfStore(
    endpoint: Annotated[str, Depends(get_ontospecies_endpoint)]
):
    return OntospeciesRDFStore(endpoint)
