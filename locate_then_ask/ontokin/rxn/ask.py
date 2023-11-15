import copy
import random
from locate_then_ask.data_model import AskDatum
from locate_then_ask.ontokin.graph2sparql import OKGraph2Sparql
from locate_then_ask.query_graph import QueryGraph


class OKReactionAsker:
    def __init__(self):
        self.graph2sparql = OKGraph2Sparql()

    def _ask_mechanism(self, query_graph: QueryGraph, verbalization: str):
        if "Mechanism" in query_graph.nodes:
            return query_graph, verbalization

        query_graph = copy.deepcopy(query_graph)

        qnode = "Mechanism"
        query_graph.add_node(qnode, question_node=True)
        query_graph.add_edge(
            "Reaction", qnode, label="^okin:hasReaction"
        )

        verbalization += " across all the mechanisms that it appears in"

        return query_graph, verbalization

    def ask_name(self, query_graph: QueryGraph, verbalization: str):
        query_graph = copy.deepcopy(query_graph)
        query_graph.nodes["Reaction"]["question_node"] = True

        query_graph, verbalization = self._ask_mechanism(query_graph, verbalization)
        verbalization = "What is " + verbalization
        query_sparql = self.graph2sparql.convert(query_graph)

        return AskDatum(
            query_graph=query_graph,
            query_sparql=query_sparql,
            verbalization=verbalization,
        )

    def ask_count(self, query_graph: QueryGraph, verbalization: str):
        assert "Mechanism" in query_graph.nodes()

        query_graph = copy.deepcopy(query_graph)
        query_graph.nodes["Reaction"]["question_node"] = True
        query_graph.nodes["Reaction"]["ask_count"] = True

        if verbalization.startswith("the"):
            verbalization = verbalization[len("the"): ].strip()
            
        verbalization = "How many " + verbalization
        query_sparql = self.graph2sparql.convert(query_graph)

        return AskDatum(
            query_graph=query_graph,
            query_sparql=query_sparql,
            verbalization=verbalization,
        )

    def ask_relation(self, query_graph: QueryGraph, verbalization: str):
        query_graph = copy.deepcopy(query_graph)

        qnode = "KineticModel"
        query_graph.add_node(qnode, question_node=True)
        query_graph.add_edge("Reaction", qnode, label="okin:hasKineticModel")

        template = random.choice(
            [
                "For {E}, what is its kinetic model",
                "What is the kinetic model of {E}",
            ]
        )
        verbalization = template.format(E=verbalization)

        query_graph, verbalization = self._ask_mechanism(query_graph, verbalization)
        query_graph.add_edge("KineticModel", "Mechanism", label="okin:definedIn")

        query_sparql = self.graph2sparql.convert(query_graph)

        return AskDatum(
            query_graph=query_graph,
            query_sparql=query_sparql,
            verbalization=verbalization,
        )
