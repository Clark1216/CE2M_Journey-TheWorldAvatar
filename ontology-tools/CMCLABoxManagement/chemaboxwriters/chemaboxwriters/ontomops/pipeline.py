from chemaboxwriters.common.pipeline import get_pipeline, Pipeline
import chemaboxwriters.common.handlers as hnds
from chemaboxwriters.common.endpoints_config import Endpoints_proxy
import chemaboxwriters.common.globals as globals
from chemaboxwriters.ontomops.handlers import (
    OMINP_JSON_TO_OM_JSON_Handler,
    OM_JSON_TO_OM_CSV_Handler,
)
from typing import Optional, Dict
import logging

logger = logging.getLogger(__name__)

OMOPS_PIPELINE = "omops"


def assemble_omops_pipeline(
    endpoints_config: Optional[Dict] = None,
    endpoints_proxy: Optional[Endpoints_proxy] = None,
) -> Pipeline:

    handlers = [
        OMINP_JSON_TO_OM_JSON_Handler(),
        OM_JSON_TO_OM_CSV_Handler(),
        hnds.CSV_TO_OWL_Handler(
            name="OM_CSV_TO_OM_OWL",
            in_stage=globals.aboxStages.OM_CSV,
            out_stage=globals.aboxStages.OM_OWL,
        ),
    ]

    pipeline = get_pipeline(
        name=OMOPS_PIPELINE,
        handlers=handlers,
        endpoints_config=endpoints_config,
        endpoints_proxy=endpoints_proxy,
    )
    return pipeline
