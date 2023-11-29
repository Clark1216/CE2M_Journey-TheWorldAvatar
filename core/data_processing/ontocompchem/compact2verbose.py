from core.sparql import SparqlQuery
from core.sparql.query_form import SelectClause
from core.sparql.graph_pattern import (
    GraphPattern,
    TriplePattern,
)


class OCCSparqlCompact2VerboseConverter:
    def _try_convert_molcomp_hasresult_triple(self, pattern: GraphPattern):
        try:
            """?MolecularComputation occ:hasResult ?{ResultKey} ."""
            assert isinstance(pattern, TriplePattern)
            assert pattern.subj == "?MolecularComputation"
            assert len(pattern.tails) == 1 and pattern.tails[0][0] == "occ:hasResult"

            result_var = pattern.tails[0][1]
            assert result_var[0] == "?", result_var
            result_key = result_var[1:]

            if result_key == "OptimizedGeometry":
                """
                ?MolecularComputation occ:hasResult ?OptimizedGeometry .
                ?OptimizedGeometry ^os:fromGeometry ?X, ?Y, ?Z  .
                ?Atom os:hasXCoordinate ?X ;
                        os:hasYCoordinate ?Y ;
                        os:hasZCoordinate ?Z .
                ?X os:value ?XValue ; os:unit ?XUnit .
                ?Y os:value ?YValue ; os:unit ?YUnit .
                ?Z os:value ?ZValue ; os:unit ?ZUnit .
                """
                patterns = [
                    pattern,
                    TriplePattern(
                        subj="?X", 
                        tails=[
                            ("os:fromGeometry", "?OptimizedGeometry"),
                            ("os:value", "?XValue"),
                            ("os:unit", "?XUnit")
                        ]
                    ),
                    TriplePattern(
                        subj="?Y",
                        tails=[
                            ("os:fromGeometry", "?OptimizedGeometry"),
                            ("os:value", "?YValue"),
                            ("os:unit", "?YUnit")
                        ]
                    ),
                    TriplePattern(
                        subj="?Z",
                        tails=[
                            ("os:fromGeometry", "?OptimizedGeometry"),
                            ("os:value", "?ZValue"),
                            ("os:unit", "?ZUnit")
                        ]
                    ),
                    TriplePattern(
                        subj="?Atom",
                        tails=[
                            ("os:hasXCoordinate", "?X"),
                            ("os:hasYCoordinate", "?Y"),
                            ("os:hasZCoordinate", "?Z")
                        ]
                    )
                ]
                select_vars = ["?Atom", "?X", "?XValue", "?XUnit", "?Y", "?YValue", "?YUnit", "?Z", "?ZValue", "?ZUnit"]
            elif result_key == "RotationalConstants":
                """
                ?MolecularComputation occ:hasResult ?RotationalConstants .
                ?RotationalConstants occ:value ?RotationalConstantsValue .
                """
                patterns = [
                    pattern,
                    TriplePattern.from_triple("?RotationalConstants", "occ:value", "?RotationalConstantsValue"),
                ]
                select_vars = ["?RotationalConstantsValue"]
            elif result_key in [
                "SCFEnergy",
                "TotalGibbsFreeEnergy",
                "ZeroPointEnergy",
                "HOMOEnergy",
                "HOMOMinus1Energy",
                "HOMOMinus2Energy",
                "LUMOEnergy",
                "LUMOPlus1Energy",
                "LUMOPlus2Energy",
                "TotalEnergy",
                "TotalEnthalpy",
                "Frequencies",
            ]:
                """
                ?MolecularComputation occ:hasResult ?{ResultKey} .
                ?{ResultKey} occ:value ?{ResultKey}Value ; occ:unit ?{ResultKey}Unit .
                """
                result_value_var = result_var + "Value"
                result_unit_var = result_var + "Unit"
                patterns = [
                    pattern,
                    TriplePattern(
                        subj=result_var,
                        tails=[
                            ("occ:value", result_value_var),
                            ("occ:unit", result_unit_var),
                        ],
                    ),
                ]
                select_vars = [result_value_var, result_unit_var]
            return patterns, select_vars

        except AssertionError:
            return None

    def convert(self, sparql_compact: SparqlQuery):
        graph_patterns = list(sparql_compact.graph_patterns)
        graph_patterns.reverse()

        select_vars_verbose = list(sparql_compact.select_clause.vars)
        graph_patterns_verbose = []

        while len(graph_patterns) > 0:
            pattern = graph_patterns.pop()

            optional = self._try_convert_molcomp_hasresult_triple(pattern)
            if optional is not None:
                patterns, select_vars = optional
                select_vars_verbose.extend(select_vars)
                graph_patterns_verbose.extend(patterns)
                continue

            graph_patterns_verbose.append(pattern)
            
        return SparqlQuery(
            select_clause=SelectClause(
                solution_modifier="DISTINCT", vars=select_vars_verbose
            ),
            graph_patterns=graph_patterns_verbose,
        )
