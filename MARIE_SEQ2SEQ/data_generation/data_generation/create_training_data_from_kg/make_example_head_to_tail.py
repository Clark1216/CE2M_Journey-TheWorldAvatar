from typing import Dict, Union, List

from data_generation.utils import add_space_and_lower


class ExampleHeadToTailMaker:
    def make_example_head_to_tail(self, subgraph: Dict[str, Union[dict, List[dict]]]):
        select_variables = []

        species = subgraph["head"]["IdentifierValue"]
        where_clause = self.make_where_species(species)
        where_clause_compact = self.make_where_species_compact(species)
        ask_items = []

        tails: List[dict] = subgraph["tails"]
        property_num = 0
        for tail in tails:
            if tail["type"] == "property":
                property_num += 1
                PropertyName = tail["PropertyName"]

                select_variables.append(f"?{PropertyName}Value")
                where_clause += self.make_where_property(PropertyName)
                where_clause_compact += self.make_where_property_compact(PropertyName)
                ask_items.append(add_space_and_lower(PropertyName))
            elif tail["type"] == "use":
                select_variables.append("?UseValue")
                where_clause += self.make_where_use()
                where_clause_compact += self.make_where_use_compact()
                ask_items.append("use")
            elif tail["type"] == "chemicalclass":
                select_variables.append("?ChemicalClassValue")
                where_clause += self.make_where_chemicalclass()
                where_clause_compact += self.make_where_chemicalclass_compact()
                ask_items.append("chemical class")
            else:
                raise ValueError("Unexpected tail type: " + tail["type"])

        sparql_query = f"""SELECT {" ".join(["?label"] + select_variables)} 
    WHERE {{{where_clause}
    }}"""
        sparql_query_compact = f"""SELECT {" ".join(select_variables)} 
    WHERE {{{where_clause_compact}
    }}"""
        canonical_question_tokens = ["What "]
        if len(ask_items) < 2:
            canonical_question_tokens.append("is")
        else:
            canonical_question_tokens.append("are")
        for i, ask_item in enumerate(ask_items):
            if i == 0:
                pass
            elif i < len(ask_items) - 1:
                canonical_question_tokens.append(",")
            else:
                canonical_question_tokens.append(" and")
            canonical_question_tokens.append(" the ")
            canonical_question_tokens.append(ask_item)
        canonical_question_tokens.append(" of {species}?")

        return dict(
            canonical_question="".join(canonical_question_tokens),
            species=subgraph["head"]["IdentifierValue"],
            sparql_query=sparql_query,
            sparql_query_compact=sparql_query_compact,
        )
    
    def make_where_species(self, species: str):
        return f"""
        ?SpeciesIRI rdf:type os:Species ; rdfs:label ?label ; ?hasIdentifier ?IdentifierIRI .
        ?IdentifierIRI rdf:type ?Identifier ; os:value ?species .
        ?Identifier rdfs:subClassOf os:Identifier .
        FILTER ( ?species = "{species}" )"""

    def make_where_species_compact(self, species: str):
        return f"""
        ?SpeciesIRI ?hasIdentifier ?species .
        FILTER ( ?species = "{species}" )"""

    def make_where_property(self, PropertyName: str):
        return f"""
        ?SpeciesIRI os:has{PropertyName} ?{PropertyName}IRI .
        ?{PropertyName}IRI os:value ?{PropertyName}Value ; os:unit ?{PropertyName}UnitIRI ; os:hasProvenance ?{PropertyName}ProvenanceIRI . 
        ?{PropertyName}UnitIRI rdfs:label ?{PropertyName}UnitValue .
        OPTIONAL{{
            ?{PropertyName}IRI os:hasReferenceState ?{PropertyName}ReferenceStateIRI .
            ?{PropertyName}ReferenceStateIRI os:value ?{PropertyName}ReferenceStateValue ; os:unit ?{PropertyName}ReferenceStateUnitIRI .
            ?{PropertyName}ReferenceStateUnitIRI rdfs:label ?{PropertyName}ReferenceStateUnitValue .
        }}"""

    def make_where_use(self):
        return f"""
        ?SpeciesIRI os:hasUse ?UseIRI .
        ?UseIRI rdfs:label ?UseValue ."""

    def make_where_use_compact(self):
        return f"""
        ?SpeciesIRI os:hasUse ?UseValue ."""

    def make_where_property_compact(self, PropertyName: str):
        return f"""
        ?SpeciesIRI os:has{PropertyName} ?{PropertyName}Value ."""

    def make_where_chemicalclass(self):
        return f"""
        ?SpeciesIRI os:hasChemicalClass* ?x .
        ?x ?y ?z .
        ?z rdfs:subClassOf* ?ChemicalClassIRI .
        ?ChemicalClassIRI rdf:type os:ChemicalClass ; rdfs:label ?ChemicalClassValue ."""

    def make_where_chemicalclass_compact(self):
        return f"""
        ?SpeciesIRI os:hasChemicalClass ?ChemicalClassValue ."""
