import chemaboxwriters.kgoperations.querytemplates as querytemplates
import chemutils.obabelutils.obconverter as obconverter
from compchemparser.helpers.utils import get_xyz_from_parsed_json
from chemaboxwriters.common.utilsfunc import get_random_id
import json
import chemaboxwriters.common.globals as globals
from chemaboxwriters.common import PREFIXES
from compchemparser.parsers.ccgaussian_parser import PROGRAM_NAME, PROGRAM_VERSION
from chemaboxwriters.common.globals import aboxStages
from chemaboxwriters.common.handler import Handler
import chemaboxwriters.common.utilsfunc as utilsfunc
from dataclasses import dataclass, field
from enum import Enum
from typing import List, Optional, Dict
from chemaboxwriters.common.endpoints_config import Endpoints_proxy

comp_pref = PREFIXES["comp_pref"]


@dataclass
class QC_JSON_TO_OC_JSON_Handler(Handler):
    """Handler converting qc_json files to oc_json.
    Inputs: List of qc_json file paths
    Outputs: List of oc_json file paths
    """

    def __init__(
        self,
        endpoints_proxy: Optional[Endpoints_proxy] = None,
    ) -> None:
        super().__init__(
            name="QC_JSON_TO_OC_JSON",
            in_stage=aboxStages.QC_JSON,
            out_stage=aboxStages.OC_JSON,
            endpoints_proxy=endpoints_proxy,
        )

    def _handle_input(
        self,
        inputs: List[str],
        out_dir: str,
        input_type: Enum,
        dry_run: bool,
        triple_store_uploads: Optional[Dict] = None,
        file_server_uploads: Optional[Dict] = None,
    ) -> List[str]:

        outputs: List[str] = []
        for json_file_path in inputs:
            out_file_path = utilsfunc.get_out_file_path(
                input_file_path=json_file_path,
                file_extension=self._out_stage.name.lower(),
                out_dir=out_dir,
            )
            self._oc_jsonwriter(
                file_path=json_file_path,
                output_file_path=out_file_path,
                **self._handler_kwargs
            )
            outputs.append(out_file_path)
        return outputs

    @staticmethod
    def _oc_jsonwriter(
        file_path: str,
        output_file_path: str,
        random_id: str = "",
        spec_IRI: Optional[str] = None,
        *args,
        **kwargs
    ) -> None:

        with open(file_path, "r") as file_handle:
            data = json.load(file_handle)

        xyz = get_xyz_from_parsed_json(data)
        inchi = obconverter.obConvert(xyz, "xyz", "inchi")
        if spec_IRI is None:
            spec_IRI = querytemplates.get_species_iri(inchi)
        if not random_id:
            random_id = get_random_id()

        # at the moment we only support gaussian
        jobType = ""
        if "Gaussian" in data[PROGRAM_NAME]:
            if PROGRAM_VERSION in data:
                jobType = "G" + data[PROGRAM_VERSION][2:4]
            else:
                jobType = "Gxx"
        data[globals.SPECIES_IRI] = spec_IRI
        data[globals.ENTRY_IRI] = comp_pref + jobType + "_" + random_id
        data[globals.ENTRY_UUID] = random_id

        utilsfunc.write_dict_to_file(dict_data=data, dest_path=output_file_path)
