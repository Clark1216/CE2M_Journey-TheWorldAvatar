from chemaboxwriters.common.stageenums import aboxStages
from chemutils.ioutils.ioutils import getFilesWithExtensions
from chemaboxwriters.common import Pipeline
from chemaboxwriters.ontospecies.handlers import QC_JSON_TO_OS_JSON, \
                                                 OS_JSON_TO_CSV
from chemaboxwriters.common import QC_LOG_TO_QC_JSON
from chemaboxwriters.common import CSV_TO_OWL
import os

def write_ospecies_abox(fileOrDir, inpFileType, outDir, qcLogExt):
    try:
        inStage = aboxStages[inpFileType.upper()]
    except KeyError as e:
        raise KeyError('Error: Wrong --os-inp-file-type="' + inpFileType+'"') from e

    if inStage == aboxStages.QC_LOG:
        fileExt = qcLogExt.split(',')
    else:
        fileExt = ['.'+inStage.name.lower()]
    files = getFilesWithExtensions(fileOrDir,fileExt)

    pipeline = Pipeline().add_handler(handler=QC_LOG_TO_QC_JSON) \
                         .add_handler(handler=QC_JSON_TO_OS_JSON) \
                         .add_handler(handler=OS_JSON_TO_CSV) \
                         .add_handler(handler=CSV_TO_OWL)
    for file_ in files:
        if outDir is None: outDir=os.path.dirname(file_)
        outBaseName=os.path.basename(file_)
        outPath = os.path.join(outDir,outBaseName)
        pipeline.execute(file_, inStage, outPath)