import json
import re
import urllib.parse
import urllib.request
from pprint import pprint

from .species_validator import SpeciesValidator
from .attribute_mapping import AttributeMapper

from .locations import JPS_SPARQL_TEMPLATE_PATH
from .search_interface import SearchInterface
from .OntoCompChem_Queries import ontocompchem_simple_intents, \
    ROTATIONAL_CONSTANT_QUERY, VIBRATION_FREQUENCY_QUERY, \
    ROTATIONAL_SYMMETRY_NUMBER, GAUSSIAN_FILE, SPIN_MULTIPLICITY, \
    FORMAL_CHARGE, ELECTRONIC_ENERGY, GEOMETRY_TYPE

from .OntoOntokin_Queries import GENERAL_QUERY, LENNARD_JONES_WELL_DEPTH, \
    POLARIZABILITY, DIPOLE_MOMENT, RELAXATION_COLLISION, \
    ontokin_simple_intents, HIGH_SPEED_GENERAL_QUERY

from functools import lru_cache
# from cachier import cachier
import datetime


# try:
#     from __main__ import socketio
#
#     print('Importing socketIO from main in interpretation')
# except ImportError:
#     from run import socketio

def fire_query_to_ldf_ontokin(query, products, reactants):
    print('----------- firing the query to LDF -------------')
    if products is None:
        products = []
    if reactants is None:
        reactants = []
    print("query fired to LDF server")
    print(query)
    url = "http://localhost:3000/query?"
    values = {"query": query, "products": json.dumps(products), "reactants": json.dumps(reactants)}
    full_url = url + urllib.parse.urlencode(values)
    req = urllib.request.Request(full_url)
    response = urllib.request.urlopen(req).read()
    print(response)
    return response


def fire_query_ontochemcomp(query):
    print('----------- firing the query to JPS ontochemcomp -------------')
    print(query)
    url = "http://www.theworldavatar.com/rdf4j-server/repositories/ontocompchem"
    values = {'query': query}
    data = urllib.parse.urlencode(values).encode('utf-8')
    req = urllib.request.Request(url, data)
    response = urllib.request.urlopen(req).read()
    return response


def process_ontocompchem_results(rst):

    rst_lines = rst.split('\r\n')
    if len(rst_lines) <= 1:
        return None
    else:
        result = []
        heads = rst_lines[0].split(',')
        data_list = rst_lines[1:]
        for data in data_list:
            temp = {}
            cols = data.split(',')
            for c, h in zip(cols, heads):
                temp[h] = c
            if data == '':
                pass
            else:
                result.append(temp)
        return json.dumps(result)


