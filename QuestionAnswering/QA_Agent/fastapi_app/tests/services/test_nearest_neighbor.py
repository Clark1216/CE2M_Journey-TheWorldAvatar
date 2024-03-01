from typing import List

from services.cache import DictCache
from services.embed import IEmbedder
from services.nearest_neighbor import NNRetriever
from tests.exceptions import UnexpectedMethodCallError


class MockEmbedder(IEmbedder):
    def __call__(self, documents: List[str]):
        lst = []
        for d in documents:
            if d.startswith("0"):
                lst.append([1, 1])
            elif d.startswith("1"):
                lst.append([1, 2])
            elif d.startswith("2"):
                lst.append([2, 1])
            else:
                lst.append([2, 2])
        return lst


class DoNotCallEmbedder(IEmbedder):
    def __call__(self, documents: List[str]):
        raise UnexpectedMethodCallError()


class TestNNRetriever:
    def test_retrieve_cacheMissAll(self):
        embedder = MockEmbedder()
        cache: DictCache[str, List[str]] = DictCache()
        nn_retriever = NNRetriever(embedder=embedder, cache=cache)
        neighbors = nn_retriever.retrieve(
            documents=["0", "1", "2", "3"], queries=["00", "10"]
        )

        assert neighbors == ["0", "1"]

    def test_retrieve_cacheHitSome(self):
        embedder = MockEmbedder()

        cache: DictCache[str, List[str]] = DictCache()
        cache.set("0", [1, 2])
        cache.set("1", [1, 1])

        nn_retriever = NNRetriever(embedder=embedder, cache=cache)
        neighbors = nn_retriever.retrieve(
            documents=["0", "1", "2", "3"], queries=["00", "10"]
        )

        assert neighbors == ["1", "0"]

    def test_retrieve_cacheHitAll(self):
        embedder = DoNotCallEmbedder()

        cache: DictCache[str, List[str]] = DictCache()
        cache.set("0", [1, 2])
        cache.set("1", [1, 1])

        nn_retriever = NNRetriever(embedder=embedder, cache=cache)
        neighbors = nn_retriever.retrieve(documents=["0", "1"], queries=["0"])

        assert neighbors == ["0"]
