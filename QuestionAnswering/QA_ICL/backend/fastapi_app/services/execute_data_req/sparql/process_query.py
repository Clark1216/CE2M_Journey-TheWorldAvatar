from functools import cache
import logging
from typing import Annotated, Any

from fastapi import Depends

from .endpoints import get_ns2endpoint


logger = logging.getLogger(__name__)


class SparqlQueryProcessor:
    def __init__(self, ns2endpoint: dict[str, str] = dict()):
        self.ns2endpoint = ns2endpoint

    def inject_service_endpoint(self, sparql: str):
        idx = 0

        while idx < len(sparql):
            start = sparql.find("SERVICE", idx)
            if start < 0:
                break

            logger.info("Found SERVICE keyword")

            start += len("SERVICE")
            while start < len(sparql) and sparql[start].isspace():
                start += 1
            if start >= len(sparql) or sparql[start] != "<":
                break

            end = sparql.find(">", start)
            if end < 0:
                break

            ns = sparql[start + 1 : end]

            logger.info("Found namespace: " + ns)

            if ns in self.ns2endpoint:
                logger.info("Namespace URI: " + self.ns2endpoint[ns])
                sparql = "{before}<{uri}>{after}".format(
                    before=sparql[:start],
                    uri=self.ns2endpoint[ns],
                    after=sparql[end + 1 :],
                )
                idx = start + len(self.ns2endpoint[ns]) + 1
            else:
                logger.info("Namespace URI not found")
                idx = end + 1

        return sparql

    def _backstep(self, sparql: str, idx: int):
        if idx < 0:
            return -1

        idx = idx - 1
        while idx >= 0 and sparql[idx].isspace():
            idx -= 1
        if idx < 0:
            return -1

        while idx >= 0 and not sparql[idx].isspace():
            idx -= 1
        if idx < 0:
            return -1

        return idx + 1

    def inject_bindings(
        self,
        sparql: str,
        entity_bindings: dict[str, list[str]],
        const_bindings: dict[str, Any],
    ):
        # because of possible subqueries, VALUES clause need to be inserted
        # at the subqueries where the variable is referenced
        for var, iris in entity_bindings.items():
            print(var, iris)
            varnode = f"?{var}"
            values_clause = "VALUES {varnode} {{ {iris} }}".format(
                varnode=varnode,
                iris=" ".join("<{iri}>".format(iri=iri) for iri in iris),
            )
            # find triple that includes `varnode`
            idx = 0
            while True:
                idx_varnode = sparql.find(varnode, idx)
                if idx_varnode < 0:
                    break
                idx = idx_varnode + 1

                idx_tripleend = sparql.find(".", idx_varnode)
                if idx_tripleend < 0:
                    break
                tokens = sparql[idx_varnode:idx_tripleend].strip().split(maxsplit=3)

                if len(tokens) == 3:
                    idx_headnode_start = idx_varnode
                elif len(tokens) == 2:
                    idx_headnode_start = self._backstep(sparql, idx=idx_varnode)
                elif len(tokens) == 1:
                    idx_rel_start = self._backstep(sparql, idx=idx_varnode)
                    idx_headnode_start = self._backstep(sparql, idx=idx_rel_start)
                else:
                    continue

                if idx_headnode_start < 0:
                    continue
                idx += len(values_clause) + 1
                
                sparql = "{before}{values} {after}".format(
                    before=sparql[:idx_headnode_start],
                    values=values_clause,
                    after=sparql[idx_headnode_start:],
                )

        return sparql

    def process(
        self,
        sparql: str,
        entity_bindings: dict[str, list[str]],
        const_bindings: dict[str, Any],
    ):
        logger.info("Processing SPARQL query...")

        sparql = self.inject_service_endpoint(sparql)
        return self.inject_bindings(
            sparql=sparql,
            entity_bindings=entity_bindings,
            const_bindings=const_bindings,
        )


@cache
def get_sparqlQuery_processor(
    ns2endpoint: Annotated[dict[str, str], Depends(get_ns2endpoint)],
):
    return SparqlQueryProcessor(ns2endpoint=ns2endpoint)
