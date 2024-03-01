from typing import Optional

from SPARQLWrapper import SPARQLWrapper, POST, JSON


class KgClient:
    def __init__(
        self, endpoint: str, user: Optional[str] = None, password: Optional[str] = None
    ):
        sparql = SPARQLWrapper(endpoint)
        sparql.setReturnFormat(JSON)
        if user is not None and password is not None:
            sparql.setCredentials(user=user, passwd=password)
        sparql.setMethod(POST)
        self.sparql = sparql

    def query(self, query: str):
        self.sparql.setQuery(query)
        return self.sparql.queryAndConvert()
