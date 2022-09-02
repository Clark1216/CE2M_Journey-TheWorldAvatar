###############################################
# Authors: Markus Hofmeister (mh807cam.ac.uk) #    
# Date: 24 Aug 2022                           #
###############################################

# This module extracts all building triples from specified namespace and 
# calls the UPRN agent on individual buildings to add UPRN information in
# order to avoid Heap Space Issues when running UPRN agent on entire namespace

import json
import requests
import time
from SPARQLWrapper import SPARQLWrapper, JSON


# Specify SPARQL endpoint to Blazegraph namespace
blazegraph = "http://127.0.0.1:9999/blazegraph/namespace/kings-lynn/sparql/"
# Specify agent endpoint
uprn_agent = "http://localhost:8080/agents/uprn"

# Specify waiting time between agent requests (in s)
# (to allow for sufficient time for KG updates)
t_wait = 5


def get_number_of_triples(endpoint):
    sparql = SPARQLWrapper(endpoint)
    sparql.setReturnFormat(JSON)
    query_string = f"""
    SELECT (COUNT(*) as ?count)
    WHERE {{ ?s ?p ?o }}
    """
    sparql.setQuery(query_string)
    results = sparql.query().convert()
    # Get number of triples
    res = int(results['results']['bindings'][0]['count']['value'])
    return res


def get_all_building_iris(endpoint):
    sparql = SPARQLWrapper(endpoint)
    sparql.setReturnFormat(JSON)
    query_string = f"""
    PREFIX ocgml: <http://www.theworldavatar.com/ontology/ontocitygml/citieskg/OntoCityGML.owl#>
    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> 
    PREFIX osid: <http://www.theworldavatar.com/ontology/ontocitygml/citieskg/OntoOSID.owl#>

    SELECT DISTINCT ?bldg

    WHERE {{ 
    GRAPH <{blazegraph}building/>                             
            {{ ?bldg ocgml:objectClassId 26 . }}
    }}
    ORDER BY ?bldg
    """
    sparql.setQuery(query_string)
    results = sparql.query().convert()
    # Get list of all buildings
    res = [r['bldg']['value'] for r in results['results']['bindings']]
    return res


def get_building_iris_w_old_but_wo_new_uprn(endpoint):
    sparql = SPARQLWrapper(endpoint)
    sparql.setReturnFormat(JSON)
    query_string = f"""
    PREFIX ocgml: <http://www.theworldavatar.com/ontology/ontocitygml/citieskg/OntoCityGML.owl#>
    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> 
    PREFIX osid: <http://www.theworldavatar.com/ontology/ontocitygml/citieskg/OntoOSID.owl#>

    SELECT DISTINCT ?bldg ?uprns_old ?uprns_new

    WHERE {{ 
    GRAPH <{blazegraph}building/>                             
            {{ ?bldg ocgml:objectClassId 26 .
            BIND(IRI(REPLACE(str(?bldg), "building", "cityobject")) AS ?cityobj) 
            }}
    GRAPH <{blazegraph}identifiers>
            {{ FILTER NOT EXISTS {{ ?cityobj ^osid:intersectsFeature/osid:hasValue ?uprns_new }}
            }}
            {{ # UPRNs from FME workflow
            SELECT DISTINCT ?cityobj ?uprns_old
            WHERE {{
                GRAPH <{blazegraph}cityobjectgenericattrib/>
                    {{ OPTIONAL {{
                        ?attr ocgml:attrName "OS_UPRNs" ;
                            ocgml:cityObjectId ?cityobj ;
                            ocgml:strVal ?uprns_old .
                        }}
                    }}
                    FILTER (!isBlank(?uprns_old))
                }}
            }}
    }}
    ORDER BY ?bldg
    """
    sparql.setQuery(query_string)
    results = sparql.query().convert()
    # Get list of all buildings
    res = [r['bldg']['value'] for r in results['results']['bindings']]
    return res


def get_building_iris_wo_new_uprn(endpoint):
    sparql = SPARQLWrapper(endpoint)
    sparql.setReturnFormat(JSON)
    query_string = f"""
    PREFIX ocgml: <http://www.theworldavatar.com/ontology/ontocitygml/citieskg/OntoCityGML.owl#>
    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> 
    PREFIX osid: <http://www.theworldavatar.com/ontology/ontocitygml/citieskg/OntoOSID.owl#>

    SELECT DISTINCT ?bldg ?uprns_old ?uprns_new

    WHERE {{ 
    GRAPH <{blazegraph}building/>                             
            {{ ?bldg ocgml:objectClassId 26 .
            BIND(IRI(REPLACE(str(?bldg), "building", "cityobject")) AS ?cityobj) 
            }}
    GRAPH <{blazegraph}identifiers>
            {{ FILTER NOT EXISTS {{ ?cityobj ^osid:intersectsFeature/osid:hasValue ?uprns_new }}
            }}
    }}
    ORDER BY ?bldg
    """
    sparql.setQuery(query_string)
    results = sparql.query().convert()
    # Get list of all buildings
    res = [r['bldg']['value'] for r in results['results']['bindings']]
    return res


def call_uprn_agent_in_batches(agent_endpoint, namespace,
                               bldg_iris, wait_time):
    # Number of buildings to process
    n = len(bldg_iris)

    # Construct query
    url = agent_endpoint
    header = {"content-type": "application/json",
              "Connection":"close"}

    # Initialise list for buildings with erroneous agent response
    erroneous = []

    # Construct payload and execute query
    # processed buildings with "get_all_building_iris": 4860
    # processed buildings with "get_building_iris_w_old_but_wo_new_uprn": 7105 (all)
    i = 12400
    for bldg in bldg_iris[i-1:]:
        print(f'Processing building {i:>6}/{n:>6}')
        print(f'{bldg}')
        payload = {"namespace": f"{namespace}",
                   "cityObjectIRI": f"{bldg}"}    
        try:
            # Execute put request
            r = requests.put(url, data=json.dumps(payload), headers=header)
            if r.status_code == 200:
                print('Success\n')
            else:
                print(f'Error - status code: {r.status_code}\n')
                erroneous.append(bldg)
        except:
            print(f'Error while processing bldg: {bldg}')
            erroneous.append(bldg)
        
        i += 1
        time.sleep(wait_time)

    print('Done')


if __name__ == '__main__':

    # Get triples at beginning
    triples1 = get_number_of_triples(blazegraph)
    print(f'\nNumber of triples: {triples1:>8}')

    # Get Building and CityObject IRIs
    #bldgs = get_all_building_iris(blazegraph)
    #bldgs = get_building_iris_w_old_but_wo_new_uprn(blazegraph)
    bldgs = get_building_iris_wo_new_uprn(blazegraph)

    # Call UPRN agent in chunks
    call_uprn_agent_in_batches(uprn_agent, blazegraph, bldgs, t_wait)

    # Get triples at beginning
    triples2 = get_number_of_triples(blazegraph)
    print(f'\nNumber of triples: {triples2:>8}')
    print(f'\nAdded triples: {triples2-triples1:>8}')
