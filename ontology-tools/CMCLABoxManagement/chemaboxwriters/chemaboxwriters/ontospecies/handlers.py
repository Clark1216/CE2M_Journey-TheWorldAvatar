from chemaboxwriters.common import StageHandler
from compchemparser.helpers.utils import jsonStringToFile
from chemaboxwriters.common.stageenums import aboxStages
from chemaboxwriters.ontospecies.csvstagewriter import species_csv_abox_from_string
from chemaboxwriters.ontospecies.osjsonstagewriter import compchem_osjson_abox_from_string
from chemutils.ioutils import writeFile
from chemaboxwriters.common.handlers import CSV_TO_OWL
import copy

QC_JSON_TO_OS_JSON = StageHandler(handlerFunc=compchem_osjson_abox_from_string,
                             inStage=aboxStages.QC_JSON,
                             outStage=aboxStages.OS_JSON,
                             fileWriter=jsonStringToFile,
                             fileExt='.os.json')

OS_JSON_TO_CSV = StageHandler(handlerFunc=species_csv_abox_from_string,
                            inStage=aboxStages.OS_JSON,
                            outStage=aboxStages.CSV,
                            fileWriter= writeFile,
                            fileWriterKwargs={'newline':''},
                            fileExt='.os.csv')

OS_CSV_TO_OS_OWL = copy.deepcopy(CSV_TO_OWL.set_file_ext('.os.owl'))