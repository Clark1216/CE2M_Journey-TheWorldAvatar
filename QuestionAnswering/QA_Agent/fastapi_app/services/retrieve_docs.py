from functools import cache
from typing import Annotated, Callable, List

from fastapi import Depends
import numpy as np
from redis.commands.search.field import (
    TextField,
    VectorField,
)
from redis.commands.search.indexDefinition import IndexDefinition, IndexType
from redis.commands.search.query import Query

from services.redis_client import get_redis_client
from services.embed import IEmbedder, get_embedder


class DocsRetriever:
    INDEX_NAME_TEMPLATE = "idx:{key}_vss"
    KEY_PREFIX_TEMPLATE = "{key}:"

    KNN_QUERY = (
        Query("(*)=>[KNN 3 @vector $query_vector AS vector_score]")
        .sort_by("vector_score")
        .return_fields("vector_score", "label")
        .dialect(2)
    )

    def __init__(self, embedder: IEmbedder):
        self.redis_client = get_redis_client()
        self.embedder = embedder

    @cache
    def does_index_exist(self, index_name: str):
        try:
            if self.redis_client.ft(index_name).info():
                return True
            return False
        except:
            return False

    def _embed(self, docs: List[str], doc_key_prefix: str, index_name: str):
        embeddings = self.embedder(docs).astype(np.float32).tolist()
        vector_dim = len(embeddings[0])

        pipeline = self.redis_client.pipeline()
        for i, (doc, embedding) in enumerate(zip(docs, embeddings)):
            redis_key = doc_key_prefix + str(i)
            pipeline.json().set(
                redis_key, "$", dict(label=doc, label_embedding=embedding)
            )
        pipeline.execute()

        schema = (
            TextField("$.label", no_stem=True, as_name="label"),
            VectorField(
                "$.label_embedding",
                "FLAT",
                {"TYPE": "FLOAT32", "DIM": vector_dim, "DISTANCE_METRIC": "COSINE"},
                as_name="vector",
            ),
        )
        definition = IndexDefinition(prefix=[doc_key_prefix], index_type=IndexType.JSON)
        self.redis_client.ft(index_name).create_index(
            fields=schema, definition=definition
        )

    def _retrieve(self, encoded_query: np.ndarray, index_name: str):
        return [
            (doc.label, float(doc.vector_score))
            for doc in self.redis_client.ft(index_name)
            .search(self.KNN_QUERY, {"query_vector": encoded_query.tobytes()})
            .docs
        ]

    def retrieve(
        self, queries: List[str], key: str, docs_getter: Callable[[], List[str]]
    ):
        index_name = self.INDEX_NAME_TEMPLATE.format(key=key)
        if not self.does_index_exist(index_name):
            docs = docs_getter()
            self._embed(
                docs,
                doc_key_prefix=self.KEY_PREFIX_TEMPLATE.format(key=key),
                index_name=index_name,
            )

        encoded_queries = self.embedder(queries).astype(np.float32)

        return [
            self._retrieve(encoded_query, index_name)
            for encoded_query in encoded_queries
        ]


@cache
def get_docs_retriever(embedder: Annotated[IEmbedder, Depends(get_embedder)]):
    return DocsRetriever(embedder)
