from chemaboxwriters.common.pipeline import get_pipeline, Pipeline
import chemaboxwriters.common.handlers as hnds
from chemaboxwriters.ontomops.abox_stages import OM_ABOX_STAGES
from chemaboxwriters.ontomops.handlers import (
    OMINP_JSON_TO_OM_JSON_Handler,
    OM_JSON_TO_OM_CSV_Handler,
)
import logging

logger = logging.getLogger(__name__)

OMOPS_PIPELINE = "omops"


def assemble_omops_pipeline() -> Pipeline:

    handlers = [
        OMINP_JSON_TO_OM_JSON_Handler(),
        OM_JSON_TO_OM_CSV_Handler(),
        hnds.CSV_TO_OWL_Handler(
            name="OM_CSV_TO_OM_OWL",
            in_stage=OM_ABOX_STAGES.om_csv,  # type: ignore
            out_stage=OM_ABOX_STAGES.om_owl,  # type: ignore
        ),
    ]

    pipeline = get_pipeline(
        name=OMOPS_PIPELINE,
        handlers=handlers,
    )
    return pipeline