class JPS_query_constructor:

    def __init__(self, socketio):
        print("================= JPS_SPARQL_TEMPLATE_PATH =============")
        print(JPS_SPARQL_TEMPLATE_PATH)
        with open(JPS_SPARQL_TEMPLATE_PATH) as f:
            self.template_dict = json.loads(f.read())
        self.serach_interface = SearchInterface()
        self.socketio = socketio
        self.validator = SpeciesValidator()
        self.attribute_mapper = AttributeMapper()
        # self.fire_query.clear_cache()
        # self.fire_query_ontochemcomp.clear_cache()

    @staticmethod
    def process_species_for_ontocompchem(species):
        # to convert H2O2 or h2o2 to H 2 O 2
        # to convert H2O2 or h2o2 to H 2 O 2

        temp = ''
        number_regex = r'[0-9]+'
        alphabet_regex = r'[a-zA-Z]'
        print('-----------------------')
        print('species', species)
        if type(species) == str:

            numbers = re.findall(number_regex, species)
            for number in list(set(numbers)):
                new_number = ' ' + number + ' '
                species = species.replace(number, new_number)

            return species.strip().upper()
            # return result
        else:
            return None

    @staticmethod
    def extract_entity_pairs(entities):
        rst = '<br/>'
        for e in entities:
            key = e['entity']
            value = e['value']
            pair = value + ' - ' + key + '<br/>'
            rst = rst + pair
        return rst

    def extract_info(self, intents):
        intent = intents['intent']['name']
        entity_pairs = JPS_query_constructor.extract_entity_pairs(intents['entities'])
        self.socketio.emit('coordinate_agent', 'Looking up entities in JPS KG<br/> -----------------' + str(
            entity_pairs) + '-----------------')

        if intent in ontocompchem_simple_intents or (intent == 'query_quantum_chemistry'):
            result = {'intent': intent}
            for e in intents['entities']:
                entity_type = e['entity']
                value = e['value']
                if entity_type == 'species':
                    result['species'] = value
                if entity_type == 'attribute':
                    result['attribute'] = value
            return result

        if intent in ontokin_simple_intents or (intent == 'query_thermodynamic'):
            result = {'intent': intent}
            for e in intents['entities']:
                entity_type = e['entity']
                value = e['value']
                if entity_type == 'species':
                    result['species'] = value
                if entity_type == 'attribute':
                    result['attribute'] = value

            return result

        if intent == 'query_reaction_property':
            result = {'intent': intent}
            result['reactants'] = []
            result['products'] = []
            flag = False
            for e in intents['entities']:
                entity_type = e['entity']
                value = e['value']
                if entity_type == 'to':
                    flag = True
                if entity_type == 'attribute':
                    result['attribute'] = value
                if entity_type == 'indicator':
                    result['indicator'] = value
                if entity_type == 'species' and not flag:
                    # this belongs to reactants
                    result['reactants'].append(value.upper())
                if entity_type == 'species' and flag:
                    # this belongs to reactants
                    result['products'].append(value.upper())
            return result
        elif intent == 'select_reaction_by_species':
            result = {'intent': intent}
            result['reactants'] = []
            result['products'] = []
            temp = []
            flag = False
            for e in intents['entities']:
                entity_type = e['entity']
                value = e['value']
                if entity_type == 'indicator':
                    if 'produc' in value:
                        flag = True
                        result['products'] = temp
                        temp = []
                    else:
                        result['reactants'] = temp
                        temp = []

                if entity_type == 'species':
                    # this belongs to reactants
                    temp.append(value.upper())
            if (not result['reactants']) and (not result['products']):
                result['products'] = temp
            print('-------------- result processed -------------', result)
            return result
        elif intent == 'select_mechanism_by_reaction':
            result = {'intent': intent}
            result['reactants'] = []
            result['products'] = []
            flag = False
            for e in intents['entities']:
                entity_type = e['entity']
                value = e['value']
                if entity_type == 'to':
                    flag = True
                if entity_type == 'attribute':
                    result['attribute'] = value
                if entity_type == 'species' and not flag:
                    # this belongs to reactants
                    result['reactants'].append(value.upper())
                if entity_type == 'species' and flag:
                    # this belongs to reactants
                    result['products'].append(value.upper())
            print('========= select_mechanism_by_reaction ===========')
            return result

    def construct_query(self, intents):
        self.socketio.emit('coordinate_agent', 'Constructing SPARQL queries')
        print('=================== intents ================')
        pprint(intents['intent']['name'])
        result = self.extract_info(intents)
        intent = result['intent']

        if intent == 'query_reaction_property':
            try:
                rst = self.query_reaction_property(result['reactants'], result['products'], result['attribute'])
            except:
                rst = self.query_reaction_property(result['reactants'], result['products'], result['indicator'])
        elif intent == 'select_reaction_by_species':
            # TODO: seperate reactants and products
            print('select_reaction_by_species')
            rst = self.query_by_reaction_only(result['reactants'], result['products'])
        elif intent == 'select_mechanism_by_reaction':
            rst = self.query_mechanism_by_reaction(result['reactants'], result['products'])
            if rst is None:
                return None
        elif intent in ontocompchem_simple_intents or (intent == 'query_quantum_chemistry'):
            rst = self.query_quantum_of_moleculars(result['intent'], result['species'], result['attribute'])
            if rst is None:
                return None
        elif intent in ontokin_simple_intents or (intent == 'query_thermodynamic'):
            rst = self.query_thermo_of_moleculars(result['intent'], result['species'], result['attribute'])
            if rst is None:
                return None

        return rst.replace('[=]', '->').replace('=]', '->')

    def query_thermo_of_moleculars(self, intent, species, attribute):

        print('=========== species received ===========')
        print('species:', species)
        print('=========== attribute received ============')
        print('attribute:', attribute)
        species = species.upper()
        attribute_iri = self.attribute_mapper.find_closest_attribute(intent, attribute)
        print('=========== attribute iri  ============')
        print('attribute iri :', attribute_iri)
        print('============= line 206 ============')
        print('intent', intent)
        print('species', species)
        species = self.validator.validate(attribute, 'ontokin', intent, species)
        print('======== species =======')
        print(species)

        if intent == 'query_thermodynamic':
            attribute_name = attribute.replace(' ', '').upper()
            q = HIGH_SPEED_GENERAL_QUERY % (attribute_name, attribute_iri, attribute_name, species, attribute_iri)
            # q = GENERAL_QUERY % (attribute.replace(' ', '').upper(), species, attribute_iri, attribute.replace(' ',
            # '').upper(), attribute_iri)
            print('================ GENERAL QUERY ===============')
            print(q)
            rst = fire_query_to_ldf_ontokin(q, None, None).decode('utf-8')
            return rst
        # # 1. att name, 1.5 species  2. att iri name 3. att name 4. att iri name
        elif intent == 'rotational_relaxation_collision':
            q = RELAXATION_COLLISION % species
            rst = fire_query_to_ldf_ontokin(q, None, None).decode('utf-8')

        else:
            return None

        if rst is None:
            return None
        else:
            rst = json.loads(rst)
            rst = json.dumps(rst)
            return rst

    def query_quantum_of_moleculars(self, intent, species, attribute):
        # ROTATIONAL_CONSTANT_QUERY
        # VIBRATION_FREQUENCY_QUERY
        # ROTATIONAL_SYMMETRY_NUMBER
        original_species = species
        print('=========== line 238 =============')
        print('intent', intent)
        print('species', species)
        attribute_iri = self.attribute_mapper.find_closest_attribute(intent, attribute)
        species = self.validator.validate(attribute, 'ontocompchem', intent, species)
        intent = self.attribute_mapper.map_to_quantum_queries(attribute_iri)
        if species is None:
            return None

        if intent == 'rotational_constants':
            q = ROTATIONAL_CONSTANT_QUERY % species
            rst = fire_query_ontochemcomp(q).decode('utf-8')
        elif intent == 'symmetry_number':
            q = ROTATIONAL_SYMMETRY_NUMBER % species
            rst = fire_query_ontochemcomp(q).decode('utf-8')
        elif intent == 'vibration_frequency':
            q = VIBRATION_FREQUENCY_QUERY % species
            rst = fire_query_ontochemcomp(q).decode('utf-8')
        elif intent == 'guassian_file':
            q = GAUSSIAN_FILE % species
            rst = fire_query_ontochemcomp(q).decode('utf-8')
        elif intent == 'spin_multiplicity':
            q = SPIN_MULTIPLICITY % species
            rst = fire_query_ontochemcomp(q).decode('utf-8')
        elif intent == 'formal_charge':
            q = FORMAL_CHARGE % species
            rst = fire_query_ontochemcomp(q).decode('utf-8')
        elif intent == 'electronic_energy':
            q = ELECTRONIC_ENERGY % species
            rst = fire_query_ontochemcomp(q).decode('utf-8')
        elif intent == 'geometry_type':
            q = GEOMETRY_TYPE % species
            rst = fire_query_ontochemcomp(q).decode('utf-8')

        else:
            return None
        if rst is None:
            return None
        else:
            rst = process_ontocompchem_results(rst)
            return rst

    # to find the reactions that meet the conditions first, then find the mechanism
    def query_mechanism_by_reaction(self, reactants, products):
        print('query_mechanism_by_reaction')
        q = self.template_dict['select_mechanism_by_reaction']
        rst = fire_query_to_ldf_ontokin(q, products, reactants).decode('utf-8')
        return rst

    # to find reactions by reactants and products
    def query_by_reaction_only(self, reactants, products):
        print('query_by_reaction_only')
        query = self.template_dict['select_reaction_by_species']
        print('query', query)
        rst = fire_query_to_ldf_ontokin(query, products, reactants).decode('utf-8')
        return rst

        # TODO: construct the query by only reactants and products

    # if to query properties of reactions including reaction rate and whether the reaction is reversible
    def query_reaction_property(self, reactants, products, attribute):
        print('query_reaction_property')
        sub_properties_products = ['rdfs:label']
        attribute = ' <' + self.serach_interface.get_first_match(attribute).strip() + '> '
        print('============== attribute =============')
        print(attribute)
        if 'hasArrheniusCoefficient' in attribute:
            query = self.template_dict['query_reaction_property']['ArrheniusCoefficient']
            rst = fire_query_to_ldf_ontokin(query, products, reactants).decode('utf-8')
            return rst

        elif 'isReversible' in attribute:
            query = self.template_dict['query_reaction_property']['isReversible']
            rst = fire_query_to_ldf_ontokin(query, products, reactants).decode('utf-8')
            return rst


