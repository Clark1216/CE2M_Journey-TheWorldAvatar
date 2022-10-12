import json
import os

import pandas
from SPARQLWrapper import SPARQLWrapper, JSON
from Marie.Util.Web.SPARQLWarehouse import ONTOCOMPCHEM_ALL_SPEICES
from Marie.Util.location import ARCHIVE_DIR


class OntoCompChemReader:

    def __init__(self):
        pass

    def query_blazegraph(self, query, namespace="ontocompchem"):
        sparql = SPARQLWrapper("http://www.theworldavatar.com/blazegraph/namespace/" + namespace + "/sparql")
        sparql.setQuery(query)
        sparql.setReturnFormat(JSON)
        results = sparql.query().convert()
        return results

    def process_query_result(self, result_json):
        results = result_json['results']['bindings']
        for r in results:
            geomType = r['geomType']['value'].split('/')[-1]
            species = r['species']['value'].split('/')[-1]
            print(geomType, species)

    def find_all_unique_species_calculation_pairs(self):
        tmp = {}
        rst = self.query_blazegraph(ONTOCOMPCHEM_ALL_SPEICES)['results']['bindings']
        for r in rst:
            species = r['species']['value'].split('/')[-1]
            ocIRI = r['ocIRI']['value'].split('/')[-1]
            if species in tmp:
                tmp[species] += 1
            else:
                tmp[species] = 1
        print(tmp)

    def construct_value_dict(self):
        value_dict = {}
        all_calculation = json.loads(open(os.path.join(ARCHIVE_DIR, 'ontocompchem_all_calculation.json')).read())
        col_names = ['geomType', 'vibAnal', 'rotConsts', 'rotSym']
        for row in all_calculation['results']['bindings']:
            species = row['species']['value'].split('/')[-1]
            ocIRI = row['ocIRI']['value'].split('/')[-1]
            for col_n in col_names:
                if col_n in row:
                    node = row[col_n]['value'].split('/')[-1]
                    if node in value_dict:
                        print('This is big problem')
                    else:
                        value_dict[node] = row[col_n + 'Value']['value']

        with open(os.path.join(ARCHIVE_DIR, 'ontocompchem_value_dict.json'), 'w') as f:
            f.write(json.dumps(value_dict))
            f.close()

    def construct_triples(self):
        # ocIRI - oc:hasUniqueSpecies -> species
        # ocIRI - gc:isCalculationOn  -> geomType - hasGeometryType -> value
        # ocIRI - gc:isCalculationOn  -> vibAnal - vibAnalTemp -> vibAnal - hasFrequencies -> vibAnalValue
        # ocIRI - gc:isCalculationOn  -> rotConsts - hasRotationalConstants -> value
        # ocIRI - gc:isCalculationOn  -> rotSym - hasRotationalSymmetryNumber -> value

        triples = []
        all_calculation = json.loads(open(os.path.join(ARCHIVE_DIR, 'ontocompchem_all_calculation.json')).read())
        col_names = ['geomType', 'vibAnal', 'rotConsts', 'rotSym']
        rel_dict = {'geomType': 'hasGeometryType', 'vibAnal':'oc:hasFrequencies',
                    'rotConsts': 'oc:hasRotationalConstants', 'rotSym': 'oc:hasRotationalSymmetryNumber'}
        for row in all_calculation['results']['bindings']:
            species = row['species']['value'].split('/')[-1]
            ocIRI = row['ocIRI']['value'].split('/')[-1]
            triples.append((ocIRI, 'oc:hasUniqueSpecies', species))


            for col_n in col_names:
                if col_n in row:
                    node = row[col_n]['value'].split('/')[-1]
                    triples.append((ocIRI, 'gc:isCalculationOn', node))
                    triples.append((node, rel_dict[col_n], node + 'Value'))

        df_triples = pandas.DataFrame(triples, columns = ["head", "rel", "tail"])
        df_triples.to_csv(os.path.join(ARCHIVE_DIR, 'ontocompchem_calculation/ontocompchem_calculation-train.txt'),
                          sep='\t', index=False, header=False)
        df_triples.sample(frac=0.2).to_csv(os.path.join(ARCHIVE_DIR, 'ontocompchem_calculation'
                                                                     '/ontocompchem_calculation-test.txt'),
                          sep='\t', index=False, header=False)
        df_triples.sample(frac=0.2).to_csv(os.path.join(ARCHIVE_DIR, 'ontocompchem_calculation'
                                                                     '/ontocompchem_calculation-valid.txt'),
                          sep='\t', index=False, header=False)


if __name__ == '__main__':
    occr = OntoCompChemReader()
    # rst = occr.query_blazegraph(query=ONTOCOMPCHEM_IRI_FROM_ONTOSPECIES_QUERY)
    # occr.process_query_result(rst)
    # pprint(rst)
    # occr.find_all_unique_species_calculation_pairs()
    # rst = occr.query_blazegraph(query=ONTOCOMPCHEM_ALL_CALCULATION_QUERY)
    # with open(os.path.join(ARCHIVE_DIR, 'ontocompchem_all_calculation.json'),'w') as f:
    #     f.write(json.dumps(rst))
    #     f.close()
    occr.construct_triples()
    occr.construct_value_dict()

# sample training unit: what is the geometry type of CH4, head CH4, tail: geometry, RotationalSymetry ...
#  TODO: construct the path between species and RotationalSymmetry
