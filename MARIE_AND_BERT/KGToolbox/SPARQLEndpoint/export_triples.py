# This module extracts all triples from an online Blazegraph SPARQL endpoint 
# and saves them as .nt file

import os
from pathlib import Path
from SPARQLWrapper import SPARQLWrapper

from Marie.Util.location import DATA_DIR


def get_all_triples(endpoint, filepath):
    """
    This function queries a namespace in the specified SPARQL endpoint and export all the triples to .nt file
    :param endpoint: full url of the SPARQL endpoint, including the namespace
    :param filepath: Directory for exporting the .nt file
    :return:
    """
    print("Query Started")
    sparql = SPARQLWrapper(endpoint)
    # Run Describe queries for all IRIs
    queryString = 'CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } '
    sparql.setQuery(queryString)
    results = sparql.queryAndConvert()
    print("Query Done")
    # For available format see: https://rdflib.readthedocs.io/en/stable/intro_to_parsing.html
    # turtle: format='ttl'
    # n-triples: format='ntriples'
    results.serialize(filepath, format='ntriples')
    print("Exported file")


if __name__ == '__main__':
    import argparse
    argParser = argparse.ArgumentParser()
    argParser.add_argument("-e_point", "--endpoint", type=str, help="full url of the SPARQL endpoint")
    argParser.add_argument("-o_file", "--output_filename", type=str, help="name of the .nt file exported")
    argParser.add_argument("-o_name", "--ontology_name", type=str, help="name of the ontology")

    args = argParser.parse_args()
    name_space = args.namespace
    endpoint = args.endpoint
    output_filename = args.output_filename
    ontology = args.ontology_name
    ontology = "CrossGraph/%s" % ontology
    # Get all Triples and serialise as turtle
    full_path = os.path.join(DATA_DIR, ontology, output_filename)
    get_all_triples(endpoint, full_path)
