from functools import cache
import os
from typing import Annotated, Generic, Type, TypeVar
from fastapi import Depends
from pydantic import BaseModel
import requests


class FeatureInfoClientSimple:
    def __init__(self, url: str):
        self.url = url

    def query(self, **kwargs):
        res = requests.get(self.url, params=kwargs)
        res.raise_for_status()
        return res.json()


T = TypeVar("T", bound=BaseModel)


class FeatureInfoClient(Generic[T]):
    def __init__(self, url: str, type: Type[T]):
        self.url = url
        self.type = type

    def query(self, **kwargs):
        res = requests.get(self.url, params=kwargs)
        res.raise_for_status()
        return self.type.model_validate_json(res.text)


@cache
def get_featureInfoAgentUrl():
    return os.getenv("ENDPOINT_FEATURE_INFO_AGENT")


@cache
def get_featureInfoClient(url: Annotated[str, Depends(get_featureInfoAgentUrl)]):
    return FeatureInfoClientSimple(url)
