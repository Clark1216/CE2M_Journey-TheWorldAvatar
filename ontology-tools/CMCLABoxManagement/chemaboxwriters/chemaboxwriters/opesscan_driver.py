from docopt import docopt, DocoptExit
from chemaboxwriters.ontopesscan import write_abox
import sys
#from chemaboxwriters.ontopesscan import write_opesscan_abox

#    opesscan <fileOrDir>  (--os-iri=<iri> --os-atoms-iris=<iris> --oc-atoms-ids=<ids>
#                            | --inp-file-type=<type>)
#                          [--out-dir=<dir>]

doc = """aboxwriter
Usage:
    opesscan <fileOrDir>  (--os-iris=<iri> --os-atoms-iris=<iris> --oc-atoms-ids=<ids>
                           | --inp-file-type=<type>)
                          [--out-dir=<dir>]
                          [--out-base-name=<name>]

Options:
--os-iris=<iri>         OntoSpecies iri associated with the
                        scan points
--os-atoms-iris=<iris>  Comma separated iris of ontospecies
                        atoms defining the scan coordinate
--oc-atoms-ids=<ids>    Positions of atoms in ontocompchem
                        scan point geometries (index starts
                        from one), e.g. "1,2"
--inp-file-type=<type>  Types of the allowed input files
                        to the opesscan abox writer:
                         - ontocompchem meta json, this
                           option is set by default if
                           species iris and atoms iris
                           and positions are input
                         - ontopesscan meta json, this       [default: ops_json]
                           option is set by default if
                           species iris and atoms iris
                           and postions are not input
                         - ontopesscan meta csv              [csv]
--out-dir=<dir>         Output directory to write the
                        abox files to. If not provided
                        defaults to the directory of the
                        input file
--out-base-name=<name>  Base name of the produced output
                        files. If not provided, defaults
                        to the input file base name.
"""

def start():
    try:
        args = docopt(doc)
    except DocoptExit:
        raise DocoptExit('Error: opesscan called with wrong arguments.')


    inpFileType=args['--inp-file-type']
    handlerFuncKwargs={}
    if args['--os-iris'] is not None:
        handlerFuncKwargs={
            'OC_JSON_TO_OPS_JSON':{'os_iris':args['--os-iris'], \
                                'os_atoms_iris':args['--os-atoms-iris'], \
                                'oc_atoms_pos':args['--oc-atoms-ids']}}
        inpFileType='oc_json'

    write_abox(fileOrDir=args['<fileOrDir>'],
                inpFileType=inpFileType, \
                outDir=args['--out-dir'], \
                outBaseName=args['--out-base-name'], \
                handlerFuncKwargs=handlerFuncKwargs)

if __name__ == '__main__':
    start()