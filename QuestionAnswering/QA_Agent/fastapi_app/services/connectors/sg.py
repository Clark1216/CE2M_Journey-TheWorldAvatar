import os
from services.core.kg import KgClient


def get_sg_ontopClient():
    return KgClient(os.getenv("KG_ENDPOINT_SG_ONTOP"))


def get_sgDispersion_bgClient():
    return KgClient(os.getenv("KG_ENDPOINT_SG_DISPERSION"))
